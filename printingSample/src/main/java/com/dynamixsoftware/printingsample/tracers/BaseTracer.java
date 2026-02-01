package com.dynamixsoftware.printingsample.tracers;

import android.content.Context;
import android.util.Log;

import com.dynamixsoftware.printingsample.models.TraceEvent;
import com.dynamixsoftware.printingsample.transmutation.MontiTransmuter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BaseTracer - Abstract base class for all tracers
 * Provides consistent lifecycle management and thread-safe logging
 */
public abstract class BaseTracer {
    
    protected static final String TAG = "MontiTracer";
    protected final Context context;
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final CopyOnWriteArrayList<TraceEvent> eventLog = new CopyOnWriteArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
    protected BaseTracer(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Start the tracer
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logEvent("START", "Tracer started");
            onStart();
        } else {
            Log.w(TAG, getTracerName() + " already running");
        }
    }
    
    /**
     * Stop the tracer
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            onStop();
            logEvent("STOP", "Tracer stopped");
        } else {
            Log.w(TAG, getTracerName() + " not running");
        }
    }
    
    /**
     * Check if tracer is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Get all logged events
     */
    public CopyOnWriteArrayList<TraceEvent> getEventLog() {
        return eventLog;
    }
    
    /**
     * Clear event log
     */
    public void clearEventLog() {
        eventLog.clear();
    }
    
    /**
     * Log an event with thread-safe operation
     */
    protected void logEvent(String eventType, String data) {
        String timestamp = dateFormat.format(new Date());
        TraceEvent event = new TraceEvent(eventType, data);
        eventLog.add(event);
        
        // Log to Android logcat
        String encoded = MontiTransmuter.formatMessage(
            getTracerName(), 
            eventType, 
            data
        );
        Log.i(TAG, encoded);
    }
    
    /**
     * Log a trace event object
     */
    protected void logTraceEvent(TraceEvent event) {
        eventLog.add(event);
        
        String encoded = MontiTransmuter.formatMessage(
            getTracerName(),
            event.getEventType(),
            event.toString()
        );
        Log.i(TAG, encoded);
    }
    
    /**
     * Get the name of this tracer
     */
    protected abstract String getTracerName();
    
    /**
     * Called when tracer is started
     */
    protected abstract void onStart();
    
    /**
     * Called when tracer is stopped
     */
    protected abstract void onStop();
    
    /**
     * Get formatted event log
     */
    public String getFormattedEventLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(MontiTransmuter.DELIMITER_START)
          .append(" ")
          .append(getTracerName())
          .append(" EVENT LOG ")
          .append(MontiTransmuter.DELIMITER_START)
          .append("\n");
        
        for (TraceEvent event : eventLog) {
            sb.append(MontiTransmuter.DELIMITER_FIELD)
              .append(" ")
              .append(dateFormat.format(new Date(event.getTimestamp())))
              .append(" - ")
              .append(event.getEventType())
              .append(": ")
              .append(event.getData())
              .append("\n");
        }
        
        sb.append(MontiTransmuter.DELIMITER_END);
        return sb.toString();
    }
}
