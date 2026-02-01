package com.dynamixsoftware.printingsample.models;

/**
 * Telephony-specific trace event
 * Captures call, SMS, and cellular network information
 */
public class TelephonyEvent extends TraceEvent {
    private String callState; // IDLE, RINGING, OFFHOOK
    private String phoneNumber;
    private String smsContent;
    private String smsDirection; // INBOUND, OUTBOUND
    private int signalStrength;
    private String networkType;
    private String simOperator;
    private String simSerialNumber;
    private String subscriptionId;
    private long callDuration;

    public TelephonyEvent(String eventType) {
        super(eventType);
    }

    public String getCallState() {
        return callState;
    }

    public void setCallState(String callState) {
        this.callState = callState;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    public String getSmsDirection() {
        return smsDirection;
    }

    public void setSmsDirection(String smsDirection) {
        this.smsDirection = smsDirection;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getSimOperator() {
        return simOperator;
    }

    public void setSimOperator(String simOperator) {
        this.simOperator = simOperator;
    }

    public String getSimSerialNumber() {
        return simSerialNumber;
    }

    public void setSimSerialNumber(String simSerialNumber) {
        this.simSerialNumber = simSerialNumber;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public long getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(long callDuration) {
        this.callDuration = callDuration;
    }

    @Override
    public String toString() {
        return "TelephonyEvent{" +
                "callState='" + callState + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", smsDirection='" + smsDirection + '\'' +
                ", signalStrength=" + signalStrength +
                ", networkType='" + networkType + '\'' +
                ", simOperator='" + simOperator + '\'' +
                ", callDuration=" + callDuration +
                ", eventId='" + getEventId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
