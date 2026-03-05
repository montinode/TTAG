# TTAG PWA – Progressive Web Application

A high-quality Svelte-based Progressive Web Application (PWA) providing a
security monitoring dashboard with:

- **Offline support** via a Workbox-powered service worker
- **Installable** on desktop and mobile (Web App Manifest)
- **Ethereum blockchain integration** – connects to a configurable private
  Ethereum JSON-RPC node (e.g. `https://eth.johncharlesmonti.com`)
- **DNS record reference** – live view of the `dns/records.json` templates
- **MAGP-ready** – structured for integration with the
  [montinode/MAGP](https://github.com/montinode/MAGP) service

## Quick Start

```bash
cd pwa
npm install
npm run dev          # development server on http://localhost:5173
npm run build        # production build → dist/
npm run preview      # preview the production build
```

## Configuration

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_ETHEREUM_RPC_URL` | Private Ethereum JSON-RPC endpoint | `https://eth.johncharlesmonti.com` |
| `VITE_MAGP_API_URL` | MAGP service API base URL | `https://api.johncharlesmonti.com/magp` |

If `VITE_ETHEREUM_RPC_URL` is not set, the app falls back to the public
[PublicNode](https://ethereum.publicnode.com) mainnet endpoint.

## MAGP Integration

Install the MAGP package (requires repository access):

```bash
npm install github:montinode/MAGP
```

Then import it in your components:

```js
import { /* MAGP exports */ } from 'magp';
```

Update `VITE_MAGP_API_URL` in `.env` to point to the MAGP service endpoint.

## Project Structure

```
pwa/
├── index.html                     – HTML entry point
├── vite.config.js                 – Vite + PWA plugin config (manifest, Workbox)
├── package.json                   – dependencies
├── .env.example                   – environment variable template
└── src/
    ├── main.js                    – mounts App, registers service worker
    ├── App.svelte                 – main dashboard (tabs: Dashboard / Ethereum / DNS)
    └── lib/
        ├── ethereum/
        │   └── provider.js        – ethers.js JsonRpcProvider (configurable)
        └── components/
            └── EthereumStatus.svelte  – live Ethereum network status widget
```

## DNS Records

See [`../dns/`](../dns/) for TLSA, TXT, A, AAAA, NS, CAA, and SOA record
templates for the `johncharlesmonti.com` zone.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | [Svelte 5](https://svelte.dev) |
| Build tool | [Vite 7](https://vite.dev) |
| PWA plugin | [vite-plugin-pwa](https://vite-pwa-org.netlify.app/) |
| Service worker | [Workbox](https://developer.chrome.com/docs/workbox) |
| Ethereum | [ethers.js 6](https://docs.ethers.org/v6/) |

## PWA Features

- **Manifest** – name, icons, theme colour, display mode, shortcuts
- **Service Worker** – auto-update strategy, offline asset caching, runtime
  caching for Ethereum RPC calls
- **HTTPS-ready** – works over HTTPS in production (required for service workers)
