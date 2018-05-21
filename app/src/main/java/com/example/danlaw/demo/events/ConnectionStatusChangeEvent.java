package com.example.danlaw.demo.events;

/**
 * This GreenRobot event <code>ConnectionStatusChangeEvent</code> is fired when the Gateway calls back
 * with the different states of the connection changes
 */
public class ConnectionStatusChangeEvent {
    public int responseCode;
    public int connectionStatus;
}
