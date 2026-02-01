package com.dynamixsoftware.printingsample.transmutation;

import java.util.HashMap;
import java.util.Map;

/**
 * MontiTransmuter - ABC Encoding Algorithm
 * Unicode-based character encoding system for generating Monti-Network-State Assets
 * 
 * Features:
 * - Squared Unicode blocks for character mapping (uppercase → black squared, lowercase → white squared)
 * - Keyword compression map (OK, NEW, FREE, ID, etc. → emoji representations)
 * - Network state delimiters (⚿, ⌬, ⫽, ⛝, ⁂)
 * - Header generation for network identification
 */
public class MontiTransmuter {
    
    // Network state delimiters
    public static final String DELIMITER_START = "⚿";
    public static final String DELIMITER_SECTION = "⌬";
    public static final String DELIMITER_FIELD = "⫽";
    public static final String DELIMITER_RECORD = "⛝";
    public static final String DELIMITER_END = "⁂";
    
    // Keyword compression map
    private static final Map<String, String> KEYWORD_MAP = new HashMap<>();
    
    static {
        KEYWORD_MAP.put("OK", "✓");
        KEYWORD_MAP.put("NEW", "✦");
        KEYWORD_MAP.put("FREE", "◆");
        KEYWORD_MAP.put("ID", "◈");
        KEYWORD_MAP.put("ERROR", "✗");
        KEYWORD_MAP.put("WARNING", "⚠");
        KEYWORD_MAP.put("INFO", "ℹ");
        KEYWORD_MAP.put("DEBUG", "◉");
        KEYWORD_MAP.put("START", "►");
        KEYWORD_MAP.put("STOP", "■");
        KEYWORD_MAP.put("PAUSE", "❚❚");
        KEYWORD_MAP.put("CONTINUE", "▶");
        KEYWORD_MAP.put("SUCCESS", "✔");
        KEYWORD_MAP.put("FAIL", "✘");
        KEYWORD_MAP.put("ACTIVE", "◉");
        KEYWORD_MAP.put("INACTIVE", "○");
    }
    
    /**
     * Converts a character to its squared Unicode representation
     * Uppercase → black squared, lowercase → white squared
     */
    private static String charToSquared(char c) {
        if (c >= 'A' && c <= 'Z') {
            // Black squared letters (🅰-🆉)
            return String.valueOf((char) (0x1F170 + (c - 'A')));
        } else if (c >= 'a' && c <= 'z') {
            // White squared letters - using circled Latin
            // Note: Full squared white letters are limited in Unicode, using circled as alternative
            return String.valueOf((char) (0x24B6 + (c - 'a')));
        } else if (c >= '0' && c <= '9') {
            // Circled digits
            return String.valueOf((char) (0x2460 + (c - '0') - 1));
        } else if (c == ' ') {
            return " ";
        } else {
            return String.valueOf(c);
        }
    }
    
    /**
     * Encodes text to Monti-Network-State format
     */
    public static String encode(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // First, apply keyword compression
        String compressed = text;
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            compressed = compressed.replace(entry.getKey(), entry.getValue());
        }
        
        // Then apply character encoding
        StringBuilder encoded = new StringBuilder();
        for (char c : compressed.toCharArray()) {
            encoded.append(charToSquared(c));
        }
        
        return encoded.toString();
    }
    
    /**
     * Generates a Monti-Network-State header
     */
    public static String generateHeader(String networkId, String timestamp) {
        StringBuilder header = new StringBuilder();
        header.append(DELIMITER_START);
        header.append(" MONTI-NETWORK-STATE ");
        header.append(DELIMITER_SECTION);
        header.append(" ID: ").append(encode(networkId));
        header.append(DELIMITER_FIELD);
        header.append(" TIME: ").append(timestamp);
        header.append(DELIMITER_SECTION);
        return header.toString();
    }
    
    /**
     * Wraps content in network state format
     */
    public static String wrapContent(String content) {
        StringBuilder wrapped = new StringBuilder();
        wrapped.append(DELIMITER_START);
        wrapped.append(encode(content));
        wrapped.append(DELIMITER_END);
        return wrapped.toString();
    }
    
    /**
     * Creates a delimited record
     */
    public static String createRecord(String... fields) {
        StringBuilder record = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            record.append(encode(fields[i]));
            if (i < fields.length - 1) {
                record.append(DELIMITER_FIELD);
            }
        }
        record.append(DELIMITER_RECORD);
        return record.toString();
    }
    
    /**
     * Compresses keywords in text
     */
    public static String compressKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String compressed = text;
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            compressed = compressed.replace(entry.getKey(), entry.getValue());
        }
        return compressed;
    }
    
    /**
     * Gets the emoji representation of a keyword
     */
    public static String getKeywordEmoji(String keyword) {
        return KEYWORD_MAP.getOrDefault(keyword, keyword);
    }
    
    /**
     * Formats a complete Monti-Network-State message
     */
    public static String formatMessage(String networkId, String messageType, String content) {
        StringBuilder message = new StringBuilder();
        message.append(generateHeader(networkId, String.valueOf(System.currentTimeMillis())));
        message.append(DELIMITER_SECTION);
        message.append(" TYPE: ").append(encode(messageType));
        message.append(DELIMITER_FIELD);
        message.append(" CONTENT: ").append(encode(content));
        message.append(DELIMITER_END);
        return message.toString();
    }
}
