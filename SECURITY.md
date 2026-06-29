# Security Advisory – Private Repository Handling

> **Status:** Informational / Documentation-only  
> **Applies to:** All contributors and operators of this repository

---

## 1. Private ≠ Secure

A private GitHub repository restricts **visibility**, but does **not** protect secrets
embedded in commit history, file contents, chat threads, issue comments, or CI logs.

- Any collaborator with read access can extract every secret ever committed.
- GitHub staff and tooling (Copilot, Actions, third-party integrations) may process
  repository content.
- Repository forks, exports, and API responses can leak content outside the intended
  audience.
- GitHub's **secret scanning** actively detects and flags known credential patterns
  in private repos when the feature is enabled.

**Never treat "private" as a substitute for proper secret management.**

---

## 2. Secret Management Requirements

| Do | Do Not |
|----|--------|
| Store secrets in a dedicated secret manager (e.g., GitHub Secrets, HashiCorp Vault, AWS Secrets Manager, 1Password Secrets Automation) | Hard-code API keys, tokens, or passwords in source files or `.env` files that are committed |
| Inject secrets at runtime via environment variables or secret-manager SDKs | Paste credentials into chat interfaces, issue comments, or documentation |
| Use scoped, short-lived tokens (e.g., Cloudflare API tokens scoped to a single zone) | Use global/root API keys when a scoped token is available |
| Add `.env`, `*.key`, and credential files to `.gitignore` | Rely on file permissions alone (e.g., `chmod 600`) as access control for secrets |
| Audit who has repository access regularly | Grant broad collaborator access without periodic review |

---

## 3. Rotation Policy

Rotate a credential **immediately** if any of the following conditions are true:

- It was pasted into any chat, issue, PR description, commit message, or log.
- It appears in any file that was ever committed (even if the file has since been deleted – history persists).
- A collaborator whose access has been revoked previously held access.
- The originating service reports suspicious or unexpected usage.
- You cannot account for all locations where the value is stored.

**Recommended baseline rotation cadence** (regardless of exposure):

| Credential type | Maximum lifetime |
|-----------------|-----------------|
| API tokens / bearer tokens | 90 days |
| Service account keys | 180 days |
| Personal access tokens | 90 days |
| Symmetric encryption keys | 1 year |
| Asymmetric key pairs | 2 years (with hardware binding preferred) |

After rotation: revoke the old credential in the issuing service, update all injection
points, verify functionality, and document the rotation event in your audit log.

---

## 4. Least-Privilege Principles

- **Scope tokens** to the minimum set of permissions required for the specific operation.
  For example, a Cloudflare token used only to update DNS records should have only
  `Zone / DNS / Edit` permission on the target zone—not `Account / Administrator`.
- **Separate credentials** per environment (development, staging, production).
  A leaked development credential must never grant production access.
- **Limit repository collaborator roles**: use `read` access unless `write` or `admin`
  is explicitly required.
- **Audit access logs** in your secret manager and issuing services to detect
  unexpected usage early.

---

## 5. Credential Exposure Response Checklist

If credentials have been shared in this repository or in adjacent chat context:

- [ ] Identify every credential value that may have been exposed.
- [ ] Rotate / revoke each credential immediately in the issuing service.
- [ ] Confirm the old credential is fully invalidated (test a request; expect 401/403).
- [ ] Update all systems that legitimately used the old credential with the new value.
- [ ] If the credential is in git history, consider a history rewrite (`git filter-repo`)
      and forced push — note this is disruptive and requires all collaborators to
      re-clone.
- [ ] Enable GitHub secret scanning alerts for this repository
      (**Settings → Code security → Secret scanning**).
- [ ] Document the incident (what was exposed, when, rotation timestamp, who was notified).

---

## 6. References

- [GitHub – Secret scanning documentation](https://docs.github.com/en/code-security/secret-scanning)
- [GitHub – Security best practices for repositories](https://docs.github.com/en/code-security/getting-started/securing-your-repository)
- [NIST SP 800-57 – Key Management Guidelines](https://csrc.nist.gov/publications/detail/sp/800-57-part-1/rev-5/final)
- [OWASP – Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
