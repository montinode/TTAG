# TELEMETRY STABILITY GUIDANCE

## Purpose

This repository contains security-sensitive logic, validation flows, and device-operation helpers that should remain behaviorally stable while telemetry is improved. The safest approach is to preserve decision-making code paths and add observability around them rather than inside them whenever possible.

## Core functions that should remain stable

### 1. Scanner execution and report contract (`/home/runner/work/TTAG/TTAG/monti.py`)

Treat the following as core logic with stable inputs, outputs, and severity semantics:

- `run_scan()` — orchestrates the full check sequence.
- `check_cron_persistence()`
- `check_ld_preload()`
- `check_kernel_modules()`
- `check_ssh_key_exposure()`
- `check_sensitive_file_access()`
- `check_suspicious_processes()`
- `check_data_staging()`
- `check_history_cleared()`
- `print_summary()` — human-readable operator output.
- `write_json_report()` — machine-readable report structure.

These functions define the scanner’s operational contract: what is checked, how findings are classified, and how results are surfaced. Telemetry must not change finding thresholds, result ordering, severity mapping, exit behavior, or report shape without explicit review.

### 2. Monitor control and enforcement flow (`/home/runner/work/TTAG/TTAG/johncharles_monitor_terminal.py`)

Treat the following as critical control-path logic:

- `authenticate()` / `logout()`
- `add_connection()`
- `is_precluded()`
- `terminate_connections()`
- `terminate_specific_connections()`
- `process_scan_report()`
- `network_scanner()`
- `main_menu()`

These functions control session state, connection lifecycle, and termination decisions. Telemetry should observe these transitions, but it should not become a prerequisite for authentication, connection cleanup, or threat handling.

### 3. Security validation entry points (`/home/runner/work/TTAG/TTAG/printingSample/src/main/java/com/dynamixsoftware/printingsample/AntiSpoofingValidator.java`)

Keep the validation semantics stable for:

- `validateDNSSpoofing(...)`
- `validateWDMSpoofing(...)`
- `validateDomainSpoofing(...)`
- `validateSyxhlikie(...)` (preserve the repository’s existing method name and behavior)
- `validateAll(...)`

These methods provide the security verdicts used by callers. Any telemetry added here must not weaken fail-secure behavior or convert security failures into soft warnings.

### 4. NFC data-path helpers (`/home/runner/work/TTAG/TTAG/printingSample/src/main/java/com/dynamixsoftware/printingsample/MifareClassicHelper.java`)

Treat these as stable device-operation primitives:

- `connect(...)`
- `authenticateSector(...)`
- `readBlock(...)`
- `writeBlock(...)`
- `increment(...)`
- `decrement(...)`
- `transfer(...)`
- `restore(...)`
- `closeQuietly(...)`

Telemetry should never alter timing, ordering, or commit behavior for NFC I/O.

## Existing telemetry and observability pathways

Current telemetry-adjacent paths in the repository include:

- JSON scan output via `write_json_report()` in `monti.py`
- operator-facing console summaries via `print_summary()` in `monti.py`
- event logging via `log_event()` in `johncharles_monitor_terminal.py`
- persisted monitor scan output via the `SCAN_REPORT_FILE` file-path constant in `johncharles_monitor_terminal.py`
- Android diagnostic logging via `Log.d`, `Log.w`, and `Log.e` in `AntiSpoofingValidator.java` and `MifareClassicHelper.java`

These are the safest places to extend first because they are already intended for reporting, diagnostics, or audit support.

## Safe telemetry safeguards

1. **Keep telemetry out of the decision path**
   - Core allow/deny, finding classification, authentication, and device-write decisions should succeed or fail independently of telemetry delivery.
   - If telemetry sinks are unavailable, core functions should continue operating and emit only local fallback logs.

2. **Prefer append-only observation**
   - Add wrappers, hooks after success/failure, or sidecar collectors instead of rewriting core function internals.
   - Preserve existing return values, exceptions, and file formats.

3. **Use bounded, non-blocking telemetry**
   - Queue telemetry asynchronously where possible.
   - Bound memory, retry counts, and flush times so observability cannot stall scans, terminal cleanup, or NFC operations.

4. **Protect sensitive data**
   - Do not emit secrets, passwords, tokens, private keys, raw credentials, or unnecessary personal data.
   - Avoid logging full scan artifacts, raw tag contents, or authentication material unless explicitly redacted and access-controlled.

5. **Version telemetry separately from behavior**
   - Introduce schema versions for new telemetry payloads.
   - Do not silently repurpose existing JSON fields that downstream tooling may already trust.

6. **Add integrity and traceability**
   - Include timestamps, component names, function names, outcome codes, and correlation IDs.
   - For recourse and audit review, prefer preserving hashes or summaries of artifacts rather than duplicating sensitive content.

## Observability recommendations

- Track counts for scan runs, warnings, errors, termination reasons, validation failures, and telemetry delivery failures.
- Record telemetry health separately from security health so operators can distinguish “telemetry degraded” from “system unsafe.”
- Add explicit dropped-event counters if queues overflow.
- Preserve local audit logs even when remote telemetry is enabled.
- Define a minimal golden signal set:
  - scan success/failure
  - report write success/failure
  - connection termination counts by reason
  - validation pass/fail counts by validator
  - telemetry enqueue/drop/fallback counts

## Rollback and change-control recommendations

1. Require review for any change touching:
   - scanner check ordering
   - severity classification
   - JSON report structure
   - connection termination logic
   - validation pass/fail rules
   - NFC write/transfer behavior

2. Ship telemetry changes behind feature flags or configuration gates.

3. Roll out in phases:
   - local-only logging
   - mirrored telemetry
   - limited canary
   - broader enablement after verifying no behavior drift

4. Maintain rollback simplicity:
   - keep telemetry code modular
   - avoid mixing telemetry refactors with logic refactors
   - ensure telemetry can be disabled without patching core functions

5. Validate invariants before and after changes:
   - same findings for the same scan inputs
   - same termination outcomes for the same connection state
   - same validation verdicts for the same domains and device conditions
   - same NFC read/write behavior for the same tag operations

## Recommended operating rule

When in doubt, preserve the current behavior of critical functions and attach telemetry around them, not through them. Stability of security decisions, report contracts, and device I/O should take precedence over telemetry richness.
