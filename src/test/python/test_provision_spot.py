# SPDX-FileCopyrightText: 2026 Thomas Vincent
# SPDX-License-Identifier: MIT
import copy
import importlib.util
import json
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

SOURCE = (Path(__file__).resolve().parents[3] /
          "resources/com/github/thomasvincent/jenkinsscripts/cloud/provision_spot.py")
spec = importlib.util.spec_from_file_location("provision_spot", SOURCE)
spot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(spot)

REQUESTS = {
    "aws": {"region": "us-east-1", "requestId": "run-123", "spec": {"ImageId": "ami-fixture"}},
    "azure": {"name": "ci-vm", "subscription": "fixture-sub", "resourceGroup": "agents",
              "spec": {"location": "eastus", "properties": {"hardwareProfile": {"vmSize": "fixture"}}}},
    "gcp": {"project": "ci-project", "zone": "us-central1-a", "name": "ci-vm", "instanceTemplate": "ci-template"},
    "oracle": {"region": "us-ashburn-1", "requestId": "run-123",
               "spec": {"compartmentId": "fixture", "availabilityDomain": "fixture-ad"}},
    "kubernetes": {"context": "arn:aws:eks:us-east-1:123456789012:cluster/ci", "namespace": "agents", "pool": "eks",
                   "spec": {"apiVersion": "v1", "kind": "Pod", "metadata": {"name": "ci-agent"},
                            "spec": {"containers": [{"name": "agent", "image": "fixture"}]}}},
}


class SpotRequestTests(unittest.TestCase):
    def test_preview_and_submission_for_each_provider(self):
        for provider, fields in REQUESTS.items():
            with self.subTest(provider=provider):
                config = dict(provider=provider, **copy.deepcopy(fields))
                with patch.object(subprocess, "run") as run:
                    plan = spot.provision(config)
                    self.assertEqual(plan["status"], "planned")
                    self.assertTrue(plan["target"])
                    run.assert_not_called()

                    def accept(command, **kwargs):
                        response = "fixture-id"
                        if provider == "gcp":
                            self.assertIn("--provisioning-model=SPOT", command)
                            self.assertIn("--no-restart-on-failure", command)
                            self.assertIn("ci-template", command)
                        else:
                            flag = {"aws": "--cli-input-json", "azure": "--body", "oracle": "--request-body",
                                    "kubernetes": "-f"}[provider]
                            path = Path(command[command.index(flag) + 1].removeprefix("file://").removeprefix("@"))
                            body = json.loads(path.read_text())
                            if provider == "aws":
                                self.assertEqual(body["InstanceMarketOptions"]["MarketType"], "spot")
                                self.assertEqual(body["ClientToken"], "run-123")
                                self.assertEqual(body["MaxCount"], 1)
                            elif provider == "azure":
                                self.assertEqual(body["properties"]["priority"], "Spot")
                                self.assertIn("If-None-Match=*", command)
                            elif provider == "oracle":
                                self.assertEqual(body["preemptibleInstanceConfig"]["preemptionAction"],
                                                 {"type": "TERMINATE", "preserveBootVolume": True})
                                self.assertIn('"opc-retry-token": "run-123"', " ".join(command))
                                response = json.dumps({"status": "200 OK", "id": "fixture-id"})
                            else:
                                self.assertEqual(body["spec"]["nodeSelector"], {"eks.amazonaws.com/capacityType": "SPOT"})
                                self.assertIn("create", command)
                        return subprocess.CompletedProcess(command, 0, response, "")
                    run.side_effect = accept
                    self.assertEqual(spot.provision(config, True), dict(plan, status="submitted", resourceId="fixture-id"))
                    self.assertEqual(run.call_count, 1)
                if "spec" in config:
                    self.assertEqual(config["spec"], fields["spec"])

    def test_kubernetes_clusters_preserve_existing_selectors(self):
        for pool, (key, value) in spot.POOL_SELECTORS.items():
            with self.subTest(pool=pool):
                config = dict(provider="kubernetes", **copy.deepcopy(REQUESTS["kubernetes"]))
                config["pool"] = pool
                config["spec"]["spec"]["nodeSelector"] = {"team": "ci"}
                _, body = spot.launch_request(config, Path("/tmp/test.json"))
                self.assertEqual(body["spec"]["nodeSelector"], {"team": "ci", key: value})
                if pool in ("aks", "gke", "oke"):
                    self.assertTrue(any(t["key"] == key for t in body["spec"]["tolerations"]))

    def test_reject_conflicts_and_scheduling_bypass_before_submission(self):
        cases = [("aws", {"ImageId": "fixture", "InstanceMarketOptions": {"MarketType": "capacity-block"}}),
                 ("azure", {"location": "eastus", "properties": {"priority": "Regular"}}),
                 ("oracle", {"compartmentId": "test", "availabilityDomain": "ad",
                             "preemptibleInstanceConfig": {"preemptionAction": {"type": "STOP"}}})]
        for bypass in ({"nodeName": "regular"}, {"schedulerName": "custom"},
                       {"nodeSelector": {"eks.amazonaws.com/capacityType": "ON_DEMAND"}}):
            pod = copy.deepcopy(REQUESTS["kubernetes"]["spec"])
            pod["spec"].update(bypass)
            cases.append(("kubernetes", pod))
        with patch.object(subprocess, "run") as run:
            for provider, body in cases:
                with self.subTest(provider=provider, body=body), self.assertRaises(ValueError):
                    spot.provision(dict(REQUESTS[provider], provider=provider, spec=body), True)
            run.assert_not_called()

    def test_provider_errors_and_missing_ids_never_report_submission(self):
        config = dict(provider="oracle", **REQUESTS["oracle"])
        for code, output in [(0, '{"status":"409 Conflict","id":null}'), (0, '{"status":"200 OK","id":null}'),
                             (1, ""), (0, "invalid")]:
            with self.subTest(code=code, output=output), patch.object(subprocess, "run") as run:
                run.return_value = subprocess.CompletedProcess([], code, output, "private diagnostic")
                with self.assertRaises(ValueError):
                    spot.provision(config, True)
                run.assert_called_once()
