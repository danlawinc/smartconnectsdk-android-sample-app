package com.example.danlaw.demo.events;

/**
 * This GreenRobot event <code>EPidDataReceivedEvent</code> is fired when the Gateway calls back with the
 * Event Pid Data
 */
public class EPidDataReceivedEvent {
    public int responseCode;
    public int EPid;
    public Object data;
}
