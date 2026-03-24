package com.dynamixsoftware.printingsample.gatt;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Voice operation interception and tracing for Bluetooth GATT.
 * Monitors and logs all voice-related operations and events.
 */
public class VoiceTracer {
    
    private static final String TAG = "GattVoiceTracer";
    private final GattTraceConfig config;
    private final SimpleDateFormat timestampFormat;
    
    public VoiceTracer(GattTraceConfig config) {
        this.config = config;
        this.timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    }
    
    /**
     * Log voice utterance event
     */
    public void logVoiceUtterance(String deviceAddress, String utteranceId, String text, VoiceEventType eventType) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] Voice Utterance\n" +
                        "  Device: %s\n" +
                        "  Event: %s\n" +
                        "  Utterance ID: %s\n" +
                        "  Text: %s",
                        timestamp, deviceAddress, eventType, utteranceId, text));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] Voice %s: \"%s\" [%s] (%s)",
                        timestamp, eventType, text, utteranceId, deviceAddress));
                break;
            case JSON:
                logJson("voice_utterance", createVoiceUtteranceJson(timestamp, deviceAddress, utteranceId, text, eventType));
                break;
        }
    }
    
    /**
     * Log voice command received
     */
    public void logVoiceCommand(String deviceAddress, String command, byte[] audioData) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        int dataSize = (audioData != null) ? audioData.length : 0;
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] Voice Command Received\n" +
                        "  Device: %s\n" +
                        "  Command: %s\n" +
                        "  Audio Data Size: %d bytes",
                        timestamp, deviceAddress, command, dataSize));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] Voice Command: %s (%d bytes) (%s)",
                        timestamp, command, dataSize, deviceAddress));
                break;
            case JSON:
                logJson("voice_command", createVoiceCommandJson(timestamp, deviceAddress, command, dataSize));
                break;
        }
    }
    
    /**
     * Log voice recognition event
     */
    public void logVoiceRecognition(String deviceAddress, String recognizedText, float confidence) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.i(TAG, String.format("[%s] Voice Recognition\n" +
                        "  Device: %s\n" +
                        "  Text: %s\n" +
                        "  Confidence: %.2f%%",
                        timestamp, deviceAddress, recognizedText, confidence * 100));
                break;
            case COMPACT:
                Log.i(TAG, String.format("[%s] Voice Recognition: \"%s\" (%.1f%%) (%s)",
                        timestamp, recognizedText, confidence * 100, deviceAddress));
                break;
            case JSON:
                logJson("voice_recognition", createVoiceRecognitionJson(timestamp, deviceAddress, recognizedText, confidence));
                break;
        }
    }
    
    /**
     * Log voice stream event
     */
    public void logVoiceStream(String deviceAddress, VoiceStreamEvent event, int bytesProcessed) {
        if (!config.isEnabled() || !config.shouldTraceDevice(deviceAddress)) {
            return;
        }
        
        String timestamp = timestampFormat.format(new Date());
        
        switch (config.getProfile()) {
            case VERBOSE:
                Log.d(TAG, String.format("[%s] Voice Stream\n" +
                        "  Device: %s\n" +
                        "  Event: %s\n" +
                        "  Bytes Processed: %d",
                        timestamp, deviceAddress, event, bytesProcessed));
                break;
            case COMPACT:
                Log.d(TAG, String.format("[%s] Stream %s: %d bytes (%s)",
                        timestamp, event, bytesProcessed, deviceAddress));
                break;
            case JSON:
                logJson("voice_stream", createVoiceStreamJson(timestamp, deviceAddress, event, bytesProcessed));
                break;
        }
    }
    
    // JSON helper methods
    
    private JSONObject createVoiceUtteranceJson(String timestamp, String address, String utteranceId, String text, VoiceEventType eventType) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("utterance_id", utteranceId);
            json.put("text", text);
            json.put("event_type", eventType.name());
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createVoiceCommandJson(String timestamp, String address, String command, int dataSize) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("command", command);
            json.put("audio_data_size", dataSize);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createVoiceRecognitionJson(String timestamp, String address, String text, float confidence) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("recognized_text", text);
            json.put("confidence", confidence);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return new JSONObject();
        }
    }
    
    private JSONObject createVoiceStreamJson(String timestamp, String address, VoiceStreamEvent event, int bytesProcessed) {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("device_address", address);
            json.put("event", event.name());
            json.put("bytes_processed", bytesProcessed);
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
     * Voice event types
     */
    public enum VoiceEventType {
        START,
        PROGRESS,
        COMPLETE,
        ERROR,
        CANCELLED
    }
    
    /**
     * Voice stream events
     */
    public enum VoiceStreamEvent {
        STARTED,
        DATA_RECEIVED,
        BUFFER_FULL,
        STOPPED,
        ERROR
    }
}
