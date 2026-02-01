package com.dynamixsoftware.printingsample.models;

/**
 * NFC-specific trace event
 * Captures NFC tag interactions and telemetry
 */
public class NfcEvent extends TraceEvent {
    private String tagType;
    private String uid;
    private int signalStrength;
    private String operation; // READ, WRITE, DETECT
    private String ndefMessage;
    private long operationDuration;

    public NfcEvent(String operation) {
        super("NFC_EVENT");
        this.operation = operation;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getNdefMessage() {
        return ndefMessage;
    }

    public void setNdefMessage(String ndefMessage) {
        this.ndefMessage = ndefMessage;
    }

    public long getOperationDuration() {
        return operationDuration;
    }

    public void setOperationDuration(long operationDuration) {
        this.operationDuration = operationDuration;
    }

    @Override
    public String toString() {
        return "NfcEvent{" +
                "operation='" + operation + '\'' +
                ", tagType='" + tagType + '\'' +
                ", uid='" + uid + '\'' +
                ", signalStrength=" + signalStrength +
                ", ndefMessage='" + ndefMessage + '\'' +
                ", operationDuration=" + operationDuration +
                ", eventId='" + getEventId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
