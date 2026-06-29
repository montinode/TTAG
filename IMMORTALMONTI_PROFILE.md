# IMMORTALMONTI Profile

Privacy-first owner-visible asset inventory template. Use placeholders only.

## Privacy protections
- Collect the minimum metadata needed to identify an asset category and review status.
- Mask identifiers by default (for example `acct-***1234`, `0x****abcd`, `device-***7F`).
- Grant access on a least-privilege, need-to-review basis only.
- Store approved records with encryption at rest and use encrypted transport in transit.
- Enable access auditing for views, exports, edits, and permission changes.

## Do not store in plaintext
- Private keys or seed phrases
- Raw passwords, API tokens, or recovery codes
- Full bank, card, or account numbers
- Full government identifiers, birth dates, or other sensitive personal identifiers

## Owner-visible asset inventory

### Accounts
| Label | Masked identifier | Provider | Status | Last reviewed |
| --- | --- | --- | --- | --- |
| `[account label]` | `[masked account id]` | `[provider]` | `[active/inactive]` | `[YYYY-MM-DD]` |

### Wallets
| Label | Masked address/reference | Network | Access method | Last reviewed |
| --- | --- | --- | --- | --- |
| `[wallet label]` | `[masked wallet reference]` | `[network]` | `[hardware/custodial/view-only]` | `[YYYY-MM-DD]` |

### Domains
| Domain label | Registrar | DNS/provider | Renewal window | Last reviewed |
| --- | --- | --- | --- | --- |
| `[domain]` | `[registrar]` | `[provider]` | `[date or term]` | `[YYYY-MM-DD]` |

### Devices
| Device label | Masked identifier | Platform | Custody/location | Last reviewed |
| --- | --- | --- | --- | --- |
| `[device name]` | `[masked serial or asset tag]` | `[platform]` | `[location]` | `[YYYY-MM-DD]` |

### Subscriptions
| Service | Billing owner | Renewal window | Access role | Last reviewed |
| --- | --- | --- | --- | --- |
| `[service]` | `[legal owner/entity]` | `[date or term]` | `[owner/admin/view-only]` | `[YYYY-MM-DD]` |

### Documents
| Document type | Storage location | Access level | Retention note | Last reviewed |
| --- | --- | --- | --- | --- |
| `[document class]` | `[vault/repository/folder]` | `[restricted]` | `[retention rule]` | `[YYYY-MM-DD]` |

## Review reminder
Use this file as a high-level index only. Keep sensitive source records in approved secure systems, not in plaintext markdown.
