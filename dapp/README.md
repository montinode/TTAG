# Kotlin dApp Sample

A sample Android decentralized application (dApp) built with Kotlin, demonstrating WalletConnect
[AppKit](https://docs.reown.com/appkit/android/core/installation) integration.

This module is inspired by the [reown-kotlin sample dApp](https://github.com/reown-com/reown-kotlin/tree/b20e813b09f5dc56bcbcc679c3a66c633250f776/sample/dapp) and implements the same core patterns using a traditional Android View-based UI.

## Features

| Feature | Description |
|---------|-------------|
| **Chain selection** | Choose one or more EVM / Solana chains before connecting |
| **WalletConnect pairing** | Generates a WC URI; copy it or open a wallet app directly |
| **Session management** | See connected wallet, list of accounts, and active namespaces |
| **Ping** | Verify the relay connection is alive |
| **personal_sign** | Send an EIP-191 signing request to the connected wallet |
| **Disconnect** | Cleanly end the session |

## Architecture

```
dapp/
├── DappSampleApp.kt          ← Application: CoreClient + AppKit init
├── domain/
│   └── DappDelegate.kt       ← AppKit.ModalDelegate + CoreClient.CoreDelegate → Kotlin Flows
└── ui/
    ├── ChainItem.kt          ← Data models + SupportedChains catalogue
    ├── DappSampleActivity.kt ← Activity: hosts fragments, handles deep links
    ├── DappSampleEvent.kt    ← Sealed one-shot UI events
    ├── routes/
    │   ├── ChainSelectionViewModel.kt
    │   └── SessionViewModel.kt
    └── screens/
        ├── ChainSelectionFragment.kt
        ├── SessionFragment.kt
        └── PairingUriDialogFragment.kt
```

## Setup

1. Obtain a **WalletConnect Cloud Project ID** from [cloud.reown.com](https://cloud.reown.com).
2. Set the environment variable before building:

   ```bash
   export WC_CLOUD_PROJECT_ID="your_project_id_here"
   ```

3. Build and install the `:dapp` module:

   ```bash
   ./gradlew :dapp:installDebug
   ```

## Deep-link support

The app registers the `kotlin-dapp-wc://request` scheme to receive wallet responses when the user
approves or rejects a session from a wallet that redirects back via deep link.

## Security

- The `WC_CLOUD_PROJECT_ID` is injected at build time via an environment variable and is **never**
  committed to source control.
- All WalletConnect communication is end-to-end encrypted by the SDK.
