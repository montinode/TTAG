#!/usr/bin/env python3
"""
MontiSecurityScanner - Host-based anomaly detection for telecom infrastructure.

Covers MITRE ATT&CK Discovery and Persistence techniques relevant to Linux-based
telecom environments (routers, switches, VoIP servers, embedded devices):

  T1053.003 - Cron Persistence
  T1574.006 - LD_PRELOAD Hijack
  T1547.006 - Kernel Modules
  T1003     - SSH Key Exposure
  T1082     - Sensitive File Access
  T1219     - Suspicious Processes
  T1074     - Data Staging
  T1070.003 - History Cleared

Usage:
  sudo python3 monti.py [--log <path>]

Outputs a colour-coded console summary and writes a structured JSON report to
the path specified by --log (default: monti_scan.json).

Always obtain proper authorisation before running on production infrastructure.
Comply with applicable regulations (GDPR, FCC, etc.) and involve the Telecom
Tracing Authority (TTA) for any confirmed derivation incidents.
"""

import argparse
import datetime
import glob
import json
import logging
import os
import pwd
import re
import stat
import subprocess
import sys

# ---------------------------------------------------------------------------
# Severity levels
# ---------------------------------------------------------------------------
INFO = "INFO"
WARNING = "WARNING"
ERROR = "ERROR"


# ---------------------------------------------------------------------------
# Finding data class
# ---------------------------------------------------------------------------
class Finding:
    """Represents a single scanner finding."""

    def __init__(self, technique, technique_id, severity, detail):
        self.technique = technique
        self.technique_id = technique_id
        self.severity = severity
        self.detail = detail
        self.timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()

    def to_dict(self):
        return {
            "timestamp": self.timestamp,
            "technique": self.technique,
            "technique_id": self.technique_id,
            "severity": self.severity,
            "detail": self.detail,
        }


# ---------------------------------------------------------------------------
# Helper utilities
# ---------------------------------------------------------------------------
def _read_file(path):
    """Return the contents of *path* as a string, or None on error."""
    try:
        with open(path, "r", errors="replace") as fh:
            return fh.read()
    except (OSError, PermissionError):
        return None


def _list_dir(path):
    """Return a list of entries in *path*, or an empty list on error."""
    try:
        return os.listdir(path)
    except (OSError, PermissionError):
        return []


def _run(cmd, timeout=10):
    """Run a shell command and return (stdout, stderr).  Never raises."""
    try:
        result = subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
            timeout=timeout,
        )
        return result.stdout, result.stderr
    except Exception:
        return "", ""


