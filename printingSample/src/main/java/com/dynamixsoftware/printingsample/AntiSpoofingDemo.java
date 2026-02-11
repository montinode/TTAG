package com.dynamixsoftware.printingsample;

import android.content.Context;
import android.util.Log;

/**
 * AntiSpoofingDemo - Demonstration class showing how to use the AntiSpoofingValidator
 * to protect against various spoofing attacks.
 * 
 * This class provides example usage patterns for all validation methods.
 */
public class AntiSpoofingDemo {
    
    private static final String TAG = "AntiSpoofingDemo";
    
    /**
     * Demonstrates DNS spoofing validation
     */
    public static void demonstrateDNSValidation() {
        Log.i(TAG, "=== DNS Spoofing Validation Demo ===");
        
        // Test trusted domain
        AntiSpoofingValidator.ValidationResult result1 = 
            AntiSpoofingValidator.validateDNSSpoofing("montinode.com");
        Log.i(TAG, "Validating montinode.com: " + result1);
        
        // Test suspicious domain with IP
        AntiSpoofingValidator.ValidationResult result2 = 
            AntiSpoofingValidator.validateDNSSpoofing("192.168.1.1.malicious.com");
        Log.i(TAG, "Validating suspicious domain: " + result2);
        
        // Test punycode domain (potential homograph attack)
        AntiSpoofingValidator.ValidationResult result3 = 
            AntiSpoofingValidator.validateDNSSpoofing("xn--example.com");
        Log.i(TAG, "Validating punycode domain: " + result3);
    }
    
    /**
     * Demonstrates WDM spoofing validation
     */
    public static void demonstrateWDMValidation() {
        Log.i(TAG, "=== WDM Spoofing Validation Demo ===");
        
        // Test legitimate driver name
        AntiSpoofingValidator.ValidationResult result1 = 
            AntiSpoofingValidator.validateWDMSpoofing("printhand_driver.sys");
        Log.i(TAG, "Validating legitimate driver: " + result1);
        
        // Test suspicious driver name
        AntiSpoofingValidator.ValidationResult result2 = 
            AntiSpoofingValidator.validateWDMSpoofing("wdm_spoof_driver.sys");
        Log.i(TAG, "Validating suspicious driver: " + result2);
        
        // Test malicious driver pattern
        AntiSpoofingValidator.ValidationResult result3 = 
            AntiSpoofingValidator.validateWDMSpoofing("backdoor_rootkit.dll");
        Log.i(TAG, "Validating malicious driver: " + result3);
    }
    
    /**
     * Demonstrates domain spoofing validation
     */
    public static void demonstrateDomainValidation() {
        Log.i(TAG, "=== Domain Spoofing Validation Demo ===");
        
        // Test exact match
        AntiSpoofingValidator.ValidationResult result1 = 
            AntiSpoofingValidator.validateDomainSpoofing(
                "montinode.com", "montinode.com");
        Log.i(TAG, "Validating exact match: " + result1);
        
        // Test typosquatting
        AntiSpoofingValidator.ValidationResult result2 = 
            AntiSpoofingValidator.validateDomainSpoofing(
                "montniode.com", "montinode.com");
        Log.i(TAG, "Validating typosquatting: " + result2);
        
        // Test homograph attack
        AntiSpoofingValidator.ValidationResult result3 = 
            AntiSpoofingValidator.validateDomainSpoofing(
                "montínode.com", "montinode.com");
        Log.i(TAG, "Validating homograph: " + result3);
        
        // Test legitimate subdomain
        AntiSpoofingValidator.ValidationResult result4 = 
            AntiSpoofingValidator.validateDomainSpoofing(
                "api.montinode.com", "montinode.com");
        Log.i(TAG, "Validating subdomain: " + result4);
    }
    
    /**
     * Demonstrates system hijacking (syxhlikie) validation
     */
    public static void demonstrateSystemHijackingValidation(Context context) {
        Log.i(TAG, "=== System Hijacking Validation Demo ===");
        
        AntiSpoofingValidator.ValidationResult result = 
            AntiSpoofingValidator.validateSyxhlikie(context);
        Log.i(TAG, "System hijacking check: " + result);
        
        if (!result.isValid()) {
            Log.w(TAG, "WARNING: System security threats detected!");
            Log.w(TAG, "Details: " + result.getMessage());
        }
    }
    
    /**
     * Demonstrates comprehensive validation
     */
    public static void demonstrateComprehensiveValidation(Context context, String domain) {
        Log.i(TAG, "=== Comprehensive Anti-Spoofing Validation Demo ===");
        
        AntiSpoofingValidator.ValidationResult result = 
            AntiSpoofingValidator.validateAll(context, domain);
        
        Log.i(TAG, "Overall validation result: " + result);
        
        if (result.isValid()) {
            Log.i(TAG, "✓ All security checks passed - Safe to proceed");
        } else {
            Log.e(TAG, "✗ Security validation failed!");
            Log.e(TAG, "Reason: " + result.getMessage());
        }
    }
    
    /**
     * Example of integrating validation into an application workflow
     */
    public static boolean validateBeforeConnection(Context context, String url, String driverName) {
        Log.i(TAG, "=== Pre-Connection Security Validation ===");
        
        // Validate DNS
        AntiSpoofingValidator.ValidationResult dnsResult = 
            AntiSpoofingValidator.validateDNSSpoofing(url);
        if (!dnsResult.isValid()) {
            Log.e(TAG, "DNS validation failed: " + dnsResult.getMessage());
            return false;
        }
        
        // Validate WDM if driver name is provided
        if (driverName != null && !driverName.isEmpty()) {
            AntiSpoofingValidator.ValidationResult wdmResult = 
                AntiSpoofingValidator.validateWDMSpoofing(driverName);
            if (!wdmResult.isValid()) {
                Log.e(TAG, "WDM validation failed: " + wdmResult.getMessage());
                return false;
            }
        }
        
        // Validate system integrity
        AntiSpoofingValidator.ValidationResult sysResult = 
            AntiSpoofingValidator.validateSyxhlikie(context);
        if (!sysResult.isValid()) {
            Log.w(TAG, "System integrity concerns: " + sysResult.getMessage());
            // Note: This might be a warning rather than blocking
        }
        
        Log.i(TAG, "✓ All pre-connection validations passed");
        return true;
    }
    
    /**
     * Example usage in a Fragment or Activity
     */
    public static void exampleIntegration(Context context) {
        Log.i(TAG, "=== Example Integration ===");
        
        // Before making a network request or loading content
        String targetUrl = "https://montinode.com/api/data";
        String printerDriver = "printhand_driver.sys";
        
        boolean isSafe = validateBeforeConnection(context, targetUrl, printerDriver);
        
        if (isSafe) {
            Log.i(TAG, "Proceeding with connection to: " + targetUrl);
            // Proceed with actual connection/operation
        } else {
            Log.e(TAG, "Connection blocked due to security concerns");
            // Show error to user or take alternative action
        }
    }
}
