package com.example.danlaw.demo.events;

/**
 * This GreenRobot event <code>BasicDataReceivedEvent</code> is fired when the Gateway calls back with the
 * Basic Data
 */
public class BasicDataReceivedEvent {
    public int responseCode;
    public int pid;
    public Object data;
}