# ---------------------------------------------------------------------------
# Check T1053.003 – Cron Persistence
# ---------------------------------------------------------------------------
def check_cron_persistence():
    """Detect scheduled tasks that may be used for persistence (T1053.003)."""
    findings = []
    suspicious_patterns = [
        re.compile(r"/tmp/", re.IGNORECASE),
        re.compile(r"/dev/shm/", re.IGNORECASE),
        re.compile(r"curl\s", re.IGNORECASE),
        re.compile(r"wget\s", re.IGNORECASE),
        re.compile(r"bash\s+-[ic]", re.IGNORECASE),
        re.compile(r"nc\s", re.IGNORECASE),
        re.compile(r"python[23]?\s+-c", re.IGNORECASE),
    ]

    cron_paths = [
        "/etc/crontab",
        "/etc/cron.d",
        "/var/spool/cron",
        "/var/spool/cron/crontabs",
    ] + glob.glob("/etc/cron.*")

    for cron_path in cron_paths:
        if os.path.isdir(cron_path):
            entries = _list_dir(cron_path)
            for entry in entries:
                full = os.path.join(cron_path, entry)
                content = _read_file(full)
                if content is None:
                    continue
                for line in content.splitlines():
                    line = line.strip()
                    if not line or line.startswith("#"):
                        continue
                    for pat in suspicious_patterns:
                        if pat.search(line):
                            findings.append(
                                Finding(
                                    "Cron Persistence",
                                    "T1053.003",
                                    WARNING,
                                    f"Suspicious cron entry in {full}: {line[:120]}",
                                )
                            )
                            break
        elif os.path.isfile(cron_path):
            content = _read_file(cron_path)
            if content is None:
                continue
            for line in content.splitlines():
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                for pat in suspicious_patterns:
                    if pat.search(line):
                        findings.append(
                            Finding(
                                "Cron Persistence",
                                "T1053.003",
                                WARNING,
                                f"Suspicious cron entry in {cron_path}: {line[:120]}",
                            )
                        )
                        break

    if not findings:
        findings.append(
            Finding("Cron Persistence", "T1053.003", INFO, "No suspicious cron entries detected.")
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1574.006 – LD_PRELOAD Hijack
# ---------------------------------------------------------------------------
def check_ld_preload():
    """Detect LD_PRELOAD manipulation that could intercept library calls (T1574.006)."""
    findings = []

    # System-wide preload file
    preload_file = "/etc/ld.so.preload"
    if os.path.exists(preload_file):
        content = _read_file(preload_file)
        if content and content.strip():
            findings.append(
                Finding(
                    "LD_PRELOAD Hijack",
                    "T1574.006",
                    WARNING,
                    f"{preload_file} is non-empty: {content.strip()[:200]}",
                )
            )
        else:
            findings.append(
                Finding(
                    "LD_PRELOAD Hijack",
                    "T1574.006",
                    INFO,
                    f"{preload_file} exists but is empty.",
                )
            )

    # Per-process LD_PRELOAD in /proc environments
    suspicious_procs = []
    for pid in _list_dir("/proc"):
        if not pid.isdigit():
            continue
        env_path = f"/proc/{pid}/environ"
        env_data = _read_file(env_path)
        if env_data and "LD_PRELOAD=" in env_data:
            # Extract the value
            for item in env_data.split("\x00"):
                if item.startswith("LD_PRELOAD="):
                    val = item[len("LD_PRELOAD="):]
                    if val:
                        suspicious_procs.append((pid, val))

    for pid, val in suspicious_procs:
        findings.append(
            Finding(
                "LD_PRELOAD Hijack",
                "T1574.006",
                WARNING,
                f"Process {pid} has LD_PRELOAD set: {val[:200]}",
            )
        )

    if not findings:
        findings.append(
            Finding("LD_PRELOAD Hijack", "T1574.006", INFO, "No LD_PRELOAD manipulation detected.")
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1547.006 – Kernel Modules
# ---------------------------------------------------------------------------
def check_kernel_modules():
    """Detect unauthorized or suspicious kernel modules (T1547.006)."""
    findings = []
    suspicious_patterns = [
        re.compile(r"hide", re.IGNORECASE),
        re.compile(r"rootkit", re.IGNORECASE),
        re.compile(r"hook", re.IGNORECASE),
        re.compile(r"keylog", re.IGNORECASE),
        re.compile(r"inject", re.IGNORECASE),
        re.compile(r"sniff", re.IGNORECASE),
    ]

    modules_content = _read_file("/proc/modules")
    if modules_content is None:
        findings.append(
            Finding(
                "Kernel Modules",
                "T1547.006",
                WARNING,
                "Cannot read /proc/modules – insufficient privileges or unavailable.",
            )
        )
        return findings

    for line in modules_content.splitlines():
        module_name = line.split()[0] if line.split() else ""
        for pat in suspicious_patterns:
            if pat.search(module_name):
                findings.append(
                    Finding(
                        "Kernel Modules",
                        "T1547.006",
                        WARNING,
                        f"Suspicious kernel module detected: {module_name}",
                    )
                )
                break

    # Check /etc/modules and /etc/modules-load.d for persistence
    persist_paths = ["/etc/modules"] + glob.glob("/etc/modules-load.d/*.conf")
    for path in persist_paths:
        content = _read_file(path)
        if content is None:
            continue
        for line in content.splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            for pat in suspicious_patterns:
                if pat.search(line):
                    findings.append(
                        Finding(
                            "Kernel Modules",
                            "T1547.006",
                            WARNING,
                            f"Suspicious module in {path}: {line[:120]}",
                        )
                    )
                    break

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding("Kernel Modules", "T1547.006", INFO, "No suspicious kernel modules detected.")
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1003 – SSH Key Exposure
# ---------------------------------------------------------------------------
def check_ssh_key_exposure():
    """Detect insecure SSH key permissions that enable credential theft (T1003)."""
    findings = []

    # Collect candidate SSH directories
    ssh_dirs = glob.glob("/home/*/.ssh") + ["/root/.ssh"]
    try:
        for pw in pwd.getpwall():
            candidate = os.path.join(pw.pw_dir, ".ssh")
            if candidate not in ssh_dirs and os.path.isdir(candidate):
                ssh_dirs.append(candidate)
    except Exception:
        pass

    for ssh_dir in ssh_dirs:
        if not os.path.isdir(ssh_dir):
            continue
        for fname in _list_dir(ssh_dir):
            fpath = os.path.join(ssh_dir, fname)
            try:
                st = os.stat(fpath)
                mode = stat.S_IMODE(st.st_mode)
                # Private key files should be readable only by owner (0o600 or 0o400)
                is_private = re.search(r"id_(rsa|dsa|ecdsa|ed25519)$", fname)
                if is_private:
                    if mode & (stat.S_IRGRP | stat.S_IROTH | stat.S_IWGRP | stat.S_IWOTH):
                        findings.append(
                            Finding(
                                "SSH Key Exposure",
                                "T1003",
                                ERROR,
                                f"Private key {fpath} has overly permissive mode "
                                f"{oct(mode)} (expected 0o600 or 0o400).",
                            )
                        )
                # authorized_keys should not be world-writable
                if fname == "authorized_keys":
                    if mode & stat.S_IWOTH:
                        findings.append(
                            Finding(
                                "SSH Key Exposure",
                                "T1003",
                                ERROR,
                                f"{fpath} is world-writable ({oct(mode)}) – attacker can add keys.",
                            )
                        )
            except (OSError, PermissionError):
                continue

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding("SSH Key Exposure", "T1003", INFO, "No SSH key permission issues detected.")
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1082 – Sensitive File Access
# ---------------------------------------------------------------------------
def check_sensitive_file_access():
    """Detect open file descriptors pointing at sensitive config files (T1082)."""
    findings = []
    sensitive_patterns = [
        re.compile(r"/etc/passwd"),
        re.compile(r"/etc/shadow"),
        re.compile(r"/etc/asterisk"),
        re.compile(r"\.conf$"),
        re.compile(r"routing"),
        re.compile(r"sip\.conf"),
        re.compile(r"extensions\.conf"),
    ]

    for pid in _list_dir("/proc"):
        if not pid.isdigit():
            continue
        fd_dir = f"/proc/{pid}/fd"
        for fd in _list_dir(fd_dir):
            fd_path = os.path.join(fd_dir, fd)
            try:
                target = os.readlink(fd_path)
            except (OSError, PermissionError):
                continue
            for pat in sensitive_patterns:
                if pat.search(target):
                    # Resolve process name
                    comm = _read_file(f"/proc/{pid}/comm") or "unknown"
                    comm = comm.strip()
                    findings.append(
                        Finding(
                            "Sensitive File Access",
                            "T1082",
                            WARNING,
                            f"Process {pid} ({comm}) has {target} open (fd {fd}).",
                        )
                    )
                    break

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding(
                "Sensitive File Access",
                "T1082",
                INFO,
                "No sensitive file access anomalies detected.",
            )
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1219 – Suspicious Processes
# ---------------------------------------------------------------------------
def check_suspicious_processes():
    """Detect processes running from temp or world-writable directories (T1219)."""
    findings = []
    suspicious_dirs = ["/tmp", "/dev/shm", "/var/tmp", "/run/shm"]

    for pid in _list_dir("/proc"):
        if not pid.isdigit():
            continue
        exe_path = f"/proc/{pid}/exe"
        try:
            exe = os.readlink(exe_path)
        except (OSError, PermissionError):
            continue
        for d in suspicious_dirs:
            if exe.startswith(d):
                comm = (_read_file(f"/proc/{pid}/comm") or "").strip()
                findings.append(
                    Finding(
                        "Suspicious Processes",
                        "T1219",
                        WARNING,
                        f"Process {pid} ({comm or 'unknown'}) is running from {exe}.",
                    )
                )
                break

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding(
                "Suspicious Processes",
                "T1219",
                INFO,
                "No processes running from suspicious directories detected.",
            )
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1074 – Data Staging
# ---------------------------------------------------------------------------
def check_data_staging(size_threshold_mb=50):
    """Detect large files in temp directories that may stage exfiltrated data (T1074)."""
    findings = []
    staging_dirs = ["/tmp", "/dev/shm", "/var/tmp"]
    threshold_bytes = size_threshold_mb * 1024 * 1024

    for staging_dir in staging_dirs:
        if not os.path.isdir(staging_dir):
            continue
        try:
            for entry in os.scandir(staging_dir):
                try:
                    if entry.is_file(follow_symlinks=False):
                        size = entry.stat().st_size
                        if size >= threshold_bytes:
                            size_mb = size / (1024 * 1024)
                            findings.append(
                                Finding(
                                    "Data Staging",
                                    "T1074",
                                    WARNING,
                                    f"Large file in {staging_dir}: {entry.name} "
                                    f"({size_mb:.1f} MB) – potential data staging.",
                                )
                            )
                except (OSError, PermissionError):
                    continue
        except (OSError, PermissionError):
            continue

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding(
                "Data Staging",
                "T1074",
                INFO,
                f"No files larger than {size_threshold_mb} MB found in staging directories.",
            )
        )
    return findings


# ---------------------------------------------------------------------------
# Check T1070.003 – History Cleared
# ---------------------------------------------------------------------------
def check_history_cleared():
    """Detect cleared or missing shell history that may indicate anti-forensics (T1070.003)."""
    findings = []

    # Collect history files for all users
    history_files = []
    try:
        for pw in pwd.getpwall():
            if pw.pw_uid < 1000 and pw.pw_name not in ("root",):
                continue  # skip most system accounts
            for hist in (".bash_history", ".sh_history", ".zsh_history"):
                candidate = os.path.join(pw.pw_dir, hist)
                history_files.append((pw.pw_name, candidate))
    except Exception:
        pass

    # Always include root (if not already added via getpwall)
    for hist in (".bash_history", ".sh_history", ".zsh_history"):
        entry = ("root", "/root/" + hist)
        if entry not in history_files:
            history_files.append(entry)

    for username, hist_path in history_files:
        if not os.path.exists(hist_path):
            # Missing history file for an interactive shell user can be suspicious
            continue
        content = _read_file(hist_path)
        if content is not None and content.strip() == "":
            findings.append(
                Finding(
                    "History Cleared",
                    "T1070.003",
                    WARNING,
                    f"Shell history file {hist_path} (user: {username}) is empty – "
                    "may indicate deliberate clearing.",
                )
            )

    # Also check if HISTFILE is set to /dev/null via /proc/*/environ
    for pid in _list_dir("/proc"):
        if not pid.isdigit():
            continue
        env_data = _read_file(f"/proc/{pid}/environ")
        if env_data:
            for item in env_data.split("\x00"):
                if item.startswith("HISTFILE=") and "/dev/null" in item:
                    comm = (_read_file(f"/proc/{pid}/comm") or "").strip()
                    findings.append(
                        Finding(
                            "History Cleared",
                            "T1070.003",
                            WARNING,
                            f"Process {pid} ({comm}) has HISTFILE=/dev/null set.",
                        )
                    )

    if not any(f.severity in (WARNING, ERROR) for f in findings):
        findings.append(
            Finding(
                "History Cleared",
                "T1070.003",
                INFO,
                "No shell history clearing indicators detected.",
            )
        )
    return findings


# ---------------------------------------------------------------------------
# Console output helpers
# ---------------------------------------------------------------------------
COLOURS = {
    INFO: "\033[32m",    # green
    WARNING: "\033[33m", # yellow
    ERROR: "\033[31m",   # red
    "RESET": "\033[0m",
}


def _colour(severity, text):
    """Return ANSI-coloured text if stdout is a terminal."""
    if sys.stdout.isatty():
        return f"{COLOURS.get(severity, '')}{text}{COLOURS['RESET']}"
    return text


def print_summary(all_findings):
    """Print a human-readable summary of all findings to stdout."""
    counts = {INFO: 0, WARNING: 0, ERROR: 0}
    for f in all_findings:
        counts[f.severity] = counts.get(f.severity, 0) + 1

    print("\n" + "=" * 60)
    print("  MontiSecurityScanner – Scan Summary")
    print("=" * 60)
    print(
        f"  {_colour(ERROR, str(counts[ERROR]) + ' ERROR(s)')}   "
        f"{_colour(WARNING, str(counts[WARNING]) + ' WARNING(s)')}   "
        f"{_colour(INFO, str(counts[INFO]) + ' INFO(s)')}"
    )
    print("=" * 60)

    current_technique = None
    for f in all_findings:
        if f.technique != current_technique:
            print(f"\n[{f.technique_id}] {f.technique}")
            current_technique = f.technique
        prefix = _colour(f.severity, f"  [{f.severity}]")
        print(f"{prefix} {f.detail}")

    print("\n" + "=" * 60)
    if counts[ERROR] > 0:
        print(_colour(ERROR, "  RESULT: CRITICAL – Immediate remediation required."))
    elif counts[WARNING] > 0:
        print(_colour(WARNING, "  RESULT: WARNINGS detected – Review and remediate."))
    else:
        print(_colour(INFO, "  RESULT: CLEAN – No significant threats detected."))
    print("=" * 60 + "\n")


# ---------------------------------------------------------------------------
# JSON report
# ---------------------------------------------------------------------------
def write_json_report(all_findings, log_path):
    """Write a structured JSON report to *log_path*."""
    counts = {INFO: 0, WARNING: 0, ERROR: 0}
    for f in all_findings:
        counts[f.severity] = counts.get(f.severity, 0) + 1

    report = {
        "scanner": "MontiSecurityScanner",
        "version": "1.0.0",
        "scan_time": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "hostname": os.uname().nodename,
        "summary": {
            "total": len(all_findings),
            "errors": counts[ERROR],
            "warnings": counts[WARNING],
            "info": counts[INFO],
        },
        "findings": [f.to_dict() for f in all_findings],
    }
    with open(log_path, "w") as fh:
        json.dump(report, fh, indent=2)
    return report


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------
def run_scan():
    """Execute all checks and return a flat list of Finding objects."""
    checks = [
        check_cron_persistence,
        check_ld_preload,
        check_kernel_modules,
        check_ssh_key_exposure,
        check_sensitive_file_access,
        check_suspicious_processes,
        check_data_staging,
        check_history_cleared,
    ]
    all_findings = []
    for check in checks:
        try:
            all_findings.extend(check())
        except Exception as exc:
            logging.warning("Check %s raised an exception: %s", check.__name__, exc)
    return all_findings


def main():
    parser = argparse.ArgumentParser(
        description="MontiSecurityScanner – Telecom host-based anomaly detector",
    )
    parser.add_argument(
        "--log",
        default="monti_scan.json",
        metavar="PATH",
        help="Path for the JSON report output (default: monti_scan.json)",
    )
    args = parser.parse_args()

    print("MontiSecurityScanner starting…")
    all_findings = run_scan()
    print_summary(all_findings)
    report = write_json_report(all_findings, args.log)
    print(f"JSON report written to: {args.log}")
    print(
        f"Scan complete – {report['summary']['errors']} error(s), "
        f"{report['summary']['warnings']} warning(s)."
    )

    # Exit with non-zero code if errors or warnings found
    if report["summary"]["errors"] > 0:
        sys.exit(2)
    if report["summary"]["warnings"] > 0:
        sys.exit(1)
    sys.exit(0)


if __name__ == "__main__":
    main()
