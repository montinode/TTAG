package com.dynamixsoftware.printingsample;

/**
 * Simple test class for FullWidthCharacterUtils.
 * This class provides basic test cases to verify the functionality.
 * 
 * Run this class to execute all tests.
 */
public class FullWidthCharacterUtilsTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("=== Running FullWidthCharacterUtils Tests ===\n");
        
        testIsFullWidthCharacter();
        testContainsFullWidthCharacters();
        testToAsciiChar();
        testToAsciiString();
        testFindFullWidthCharacterPositions();
        testCreateFullWidthReport();
        testNullAndEmptyStrings();
        testMixedContent();
        testFullWidthSpace();
        
        System.out.println("\n=== Test Results ===");
        System.out.println("Total:  " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        
        if (failedTests == 0) {
            System.out.println("\n✓ All tests passed!");
        } else {
            System.out.println("\n✗ Some tests failed!");
            System.exit(1);
        }
    }

    private static void testIsFullWidthCharacter() {
        System.out.println("Test: isFullWidthCharacter()");
        
        // Test full-width characters
        assertTrue("Full-width 'a' should be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('ａ'));
        assertTrue("Full-width 'Z' should be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('Ｚ'));
        assertTrue("Full-width '0' should be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('０'));
        assertTrue("Full-width '(' should be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('（'));
        assertTrue("Full-width space should be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('\u3000'));
        
        // Test regular ASCII characters
        assertFalse("Regular 'a' should not be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('a'));
        assertFalse("Regular 'Z' should not be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('Z'));
        assertFalse("Regular '0' should not be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter('0'));
        assertFalse("Regular space should not be detected", 
            FullWidthCharacterUtils.isFullWidthCharacter(' '));
        
        System.out.println();
    }

    private static void testContainsFullWidthCharacters() {
        System.out.println("Test: containsFullWidthCharacters()");
        
        assertTrue("Should detect full-width in 'ａｂｃ'", 
            FullWidthCharacterUtils.containsFullWidthCharacters("ａｂｃ"));
        assertTrue("Should detect full-width in mixed 'aｂc'", 
            FullWidthCharacterUtils.containsFullWidthCharacters("aｂc"));
        assertFalse("Should not detect in 'abc'", 
            FullWidthCharacterUtils.containsFullWidthCharacters("abc"));
        assertFalse("Should not detect in empty string", 
            FullWidthCharacterUtils.containsFullWidthCharacters(""));
        assertFalse("Should not detect in null", 
            FullWidthCharacterUtils.containsFullWidthCharacters(null));
        
        System.out.println();
    }

    private static void testToAsciiChar() {
        System.out.println("Test: toAscii(char)");
        
        assertEquals("Full-width 'a' should convert to 'a'", 
            'a', FullWidthCharacterUtils.toAscii('ａ'));
        assertEquals("Full-width 'Z' should convert to 'Z'", 
            'Z', FullWidthCharacterUtils.toAscii('Ｚ'));
        assertEquals("Full-width '0' should convert to '0'", 
            '0', FullWidthCharacterUtils.toAscii('０'));
        assertEquals("Full-width '(' should convert to '('", 
            '(', FullWidthCharacterUtils.toAscii('（'));
        assertEquals("Full-width space should convert to ' '", 
            ' ', FullWidthCharacterUtils.toAscii('\u3000'));
        assertEquals("Regular 'a' should remain 'a'", 
            'a', FullWidthCharacterUtils.toAscii('a'));
        
        System.out.println();
    }

    private static void testToAsciiString() {
        System.out.println("Test: toAscii(String)");
        
        assertEquals("Full-width string should convert", 
            "abc", FullWidthCharacterUtils.toAscii("ａｂｃ"));
        assertEquals("Mixed string should convert", 
            "abc", FullWidthCharacterUtils.toAscii("aｂc"));
        assertEquals("Regular string should remain unchanged", 
            "abc", FullWidthCharacterUtils.toAscii("abc"));
        assertEquals("Empty string should remain empty", 
            "", FullWidthCharacterUtils.toAscii(""));
        assertEquals("Null should remain null", 
            null, FullWidthCharacterUtils.toAscii(null));
        
        // Test the authentication code sample
        String fullWidth = "ａｐｐ.ｇｅｔ";
        String expected = "app.get";
        assertEquals("Authentication code should convert", 
            expected, FullWidthCharacterUtils.toAscii(fullWidth));
        
        System.out.println();
    }

    private static void testFindFullWidthCharacterPositions() {
        System.out.println("Test: findFullWidthCharacterPositions()");
        
        int[] positions = FullWidthCharacterUtils.findFullWidthCharacterPositions("aｂc");
        assertEquals("Should find 1 position", 1, positions.length);
        assertEquals("Position should be 1", 1, positions[0]);
        
        positions = FullWidthCharacterUtils.findFullWidthCharacterPositions("abc");
        assertEquals("Should find 0 positions", 0, positions.length);
        
        positions = FullWidthCharacterUtils.findFullWidthCharacterPositions("ａｂｃ");
        assertEquals("Should find 3 positions", 3, positions.length);
        
        System.out.println();
    }

    private static void testCreateFullWidthReport() {
        System.out.println("Test: createFullWidthReport()");
        
        String report = FullWidthCharacterUtils.createFullWidthReport("aｂc");
        assertTrue("Report should contain position info", 
            report.contains("Position 1"));
        
        report = FullWidthCharacterUtils.createFullWidthReport("abc");
        assertTrue("Report should indicate no full-width chars", 
            report.contains("No full-width"));
        
        report = FullWidthCharacterUtils.createFullWidthReport(null);
        assertTrue("Report should handle null", 
            report.contains("No text"));
        
        System.out.println();
    }

    private static void testNullAndEmptyStrings() {
        System.out.println("Test: Null and empty string handling");
        
        assertFalse("Null should not contain full-width", 
            FullWidthCharacterUtils.containsFullWidthCharacters(null));
        assertFalse("Empty should not contain full-width", 
            FullWidthCharacterUtils.containsFullWidthCharacters(""));
        
        assertEquals("Null should remain null", 
            null, FullWidthCharacterUtils.toAscii((String) null));
        assertEquals("Empty should remain empty", 
            "", FullWidthCharacterUtils.toAscii(""));
        
        int[] positions = FullWidthCharacterUtils.findFullWidthCharacterPositions(null);
        assertEquals("Null should return empty array", 0, positions.length);
        
        System.out.println();
    }

    private static void testMixedContent() {
        System.out.println("Test: Mixed content");
        
        String mixed = "Hello ｗｏｒｌｄ!";
        assertTrue("Should detect full-width in mixed", 
            FullWidthCharacterUtils.containsFullWidthCharacters(mixed));
        assertEquals("Should convert only full-width", 
            "Hello world!", FullWidthCharacterUtils.toAscii(mixed));
        
        System.out.println();
    }

    private static void testFullWidthSpace() {
        System.out.println("Test: Full-width space");
        
        char fullWidthSpace = '\u3000';
        assertTrue("Should detect full-width space", 
            FullWidthCharacterUtils.isFullWidthCharacter(fullWidthSpace));
        assertEquals("Should convert to regular space", 
            ' ', FullWidthCharacterUtils.toAscii(fullWidthSpace));
        
        String withSpace = "a\u3000b";
        assertEquals("Should convert space in string", 
            "a b", FullWidthCharacterUtils.toAscii(withSpace));
        
        System.out.println();
    }

    // Helper methods
    private static void assertTrue(String message, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  ✓ " + message);
        } else {
            failedTests++;
            System.out.println("  ✗ " + message + " [FAILED]");
        }
    }

    private static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        totalTests++;
        boolean equal = (expected == null && actual == null) || 
                       (expected != null && expected.equals(actual));
        if (equal) {
            passedTests++;
            System.out.println("  ✓ " + message);
        } else {
            failedTests++;
            System.out.println("  ✗ " + message + " [FAILED]");
            System.out.println("    Expected: " + expected);
            System.out.println("    Actual:   " + actual);
        }
    }

    private static void assertEquals(String message, int expected, int actual) {
        totalTests++;
        if (expected == actual) {
            passedTests++;
            System.out.println("  ✓ " + message);
        } else {
            failedTests++;
            System.out.println("  ✗ " + message + " [FAILED]");
            System.out.println("    Expected: " + expected);
            System.out.println("    Actual:   " + actual);
        }
    }

    private static void assertEquals(String message, char expected, char actual) {
        totalTests++;
        if (expected == actual) {
            passedTests++;
            System.out.println("  ✓ " + message);
        } else {
            failedTests++;
            System.out.println("  ✗ " + message + " [FAILED]");
            System.out.println("    Expected: '" + expected + "' (U+" + 
                String.format("%04X", (int) expected) + ")");
            System.out.println("    Actual:   '" + actual + "' (U+" + 
                String.format("%04X", (int) actual) + ")");
        }
    }
}
