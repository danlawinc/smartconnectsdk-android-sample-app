package com.danlaw.smartconnect.sdk.sampleapp.events;

/**
 * This GreenRobot event <code>DPidRegisteredEvent</code> is fired when the Gateway calls back with the
 * EPid registration confirmation
 */
public class DPidRegisteredEvent {
    public int responseCode;
    public int dPid;
}
