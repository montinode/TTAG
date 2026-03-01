# TTAG — NFC Tag Tool (Android)

A sample Android application demonstrating NFC tag interactions, with a focus on **MIFARE Classic** read/write operations and anti-spoofing security validation.

---

## MIFARE Classic Overview

MIFARE Classic is a widely used RFID/NFC standard by NXP Semiconductors, operating at **13.56 MHz** (ISO/IEC 14443 Type A), with a typical read range of ~10 cm.

| Variant | Storage | Sectors | Blocks/Sector |
|---------|---------|---------|---------------|
| 1K      | 1024 B  | 16      | 4             |
| 2K      | 2048 B  | 32      | 4             |
| 4K      | 4096 B  | 40      | 4 (sectors 0–31) / 16 (sectors 32–39) |

**Key memory facts:**
- Each block is **16 bytes**.
- Block 0 of Sector 0 is the **manufacturer block** (UID, read-only).
- The **last block of every sector** is the *sector trailer*, which stores Key A (6 bytes), access-condition bits (3 bytes), and Key B (6 bytes). Do not overwrite it with arbitrary data.
- Authentication uses a 6-byte **Key A** or **Key B** per sector. The factory default is `FF FF FF FF FF FF` — always change this in production.

---

## `MifareClassicManager` — Core API

`MifareClassicManager` wraps Android's `android.nfc.tech.MifareClassic` and exposes a clean, security-conscious API.

### 1. Initialization & Connection

```java
// Obtain the tag from an NFC intent
Tag rawTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
MifareClassic mfc = MifareClassic.get(rawTag);

MifareClassicManager mgr = new MifareClassicManager(mfc);
mgr.connect();     // must be called before any operation
// … operations …
mgr.close();       // always close in a finally block
```

### 2. Authentication

Authenticate a sector before reading or writing any of its blocks.

```java
// Authenticate sector 1 with Key A using the default key
boolean ok = mgr.authenticate(
    1,                          // sector index (0-based)
    MifareClassic.KEY_A,        // key type
    MifareClassic.KEY_DEFAULT   // 6-byte key
);
```

**Security notes:**
- Use unique, diversified keys per sector.
- Store keys in the [Android Keystore](https://developer.android.com/training/articles/keystore).
- Implement retry backoff to resist brute-force attacks.

### 3. Reading a Block

```java
// blockIndex() converts (sector, blockInSector) → absolute block number
int block = mgr.blockIndex(1, 0);   // sector 1, first data block → block 4
byte[] data = mgr.readBlock(block); // returns 16 bytes
```

**Security notes:**
- Encrypt sensitive data before storage so a lost card does not expose plaintext.
- Avoid reading sector trailers unnecessarily (Key A always reads back as zeros).

### 4. Writing a Block

```java
byte[] payload = new byte[MifareClassicManager.BLOCK_SIZE]; // 16 bytes
// … fill payload …
mgr.writeBlock(block, payload);
```

**Security notes:**
- Never write to Block 0 of Sector 0 (manufacturer block).
- Never overwrite a sector trailer unless you explicitly intend to change keys/access conditions.
- Append a checksum to detect silent data corruption.

### 5. Value Block Operations

Value blocks store a 32-bit counter in a tamper-resistant format (suitable for e-wallets, ticket counters).

```java
// Format a block as a value block with initial value 100
mgr.formatValueBlock(block, 100, (byte) block);

// Increment by 10
mgr.incrementValue(block, 10);   // counter → 110

// Decrement by 5
mgr.decrementValue(block, 5);    // counter → 105
```

Both `incrementValue` and `decrementValue` automatically call `transfer()` to commit the result back to the block.

---

## Full Usage Example

```java
MifareClassicManager mgr = new MifareClassicManager(MifareClassic.get(rawTag));
mgr.connect();
try {
    int sector = 1;
    byte[] key  = MifareClassic.KEY_DEFAULT; // replace with your key

    if (mgr.authenticate(sector, MifareClassic.KEY_A, key)) {
        int block = mgr.blockIndex(sector, 0);

        // Read
        byte[] existing = mgr.readBlock(block);

        // Write
        byte[] newData = Arrays.copyOf(existing, MifareClassicManager.BLOCK_SIZE);
        newData[0] = 0x42;
        mgr.writeBlock(block, newData);

        // Value block
        mgr.formatValueBlock(block, 0, (byte) block);
        mgr.incrementValue(block, 50);
        mgr.decrementValue(block, 20);
    }
} finally {
    mgr.close();
}
```

---

## Security Best Practices

| Topic | Recommendation |
|-------|---------------|
| Key management | Store keys in Android Keystore or an HSM; rotate periodically. |
| Default keys | Replace factory default (`FF FF FF FF FF FF`) before deployment. |
| Key diversification | Derive per-card keys from a master secret + UID (e.g., CMAC-AES). |
| Access conditions | Configure sector trailers to enforce least-privilege access. |
| Data confidentiality | Encrypt block payloads (e.g., AES-128-CBC) before writing. |
| Known vulnerabilities | MIFARE Classic Crypto-1 is weak; prefer MIFARE DESFire EV2/EV3 or NTAG 424 DNA for high-security use cases. |
| Auditing | Use tools such as Proxmark3 to verify access-condition configurations. |

---

## Android Permissions

The application declares the following NFC permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.NFC"/>
<uses-feature android:name="android.hardware.nfc" android:required="false"/>
```

---

## Printing Features

The app also demonstrates the [PrintHand](https://printhand.com) printing SDK via three fragments:

| Tab | Class | Description |
|-----|-------|-------------|
| Share Intent | `ShareIntentFragment` | Shares files/images via Android's `ACTION_SEND` intent |
| Intent API | `IntentApiFragment` | Discovers printers and controls print parameters via the Intent API |
| Printing SDK | `PrintServiceFragment` | Background printing service with direct binary stream control |

---

## Anti-Spoofing Security

The `AntiSpoofingValidator` class provides runtime validation against:

- **DNS spoofing** — domain format and pattern checks
- **WDM spoofing** — driver/library name validation
- **Domain spoofing** — homograph and typosquatting detection
- **System hijacking** — root, debug-mode, and emulator detection

Validation runs automatically on app startup via `MainActivity.initializeAntiSpoofingValidation()`.

---

## License

See individual source files for license information.
