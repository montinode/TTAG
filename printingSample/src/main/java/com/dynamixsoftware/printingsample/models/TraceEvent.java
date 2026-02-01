package com.dynamixsoftware.printingsample.models;

import java.util.UUID;

/**
 * Base class for all trace events
 * Provides common fields for event correlation and timestamping
 */
public class TraceEvent {
    private final String eventId;
    private final long timestamp;
    private final String eventType;
    private String data;

    public TraceEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.eventType = eventType;
        this.data = "";
    }

    public TraceEvent(String eventType, String data) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.eventType = eventType;
        this.data = data;
    }

    public String getEventId() {
        return eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TraceEvent{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
