package com.dynamixsoftware.printingsample;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;

/**
 * Helper class for MIFARE Classic NFC tag operations.
 *
 * Supports connect, authenticate, read, write, and value block operations
 * (increment, decrement, restore, transfer) on MIFARE Classic 1K/4K tags.
 *
 * All operations follow ISO/IEC 14443 Type A and require prior authentication
 * of the target sector using Key A or Key B.
 */
public class MifareClassicHelper {

    private static final String TAG = "MifareClassicHelper";

    /** Default MIFARE Classic key (all bytes 0xFF). Should be changed in production. */
    public static final byte[] DEFAULT_KEY = MifareClassic.KEY_DEFAULT;

    private final MifareClassic mifareClassic;

    /**
     * Constructs a helper wrapping the given {@link MifareClassic} tag technology.
     *
     * @param mifareClassic the tag technology object obtained from an NFC intent
     */
    public MifareClassicHelper(MifareClassic mifareClassic) {
        if (mifareClassic == null) {
            throw new IllegalArgumentException("MifareClassic tag must not be null");
        }
        this.mifareClassic = mifareClassic;
    }

    /**
     * Connects to the MIFARE Classic tag.
     *
     * Must be called before any read/write/authenticate operations.
     *
     * @throws IOException if the connection fails
     */
    public void connect() throws IOException {
        if (!mifareClassic.isConnected()) {
            mifareClassic.connect();
            Log.d(TAG, "Connected to MIFARE Classic tag");
        }
    }

    /**
     * Closes the connection to the tag.
     */
    public void close() {
        try {
            if (mifareClassic.isConnected()) {
                mifareClassic.close();
                Log.d(TAG, "Disconnected from MIFARE Classic tag");
            }
        } catch (IOException e) {
            Log.w(TAG, "Error closing tag connection", e);
        }
    }

    /**
     * Authenticates a sector using Key A.
     *
     * @param sectorIndex zero-based sector index
     * @param key         6-byte Key A for the sector
     * @return {@code true} if authentication succeeded, {@code false} otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean authenticateSectorWithKeyA(int sectorIndex, byte[] key) throws IOException {
        validateSectorIndex(sectorIndex);
        validateKey(key);
        boolean result = mifareClassic.authenticateSectorWithKeyA(sectorIndex, key);
        Log.d(TAG, "Authenticate sector " + sectorIndex + " with Key A: " + result);
        return result;
    }

    /**
     * Authenticates a sector using Key B.
     *
     * @param sectorIndex zero-based sector index
     * @param key         6-byte Key B for the sector
     * @return {@code true} if authentication succeeded, {@code false} otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException {
        validateSectorIndex(sectorIndex);
        validateKey(key);
        boolean result = mifareClassic.authenticateSectorWithKeyB(sectorIndex, key);
        Log.d(TAG, "Authenticate sector " + sectorIndex + " with Key B: " + result);
        return result;
    }

    /**
     * Reads a 16-byte data block.
     *
     * The sector containing the block must be authenticated before calling this method.
     *
     * @param blockIndex absolute block index (0-based across the entire tag)
     * @return 16-byte array containing the block data
     * @throws IOException if the read fails or the tag is not authenticated
     */
    public byte[] readBlock(int blockIndex) throws IOException {
        validateBlockIndex(blockIndex);
        byte[] data = mifareClassic.readBlock(blockIndex);
        Log.d(TAG, "Read block " + blockIndex + ": " + bytesToHex(data));
        return data;
    }

    /**
     * Writes 16 bytes to a data block.
     *
     * The sector containing the block must be authenticated before calling this method.
     * Writing to the manufacturer block (block 0) or a sector trailer should be done
     * with extreme caution to avoid permanent tag damage.
     *
     * @param blockIndex absolute block index (0-based across the entire tag)
     * @param data       exactly 16 bytes to write
     * @throws IOException              if the write fails or the tag is not authenticated
     * @throws IllegalArgumentException if {@code data} is not exactly 16 bytes
     */
    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        validateBlockIndex(blockIndex);
        if (data == null || data.length != MifareClassic.BLOCK_SIZE) {
            throw new IllegalArgumentException(
                    "Write data must be exactly " + MifareClassic.BLOCK_SIZE + " bytes");
        }
        mifareClassic.writeBlock(blockIndex, data);
        Log.d(TAG, "Wrote block " + blockIndex);
    }

    /**
     * Increments a value block by the given amount and stores the result in the block.
     *
     * The block must be formatted as a MIFARE value block and the sector must be
     * authenticated with a key that grants write access.
     *
     * @param blockIndex absolute block index of the value block
     * @param value      positive amount to add
     * @throws IOException if the operation fails
     */
    public void increment(int blockIndex, int value) throws IOException {
        validateBlockIndex(blockIndex);
        mifareClassic.increment(blockIndex, value);
        mifareClassic.transfer(blockIndex);
        Log.d(TAG, "Incremented block " + blockIndex + " by " + value);
    }

    /**
     * Decrements a value block by the given amount and stores the result in the block.
     *
     * The block must be formatted as a MIFARE value block and the sector must be
     * authenticated with a key that grants write access.
     *
     * @param blockIndex absolute block index of the value block
     * @param value      positive amount to subtract
     * @throws IOException if the operation fails
     */
    public void decrement(int blockIndex, int value) throws IOException {
        validateBlockIndex(blockIndex);
        mifareClassic.decrement(blockIndex, value);
        mifareClassic.transfer(blockIndex);
        Log.d(TAG, "Decremented block " + blockIndex + " by " + value);
    }

    /**
     * Restores (copies) the value of a value block into the internal transfer buffer
     * and then writes it back to the same block.
     *
     * @param blockIndex absolute block index of the value block
     * @throws IOException if the operation fails
     */
    public void restore(int blockIndex) throws IOException {
        validateBlockIndex(blockIndex);
        mifareClassic.restore(blockIndex);
        mifareClassic.transfer(blockIndex);
        Log.d(TAG, "Restored block " + blockIndex);
    }

    /**
     * Returns the number of sectors on this tag.
     *
     * @return sector count
     */
    public int getSectorCount() {
        return mifareClassic.getSectorCount();
    }

    /**
     * Returns the absolute block index of the first block in the given sector.
     *
     * @param sectorIndex zero-based sector index
     * @return absolute block index
     */
    public int sectorToBlock(int sectorIndex) {
        validateSectorIndex(sectorIndex);
        return mifareClassic.sectorToBlock(sectorIndex);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateSectorIndex(int sectorIndex) {
        int sectorCount = mifareClassic.getSectorCount();
        if (sectorIndex < 0 || sectorIndex >= sectorCount) {
            throw new IllegalArgumentException(
                    "Sector index " + sectorIndex + " out of range [0, " + (sectorCount - 1) + "]");
        }
    }

    private void validateBlockIndex(int blockIndex) {
        int blockCount = mifareClassic.getBlockCount();
        if (blockIndex < 0 || blockIndex >= blockCount) {
            throw new IllegalArgumentException(
                    "Block index " + blockIndex + " out of range [0, " + (blockCount - 1) + "]");
        }
    }

    private static void validateKey(byte[] key) {
        if (key == null || key.length != 6) {
            throw new IllegalArgumentException("Key must be exactly 6 bytes");
        }
    }

    /**
     * Converts a byte array to an uppercase hex string.
     *
     * @param bytes byte array to convert
     * @return hex string, or "(null)" if the array is null
     */
    static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "(null)";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
