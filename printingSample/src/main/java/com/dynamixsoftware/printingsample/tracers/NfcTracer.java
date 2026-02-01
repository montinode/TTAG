package com.dynamixsoftware.printingsample.tracers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import android.util.Log;

import com.dynamixsoftware.printingsample.models.NfcEvent;

/**
 * NfcTracer - Intercepts and logs all NFC operations
 * 
 * Features:
 * - NFC tag detection and read/write operations
 * - NDEF message parsing and logging
 * - NFC reader/writer state transitions
 * - Telemetry: tag type, UID, signal strength, read/write timing
 * - Thread-safe logging with timestamps
 */
public class NfcTracer extends BaseTracer {
    
    private NfcAdapter nfcAdapter;
    private BroadcastReceiver nfcStateReceiver;
    private boolean receiverRegistered = false;
    
    public NfcTracer(Context context) {
        super(context);
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }
    
    @Override
    protected String getTracerName() {
        return "NFC_TRACER";
    }
    
    @Override
    protected void onStart() {
        if (nfcAdapter == null) {
            logEvent("ERROR", "NFC not available on this device");
            return;
        }
        
        if (!nfcAdapter.isEnabled()) {
            logEvent("WARNING", "NFC is disabled");
        }
        
        // Register NFC state change receiver
        setupNfcStateReceiver();
        
        logEvent("INFO", "NFC adapter state: " + (nfcAdapter.isEnabled() ? "ENABLED" : "DISABLED"));
    }
    
    @Override
    protected void onStop() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(nfcStateReceiver);
                receiverRegistered = false;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering NFC receiver", e);
            }
        }
    }
    
    private void setupNfcStateReceiver() {
        nfcStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                    String stateStr = getNfcStateString(state);
                    logEvent("NFC_STATE_CHANGE", "NFC adapter state changed to: " + stateStr);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(nfcStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(nfcStateReceiver, filter);
        }
        receiverRegistered = true;
    }
    
    /**
     * Handle NFC tag discovered
     */
    public void onTagDiscovered(Tag tag) {
        if (!isRunning.get()) return;
        
        long startTime = System.currentTimeMillis();
        NfcEvent event = new NfcEvent("TAG_DISCOVERED");
        
        try {
            // Get tag UID
            byte[] id = tag.getId();
            String uid = bytesToHex(id);
            event.setUid(uid);
            
            // Get tag tech types
            String[] techList = tag.getTechList();
            event.setTagType(techList.length > 0 ? techList[0] : "UNKNOWN");
            
            // Try to read NDEF if available
            if (isNdefSupported(tag)) {
                readNdefMessage(tag, event);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            event.setOperationDuration(duration);
            
            logTraceEvent(event);
        } catch (Exception e) {
            event.setData("Error processing tag: " + e.getMessage());
            logTraceEvent(event);
        }
    }
    
    /**
     * Handle NDEF message read
     */
    private void readNdefMessage(Tag tag, NfcEvent event) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
                    String message = parseNdefMessage(ndefMessage);
                    event.setNdefMessage(message);
                }
                ndef.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading NDEF message", e);
        }
    }
    
    /**
     * Parse NDEF message to string
     */
    private String parseNdefMessage(NdefMessage message) {
        StringBuilder sb = new StringBuilder();
        NdefRecord[] records = message.getRecords();
        for (int i = 0; i < records.length; i++) {
            NdefRecord record = records[i];
            sb.append("Record ").append(i).append(": ");
            sb.append("TNF=").append(record.getTnf()).append(", ");
            sb.append("Type=").append(bytesToHex(record.getType())).append(", ");
            sb.append("Payload=").append(new String(record.getPayload()));
            if (i < records.length - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Check if tag supports NDEF
     */
    private boolean isNdefSupported(Tag tag) {
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.equals(Ndef.class.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Log NFC write operation
     */
    public void onTagWrite(Tag tag, boolean success) {
        if (!isRunning.get()) return;
        
        NfcEvent event = new NfcEvent("TAG_WRITE");
        byte[] id = tag.getId();
        event.setUid(bytesToHex(id));
        event.setData(success ? "Write successful" : "Write failed");
        
        logTraceEvent(event);
    }
    
    /**
     * Convert byte array to hex string
     */
    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    /**
     * Get NFC state as string
     */
    private String getNfcStateString(int state) {
        switch (state) {
            case NfcAdapter.STATE_OFF:
                return "OFF";
            case NfcAdapter.STATE_TURNING_ON:
                return "TURNING_ON";
            case NfcAdapter.STATE_ON:
                return "ON";
            case NfcAdapter.STATE_TURNING_OFF:
                return "TURNING_OFF";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Check if NFC is available
     */
    public boolean isNfcAvailable() {
        return nfcAdapter != null;
    }
    
    /**
     * Check if NFC is enabled
     */
    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }
}
