package com.dynamixsoftware.printingsample;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * Manager for MIFARE Classic NFC tag operations.
 *
 * <p>Supports MIFARE Classic 1K (16 sectors × 4 blocks), 2K (32 sectors × 4 blocks),
 * and 4K (32 sectors × 4 blocks + 8 sectors × 16 blocks) variants.
 *
 * <p>Each sector's last block (the sector trailer) holds Key A, access bits,
 * and Key B, and must never be overwritten with arbitrary data.
 *
 * <p>Usage:
 * <pre>
 *   MifareClassicManager mgr = new MifareClassicManager(mifareClassicTag);
 *   mgr.connect();
 *   try {
 *       if (mgr.authenticate(1, MifareClassic.KEY_A, MifareClassic.KEY_DEFAULT)) {
 *           byte[] data = mgr.readBlock(4);           // block 4 = sector 1, block 0
 *           mgr.writeBlock(4, myData);
 *       }
 *   } finally {
 *       mgr.close();
 *   }
 * </pre>
 */
public class MifareClassicManager {

    private static final String TAG = "MifareClassicManager";

    /** Number of bytes in a single MIFARE Classic data block. */
    public static final int BLOCK_SIZE = MifareClassic.BLOCK_SIZE; // 16

    /** Default factory key (all 0xFF bytes). Change for production deployments. */
    public static final byte[] KEY_DEFAULT = MifareClassic.KEY_DEFAULT;

    private final MifareClassic mTag;

    /**
     * Creates a new manager wrapping the given {@link MifareClassic} tag handle.
     *
     * @param tag a valid {@link MifareClassic} instance obtained from an NFC intent
     */
    public MifareClassicManager(MifareClassic tag) {
        if (tag == null) {
            throw new IllegalArgumentException("MifareClassic tag must not be null");
        }
        this.mTag = tag;
    }

    // -------------------------------------------------------------------------
    // 1. Initialization / Connection
    // -------------------------------------------------------------------------

    /**
     * Opens a connection to the tag.
     *
     * <p>Must be called before any read/write/authenticate operation.
     * Always call {@link #close()} in a {@code finally} block.
     *
     * @throws IOException if the connection cannot be established
     */
    public void connect() throws IOException {
        if (!mTag.isConnected()) {
            mTag.connect();
            Log.i(TAG, "Connected to MIFARE Classic tag (type=" + mTag.getType()
                    + ", size=" + mTag.getSize() + " bytes"
                    + ", sectors=" + mTag.getSectorCount() + ")");
        }
    }

    /**
     * Closes the connection to the tag.
     *
     * <p>Safe to call even if the connection is already closed.
     */
    public void close() {
        try {
            if (mTag.isConnected()) {
                mTag.close();
                Log.i(TAG, "Disconnected from MIFARE Classic tag");
            }
        } catch (IOException e) {
            Log.w(TAG, "Error closing tag connection", e);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Authentication
    // -------------------------------------------------------------------------

    /**
     * Authenticates a sector using the specified key type and key bytes.
     *
     * <p>Authentication is required before reading or writing any block within
     * the sector. The tag uses a challenge-response mechanism; the underlying
     * Android API handles the crypto exchange transparently.
     *
     * <p>Security notes:
     * <ul>
     *   <li>Use unique, diversified keys per sector; never leave the default key
     *       ({@link MifareClassic#KEY_DEFAULT}) in production.</li>
     *   <li>Store keys in a secure location (e.g., Android Keystore).</li>
     *   <li>Implement retry backoff to resist brute-force attempts.</li>
     * </ul>
     *
     * @param sectorIndex zero-based sector index
     * @param keyType     {@link MifareClassic#KEY_A} or {@link MifareClassic#KEY_B}
     * @param key         6-byte authentication key
     * @return {@code true} if authentication succeeded
     * @throws IOException              if an I/O error occurs during authentication
     * @throws IllegalArgumentException if the key is not exactly 6 bytes
     */
    public boolean authenticate(int sectorIndex, int keyType, byte[] key) throws IOException {
        validateKey(key);
        boolean success;
        if (keyType == MifareClassic.KEY_A) {
            success = mTag.authenticateSectorWithKeyA(sectorIndex, key);
        } else if (keyType == MifareClassic.KEY_B) {
            success = mTag.authenticateSectorWithKeyB(sectorIndex, key);
        } else {
            throw new IllegalArgumentException("keyType must be MifareClassic.KEY_A or KEY_B");
        }
        if (success) {
            Log.d(TAG, "Authentication succeeded for sector " + sectorIndex);
        } else {
            Log.w(TAG, "Authentication failed for sector " + sectorIndex);
        }
        return success;
    }

    // -------------------------------------------------------------------------
    // 3. Reading Data
    // -------------------------------------------------------------------------

    /**
     * Reads 16 bytes from the specified block.
     *
     * <p>The sector containing {@code blockIndex} must be authenticated before
     * calling this method.
     *
     * <p>Block 0 of Sector 0 is the manufacturer block and is read-only.
     * The sector trailer (last block of each sector) contains keys and access
     * bits; reading it returns Key A as zeros regardless of the actual value.
     *
     * @param blockIndex absolute block index (0–63 for 1K, 0–127 for 4K)
     * @return 16-byte array containing the block contents
     * @throws IOException if the read fails or the block is not authenticated
     */
    public byte[] readBlock(int blockIndex) throws IOException {
        byte[] data = mTag.readBlock(blockIndex);
        Log.d(TAG, "Read block " + blockIndex + ": " + bytesToHex(data));
        return data;
    }

    // -------------------------------------------------------------------------
    // 4. Writing Data
    // -------------------------------------------------------------------------

    /**
     * Writes exactly 16 bytes to the specified block.
     *
     * <p>The sector containing {@code blockIndex} must be authenticated with a
     * key that has write permission before calling this method.
     *
     * <p>Security notes:
     * <ul>
     *   <li>Do <strong>not</strong> write to Block 0 of Sector 0 (manufacturer block).</li>
     *   <li>Do <strong>not</strong> overwrite sector trailers unless you intend to
     *       change keys or access conditions, and you know exactly what you are doing.</li>
     *   <li>Consider encrypting sensitive payload before writing (e.g., AES-128).</li>
     *   <li>Append a CRC or checksum to detect unintended data corruption.</li>
     * </ul>
     *
     * @param blockIndex absolute block index
     * @param data       exactly 16 bytes to write
     * @throws IOException              if the write fails
     * @throws IllegalArgumentException if {@code data} is not exactly 16 bytes
     */
    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        if (data == null || data.length != BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "data must be exactly " + BLOCK_SIZE + " bytes, got "
                            + (data == null ? "null" : data.length));
        }
        mTag.writeBlock(blockIndex, data);
        Log.d(TAG, "Wrote block " + blockIndex + ": " + bytesToHex(data));
    }

