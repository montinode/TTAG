# GATT Node Tracer Module

A comprehensive Bluetooth GATT operations tracer for Android applications that intercepts and logs all GATT operations, telemetry data, and voice utterances.

## Overview

The GATT Node Tracer module provides a complete solution for monitoring and logging Bluetooth Low Energy (BLE) GATT operations in Android applications. It implements a callback interceptor pattern with thread-safe logging and supports various filtering and output format options.

## Features

### 1. GATT Operation Interception
- **Read Operations**: Captures all GATT characteristic read operations with full details
- **Write Operations**: Logs all write operations with data payload
- **Notifications**: Monitors characteristic change notifications
- **Descriptor Operations**: Tracks descriptor read/write operations

### 2. Telemetry Logging
- **Connection State**: Tracks device connection lifecycle (CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED)
- **RSSI Monitoring**: Logs signal strength measurements
- **MTU Changes**: Records Maximum Transmission Unit modifications
- **PHY Updates**: Monitors physical layer configuration changes

### 3. Voice Operation Tracing
- **Voice Utterances**: Captures voice command events with text and metadata
- **Voice Commands**: Logs received audio commands with data size
- **Voice Recognition**: Records speech recognition results with confidence scores
- **Voice Streams**: Monitors audio stream events and data flow

### 4. Filtering Support
The tracer supports command-line style filtering flags:
- **`-f` (filter)**: Filter by service or characteristic UUID
- **`-s` (scope)**: Scope tracing to specific device by MAC address
- **`-p` (profile)**: Select trace output profile (VERBOSE, COMPACT, JSON)

## Architecture

The module consists of four main classes:

```
gatt/
├── GattNodeTracer.java      - Main tracer with GATT interception logic
├── GattTraceConfig.java     - Configuration and filtering options
├── TelemetryLogger.java     - Device telemetry capture and logging
└── VoiceTracer.java         - Voice operation interception
```

## Usage

### Basic Usage

```java
// Initialize with default configuration
GattTraceConfig config = GattTraceConfig.createDefault();
GattNodeTracer tracer = new GattNodeTracer(config);

// Log GATT operations
tracer.logGattRead(deviceAddress, serviceUuid, characteristicUuid, value, status);
tracer.logGattWrite(deviceAddress, serviceUuid, characteristicUuid, value, status);
tracer.logGattNotification(deviceAddress, serviceUuid, characteristicUuid, value);

// Log telemetry
tracer.getTelemetryLogger().logConnectionState(address, name, ConnectionState.CONNECTED);
tracer.getTelemetryLogger().logRssi(address, -60);
tracer.getTelemetryLogger().logMtuChange(address, 512);

// Log voice operations
tracer.getVoiceTracer().logVoiceUtterance(address, "utterance-1", "Hello World", VoiceEventType.START);
tracer.getVoiceTracer().logVoiceCommand(address, "PRINT_COMMAND", audioData);
```

### Advanced Configuration

```java
// Configure with filters and custom profile
GattTraceConfig config = new GattTraceConfig.Builder()
    // Filter by Battery Service UUID
    .addServiceFilter("0000180f-0000-1000-8000-00805f9b34fb")
    // Filter by specific device
    .addDeviceFilter("AA:BB:CC:DD:EE:FF")
    // Use verbose output
    .setProfile(GattTraceConfig.TraceProfile.VERBOSE)
    .build();

GattNodeTracer tracer = new GattNodeTracer(config);
```

### Trace Profiles

#### VERBOSE Profile
Detailed multi-line output with all available information:
```
[2026-02-01 03:52:59.123] GATT READ
  Device: AA:BB:CC:DD:EE:FF
  Service: 0000180f-0000-1000-8000-00805f9b34fb
  Characteristic: 00002a19-0000-1000-8000-00805f9b34fb
  Status: 0
  Value: [64]
```

#### COMPACT Profile (Default)
Single-line concise output:
```
[2026-02-01 03:52:59.123] READ 0000180f/00002a19: [64] (status=0) (AA:BB:CC:DD:EE:FF)
```

