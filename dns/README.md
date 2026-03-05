# DNS Records – johncharlesmonti.com

This directory contains DNS record configuration templates for the
`johncharlesmonti.com` zone, used by the TTAG PWA and the private Ethereum
node endpoint.

## Record Types

| Type | Purpose |
|------|---------|
| **SOA** | Zone authority and serial number |
| **NS** | Authoritative nameservers |
| **A / AAAA** | IPv4 / IPv6 addresses for root, `pwa.`, and `eth.` subdomains |
| **TXT** | SPF e-mail authentication and DMARC policy |
| **TLSA** | DANE certificate pinning for HTTPS endpoints (also known as TSLA records) |
| **CAA** | Restrict which CAs may issue certificates |

## TLSA Records (DANE)

TLSA records (RFC 6698) bind a TLS certificate to a specific domain via
DNSSEC-validated DNS, removing dependence on the traditional CA trust
hierarchy.

> **Note:** The project requirement referred to these as "TSLA records" – this
> is a misspelling. The correct DNS record type is **TLSA**
> (Transport Layer Security Authentication).

Two TLSA records are defined:

- `_443._tcp.pwa.johncharlesmonti.com` – the PWA front-end
- `_443._tcp.eth.johncharlesmonti.com` – the private Ethereum RPC node

### Generating the certificate hash

```bash
# From a PEM certificate file:
openssl x509 -in cert.pem -noout -pubkey \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -hex
```

Replace `<sha256-hex-of-subject-public-key-info>` in `records.json` with the
output hex string.

## Applying the Records

### BIND / RFC 1035 zone file format

```bind
$ORIGIN johncharlesmonti.com.
$TTL 300

@       IN  A       <IPv4-address>
pwa     IN  A       <IPv4-address>
eth     IN  A       <IPv4-address>

@       IN  TXT     "v=spf1 ip4:<IPv4-address> ~all"

_443._tcp.pwa  IN  TLSA  3 1 1 <cert-sha256>
_443._tcp.eth  IN  TLSA  3 1 1 <cert-sha256>
```

### Cloudflare (via Terraform)

```hcl
resource "cloudflare_record" "tlsa_pwa" {
  zone_id = var.zone_id
  name    = "_443._tcp.pwa"
  type    = "TLSA"
  data = {
    usage         = 3
    selector      = 1
    matching_type = 1
    certificate   = "<cert-sha256>"
  }
  ttl = 300
}
```

### Route 53 / other providers

Use the JSON structure in `records.json` as the authoritative reference and
translate to your provider's API format.

## DNSSEC

TLSA records are only useful when DNSSEC is enabled on the zone. Enable DNSSEC
in your DNS provider's control panel and publish the DS record at your registrar
before adding TLSA records.

## Updating Records

1. Increment the `serial` field in the SOA record (format: `YYYYMMDDnn`).
2. Update the relevant record values.
3. Apply the changes through your DNS provider.
4. Verify propagation: `dig TLSA _443._tcp.pwa.johncharlesmonti.com`
