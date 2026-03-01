package com.dynamixsoftware.printingsample;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * MifareClassicHelper - Utility class for MIFARE Classic NFC tag operations.
 *
 * Supports the full lifecycle of MIFARE Classic tag interaction:
 *   1. Connection / initialization
 *   2. Sector authentication (Key A or Key B)
 *   3. Block read (16 bytes per block)
 *   4. Block write (16 bytes per block)
 *   5. Value-block operations: increment, decrement, restore, transfer
 *
 * MIFARE Classic memory layout:
 *   - 1K tag : 16 sectors × 4 blocks (blocks 0–63)
 *   - 2K tag : 32 sectors × 4 blocks (blocks 0–127)
 *   - 4K tag : 32 sectors × 4 blocks + 8 sectors × 16 blocks (blocks 0–255)
 *   Each block holds 16 bytes.  The last block in every sector is the
 *   Sector Trailer (contains Key A, access bits, Key B) and must not be
 *   treated as a data block.
 *   Block 0 of Sector 0 is the manufacturer block (read-only, contains UID).
 *
 * Security notes:
 *   - Replace the default key (0xFF×6) with per-sector diversified keys in
 *     production. Key diversification can derive per-tag keys from a master
 *     key and the tag UID.
 *   - Always close the tag connection in a finally block to prevent resource
 *     leaks and leaving the RF field active unnecessarily.
 *   - All I/O operations must run on a background thread; never call these
 *     methods from the UI thread.
 *   - MIFARE Classic uses the proprietary Crypto-1 cipher. Migrate to
 *     MIFARE DESFire EV2/EV3 or NTAG 424 DNA for cryptographically strong
 *     applications.
 */
public final class MifareClassicHelper {

    private static final String TAG = "MifareClassicHelper";

    /** Default 6-byte key (0xFF × 6). Change this for production use. */
    public static final byte[] DEFAULT_KEY = MifareClassic.KEY_DEFAULT;

    /** Key type: Key A (used for read access by default). */
    public static final int KEY_TYPE_A = 0;

    /** Key type: Key B (used for write access by default). */
    public static final int KEY_TYPE_B = 1;

    /** Number of bytes in a single MIFARE Classic block. */
    public static final int BLOCK_SIZE = MifareClassic.BLOCK_SIZE;

    private MifareClassicHelper() {
        // Utility class — do not instantiate.
    }

    // -------------------------------------------------------------------------
    // 1. Connection / Initialization
    // -------------------------------------------------------------------------