#### JSON Profile
Structured JSON output for machine parsing:
```json
{
  "event_type": "gatt_read",
  "timestamp": "2026-02-01 03:52:59.123",
  "device_address": "AA:BB:CC:DD:EE:FF",
  "service_uuid": "0000180f-0000-1000-8000-00805f9b34fb",
  "characteristic_uuid": "00002a19-0000-1000-8000-00805f9b34fb",
  "operation": "READ",
  "status": 0,
  "value_hex": "[64]",
  "value_length": 1
}
```

## Integration Example

See `PrintServiceFragment.java` for a complete integration example. Key integration points:

```java
public class PrintServiceFragment extends Fragment {
    private GattNodeTracer gattTracer;
    
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        
        // Initialize tracer
        GattTraceConfig config = GattTraceConfig.createDefault();
        gattTracer = new GattNodeTracer(config);
        
        // Use tracer in callbacks
        printingSdk.startService(new IServiceCallback() {
            @Override
            public void onServiceConnected() {
                gattTracer.getTelemetryLogger().logConnectionState(
                    "PRINTING_SERVICE", "PrintingSdk", 
                    TelemetryLogger.ConnectionState.CONNECTED);
            }
        });
    }
}
```

## Thread Safety

All logging operations are thread-safe and can be called from any thread. The `GattNodeTracer` class uses a `ReentrantReadWriteLock` to ensure concurrent access is properly synchronized.

## Performance Considerations

- **Filtering**: Use UUID and device filters to reduce logging overhead
- **Profile Selection**: COMPACT and JSON profiles are more efficient than VERBOSE
- **Buffer Management**: Large data payloads (>32 bytes) are automatically truncated with size indication
- **Timestamp Generation**: Timestamps are generated once per log entry for consistency

## Output Channels

All logs are written to Android's LogCat with appropriate log levels:
- **Info (I)**: GATT operations, connection state changes, voice events
- **Debug (D)**: Descriptor operations, stream events
- **Error (E)**: JSON serialization errors

Use LogCat filters to view specific tracer output:
```bash
adb logcat -s GattNodeTracer GattTelemetry GattVoiceTracer
```

## Common Use Cases

### 1. Debugging Bluetooth Print Operations
```java
GattTraceConfig config = new GattTraceConfig.Builder()
    .setProfile(GattTraceConfig.TraceProfile.VERBOSE)
    .build();
GattNodeTracer tracer = new GattNodeTracer(config);

// Trace all Bluetooth printer operations
```

### 2. Monitoring Connection Quality
```java
// Log RSSI periodically to monitor signal strength
tracer.getTelemetryLogger().logRssi(deviceAddress, rssi);
tracer.getTelemetryLogger().logMtuChange(deviceAddress, mtu);
```

### 3. Analyzing Voice Commands
```java
// Track voice command processing
tracer.getVoiceTracer().logVoiceCommand(address, command, audioData);
tracer.getVoiceTracer().logVoiceRecognition(address, recognizedText, confidence);
```

### 4. Production Logging with Filtering
```java
// Only log specific service and device in production
GattTraceConfig config = new GattTraceConfig.Builder()
    .addServiceFilter("your-service-uuid")
    .addDeviceFilter("specific-device-mac")
    .setProfile(GattTraceConfig.TraceProfile.JSON)
    .build();
```

## Limitations

- The tracer logs to LogCat; consider implementing log rotation for long-running applications
- Large data payloads are truncated to 32 bytes in hex output (full length is still reported)
- Voice tracing features require integration with actual voice processing systems
- GATT operation logging requires manual integration points in your BLE callback code

## Future Enhancements

Possible future improvements:
- File-based logging with rotation
- Remote logging to analytics services
- Automatic GATT callback wrapping
- Performance metrics and statistics
- Log export functionality
- Custom formatters and log processors

## License

This module is part of the PrintingSample application and follows the same license terms.
