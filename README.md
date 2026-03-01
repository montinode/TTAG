# PrintHand Printing Sample

Android sample application demonstrating how to integrate the [PrintHand](https://printhand.com) mobile printing SDK.

## Features

- **Share Intent** – Print images, web pages, and arbitrary files via Android's standard share mechanism.
- **Intent API** – Discover printers, obtain printer information, and submit print jobs directly via the PrintHand Intent API.
- **Printing SDK** – Full SDK integration using the PrintHand background service for silent, programmatic printing.
- **MIFARE Classic NFC** – Read and write MIFARE Classic NFC tags (access control data, counters, secure storage).

## MIFARE Classic NFC Support

The app includes `MifareClassicHelper`, a utility class for all core MIFARE Classic tag operations.

### Supported Operations

| Operation | Method | Description |
|-----------|--------|-------------|
| Connect | `connect(Tag)` | Open a connection to a MIFARE Classic tag |
| Authenticate | `authenticateSector(mfc, block, keyType, key)` | Authenticate a sector with Key A or Key B |
| Read | `readBlock(mfc, block)` | Read 16 bytes from a block |
| Write | `writeBlock(mfc, block, data)` | Write 16 bytes to a block |
| Increment | `increment(mfc, block, amount)` | Add to a value block |
| Decrement | `decrement(mfc, block, amount)` | Subtract from a value block |
| Transfer | `transfer(mfc, block)` | Commit a staged increment/decrement |
| Restore | `restore(mfc, block)` | Load a value block into the transfer buffer |
| Close | `closeQuietly(mfc)` | Close the tag connection |

### Quick Start

```java
// Must run on a background thread – not the UI thread.
MifareClassic mfc = MifareClassicHelper.connect(tag);
if (mfc == null) return; // not a MIFARE Classic tag

try {
    byte[] key = MifareClassicHelper.DEFAULT_KEY; // replace with your per-sector key
    int block = 4; // first data block of sector 1

    // Authenticate
    boolean ok = MifareClassicHelper.authenticateSector(
            mfc, block, MifareClassicHelper.KEY_TYPE_A, key);
    if (!ok) return;

    // Read
    byte[] data = MifareClassicHelper.readBlock(mfc, block);

    // Write
    byte[] payload = new byte[MifareClassicHelper.BLOCK_SIZE]; // 16 bytes
    System.arraycopy(data, 0, payload, 0, payload.length);     // modify as needed
    MifareClassicHelper.writeBlock(mfc, block, payload);

    // Value-block increment and commit
    MifareClassicHelper.increment(mfc, block, 1);
    MifareClassicHelper.transfer(mfc, block);
} catch (IOException e) {
    Log.e(TAG, "NFC I/O error", e);
} finally {
    MifareClassicHelper.closeQuietly(mfc);
}
```

### Security Notes

- Replace the default key (`FF FF FF FF FF FF`) with unique per-sector keys in production.
- Derive sector keys from a master key and the tag UID (key diversification) so that compromising one tag does not expose others.
- All NFC I/O must run on a background thread.
- MIFARE Classic uses the proprietary Crypto-1 cipher, which has known weaknesses. Migrate to MIFARE DESFire EV2/EV3 or NTAG 424 DNA for high-security applications.

## Integration

Add the PrintHand SDK to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.dynamixsoftware.intentapi:intentAPI:12'
    implementation 'com.dynamixsoftware.printingsdk:printingSDK:12'
}
```

## License

See the upstream [DynamixSoftware/PrintingSample](https://github.com/DynamixSoftware/PrintingSample) repository for license information.
