package com.example.danlaw.demo.events;

/**
 * Created by akshitg on 1/18/2018.
 */

public class WifiRequestEvent {
    public boolean isWifiAddRequest;
    public boolean isCompletedSuccessfully;

    public WifiRequestEvent(boolean isWifiAddRequest, boolean isCompletedSuccessfully) {
        this.isWifiAddRequest = isWifiAddRequest;
        this.isCompletedSuccessfully = isCompletedSuccessfully;
    }
}
