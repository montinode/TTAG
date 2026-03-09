package com.dynamixsoftware.printingsample;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FilesUtils.extractFilesFromAssets(this);
        
        // Initialize Anti-Spoofing Security Validation
        initializeAntiSpoofingValidation();

        this.<ViewPager>findViewById(R.id.pager).setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 4;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    default:
                    case 0:
                        return new ShareIntentFragment();
                    case 1:
                        return new IntentApiFragment();
                    case 2:
                        return new PrintServiceFragment();
                    case 3:
                        return new WireframeFragment();
                }
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    default:
                    case 0:
                        return getString(R.string.share_intent);
                    case 1:
                        return getString(R.string.intent_api);
                    case 2:
                        return getString(R.string.printing_sdk);
                    case 3:
                        return getString(R.string.wireframe);
                }
            }
        });
    }
    
    /**
     * Initialize and demonstrate Anti-Spoofing validation
     * This validates against DNS Spoofing, WDM Spoofing, Domain Spoofing, and System Hijacking
     */
    private void initializeAntiSpoofingValidation() {
        Log.i(TAG, "=== Initializing Anti-Spoofing Security Layer ===");
        
        // Run comprehensive security validation
        AntiSpoofingValidator.ValidationResult result = 
            AntiSpoofingValidator.validateAll(this, "https://montinode.com");
        
        Log.i(TAG, "Security validation result: " + result);
        
        if (!result.isValid()) {
            Log.w(TAG, "Security concerns detected: " + result.getMessage());
        } else {
            Log.i(TAG, "All anti-spoofing checks passed successfully");
        }
        
        // Optional: Run demo validations
        // Uncomment to see detailed validation examples in logs
        // runAntiSpoofingDemo();
    }
    
    /**
     * Run comprehensive anti-spoofing demonstrations
     * Uncomment the call to this method in initializeAntiSpoofingValidation() to see demos
     */
    private void runAntiSpoofingDemo() {
        Log.i(TAG, "=== Running Anti-Spoofing Demonstrations ===");
        
        // DNS Spoofing Demo
        AntiSpoofingDemo.demonstrateDNSValidation();
        
        // WDM Spoofing Demo
        AntiSpoofingDemo.demonstrateWDMValidation();
        
        // Domain Spoofing Demo
        AntiSpoofingDemo.demonstrateDomainValidation();
        
        // System Hijacking Demo
        AntiSpoofingDemo.demonstrateSystemHijackingValidation(this);
        
        // Comprehensive Demo
        AntiSpoofingDemo.demonstrateComprehensiveValidation(this, "https://montinode.com");
    }

}
