package com.dynamixsoftware.printingsample.gatt;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for GATT tracer filters and options.
 * Supports command-line style flags for filtering and scoping.
 */
public class GattTraceConfig {
    
    /**
     * Trace profile types
     */
    public enum TraceProfile {
        VERBOSE,    // Detailed logs with all information
        COMPACT,    // Concise logs with essential information
        JSON        // Structured JSON format
    }
    
    private final Set<String> serviceUuidFilters;
    private final Set<String> characteristicUuidFilters;
    private final Set<String> deviceMacFilters;
    private final TraceProfile profile;
    private final boolean enabled;
    
    private GattTraceConfig(Builder builder) {
        this.serviceUuidFilters = builder.serviceUuidFilters;
        this.characteristicUuidFilters = builder.characteristicUuidFilters;
        this.deviceMacFilters = builder.deviceMacFilters;
        this.profile = builder.profile;
        this.enabled = builder.enabled;
    }
    
    /**
     * Check if a service UUID should be traced
     */
    public boolean shouldTraceService(String uuid) {
        return serviceUuidFilters.isEmpty() || serviceUuidFilters.contains(uuid);
    }
    
    /**
     * Check if a characteristic UUID should be traced
     */
    public boolean shouldTraceCharacteristic(String uuid) {
        return characteristicUuidFilters.isEmpty() || characteristicUuidFilters.contains(uuid);
    }
    
    /**
     * Check if a device MAC address should be traced
     */
    public boolean shouldTraceDevice(String macAddress) {
        return deviceMacFilters.isEmpty() || deviceMacFilters.contains(macAddress);
    }
    
    public TraceProfile getProfile() {
        return profile;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Builder for GattTraceConfig
     */
    public static class Builder {
        private Set<String> serviceUuidFilters = new HashSet<>();
        private Set<String> characteristicUuidFilters = new HashSet<>();
        private Set<String> deviceMacFilters = new HashSet<>();
        private TraceProfile profile = TraceProfile.COMPACT;
        private boolean enabled = true;
        
        /**
         * Add service UUID filter (-f flag)
         */
        public Builder addServiceFilter(String uuid) {
            this.serviceUuidFilters.add(uuid.toLowerCase());
            return this;
        }
        
        /**
         * Add characteristic UUID filter (-f flag)
         */
        public Builder addCharacteristicFilter(String uuid) {
            this.characteristicUuidFilters.add(uuid.toLowerCase());
            return this;
        }
        
        /**
         * Add device MAC address filter (-s flag)
         */
        public Builder addDeviceFilter(String macAddress) {
            this.deviceMacFilters.add(macAddress.toUpperCase());
            return this;
        }
        
        /**
         * Set trace profile (-p flag)
         */
        public Builder setProfile(TraceProfile profile) {
            this.profile = profile;
            return this;
        }
        
        /**
         * Enable or disable tracing
         */
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public GattTraceConfig build() {
            return new GattTraceConfig(this);
        }
    }
    
    /**
     * Create default configuration
     */
    public static GattTraceConfig createDefault() {
        return new Builder().build();
    }
}
