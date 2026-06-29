# Telemetry Stability Guidance

## Principle

**Core paths are deterministic and must remain unchanged.**  
Telemetry is added *around* core logic, never *through* it. Critical functions must behave identically whether telemetry is enabled or disabled.

---

## Non-Blocking Telemetry Collection

- All telemetry emission must be **asynchronous and non-blocking**.
- Use a dedicated background thread, worker queue, or event bus — never inline blocking I/O inside a critical code path.
- Telemetry calls must not hold locks owned by the core path.

---

## Failure Tolerance

- Telemetry failure **must never break or degrade core execution**.
- On error, apply one of: **queue → retry → drop** (in that order of preference).
- If the queue is full, drop silently and increment a drop counter.
- Never propagate telemetry exceptions to the caller.

---

## Feature Flags

- Each telemetry sink (remote collector, local log, analytics pipeline) must be independently **togglable via a feature flag** at runtime.
- Verbosity levels (e.g., `NONE`, `ERROR`, `INFO`, `DEBUG`) must be configurable without a code deploy.
- Default to the least-verbose safe level in production.

---

## Data Minimization and Redaction

- Collect only the **minimum data required** for the stated observability purpose.
- **Do not capture** passwords, private keys, authentication tokens, full secrets, or unnecessary PII.
- Apply redaction/masking before any data leaves the process boundary.
- Document every collected field and its retention period.

---

## Observability SLOs

| Signal | SLO |
|---|---|
| Core function success rate | ≥ 99.9 % over any 5-minute window |
| Core function P99 latency | No regression vs. pre-telemetry baseline |
| Telemetry delivery rate | ≥ 95 % of emitted events reach the sink within 60 s |
| Telemetry queue lag | < 10 s under normal load |
| Telemetry drop rate | < 1 % under normal load |

---

## Rollback, Canary, and Change Control

- Any change to telemetry schema, collector, or sampling rate is treated as a **production change** and requires a change-control record.
- Deploy telemetry changes via **canary rollout** (≥ 5 % of traffic) before full promotion.
- Maintain a one-step **rollback** path: a flag flip or config revert that disables the new telemetry without touching the core function.
- Changes that touch both core logic and telemetry in the same commit are prohibited.

---

## Implementation Checklist

- [ ] Core function is isolated behind a stable interface; telemetry is in a wrapper/decorator.
- [ ] All telemetry calls are async and guarded by `try/catch` (or equivalent).
- [ ] Feature flag exists and defaults to off in staging/testing.
- [ ] Redaction layer applied before data leaves the process.
- [ ] No secrets or full credentials in any telemetry payload.
- [ ] SLO dashboards and alerts configured for both core and telemetry signals.
- [ ] Canary rollout plan documented before deployment.
- [ ] Rollback procedure tested and confirmed < 5 minutes to execute.
- [ ] Change-control record created and approved.
