package com.example.danlaw.demo.events;

/**
 * This GreenRobot event <code>OBDDevicesFoundEvent</code> is fired when the Gateway calls back with the
 * newly found OBD devices as part of its scanning
 */
public class OBDDevicesFoundEvent {
    public String deviceName;
    public String deviceAddress;
}
