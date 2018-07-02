package com.danlaw.smartconnect.sdk.sampleapp.events;

import java.util.HashMap;

/**
 * Created by kannanl on 8/24/2016.
 */
public class DPidDataReceivedEvent {
    public int responseCode;
    public int DPid;
    public HashMap<Integer, Object> PID_Data;
}