    // -------------------------------------------------------------------------
    // 5. Value Block Operations
    // -------------------------------------------------------------------------

    /**
     * Increments the value stored in a value block by {@code amount} and
     * transfers the result back to the same block.
     *
     * <p>The block must be formatted as a MIFARE Classic value block before
     * use. Value blocks store a 32-bit signed integer in a tamper-resistant
     * format (value + inverted value + value, plus an address byte).
     *
     * <p>Typical use: e-wallet credit, decrementing ticket counters.
     *
     * @param blockIndex absolute block index of the value block
     * @param amount     positive increment value
     * @throws IOException              if the operation fails
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public void incrementValue(int blockIndex, int amount) throws IOException {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got " + amount);
        }
        mTag.increment(blockIndex, amount);
        mTag.transfer(blockIndex);
        Log.d(TAG, "Incremented block " + blockIndex + " by " + amount);
    }

    /**
     * Decrements the value stored in a value block by {@code amount} and
     * transfers the result back to the same block.
     *
     * @param blockIndex absolute block index of the value block
     * @param amount     positive decrement value
     * @throws IOException              if the operation fails
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public void decrementValue(int blockIndex, int amount) throws IOException {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got " + amount);
        }
        mTag.decrement(blockIndex, amount);
        mTag.transfer(blockIndex);
        Log.d(TAG, "Decremented block " + blockIndex + " by " + amount);
    }

    /**
     * Formats the given block as a MIFARE Classic value block with the specified
     * {@code initialValue} by writing the canonical value-block byte layout.
     *
     * <p>The value-block format is:
     * <ul>
     *   <li>Bytes 0–3: value (little-endian 32-bit signed int)</li>
     *   <li>Bytes 4–7: bitwise NOT of value</li>
     *   <li>Bytes 8–11: value (repeated)</li>
     *   <li>Byte 12: address byte</li>
     *   <li>Byte 13: bitwise NOT of address</li>
     *   <li>Byte 14: address (repeated)</li>
     *   <li>Byte 15: bitwise NOT of address (repeated)</li>
     * </ul>
     *
     * @param blockIndex absolute block index to format
     * @param initialValue initial integer value to store
     * @param address    1-byte block address (typically equals {@code blockIndex})
     * @throws IOException if the write fails
     */
    public void formatValueBlock(int blockIndex, int initialValue, byte address)
            throws IOException {
        byte[] data = new byte[BLOCK_SIZE];
        // Encode value in little-endian order
        encodeInt(data, 0, initialValue);
        // Encode bitwise NOT of value
        encodeInt(data, 4, ~initialValue);
        // Repeat value
        encodeInt(data, 8, initialValue);
        // Address bytes
        data[12] = address;
        data[13] = (byte) ~address;
        data[14] = address;
        data[15] = (byte) ~address;
        writeBlock(blockIndex, data);
        Log.d(TAG, "Formatted block " + blockIndex + " as value block, initial=" + initialValue);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the absolute block index for a given sector and block-within-sector.
     *
     * @param sectorIndex       zero-based sector index
     * @param blockInSector     0–3 for sectors 0–31; 0–15 for sectors 32–39 (4K)
     * @return absolute block index
     */
    public int blockIndex(int sectorIndex, int blockInSector) {
        return mTag.sectorToBlock(sectorIndex) + blockInSector;
    }

    /**
     * Returns the number of sectors on this tag.
     */
    public int getSectorCount() {
        return mTag.getSectorCount();
    }

    /**
     * Returns the total storage capacity in bytes.
     */
    public int getSize() {
        return mTag.getSize();
    }

    private static void validateKey(byte[] key) {
        if (key == null || key.length != 6) {
            throw new IllegalArgumentException(
                    "MIFARE Classic key must be exactly 6 bytes, got "
                            + (key == null ? "null" : key.length));
        }
    }

    private static void encodeInt(byte[] dest, int offset, int value) {
        dest[offset]     = (byte)  value;
        dest[offset + 1] = (byte) (value >> 8);
        dest[offset + 2] = (byte) (value >> 16);
        dest[offset + 3] = (byte) (value >> 24);
    }

    /** Returns a space-separated hex string representation of {@code bytes}. */
    static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
