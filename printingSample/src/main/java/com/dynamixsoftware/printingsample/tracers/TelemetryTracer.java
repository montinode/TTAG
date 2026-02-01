package com.dynamixsoftware.printingsample.tracers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.dynamixsoftware.printingsample.models.TelemetryData;

import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TelemetryTracer - Comprehensive device telemetry capture
 * 
 * Features:
 * - CPU usage, memory consumption, battery status
 * - Network connectivity metrics (WiFi, cellular)
 * - Device sensor data (accelerometer, gyro, location)
 * - Process and app lifecycle events
 * - Real-time metrics aggregation
 */
public class TelemetryTracer extends BaseTracer implements SensorEventListener {
    
    private ScheduledExecutorService scheduler;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private BroadcastReceiver batteryReceiver;
    private boolean receiverRegistered = false;
    
    private float[] lastAccelValues = new float[3];
    private float[] lastGyroValues = new float[3];
    private int lastBatteryLevel = -1;
    private String lastBatteryStatus = "UNKNOWN";
    
    public TelemetryTracer(Context context) {
        super(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }
    
    @Override
    protected String getTracerName() {
        return "TELEMETRY_TRACER";
    }
    
    @Override
    protected void onStart() {
        // Register sensor listeners
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                logEvent("INFO", "Accelerometer registered");
            } else {
                logEvent("WARNING", "Accelerometer not available");
            }
            
            if (gyroscope != null) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                logEvent("INFO", "Gyroscope registered");
            } else {
                logEvent("WARNING", "Gyroscope not available");
            }
        }
        
        // Register battery receiver
        setupBatteryReceiver();
        
        // Start periodic telemetry collection
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                collectTelemetry();
            }
        }, 0, 5, TimeUnit.SECONDS);
        
        logEvent("INFO", "Telemetry collection started (5s interval)");
    }
    
    @Override
    protected void onStop() {
        // Unregister sensor listeners
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        
        // Unregister battery receiver
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver);
                receiverRegistered = false;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering battery receiver", e);
            }
        }
        
        // Stop scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
    
    private void setupBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                
                lastBatteryLevel = (int) ((level / (float) scale) * 100);
                lastBatteryStatus = getBatteryStatusString(status);
                
                logEvent("BATTERY_UPDATE", "Level: " + lastBatteryLevel + "%, Status: " + lastBatteryStatus);
            }
        };
        
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(batteryReceiver, filter);
        }
        receiverRegistered = true;
    }
    
    private void collectTelemetry() {
        if (!isRunning.get()) return;
        
        TelemetryData data = new TelemetryData();
        
        // CPU usage
        data.setCpuUsage(getCpuUsage());
        
        // Memory info
        ActivityManager.MemoryInfo memoryInfo = getMemoryInfo();
        if (memoryInfo != null) {
            data.setMemoryUsed(memoryInfo.totalMem - memoryInfo.availMem);
            data.setMemoryTotal(memoryInfo.totalMem);
        }
        
        // Battery info
        data.setBatteryLevel(lastBatteryLevel);
        data.setBatteryStatus(lastBatteryStatus);
        
        // Network info
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null) {
            data.setNetworkType(networkInfo.getTypeName());
            data.setWifiConnected(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected());
            data.setCellularConnected(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected());
        }
        
        // Sensor data
        data.setAccelerometerX(lastAccelValues[0]);
        data.setAccelerometerY(lastAccelValues[1]);
        data.setAccelerometerZ(lastAccelValues[2]);
        data.setGyroX(lastGyroValues[0]);
        data.setGyroY(lastGyroValues[1]);
        data.setGyroZ(lastGyroValues[2]);
        
        logTraceEvent(data);
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRunning.get()) return;
        
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelValues, 0, 3);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, lastGyroValues, 0, 3);
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    private float getCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();
            
            String[] toks = load.split(" +");
            long idle = Long.parseLong(toks[4]);
            long total = 0;
            for (int i = 1; i < toks.length; i++) {
                if (!toks[i].isEmpty()) {
                    total += Long.parseLong(toks[i]);
                }
            }
            
            // Simple approximation
            return total > 0 ? ((total - idle) / (float) total) * 100f : 0f;
        } catch (Exception e) {
            Log.e(TAG, "Error reading CPU usage", e);
            return 0f;
        }
    }
    
    private ActivityManager.MemoryInfo getMemoryInfo() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                return memoryInfo;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting memory info", e);
        }
        return null;
    }
    
    private NetworkInfo getNetworkInfo() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                return cm.getActiveNetworkInfo();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting network info", e);
        }
        return null;
    }
    
    private String getBatteryStatusString(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "CHARGING";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "DISCHARGING";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "FULL";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "NOT_CHARGING";
            default:
                return "UNKNOWN";
        }
    }
}
