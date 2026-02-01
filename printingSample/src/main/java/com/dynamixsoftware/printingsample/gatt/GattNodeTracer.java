package com.dynamixsoftware.printingsample.gatt;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Main GATT Node Tracer class that intercepts and logs all Bluetooth GATT operations.
 * Implements a GATT callback interceptor pattern with thread-safe logging.
 */
public class GattNodeTracer {
    
    private static final String TAG = "GattNodeTracer";
    
    private final GattTraceConfig config;
    private final TelemetryLogger telemetryLogger;
    private final VoiceTracer voiceTracer;
    private final SimpleDateFormat timestampFormat;
    private final ReentrantReadWriteLock lock;
    private final ConcurrentHashMap<String, DeviceContext> deviceContexts;
    
    public GattNodeTracer(GattTraceConfig config) {
        this.config = config;
        this.telemetryLogger = new TelemetryLogger(config);
        this.voiceTracer = new VoiceTracer(config);
        this.timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        this.lock = new ReentrantReadWriteLock();
        this.deviceContexts = new ConcurrentHashMap<>();
        
        logTracerStart();
    }
    
    /**
     * Log GATT read operation
     */
    public void logGattRead(String deviceAddress, String serviceUuid, String characteristicUuid, 
                           byte[] value, int status) {
        if (!shouldTrace(deviceAddress, serviceUuid, characteristicUuid)) {
            return;
        }
        
        lock.readLock().lock();
        try {
            String timestamp = timestampFormat.format(new Date());
            
            switch (config.getProfile()) {
                case VERBOSE:
                    Log.i(TAG, String.format("[%s] GATT READ\n" +
                            "  Device: %s\n" +
                            "  Service: %s\n" +
                            "  Characteristic: %s\n" +
                            "  Status: %d\n" +
                            "  Value: %s",
                            timestamp, deviceAddress, serviceUuid, characteristicUuid, 
                            status, bytesToHex(value)));
                    break;
                case COMPACT:
                    Log.i(TAG, String.format("[%s] READ %s/%s: %s (status=%d) (%s)",
                            timestamp, shortenUuid(serviceUuid), shortenUuid(characteristicUuid), 
                            bytesToHex(value), status, deviceAddress));
                    break;
                case JSON:
                    logJson("gatt_read", createGattOperationJson(timestamp, deviceAddress, 
                            serviceUuid, characteristicUuid, "READ", value, status));
                    break;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Log GATT write operation
     */
    public void logGattWrite(String deviceAddress, String serviceUuid, String characteristicUuid, 
                            byte[] value, int status) {
        if (!shouldTrace(deviceAddress, serviceUuid, characteristicUuid)) {
            return;
        }
        
        lock.readLock().lock();
        try {
            String timestamp = timestampFormat.format(new Date());
            
            switch (config.getProfile()) {
                case VERBOSE:
                    Log.i(TAG, String.format("[%s] GATT WRITE\n" +
                            "  Device: %s\n" +
                            "  Service: %s\n" +
                            "  Characteristic: %s\n" +
                            "  Status: %d\n" +
                            "  Value: %s",
                            timestamp, deviceAddress, serviceUuid, characteristicUuid, 
                            status, bytesToHex(value)));
                    break;
                case COMPACT:
                    Log.i(TAG, String.format("[%s] WRITE %s/%s: %s (status=%d) (%s)",
                            timestamp, shortenUuid(serviceUuid), shortenUuid(characteristicUuid), 
                            bytesToHex(value), status, deviceAddress));
                    break;
                case JSON:
                    logJson("gatt_write", createGattOperationJson(timestamp, deviceAddress, 
                            serviceUuid, characteristicUuid, "WRITE", value, status));
                    break;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Log GATT notification received
     */
    public void logGattNotification(String deviceAddress, String serviceUuid, String characteristicUuid, 
                                   byte[] value) {
        if (!shouldTrace(deviceAddress, serviceUuid, characteristicUuid)) {
            return;
        }
        
        lock.readLock().lock();
        try {
            String timestamp = timestampFormat.format(new Date());
            
            switch (config.getProfile()) {
                case VERBOSE:
                    Log.i(TAG, String.format("[%s] GATT NOTIFICATION\n" +
                            "  Device: %s\n" +
                            "  Service: %s\n" +
                            "  Characteristic: %s\n" +
                            "  Value: %s",
                            timestamp, deviceAddress, serviceUuid, characteristicUuid, 
                            bytesToHex(value)));
                    break;
                case COMPACT:
                    Log.i(TAG, String.format("[%s] NOTIFY %s/%s: %s (%s)",
                            timestamp, shortenUuid(serviceUuid), shortenUuid(characteristicUuid), 
                            bytesToHex(value), deviceAddress));
                    break;
                case JSON:
                    logJson("gatt_notification", createGattOperationJson(timestamp, deviceAddress, 
                            serviceUuid, characteristicUuid, "NOTIFY", value, 0));
                    break;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Log GATT descriptor read
     */
    public void logDescriptorRead(String deviceAddress, String serviceUuid, String characteristicUuid,
                                  String descriptorUuid, byte[] value, int status) {
        if (!shouldTrace(deviceAddress, serviceUuid, characteristicUuid)) {
            return;
        }
        
        lock.readLock().lock();
        try {
            String timestamp = timestampFormat.format(new Date());
            
            switch (config.getProfile()) {
                case VERBOSE:
                    Log.d(TAG, String.format("[%s] DESCRIPTOR READ\n" +
                            "  Device: %s\n" +
                            "  Service: %s\n" +
                            "  Characteristic: %s\n" +
                            "  Descriptor: %s\n" +
                            "  Status: %d\n" +
                            "  Value: %s",
                            timestamp, deviceAddress, serviceUuid, characteristicUuid, 
                            descriptorUuid, status, bytesToHex(value)));
                    break;
                case COMPACT:
                    Log.d(TAG, String.format("[%s] DESC_READ %s: %s (status=%d) (%s)",
                            timestamp, shortenUuid(descriptorUuid), bytesToHex(value), 
                            status, deviceAddress));
                    break;
                case JSON:
                    logJson("descriptor_read", createDescriptorOperationJson(timestamp, deviceAddress,
                            serviceUuid, characteristicUuid, descriptorUuid, "READ", value, status));
                    break;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Log GATT descriptor write
     */
    public void logDescriptorWrite(String deviceAddress, String serviceUuid, String characteristicUuid,
                                   String descriptorUuid, byte[] value, int status) {
        if (!shouldTrace(deviceAddress, serviceUuid, characteristicUuid)) {
            return;
        }
        
        lock.readLock().lock();
        try {
            String timestamp = timestampFormat.format(new Date());
            
            switch (config.getProfile()) {
                case VERBOSE:
                    Log.d(TAG, String.format("[%s] DESCRIPTOR WRITE\n" +
                            "  Device: %s\n" +
                            "  Service: %s\n" +
                            "  Characteristic: %s\n" +
                            "  Descriptor: %s\n" +
                            "  Status: %d\n" +
                            "  Value: %s",
                            timestamp, deviceAddress, serviceUuid, characteristicUuid, 
                            descriptorUuid, status, bytesToHex(value)));
                    break;
                case COMPACT:
                    Log.d(TAG, String.format("[%s] DESC_WRITE %s: %s (status=%d) (%s)",
                            timestamp, shortenUuid(descriptorUuid), bytesToHex(value), 
                            status, deviceAddress));
                    break;
                case JSON:
                    logJson("descriptor_write", createDescriptorOperationJson(timestamp, deviceAddress,
                            serviceUuid, characteristicUuid, descriptorUuid, "WRITE", value, status));
                    break;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get telemetry logger
     */
    public TelemetryLogger getTelemetryLogger() {
        return telemetryLogger;
    }
    
    /**
     * Get voice tracer
     */
    public VoiceTracer getVoiceTracer() {
        return voiceTracer;
    }
    
    /**
     * Update device context
     */
    public void updateDeviceContext(String deviceAddress, String deviceName) {
        DeviceContext context = new DeviceContext(deviceAddress, deviceName);
        deviceContexts.put(deviceAddress, context);
    }
    
    // Helper methods
    
    private boolean shouldTrace(String deviceAddress, String serviceUuid, String characteristicUuid) {
        if (!config.isEnabled()) {
            return false;
        }
        if (!config.shouldTraceDevice(deviceAddress)) {
            return false;
        }
        if (!config.shouldTraceService(serviceUuid.toLowerCase())) {
            return false;
        }
        if (!config.shouldTraceCharacteristic(characteristicUuid.toLowerCase())) {
            return false;
        }
        return true;
    }
    
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(bytes.length, 32); i++) {
            if (i > 0) sb.append(" ");
            sb.append(String.format("%02X", bytes[i]));
        }
        if (bytes.length > 32) {
            sb.append(" ... (").append(bytes.length).append(" bytes)");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String shortenUuid(String uuid) {
        if (uuid == null || uuid.length() < 8) {
            return uuid;
        }
        // Return first 8 characters for brevity (e.g., "0000180f" from "0000180f-0000-1000-8000-00805f9b34fb")
        return uuid.substring(0, 8);
    }
    
    private JSONObject createGattOperationJson(String timestamp, String address, String serviceUuid,
                                              String characteristicUuid, String operation, 
                                              byte[] value, int status) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("service_uuid", serviceUuid);
            json.put("characteristic_uuid", characteristicUuid);
            json.put("operation", operation);
            json.put("status", status);
            json.put("value_hex", bytesToHex(value));
            json.put("value_length", value != null ? value.length : 0);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createDescriptorOperationJson(String timestamp, String address, String serviceUuid,
                                                    String characteristicUuid, String descriptorUuid,
                                                    String operation, byte[] value, int status) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("service_uuid", serviceUuid);
            json.put("characteristic_uuid", characteristicUuid);
            json.put("descriptor_uuid", descriptorUuid);
            json.put("operation", operation);
            json.put("status", status);
            json.put("value_hex", bytesToHex(value));
            json.put("value_length", value != null ? value.length : 0);
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
    
    private void logTracerStart() {
        String timestamp = timestampFormat.format(new Date());
        Log.i(TAG, String.format("[%s] GATT Node Tracer initialized (Profile: %s, Enabled: %s)",
                timestamp, config.getProfile(), config.isEnabled()));
    }
    
    /**
     * Device context holder
     */
    private static class DeviceContext {
        final String address;
        final String name;
        
        DeviceContext(String address, String name) {
            this.address = address;
            this.name = name;
        }
    }
}
