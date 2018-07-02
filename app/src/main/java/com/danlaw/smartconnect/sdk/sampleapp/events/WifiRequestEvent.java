package com.danlaw.smartconnect.sdk.sampleapp.events;

public class WifiRequestEvent {
    public boolean isWifiAddRequest;
    public boolean isCompletedSuccessfully;

    public WifiRequestEvent(boolean isWifiAddRequest, boolean isCompletedSuccessfully) {
        this.isWifiAddRequest = isWifiAddRequest;
        this.isCompletedSuccessfully = isCompletedSuccessfully;
    }
}
