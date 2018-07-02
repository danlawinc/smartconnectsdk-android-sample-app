package com.danlaw.smartconnect.sdk.sampleapp.events;

import java.util.ArrayList;

/**
 * Created by akshitg on 1/17/2018.
 */

public class WifiListReceivedEvent {
    public ArrayList<String> wifiList;

    public WifiListReceivedEvent(ArrayList<String> wifiList) {
        this.wifiList = wifiList;
    }
}
