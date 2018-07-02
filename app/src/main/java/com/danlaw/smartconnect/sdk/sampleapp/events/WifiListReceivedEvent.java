package com.danlaw.smartconnect.sdk.sampleapp.events;

import java.util.ArrayList;

public class WifiListReceivedEvent {
    public ArrayList<String> wifiList;

    public WifiListReceivedEvent(ArrayList<String> wifiList) {
        this.wifiList = wifiList;
    }
}