    /**
     * Opens a connection to a MIFARE Classic tag.
     *
     * <p>The caller is responsible for closing the returned {@link MifareClassic}
     * instance (via {@link MifareClassic#close()}) when finished, typically in a
     * {@code finally} block.
     *
     * @param tag The {@link Tag} discovered by the NFC dispatcher.
     * @return A connected {@link MifareClassic} instance, or {@code null} if the
     *         tag is not a MIFARE Classic tag or the connection fails.
     */
    public static MifareClassic connect(Tag tag) {
        if (tag == null) {
            Log.e(TAG, "connect: tag is null");
            return null;
        }
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            Log.e(TAG, "connect: tag is not a MIFARE Classic tag");
            return null;
        }
        try {
            mfc.connect();
            Log.d(TAG, "connect: connected – type=" + mfc.getType()
                    + " size=" + mfc.getSize() + "B"
                    + " sectors=" + mfc.getSectorCount()
                    + " blocks=" + mfc.getBlockCount());
            return mfc;
        } catch (IOException e) {
            Log.e(TAG, "connect: failed to connect to tag", e);
            closeQuietly(mfc);
            return null;
        }
    }

    /**
     * Closes the tag connection, suppressing any {@link IOException}.
     *
     * @param mfc The {@link MifareClassic} instance to close; may be {@code null}.
     */
    public static void closeQuietly(MifareClassic mfc) {
        if (mfc != null) {
            try {
                mfc.close();
            } catch (IOException e) {
                Log.w(TAG, "closeQuietly: error closing tag", e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. Authentication
    // -------------------------------------------------------------------------

    /**
     * Authenticates the sector that contains {@code blockIndex}.
     *
     * <p>Must be called before any read or write operation within the sector.
     * Authentication is valid until the tag is powered off or a new authentication
     * is performed on a different sector.
     *
     * @param mfc        A connected {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based) within the tag.
     * @param keyType    {@link #KEY_TYPE_A} or {@link #KEY_TYPE_B}.
     * @param key        6-byte authentication key.
     * @return {@code true} if authentication succeeded, {@code false} otherwise.
     * @throws IOException              If an I/O error occurs during communication.
     * @throws IllegalArgumentException If {@code key} is not exactly 6 bytes.
     */
    public static boolean authenticateSector(MifareClassic mfc, int blockIndex,
            int keyType, byte[] key) throws IOException {
        validateKey(key);
        int sector = mfc.blockToSector(blockIndex);
        boolean authenticated;
        if (keyType == KEY_TYPE_B) {
            authenticated = mfc.authenticateSectorWithKeyB(sector, key);
        } else {
            authenticated = mfc.authenticateSectorWithKeyA(sector, key);
        }
        if (authenticated) {
            Log.d(TAG, "authenticateSector: sector " + sector + " authenticated with Key "
                    + (keyType == KEY_TYPE_B ? "B" : "A"));
        } else {
            Log.w(TAG, "authenticateSector: authentication FAILED for sector " + sector);
        }
        return authenticated;
    }

    // -------------------------------------------------------------------------
    // 3. Reading Data
    // -------------------------------------------------------------------------

    /**
     * Reads a single block (16 bytes) from the tag.
     *
     * <p>The sector containing {@code blockIndex} must be authenticated before
     * calling this method.  Block 0 of Sector 0 is the manufacturer block and
     * should only be read, never written.  The last block in each sector is the
     * Sector Trailer; reading it returns Key A as all-zeros (keys are not
     * readable), the access bits, and Key B (if the current key has read access).
     *
     * @param mfc        A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based) to read.
     * @return A 16-byte array containing the block data.
     * @throws IOException If an I/O error occurs or the sector is not authenticated.
     */
    public static byte[] readBlock(MifareClassic mfc, int blockIndex) throws IOException {
        byte[] data = mfc.readBlock(blockIndex);
        Log.d(TAG, "readBlock: block " + blockIndex + " = " + bytesToHex(data));
        return data;
    }

    // -------------------------------------------------------------------------
    // 4. Writing Data
    // -------------------------------------------------------------------------

    /**
     * Writes 16 bytes to a single block on the tag.
     *
     * <p>The sector containing {@code blockIndex} must be authenticated with a
     * key that has write permission before calling this method.  Never write to
     * the manufacturer block (block 0 of sector 0) or overwrite a Sector Trailer
     * with incorrect access bits, as this may permanently lock the tag.
     *
     * @param mfc        A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based) to write.
     * @param data       Exactly 16 bytes to write.
     * @throws IOException              If an I/O error occurs or the sector is not authenticated.
     * @throws IllegalArgumentException If {@code data} is not exactly 16 bytes.
     */
    public static void writeBlock(MifareClassic mfc, int blockIndex, byte[] data)
            throws IOException {
        if (data == null || data.length != BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "data must be exactly " + BLOCK_SIZE + " bytes, got "
                            + (data == null ? "null" : data.length));
        }
        mfc.writeBlock(blockIndex, data);
        Log.d(TAG, "writeBlock: block " + blockIndex + " written = " + bytesToHex(data));
    }

    // -------------------------------------------------------------------------
    // 5. Value Block Operations
    // -------------------------------------------------------------------------

    /**
     * Increments the value stored in a value-formatted block.
     *
     * <p>A value block stores a signed 32-bit integer in a specific 16-byte
     * format: [value(4)] [~value(4)] [value(4)] [address(1)] [~address(1)]
     * [address(1)] [~address(1)].  The increment is staged in the internal
     * transfer buffer; call {@link #transfer} to commit the result back to the
     * block (or to a different block).
     *
     * @param mfc        A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex Absolute block index of the value block.
     * @param amount     Non-negative integer to add to the current value.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code amount} is negative.
     */
    public static void increment(MifareClassic mfc, int blockIndex, int amount)
            throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException("increment amount must be non-negative");
        }
        mfc.increment(blockIndex, amount);
        Log.d(TAG, "increment: block " + blockIndex + " += " + amount);
    }

    /**
     * Decrements the value stored in a value-formatted block.
     *
     * <p>The result is staged in the internal transfer buffer; call
     * {@link #transfer} to commit it.
     *
     * @param mfc        A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex Absolute block index of the value block.
     * @param amount     Non-negative integer to subtract from the current value.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code amount} is negative.
     */
    public static void decrement(MifareClassic mfc, int blockIndex, int amount)
            throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException("decrement amount must be non-negative");
        }
        mfc.decrement(blockIndex, amount);
        Log.d(TAG, "decrement: block " + blockIndex + " -= " + amount);
    }

    /**
     * Copies the internal transfer buffer to a value block, committing the
     * result of a previous {@link #increment}, {@link #decrement}, or
     * {@link #restore} operation.
     *
     * @param mfc         A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex  Absolute block index to write the buffered value into.
     * @throws IOException If an I/O error occurs.
     */
    public static void transfer(MifareClassic mfc, int blockIndex) throws IOException {
        mfc.transfer(blockIndex);
        Log.d(TAG, "transfer: value committed to block " + blockIndex);
    }

    /**
     * Copies the value of a value block into the internal transfer buffer
     * without modifying it, allowing a subsequent {@link #transfer} to copy
     * that value to a different block.
     *
     * @param mfc        A connected and authenticated {@link MifareClassic} instance.
     * @param blockIndex Absolute block index of the source value block.
     * @throws IOException If an I/O error occurs.
     */
    public static void restore(MifareClassic mfc, int blockIndex) throws IOException {
        mfc.restore(blockIndex);
        Log.d(TAG, "restore: value from block " + blockIndex + " loaded into transfer buffer");
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the sector index that contains the given absolute block index.
     *
     * @param mfc        A connected {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based).
     * @return The sector index (0-based) that contains {@code blockIndex}.
     */
    public static int getSectorForBlock(MifareClassic mfc, int blockIndex) {
        return mfc.blockToSector(blockIndex);
    }

    /**
     * Returns the absolute index of the Sector Trailer block for the sector
     * that contains {@code blockIndex}.
     *
     * @param mfc        A connected {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based).
     * @return Absolute block index of the Sector Trailer.
     */
    public static int getSectorTrailer(MifareClassic mfc, int blockIndex) {
        int sector = mfc.blockToSector(blockIndex);
        return mfc.sectorToBlock(sector) + mfc.getBlockCountInSector(sector) - 1;
    }

    /**
     * Returns whether {@code blockIndex} is the Sector Trailer of its sector.
     *
     * @param mfc        A connected {@link MifareClassic} instance.
     * @param blockIndex Absolute block index (0-based).
     * @return {@code true} if the block is a Sector Trailer.
     */
    public static boolean isSectorTrailer(MifareClassic mfc, int blockIndex) {
        return blockIndex == getSectorTrailer(mfc, blockIndex);
    }

    // -------------------------------------------------------------------------
    // Private utilities
    // -------------------------------------------------------------------------

    private static void validateKey(byte[] key) {
        if (key == null || key.length != 6) {
            throw new IllegalArgumentException(
                    "MIFARE Classic key must be exactly 6 bytes, got "
                            + (key == null ? "null" : key.length));
        }
    }

    /**
     * Converts a byte array to an uppercase hexadecimal string for logging.
     */
    static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
