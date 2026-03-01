# TTAG – NFC Tag Toolkit for Android

A sample Android application demonstrating NFC tag reading and writing using
MIFARE Classic tags, integrated with the
[PrintHand](https://github.com/DynamixSoftware/PrintingSample) printing SDK.

---

## Features

- **MIFARE Classic NFC tag operations**: connect, authenticate (Key A / Key B),
  read blocks, write blocks, and value-block arithmetic (increment, decrement,
  restore/transfer).
- **PrintHand integration**: print content via Share Intent, Intent API, or the
  Printing SDK service.
- **Anti-Spoofing validation**: runtime checks for DNS spoofing, domain spoofing,
  and system-hijacking attempts.

---

## MIFARE Classic Support

### Overview

[MIFARE Classic](https://www.nxp.com/products/rfid-nfc/mifare-classic:MC_41863)
is a widely used contactless smart-card standard (ISO/IEC 14443 Type A) operating
at 13.56 MHz with a typical read range of ~10 cm.

| Variant | Memory  | Sectors | Blocks/sector |
|---------|---------|---------|---------------|
| 1K      | 1024 B  | 16      | 4             |
| 4K      | 4096 B  | 40      | 4 or 16       |

Each sector's last block (the *sector trailer*) holds Key A, access conditions,
and Key B. Block 0 of Sector 0 is the manufacturer block (UID, read-only).

### Key Operations (`MifareClassicHelper`)

| Method | Description |
|--------|-------------|
| `connect()` | Establishes an ISO-DEP connection to the tag |
| `authenticateSectorWithKeyA(sector, key)` | Authenticates using Key A |
| `authenticateSectorWithKeyB(sector, key)` | Authenticates using Key B |
| `readBlock(blockIndex)` | Returns 16 bytes from the specified block |
| `writeBlock(blockIndex, data)` | Writes exactly 16 bytes to a block |
| `increment(blockIndex, value)` | Increments a value block and transfers |
| `decrement(blockIndex, value)` | Decrements a value block and transfers |
| `restore(blockIndex)` | Restores a value block in place |

### Security Notes

- Change default keys (`FFFFFFFFFFFF`) before deploying tags in production.
- Use per-sector key diversification (derive keys from a master secret + UID).
- The app declares `android.hardware.nfc` as **optional** so it installs on
  devices without NFC hardware; guard NFC calls with `NfcAdapter` availability
  checks at runtime.

---

## Printing Integration

The app demonstrates three ways to send content to
[PrintHand Mobile Print](https://play.google.com/store/apps/details?id=com.dynamixsoftware.printhand):

1. **Share Intent** – fire-and-forget broadcast via `ACTION_SEND`; supports
   images, text, and binary files.
2. **Intent API** – direct uplink to the PrintHand service; allows discovery of
   available printers and control over rendering parameters.
3. **Printing SDK** – background service integration; no UI required on the host
   device; supports Bluetooth and USB printers.

---

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio (Arctic Fox or later).
3. Build and install on a device or emulator:
   ```
   ./gradlew :printingSample:installDebug
   ```
4. Tap a MIFARE Classic tag against the device to trigger the NFC intent filter
   and exercise the tag operations.

---

## Requirements

- Android SDK 21+
- NFC-capable device (optional – the app degrades gracefully without NFC)
- [PrintHand](https://play.google.com/store/apps/details?id=com.dynamixsoftware.printhand)
  installed for printing features
