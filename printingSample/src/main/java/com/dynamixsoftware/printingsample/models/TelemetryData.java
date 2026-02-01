package com.dynamixsoftware.printingsample.models;

/**
 * Telemetry data model
 * Captures comprehensive device metrics and sensor data
 */
public class TelemetryData extends TraceEvent {
    private float cpuUsage;
    private long memoryUsed;
    private long memoryTotal;
    private int batteryLevel;
    private String batteryStatus;
    private String networkType;
    private boolean wifiConnected;
    private boolean cellularConnected;
    private String locationLat;
    private String locationLon;
    private float accelerometerX;
    private float accelerometerY;
    private float accelerometerZ;
    private float gyroX;
    private float gyroY;
    private float gyroZ;

    public TelemetryData() {
        super("TELEMETRY_DATA");
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public boolean isWifiConnected() {
        return wifiConnected;
    }

    public void setWifiConnected(boolean wifiConnected) {
        this.wifiConnected = wifiConnected;
    }

    public boolean isCellularConnected() {
        return cellularConnected;
    }

    public void setCellularConnected(boolean cellularConnected) {
        this.cellularConnected = cellularConnected;
    }

    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat;
    }

    public String getLocationLon() {
        return locationLon;
    }

    public void setLocationLon(String locationLon) {
        this.locationLon = locationLon;
    }

    public float getAccelerometerX() {
        return accelerometerX;
    }

    public void setAccelerometerX(float accelerometerX) {
        this.accelerometerX = accelerometerX;
    }

    public float getAccelerometerY() {
        return accelerometerY;
    }

    public void setAccelerometerY(float accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public float getAccelerometerZ() {
        return accelerometerZ;
    }

    public void setAccelerometerZ(float accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public float getGyroX() {
        return gyroX;
    }

    public void setGyroX(float gyroX) {
        this.gyroX = gyroX;
    }

    public float getGyroY() {
        return gyroY;
    }

    public void setGyroY(float gyroY) {
        this.gyroY = gyroY;
    }

    public float getGyroZ() {
        return gyroZ;
    }

    public void setGyroZ(float gyroZ) {
        this.gyroZ = gyroZ;
    }

    @Override
    public String toString() {
        return "TelemetryData{" +
                "cpuUsage=" + cpuUsage +
                ", memoryUsed=" + memoryUsed +
                ", memoryTotal=" + memoryTotal +
                ", batteryLevel=" + batteryLevel +
                ", batteryStatus='" + batteryStatus + '\'' +
                ", networkType='" + networkType + '\'' +
                ", wifiConnected=" + wifiConnected +
                ", cellularConnected=" + cellularConnected +
                ", locationLat='" + locationLat + '\'' +
                ", locationLon='" + locationLon + '\'' +
                ", accelerometer=[" + accelerometerX + "," + accelerometerY + "," + accelerometerZ + "]" +
                ", gyro=[" + gyroX + "," + gyroY + "," + gyroZ + "]" +
                ", eventId='" + getEventId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
