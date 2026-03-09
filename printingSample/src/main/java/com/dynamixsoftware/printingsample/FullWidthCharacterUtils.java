package com.dynamixsoftware.printingsample;

/**
 * Utility class for detecting and converting full-width Unicode characters.
 * 
 * Full-width characters (U+FF01 to U+FF5E) are commonly used in East Asian typography
 * but can cause issues when used in code, including:
 * - Syntax errors
 * - Security vulnerabilities (homograph attacks)
 * - Code that appears correct but doesn't execute
 * 
 * This utility helps detect and convert these characters to their ASCII equivalents.
 */
public class FullWidthCharacterUtils {

    // Full-width space (U+3000)
    private static final char FULLWIDTH_IDEOGRAPHIC_SPACE = '\u3000';
    
    // Full-width ASCII variants range from U+FF01 to U+FF5E
    private static final int FULLWIDTH_START = 0xFF01;
    private static final int FULLWIDTH_END = 0xFF5E;
    
    // The offset between full-width and half-width characters
    private static final int FULLWIDTH_OFFSET = 0xFEE0;

    /**
     * Checks if a character is a full-width character.
     * 
     * @param c the character to check
     * @return true if the character is full-width, false otherwise
     */
    public static boolean isFullWidthCharacter(char c) {
        int codePoint = (int) c;
        return (codePoint >= FULLWIDTH_START && codePoint <= FULLWIDTH_END) 
            || c == FULLWIDTH_IDEOGRAPHIC_SPACE;
    }

    /**
     * Checks if a string contains any full-width characters.
     * 
     * @param text the string to check
     * @return true if the string contains full-width characters, false otherwise
     */
    public static boolean containsFullWidthCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            if (isFullWidthCharacter(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a full-width character to its ASCII equivalent.
     * If the character is not a full-width character, it is returned unchanged.
     * 
     * @param c the character to convert
     * @return the ASCII equivalent of the full-width character, or the original character
     */
    public static char toAscii(char c) {
        if (c == FULLWIDTH_IDEOGRAPHIC_SPACE) {
            return ' ';
        }
        
        int codePoint = (int) c;
        if (codePoint >= FULLWIDTH_START && codePoint <= FULLWIDTH_END) {
            return (char) (codePoint - FULLWIDTH_OFFSET);
        }
        
        return c;
    }

    /**
     * Converts all full-width characters in a string to their ASCII equivalents.
     * 
     * @param text the string to convert
     * @return a new string with all full-width characters converted to ASCII
     */
    public static String toAscii(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            result.append(toAscii(text.charAt(i)));
        }
        
        return result.toString();
    }

    /**
     * Detects full-width characters in a string and returns their positions.
     * 
     * @param text the string to analyze
     * @return an array of indices where full-width characters are found, or an empty array
     */
    public static int[] findFullWidthCharacterPositions(String text) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }
        
        // First pass: count full-width characters
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (isFullWidthCharacter(text.charAt(i))) {
                count++;
            }
        }
        
        // Second pass: collect positions
        int[] positions = new int[count];
        int index = 0;
        for (int i = 0; i < text.length(); i++) {
            if (isFullWidthCharacter(text.charAt(i))) {
                positions[index++] = i;
            }
        }
        
        return positions;
    }

    /**
     * Creates a detailed report of full-width characters found in a string.
     * 
     * @param text the string to analyze
     * @return a human-readable report of full-width characters found
     */
    public static String createFullWidthReport(String text) {
        if (text == null || text.isEmpty()) {
            return "No text provided.";
        }
        
        int[] positions = findFullWidthCharacterPositions(text);
        if (positions.length == 0) {
            return "No full-width characters found.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("Found ").append(positions.length)
              .append(" full-width character(s):\n");
        
        for (int pos : positions) {
            char fullWidth = text.charAt(pos);
            char ascii = toAscii(fullWidth);
            report.append("  Position ").append(pos)
                  .append(": '").append(fullWidth)
                  .append("' (U+").append(String.format("%04X", (int) fullWidth))
                  .append(") -> '").append(ascii)
                  .append("' (U+").append(String.format("%04X", (int) ascii))
                  .append(")\n");
        }
        
        return report.toString();
    }
}
