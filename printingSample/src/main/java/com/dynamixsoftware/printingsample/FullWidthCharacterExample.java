package com.dynamixsoftware.printingsample;

/**
 * Example demonstrating the detection and conversion of full-width characters.
 * 
 * This example shows how full-width Unicode characters can appear in code
 * and how to detect and convert them using FullWidthCharacterUtils.
 */
public class FullWidthCharacterExample {

    /**
     * Example code containing full-width characters (from authentication endpoint example).
     * This code looks correct but will not execute because it uses full-width characters
     * instead of normal ASCII characters.
     */
    private static final String FULLWIDTH_CODE_SAMPLE = 
        "ａｐｐ.ｇｅｔ('/ａｕｔｈｅｎｔｉｃａｔｅ', ａｓｙｎｃ (ｒｅｑ, ｒｅｓ) => {\n" +
        "  ｃｏｎｓｔ ｔｏｋｅｎ = ｒｅｑ.ｑｕｅｒｙ.ｔｏｋｅｎ;\n" +
        "  ｃｏｎｓｔ ｔｏｋｅｎＴｙｐｅ = ｒｅｑ.ｑｕｅｒｙ.ｓｔｙｔｃｈ_ｔｏｋｅｎ_ｔｙｐｅ;\n" +
        "}";

    /**
     * Main method demonstrating the usage of FullWidthCharacterUtils.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Full-Width Character Detection Example ===\n");
        
        // Example 1: Detect full-width characters
        System.out.println("Example 1: Detecting full-width characters");
        System.out.println("Original text: " + FULLWIDTH_CODE_SAMPLE);
        System.out.println("\nContains full-width characters? " + 
            FullWidthCharacterUtils.containsFullWidthCharacters(FULLWIDTH_CODE_SAMPLE));
        System.out.println();
        
        // Example 2: Create detailed report
        System.out.println("Example 2: Detailed report");
        String shortSample = "ａｐｐ.ｇｅｔ";
        System.out.println(FullWidthCharacterUtils.createFullWidthReport(shortSample));
        
        // Example 3: Convert to ASCII
        System.out.println("Example 3: Converting to ASCII");
        System.out.println("Original:  " + shortSample);
        System.out.println("Converted: " + FullWidthCharacterUtils.toAscii(shortSample));
        System.out.println();
        
        // Example 4: Convert full code sample
        System.out.println("Example 4: Converting full code sample");
        String converted = FullWidthCharacterUtils.toAscii(FULLWIDTH_CODE_SAMPLE);
        System.out.println("Converted code:");
        System.out.println(converted);
        System.out.println();
        
        // Example 5: Compare individual characters
        System.out.println("Example 5: Character comparison");
        String test = "ａ vs a";
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            if (FullWidthCharacterUtils.isFullWidthCharacter(c)) {
                System.out.printf("Position %d: '%c' (U+%04X) is full-width -> '%c' (U+%04X)%n",
                    i, c, (int) c, 
                    FullWidthCharacterUtils.toAscii(c), 
                    (int) FullWidthCharacterUtils.toAscii(c));
            }
        }
        
        System.out.println("\n=== End of Examples ===");
    }

    /**
     * Demonstrates the security risk of full-width characters in code.
     * This method shows how code with full-width characters can look identical
     * to normal code but behave differently.
     * 
     * @return example description
     */
    public static String demonstrateSecurityRisk() {
        StringBuilder demo = new StringBuilder();
        demo.append("Security Risk: Homograph Attack Example\n");
        demo.append("=========================================\n\n");
        
        String normal = "token";
        String fullwidth = "ｔｏｋｅｎ";
        
        demo.append("Normal ASCII:     '").append(normal).append("'\n");
        demo.append("Full-width:       '").append(fullwidth).append("'\n");
        demo.append("Visually similar: ").append(normal.equals(fullwidth) ? "NO" : "YES").append("\n");
        demo.append("String equality:  ").append(normal.equals(fullwidth)).append("\n\n");
        
        demo.append("This can lead to:\n");
        demo.append("- Variable name confusion\n");
        demo.append("- Function call errors\n");
        demo.append("- Security vulnerabilities\n");
        demo.append("- Hard-to-debug issues\n");
        
        return demo.toString();
    }
}
