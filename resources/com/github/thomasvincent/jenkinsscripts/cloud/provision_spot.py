# SPDX-FileCopyrightText: 2026 Thomas Vincent
# SPDX-License-Identifier: MIT
"""Prepare or submit one interruptible-capacity request using the agent's cloud CLI."""
import argparse
import copy
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile


POOL_SELECTORS = {
    "eks": ("eks.amazonaws.com/capacityType", "SPOT"),
    "gke": ("cloud.google.com/gke-spot", "true"),
    "aks": ("kubernetes.azure.com/scalesetpriority", "spot"),
    "oke": ("oci.oraclecloud.com/oke-is-preemptible", "true"),
}


def identifier(config, key):
    value = config.get(key)
    if not isinstance(value, str) or not re.fullmatch(r"[a-zA-Z0-9][a-zA-Z0-9._-]{0,127}", value):
        raise ValueError(f"Invalid or missing {key}")
    return value


def retry_key(config):
    value = identifier(config, "requestId")
    if len(value) > 64:
        raise ValueError("requestId must be 1 to 64 characters")
    return value


def require_value(mapping, key, value):
    if key in mapping and mapping[key] != value:
        raise ValueError(f"Conflicting {key} for interruptible capacity")
    mapping[key] = value


def launch_request(config, payload):
    if not isinstance(config, dict):
        raise ValueError("Request must be an object")
    provider = config["provider"]
    body = copy.deepcopy(config.get("spec", {}))
    if not isinstance(body, dict):
        raise ValueError("spec must be an object")
    if provider == "aws":
        region = identifier(config, "region")
        require_value(body, "MinCount", 1)
        require_value(body, "MaxCount", 1)
        require_value(body, "ClientToken", retry_key(config))
        market = body.setdefault("InstanceMarketOptions", {})
        require_value(market, "MarketType", "spot")
        require_value(market.setdefault("SpotOptions", {}), "SpotInstanceType", "one-time")
        if not body.get("ImageId") and not body.get("LaunchTemplate"):
            raise ValueError("AWS requires ImageId or LaunchTemplate")
        return ["aws", "ec2", "run-instances", "--region", region, "--cli-input-json",
                "file://" + str(payload), "--query", "Instances[0].InstanceId", "--output", "text"], body
    if provider == "azure":
        subscription = identifier(config, "subscription")
        group = identifier(config, "resourceGroup")
        name = identifier(config, "name")
        if not body.get("location") or not body.get("properties"):
            raise ValueError("Azure requires a VM request body with location and properties")
        props = body["properties"]
        require_value(props, "priority", "Spot")
        if props.setdefault("evictionPolicy", "Deallocate") not in ("Delete", "Deallocate"):
            raise ValueError("Invalid Azure evictionPolicy")
        url = (f"https://management.azure.com/subscriptions/{subscription}/resourceGroups/{group}"
               f"/providers/Microsoft.Compute/virtualMachines/{name}?api-version=2025-04-01")
        return ["az", "rest", "--method", "put", "--url", url, "--headers", "If-None-Match=*",
                "--body", "@" + str(payload), "--query", "id", "--output", "tsv"], body
    if provider == "gcp":
        if body:
            raise ValueError("Use an instanceTemplate for GCP")
        name = identifier(config, "name")
        project = identifier(config, "project")
        zone = identifier(config, "zone")
        template = config.get("instanceTemplate", "")
        if not re.fullmatch(r"(?:projects/[a-zA-Z0-9.-]+/(?:global|regions/[a-z0-9-]+)/instanceTemplates/)?[a-z][a-z0-9-]*", template):
            raise ValueError("Invalid or missing instanceTemplate")
        return ["gcloud", "compute", "instances", "create", name, "--project", project, "--zone", zone,
                "--source-instance-template", template, "--provisioning-model=SPOT",
                "--instance-termination-action=STOP", "--maintenance-policy=TERMINATE", "--no-restart-on-failure",
                "--quiet", "--format=value(selfLink)"], body
    if provider == "oracle":
        region = identifier(config, "region")
        headers = json.dumps({"opc-retry-token": retry_key(config)})
        if not body.get("compartmentId") or not body.get("availabilityDomain"):
            raise ValueError("OCI requires compartmentId and availabilityDomain")
        action = body.setdefault("preemptibleInstanceConfig", {}).setdefault("preemptionAction", {})
        require_value(action, "type", "TERMINATE")
        action.setdefault("preserveBootVolume", True)
        url = f"https://iaas.{region}.oraclecloud.com/20160918/instances"
        return ["oci", "raw-request", "--http-method", "POST", "--target-uri", url,
                "--request-headers", headers, "--request-body", "file://" + str(payload),
                "--query", "{status:status,id:data.id}", "--output", "json"], body
    if provider == "kubernetes":
        pool = config.get("pool")
        if pool not in POOL_SELECTORS:
            raise ValueError("pool must be eks, gke, aks or oke")
        if body.get("kind") != "Pod" or body.get("apiVersion") != "v1":
            raise ValueError("Kubernetes requires a v1 Pod")
        metadata = body.get("metadata", {})
        identifier(metadata, "name")
        namespace = identifier(config, "namespace")
        require_value(metadata, "namespace", namespace)
        spec = body["spec"]
        if spec.get("nodeName") or spec.get("schedulerName", "default-scheduler") != "default-scheduler":
            raise ValueError("Pod must use the default scheduler without nodeName")
        key, value = POOL_SELECTORS[pool]
        require_value(spec.setdefault("nodeSelector", {}), key, value)
        if pool in ("aks", "gke", "oke"):
            toleration = {"key": key, "operator": "Equal", "value": value, "effect": "NoSchedule"}
            if pool == "oke":
                toleration = {"key": key, "operator": "Exists", "effect": "NoSchedule"}
            tolerations = spec.setdefault("tolerations", [])
            if toleration not in tolerations:
                tolerations.append(toleration)
        context = config.get("context")
        if not isinstance(context, str) or not context or not context.isprintable() or context.startswith("-"):
            raise ValueError("Invalid or missing kubeconfig context")
        return ["kubectl", "--context", context, "--namespace", namespace,
                "create", "-f", str(payload), "-o", "name"], body
    raise ValueError(f"Unsupported provider: {provider}")


