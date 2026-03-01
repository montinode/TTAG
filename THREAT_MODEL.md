# Threat Model

## Scope
This threat model covers the MontiSecurityScanner workflow and supporting scripts in this repository, including local execution, scan output handling, and any optional integrations.

## Assets
- Host integrity on scanned devices
- Scan reports (JSON output)
- Credentials or privileged execution contexts (e.g., sudo)
- Logs and audit trails
- Repository code and release artifacts

## Assumptions
- Scanner is executed by authorized operators
- Target hosts are Linux-based systems
- Network access is limited to administrative use
- Logs are protected from unauthorized modification

## Threats
1. **Unauthorized execution**
   - Risk: Unapproved users run scans or gain access to reports.
   - Mitigation: Require explicit operator authentication; restrict file permissions.

2. **Tampering with scan results**
   - Risk: Attackers alter JSON outputs to hide findings.
   - Mitigation: Write reports to protected directories; add hash/signature validation if needed.

3. **Privilege escalation abuse**
   - Risk: Running with sudo expands attack surface.
   - Mitigation: Use least-privilege where possible; document required privileges.

4. **Data leakage**
   - Risk: Reports contain sensitive paths or credentials.
   - Mitigation: Redact secrets; control access to logs and reports.

5. **Supply chain compromise**
   - Risk: Modified scanner code introduces malicious behavior.
   - Mitigation: Require code review; pin dependencies; verify release checksums.

## Out of Scope
- Network intrusion detection or prevention
- Telecom signaling protocol security
- Physical security controls

## Residual Risk
Some findings may still depend on operator judgment; periodic review of the threat model is required.