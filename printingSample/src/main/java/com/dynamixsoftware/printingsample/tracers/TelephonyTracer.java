package com.dynamixsoftware.printingsample.tracers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.dynamixsoftware.printingsample.models.TelephonyEvent;

import java.util.List;

/**
 * TelephonyTracer - Intercepts telephony and cellular operations
 * 
 * Features:
 * - Call state monitoring (incoming, outgoing, missed)
 * - SMS/MMS send/receive tracking
 * - Cellular signal strength and network type logging
 * - Phone number and contact tracing
 * - SIM card and subscription information
 * - Thread-safe callback handling
 */
public class TelephonyTracer extends BaseTracer {
    
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private BroadcastReceiver smsReceiver;
    private boolean receiverRegistered = false;
    private int lastSignalStrength = -1;
    
    public TelephonyTracer(Context context) {
        super(context);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
    
    @Override
    protected String getTracerName() {
        return "TELEPHONY_TRACER";
    }
    
    @Override
    protected void onStart() {
        if (telephonyManager == null) {
            logEvent("ERROR", "TelephonyManager not available");
            return;
        }
        
        // Log initial telephony info
        logTelephonyInfo();
        
        // Setup phone state listener
        setupPhoneStateListener();
        
        // Setup SMS receiver
        setupSmsReceiver();
        
        logEvent("INFO", "Telephony monitoring started");
    }
    
    @Override
    protected void onStop() {
        // Unregister phone state listener
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        
        // Unregister SMS receiver
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(smsReceiver);
                receiverRegistered = false;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering SMS receiver", e);
            }
        }
    }
    
    private void logTelephonyInfo() {
        try {
            // Network operator
            String networkOperator = telephonyManager.getNetworkOperatorName();
            logEvent("INFO", "Network operator: " + networkOperator);
            
            // Network type
            String networkType = getNetworkTypeString(telephonyManager.getNetworkType());
            logEvent("INFO", "Network type: " + networkType);
            
            // Phone type
            String phoneType = getPhoneTypeString(telephonyManager.getPhoneType());
            logEvent("INFO", "Phone type: " + phoneType);
            
            // SIM state
            String simState = getSimStateString(telephonyManager.getSimState());
            logEvent("INFO", "SIM state: " + simState);
            
            // Subscription info (requires READ_PHONE_STATE permission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                logSubscriptionInfo();
            }
        } catch (SecurityException e) {
            logEvent("WARNING", "Missing permissions for telephony info: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error logging telephony info", e);
        }
    }
    
    private void logSubscriptionInfo() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (subscriptionInfoList != null) {
                        for (SubscriptionInfo info : subscriptionInfoList) {
                            logEvent("SUBSCRIPTION_INFO", 
                                "Carrier: " + info.getCarrierName() + 
                                ", Slot: " + info.getSimSlotIndex() +
                                ", Number: " + info.getNumber());
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            logEvent("WARNING", "Missing permissions for subscription info");
        } catch (Exception e) {
            Log.e(TAG, "Error logging subscription info", e);
        }
    }
    
    private void setupPhoneStateListener() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                
                if (!isRunning.get()) return;
                
                TelephonyEvent event = new TelephonyEvent("CALL_STATE_CHANGE");
                event.setCallState(getCallStateString(state));
                
                // Only log phone number if permissions allow (and it's not empty)
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    // Mask phone number for privacy
                    event.setPhoneNumber(maskPhoneNumber(phoneNumber));
                }
                
                logTraceEvent(event);
            }
            
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                
                if (!isRunning.get()) return;
                
                int level = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    level = signalStrength.getLevel();
                } else {
                    // For older versions, try to get GSM signal strength
                    try {
                        level = signalStrength.getGsmSignalStrength();
                    } catch (Exception e) {
                        level = 0;
                    }
                }
                
                // Only log if signal strength changed significantly
                if (Math.abs(level - lastSignalStrength) >= 1) {
                    lastSignalStrength = level;
                    
                    TelephonyEvent event = new TelephonyEvent("SIGNAL_STRENGTH_CHANGE");
                    event.setSignalStrength(level);
                    event.setNetworkType(getNetworkTypeString(telephonyManager.getNetworkType()));
                    
                    logTraceEvent(event);
                }
            }
        };
        
        // Listen for call state and signal strength changes
        telephonyManager.listen(phoneStateListener, 
            PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
    
    private void setupSmsReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isRunning.get()) return;
                
                String action = intent.getAction();
                TelephonyEvent event = new TelephonyEvent("SMS_EVENT");
                
                if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
                    event.setSmsDirection("INBOUND");
                    event.setData("SMS received");
                    logTraceEvent(event);
                } else if ("android.provider.Telephony.SMS_SENT".equals(action)) {
                    event.setSmsDirection("OUTBOUND");
                    event.setData("SMS sent");
                    logTraceEvent(event);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.provider.Telephony.SMS_SENT");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(smsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(smsReceiver, filter);
        }
        receiverRegistered = true;
    }
    
    private String getCallStateString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "IDLE";
            case TelephonyManager.CALL_STATE_RINGING:
                return "RINGING";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "OFFHOOK";
            default:
                return "UNKNOWN";
        }
    }
    
    private String getNetworkTypeString(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    if (type == TelephonyManager.NETWORK_TYPE_NR) {
                        return "5G_NR";
                    }
                }
                return "UNKNOWN";
        }
    }
    
    private String getPhoneTypeString(int type) {
        switch (type) {
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            default:
                return "NONE";
        }
    }
    
    private String getSimStateString(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "ABSENT";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "PIN_REQUIRED";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "PUK_REQUIRED";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "NETWORK_LOCKED";
            case TelephonyManager.SIM_STATE_READY:
                return "READY";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Mask phone number for privacy (show only last 4 digits)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return "***";
        }
        String lastFour = phoneNumber.substring(phoneNumber.length() - 4);
        return "***" + lastFour;
    }
}
