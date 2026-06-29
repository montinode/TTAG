# Environment Variable Security Guide

This guide documents secure practices for managing environment variables and secrets in the MONTIDROID OS project.

## Quick-start

1. Copy the sanitized template to a local file:
   ```sh
   cp .env.example .env
   chmod 600 .env
   ```
2. Fill in real values in `.env` on the target machine.
3. **Never** commit `.env` or any file that contains actual credentials. The `.gitignore` already excludes it.

---

## What to put in `.env.example`

`.env.example` exists in version control as documentation only. Every value **must** be a placeholder (e.g., `<your-cloudflare-api-token>`), never a real secret.

---

## Storing secrets safely

| Method | When to use |
|--------|-------------|
| **OS keyring / secret-manager daemon** | Developer workstations; avoids plain-text storage |
| **HashiCorp Vault / AWS Secrets Manager / GCP Secret Manager / Azure Key Vault** | Server / cloud deployments; provides audit logs, ACLs, and automatic rotation |
| **CI/CD secret variables** (GitHub Actions `secrets.*`) | Pipelines; values are masked in logs and never written to disk |
| **Encrypted `.env` file** (e.g., `age`, `sops`) | When a file on disk is unavoidable; encrypt before committing |

### Minimal example – GitHub Actions

```yaml
env:
  CLOUDFLARE_API_KEY: ${{ secrets.CLOUDFLARE_API_KEY }}
```

Set the secret once in **Settings → Secrets and variables → Actions**. It is never echoed in logs.

---

## Secret rotation checklist

Use this checklist whenever a secret may have been exposed (accidental commit, paste in a chat, screenshot, etc.):

- [ ] **Revoke immediately** – invalidate the compromised credential in the issuing service (Cloudflare dashboard, wallet provider, etc.) before doing anything else.
- [ ] **Generate a replacement** – create a new credential with the minimum necessary permissions.
- [ ] **Update all consumers** – deploy the new value to every environment (dev, staging, prod) that uses it.
- [ ] **Rotate related secrets** – if the exposed secret could have been used to derive or access others, rotate those too.
- [ ] **Audit access logs** – check the issuing service's audit trail for unauthorized use during the window of exposure.
- [ ] **Purge from history** – if the secret was committed to git, use `git filter-repo` (or BFG Repo Cleaner) to rewrite history and then force-push; notify all collaborators to re-clone.
- [ ] **Document the incident** – record what was exposed, the exposure window, actions taken, and any required notifications.

---

## Cloudflare API token best practices

* Use **API tokens** (scoped) rather than the global API key.
* Grant the minimum zone/account permissions required.
* Set an expiry date and IP source filter where possible.
* Rotate tokens every 90 days as a baseline.

---

## Wallet / private key guidance

* **Never** store private keys in `.env` files on internet-connected systems. Use a hardware wallet (Ledger, Trezor) or an air-gapped signing device.
* Public addresses and chain IDs are non-secret and safe to store in configuration.
* If a private key has been pasted into any chat, email, or public surface, treat it as permanently compromised and move funds immediately.

---

## Local development

```sh
# One-time setup
cp .env.example .env
chmod 600 .env          # restrict to owner read/write only
# Edit .env with real values using your preferred editor
```

Never export `.env` values into your shell profile (`~/.bashrc`, `~/.zshrc`) – this exposes them to every process in your session.

---

## References

* [OWASP: Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
* [GitHub: Encrypted secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
* [HashiCorp Vault documentation](https://developer.hashicorp.com/vault/docs)
* [git-filter-repo](https://github.com/newren/git-filter-repo) – remove secrets from git history
