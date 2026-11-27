# SPDX-FileCopyrightText: 2026 Thomas Vincent
# SPDX-License-Identifier: MIT
"""Identify the capacity of the current worker. Exit 0 only on positive evidence."""
import http.client
import json
import os
from pathlib import Path
import re
import subprocess
import sys


def metadata(path, *, headers, method="GET", google=False):
    # Connect directly to link-local metadata. No redirects or environment proxies.
    connection = http.client.HTTPConnection("169.254.169.254", timeout=3)
    try:
        connection.request(method, path, headers=headers)
        response = connection.getresponse()
        if response.status != 200 or (google and response.getheader("Metadata-Flavor") != "Google"):
            raise ValueError("Metadata service could not confirm capacity")
        data = response.read(4097)
        if len(data) > 4096:
            raise ValueError("Unexpected metadata response size")
        return data.decode("utf-8").strip()
    finally:
        connection.close()


def cli(args):
    return subprocess.run(args, capture_output=True, text=True, timeout=10, check=True).stdout.strip()


def matches(value, spot, regular):
    if value in spot:
        return True
    if value in regular:
        return False
    raise ValueError("Unknown capacity value")


def kubernetes_spot(pool):
    selectors = json.loads(Path(__file__).with_name("spot_pools.json").read_text(encoding="utf-8"))
    key, expected = selectors[pool]
    pod, namespace, uid = (os.environ[name] for name in ("POD_NAME", "POD_NAMESPACE", "POD_UID"))
    if not all(re.fullmatch(r"[a-z0-9][a-z0-9.-]*", item) for item in (pod, namespace, uid)):
        raise ValueError("Invalid pod identity")
    kubectl = ["kubectl", "--request-timeout=5s"]
    # Match the UID to distinguish the running Pod from a replacement or another cluster.
    identity = cli(kubectl + ["get", "pod", pod, "--namespace", namespace, "-o",
                              'jsonpath={.metadata.uid}{"\\t"}{.spec.nodeName}']).split("\t")
    if len(identity) != 2 or identity[0] != uid or not re.fullmatch(r"[a-z0-9][a-z0-9.-]*", identity[1]):
        raise ValueError("Could not identify the running pod's worker")
    value = cli(kubectl + ["get", "node", identity[1], "-o", f'go-template={{{{index .metadata.labels "{key}"}}}}'])
    return value == expected


def is_spot(provider, pool=None):
    if provider == "aws":
        token = metadata("/latest/api/token", method="PUT", headers={"X-aws-ec2-metadata-token-ttl-seconds": "60"})
        if not token:
            raise ValueError("No metadata session")
        lifecycle = metadata("/latest/meta-data/instance-life-cycle", headers={"X-aws-ec2-metadata-token": token})
        return matches(lifecycle, {"spot"}, {"on-demand", "scheduled", "capacity-block"})
    if provider == "azure":
        priority = metadata("/metadata/instance/compute/priority?api-version=2025-04-07&format=text",
                            headers={"Metadata": "true"})
        return matches(priority.lower(), {"spot", "low"}, {"regular"})
    if provider == "gcp":
        flag = metadata("/computeMetadata/v1/instance/scheduling/preemptible",
                        headers={"Metadata-Flavor": "Google"}, google=True)
        return matches(flag.upper(), {"TRUE"}, {"FALSE"})
    if provider == "oracle":
        headers = {"Authorization": "Bearer Oracle"}
        instance = metadata("/opc/v2/instance/id", headers=headers)
        region = metadata("/opc/v2/instance/canonicalRegionName", headers=headers)
        if not re.fullmatch(r"ocid1\.instance\.[a-zA-Z0-9.-]+", instance) or not re.fullmatch(r"[a-z0-9-]+", region):
            raise ValueError("Invalid OCI instance identity")
        info = json.loads(cli(["oci", "compute", "instance", "get", "--region", region, "--instance-id", instance,
                               "--query", '{id:data.id,action:data."preemptible-instance-config"."preemption-action".type}',
                               "--output", "json"]))
        if info["id"] != instance:
            raise ValueError("OCI returned a different instance")
        return matches(info["action"], {"TERMINATE"}, {None})
    if provider == "kubernetes":
        return kubernetes_spot(pool)
    raise ValueError("Unknown provider")


if __name__ == "__main__":
    try:
        verified = is_spot(os.environ["SPOT_PROVIDER"], os.environ.get("SPOT_POOL"))
    except (ValueError, KeyError, TypeError, OSError, http.client.HTTPException, subprocess.SubprocessError):
        sys.exit(4)
    sys.exit(0 if verified else 3)
