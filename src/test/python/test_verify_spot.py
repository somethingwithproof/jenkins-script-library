# SPDX-FileCopyrightText: 2026 Thomas Vincent
# SPDX-License-Identifier: MIT
import importlib.util
import json
import os
from pathlib import Path
import unittest
from unittest.mock import patch

SOURCE = Path(__file__).resolve().parents[3] / "resources/com/github/thomasvincent/jenkinsscripts/cloud/verify_spot.py"
spec = importlib.util.spec_from_file_location("verify_spot", SOURCE)
verify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verify)


class CapacityTests(unittest.TestCase):
    def test_vm_lifecycles(self):
        for provider, spot, regular in [("aws", "spot", "on-demand"), ("azure", "Spot", "Regular"),
                                        ("gcp", "TRUE", "FALSE")]:
            for value, expected in ((spot, True), (regular, False), ("unexpected", None)):
                with self.subTest(provider=provider, value=value), patch.object(verify, "metadata") as metadata:
                    metadata.side_effect = ["session", value] if provider == "aws" else [value]
                    if expected is None:
                        with self.assertRaises(ValueError):
                            verify.is_spot(provider)
                    else:
                        self.assertEqual(verify.is_spot(provider), expected)
                    if provider == "aws":
                        self.assertEqual(metadata.call_args_list[0].kwargs["method"], "PUT")
                        self.assertEqual(metadata.call_args.kwargs["headers"]["X-aws-ec2-metadata-token"], "session")
                    elif provider == "gcp":
                        self.assertTrue(metadata.call_args.kwargs["google"])

    def test_metadata_requires_direct_success_and_google_response_header(self):
        with patch.object(verify.http.client, "HTTPConnection") as http:
            connection = http.return_value
            response = connection.getresponse.return_value
            response.status = 302
            with self.assertRaises(ValueError):
                verify.metadata("/path", headers={})
            response.status = 200
            response.getheader.return_value = None
            with self.assertRaises(ValueError):
                verify.metadata("/path", headers={"Metadata-Flavor": "Google"}, google=True)
            response.getheader.return_value = "Google"
            response.read.return_value = b"TRUE"
            self.assertEqual(verify.metadata("/path", headers={}, google=True), "TRUE")
            http.assert_called_with("169.254.169.254", timeout=3)
            self.assertEqual(connection.close.call_count, 3)

    def test_oci_checks_the_local_instance(self):
        instance = "ocid1.instance.oc1.phx.fixture"
        for reply_id, action, expected in ((instance, "TERMINATE", True), (instance, None, False),
                                            ("other", "TERMINATE", None)):
            with self.subTest(reply_id=reply_id, action=action), patch.object(verify, "metadata") as metadata, \
                    patch.object(verify, "cli") as cli:
                metadata.side_effect = [instance, "us-phoenix-1"]
                cli.return_value = json.dumps({"id": reply_id, "action": action})
                if expected is None:
                    with self.assertRaises(ValueError):
                        verify.is_spot("oracle")
                else:
                    self.assertEqual(verify.is_spot("oracle"), expected)
                self.assertIn(instance, cli.call_args.args[0])

    def test_pod_uid_and_actual_worker_label(self):
        for pool, key, spot in (("eks", "eks.amazonaws.com/capacityType", "SPOT"),
                                ("aks", "kubernetes.azure.com/scalesetpriority", "spot"),
                                ("gke", "cloud.google.com/gke-spot", "true"),
                                ("oke", "oci.oraclecloud.com/oke-is-preemptible", "true")):
            with self.subTest(pool=pool), patch.dict(os.environ, POD_NAME="ci-1", POD_NAMESPACE="test", POD_UID="1234"), \
                    patch.object(verify, "cli") as cli:
                cli.side_effect = ["1234\tworker-1", spot]
                self.assertTrue(verify.is_spot("kubernetes", pool))
                self.assertIn(key, cli.call_args.args[0][-1])
                cli.side_effect = ["1234\tworker-1", "regular"]
                self.assertFalse(verify.is_spot("kubernetes", pool))
                cli.side_effect = ["changed\tworker-1"]
                with self.assertRaises(ValueError):
                    verify.is_spot("kubernetes", pool)
