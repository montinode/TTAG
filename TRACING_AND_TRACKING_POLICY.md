# Tracing and Tracking Policy

*In the best interest of John Charles Monti — and exclusively so.*

---

## 1. Purpose and Scope

This policy governs the collection, storage, and use of tracing and tracking data within systems owned or operated under the authority of John Charles Monti ("the Owner"). It applies to all software components, services, agents, and pipelines in this repository and any downstream deployments.

Tracing and tracking activities are permitted **only on authorized systems** and **only for purposes explicitly listed in this policy**.

---

## 2. Lawful Basis and Authorization

- All tracing and tracking activities require **prior written or documented authorization** from the Owner.
- Automated instrumentation must be reviewed and approved before deployment.
- No system may trace, log, or track user behavior, system events, or data flows without an active authorization record.
- Authorization records must identify: the purpose, data types collected, retention period, and responsible party.

---

## 3. Data Minimization and Retention

- Collect **only the data necessary** for the stated authorized purpose.
- Each data field must be justified; unjustified fields must be removed.
- **Default retention**: 30 days for operational traces; 90 days for audit logs.
- Data must be deleted promptly at the end of its retention period.
- Aggregated or anonymized metrics may be retained longer where individual identification is not possible.

---

## 4. Sensitive Data Exclusions

The following **must never** be captured in any trace or tracking record:

- Passwords, passphrases, or authentication secrets
- Private keys, signing keys, or seed phrases
- Full API tokens or credentials (partial/masked references only, e.g., last 4 characters)
- Full payment card numbers, bank account numbers
- Unnecessary personally identifiable information (PII) beyond what is explicitly authorized
- Medical, biometric, or other special-category data

Any pipeline that inadvertently captures the above must mask, redact, or delete the data immediately and report the incident (see §6).

---

## 5. Access Controls and Audit Logging

- Access to raw trace and tracking data is **restricted to authorized personnel** with a documented need.
- Access must be granted on a least-privilege basis and reviewed quarterly.
- All access to trace stores must itself be **audit-logged** with: accessor identity, timestamp, and action.
- Audit logs are immutable and retained for a minimum of 1 year.

---

## 6. Incident Response and Breach Handling

- Any unauthorized access, unexpected data capture, or suspected breach must be reported to the Owner within **24 hours** of discovery.
- The affected trace pipeline must be isolated or disabled pending investigation.
- A post-incident report must be produced within 7 days, covering: timeline, root cause, data affected, and remediation steps.
- Affected parties must be notified in accordance with applicable law.

---

## 7. Change Management and Review Cadence

- Changes to tracing scope, data fields, retention periods, or access controls require a change-control record approved by the Owner before deployment.
- This policy is reviewed **at minimum every 6 months** or after any significant system change or incident.
- The review date and reviewer must be recorded in the repository's change log.

---

## 8. Prohibited Uses

The following uses of tracing or tracking data are **strictly prohibited**:

- Harassment, monitoring, or surveillance of individuals without their knowledge and authorization
- Unauthorized surveillance of third-party systems or networks
- Anti-competitive intelligence gathering against other parties
- Profiling individuals for purposes beyond the authorized operational scope
- Sharing trace data with third parties without explicit Owner authorization
- Using trace data to make automated decisions with significant legal or personal impact without human review

Violation of any prohibition may result in immediate suspension of access, removal of authorization, and escalation to appropriate legal authorities.

---

*Policy owner: John Charles Monti. Effective from date of commit. Subject to periodic review per §7.*
