# Full-Width Character Detection Utility

## Overview

This utility provides tools to detect and convert full-width Unicode characters to their ASCII equivalents. Full-width characters (U+FF01 to U+FF5E) are commonly used in East Asian typography but can cause serious issues when used in code.

## Problem

Full-width characters look nearly identical to regular ASCII characters but have different Unicode code points. When used in source code, they can cause:

1. **Syntax Errors**: Code that appears correct but fails to compile or execute
2. **Security Vulnerabilities**: Homograph attacks where malicious code looks legitimate
3. **Hard-to-Debug Issues**: Invisible differences that are difficult to spot in code review

### Example

The following code looks correct but contains full-width characters:

```javascript
ａｐｐ.ｇｅｔ('/ａｕｔｈｅｎｔｉｃａｔｅ', ａｓｙｎｃ (ｒｅｑ, ｒｅｓ) => {
  ｃｏｎｓｔ ｔｏｋｅｎ = ｒｅｑ.ｑｕｅｒｙ.ｔｏｋｅｎ;
  ｃｏｎｓｔ ｔｏｋｅｎＴｙｐｅ = ｒｅｑ.ｑｕｅｒｙ.ｓｔｙｔｃｈ_ｔｏｋｅｎ_ｔｙｐｅ;
}
```

This code will not execute because every character is a full-width Unicode character instead of regular ASCII.

## Solution

The `FullWidthCharacterUtils` class provides methods to:

- Detect full-width characters
- Convert full-width characters to ASCII equivalents
- Generate detailed reports of full-width character occurrences

## Usage

### Detect Full-Width Characters

```java
String code = "ａｐｐ.ｇｅｔ";
boolean hasFullWidth = FullWidthCharacterUtils.containsFullWidthCharacters(code);
// Returns: true
```

### Convert to ASCII

```java
String fullWidth = "ａｐｐ.ｇｅｔ";
String ascii = FullWidthCharacterUtils.toAscii(fullWidth);
// Returns: "app.get"
```

### Check Individual Characters

```java
char fullWidth = 'ａ';
boolean isFullWidth = FullWidthCharacterUtils.isFullWidthCharacter(fullWidth);
// Returns: true

char ascii = FullWidthCharacterUtils.toAscii(fullWidth);
// Returns: 'a'
```

### Find Positions

```java
String text = "aｂc";
int[] positions = FullWidthCharacterUtils.findFullWidthCharacterPositions(text);
// Returns: [1] (the position of 'ｂ')
```

### Generate Report

```java
String code = "ａｐｐ.ｇｅｔ";
String report = FullWidthCharacterUtils.createFullWidthReport(code);
```

Output:
```
Found 6 full-width character(s):
  Position 0: 'ａ' (U+FF41) -> 'a' (U+0061)
  Position 1: 'ｐ' (U+FF50) -> 'p' (U+0070)
  Position 2: 'ｐ' (U+FF50) -> 'p' (U+0070)
  Position 4: 'ｇ' (U+FF47) -> 'g' (U+0067)
  Position 5: 'ｅ' (U+FF45) -> 'e' (U+0065)
  Position 6: 'ｔ' (U+FF54) -> 't' (U+0074)
```

## Files

- **FullWidthCharacterUtils.java**: Core utility class with detection and conversion methods
- **FullWidthCharacterExample.java**: Demonstration of the utility with real-world examples
- **FullWidthCharacterUtilsTest.java**: Comprehensive test suite

## Running the Examples

### Compile

```bash
cd printingSample/src/main/java
javac -d /tmp/classes com/dynamixsoftware/printingsample/FullWidthCharacterUtils.java
javac -cp /tmp/classes -d /tmp/classes com/dynamixsoftware/printingsample/FullWidthCharacterExample.java
```

### Run Example

```bash
cd /tmp/classes
java com.dynamixsoftware.printingsample.FullWidthCharacterExample
```

### Run Tests

```bash
cd printingSample/src/main/java
javac -cp /tmp/classes -d /tmp/classes com/dynamixsoftware/printingsample/FullWidthCharacterUtilsTest.java
cd /tmp/classes
java com.dynamixsoftware.printingsample.FullWidthCharacterUtilsTest
```

## Technical Details

### Full-Width Character Range

- **ASCII variants**: U+FF01 to U+FF5E (！ to ～)
- **Full-width space**: U+3000 (　)

### Conversion Method

Full-width ASCII variants are converted by subtracting `0xFEE0` from their code point:
- 'ａ' (U+FF41) - 0xFEE0 = 'a' (U+0061)
- '０' (U+FF10) - 0xFEE0 = '0' (U+0030)

The full-width ideographic space (U+3000) is converted to a regular space (U+0020).

## Security Implications

Full-width characters can be used for:

1. **Homograph Attacks**: Making malicious code look legitimate
2. **Variable Confusion**: Creating variables that look identical but are different
3. **Code Injection**: Bypassing simple string matching security checks

This utility helps identify and prevent such security issues.

## References

- Unicode Full-width Forms: https://en.wikipedia.org/wiki/Halfwidth_and_fullwidth_forms
- Unicode Block: https://www.unicode.org/charts/PDF/UFF00.pdf
