package com.dynamixsoftware.printingsample.gatt;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Telemetry data capture and logging for Bluetooth GATT operations.
 * Captures device telemetry like RSSI, MTU, connection state, etc.
 */
public class TelemetryLogger {
    
    private static final String TAG = "GattTelemetry";
    private final GattTraceConfig config;
    private final SimpleDateFormat timestampFormat;
    
    public TelemetryLogger(GattTraceConfig config) {
        this.config = config;
        this.timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    }
    
    /**
     * Log connection state change
     */
    public void logConnectionState(String deviceAddress, String deviceName, ConnectionState state) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] Connection State Change\n" +
                        "  Device: %s (%s)\n" +
                        "  State: %s",
                        timestamp, deviceName, deviceAddress, state));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] %s: %s (%s)",
                        timestamp, state, deviceName, deviceAddress));
                break;
            case JSON:
                logJson("connection_state", createConnectionStateJson(timestamp, deviceAddress, deviceName, state));
                break;
        }
    }
    
    /**
     * Log RSSI (signal strength)
     */
    public void logRssi(String deviceAddress, int rssi) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.d(TAG, String.format("[%s] RSSI Update\n" +
                        "  Device: %s\n" +
                        "  Signal Strength: %d dBm",
                        timestamp, deviceAddress, rssi));
                break;
            case COMPACT:
                Log.d(TAG, String.format("[%s] RSSI: %d dBm (%s)",
                        timestamp, rssi, deviceAddress));
                break;
            case JSON:
                logJson("rssi", createRssiJson(timestamp, deviceAddress, rssi));
                break;
        }
    }
    
    /**
     * Log MTU (Maximum Transmission Unit) change
     */
    public void logMtuChange(String deviceAddress, int mtu) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] MTU Changed\n" +
                        "  Device: %s\n" +
                        "  MTU: %d bytes",
                        timestamp, deviceAddress, mtu));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] MTU: %d (%s)",
                        timestamp, mtu, deviceAddress));
                break;
            case JSON:
                logJson("mtu_change", createMtuJson(timestamp, deviceAddress, mtu));
                break;
        }
    }
    
    /**
     * Log PHY (Physical Layer) change
     */
    public void logPhyChange(String deviceAddress, int txPhy, int rxPhy) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] PHY Changed\n" +
                        "  Device: %s\n" +
                        "  TX PHY: %d\n" +
                        "  RX PHY: %d",
                        timestamp, deviceAddress, txPhy, rxPhy));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] PHY: TX=%d RX=%d (%s)",
                        timestamp, txPhy, rxPhy, deviceAddress));
                break;
            case JSON:
                logJson("phy_change", createPhyJson(timestamp, deviceAddress, txPhy, rxPhy));
                break;
        }
    }
    
    // JSON helper methods
    
    private JSONObject createConnectionStateJson(String timestamp, String address, String name, ConnectionState state) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("device_name", name);
            json.put("state", state.name());
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createRssiJson(String timestamp, String address, int rssi) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("rssi_dbm", rssi);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createMtuJson(String timestamp, String address, int mtu) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("mtu_bytes", mtu);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createPhyJson(String timestamp, String address, int txPhy, int rxPhy) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("tx_phy", txPhy);
            json.put("rx_phy", rxPhy);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private void logJson(String eventType, JSONObject json) {
        try {
            json.put("event_type", eventType);
            Log.i(TAG, json.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error logging JSON", e);
        }
    }
    
    /**
     * Connection state enum
     */
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }
}
