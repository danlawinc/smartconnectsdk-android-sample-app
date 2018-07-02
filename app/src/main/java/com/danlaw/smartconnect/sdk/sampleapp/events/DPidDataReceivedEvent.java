package com.danlaw.smartconnect.sdk.sampleapp.events;

import java.util.HashMap;

public class DPidDataReceivedEvent {
    public int responseCode;
    public int DPid;
    public HashMap<Integer, Object> PID_Data;
}