def provision(config, apply=False):
    with tempfile.TemporaryDirectory(prefix="jenkins-spot-") as directory:
        payload = Path(directory).resolve() / "request.json"
        command, body = launch_request(config, payload)
        provider = config["provider"]
        if provider == "kubernetes":
            target = f"{config['context']}/{config['namespace']}/{body['metadata']['name']}"
        elif provider in ("aws", "oracle"):
            target = f"{config['region']}/{config['requestId']}"
        elif provider == "gcp":
            target = f"{config['project']}/{config['zone']}/{config['name']}"
        else:
            target = f"{config['subscription']}/{config['resourceGroup']}/{config['name']}"
        report = {"provider": provider, "target": target, "status": "planned"}
        if apply:
            payload.write_text(json.dumps(body), encoding="utf-8")
            result = subprocess.run(command, capture_output=True, text=True)
            if result.returncode:
                # Provider diagnostics can echo VM initialization data.
                raise ValueError(f"{config['provider']} request failed (CLI exit {result.returncode}); check the provider operation")
            response = result.stdout.strip()
            if config["provider"] == "oracle":
                result = json.loads(response)
                if not str(result["status"]).startswith("2"):
                    raise ValueError("OCI rejected the launch request")
                response = result["id"]
            if not response or response in ("None", "null"):
                raise ValueError("Provider did not return a resource identifier; check the existing request before retrying")
            report.update(status="submitted", resourceId=response)
        return report


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("request", type=Path)
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    try:
        print(json.dumps(provision(json.loads(args.request.read_text(encoding="utf-8")), args.apply)))
    except (KeyError, ValueError, TypeError, OSError, subprocess.CalledProcessError) as error:
        sys.exit(f"Spot request failed: {error}")
