package com.appglue.library;

import com.appglue.engine.model.CompositeService;

import java.util.ArrayList;

public class LogItem {
    public static final int SUCCESS = 0x1;

    public static final int FILTER = 0x1;

    public static final int TRIGGER_FAIL = 0x2; // If a trigger is in the composition where it shouldn't be
    public static final int COMPONENT_FAIL = 0x3; // IF the component crashes
    public static final int MESSAGE_FAIL = 0x4; // IF the message passing fails
    public static final int NETWORK_FAIL = 0x5; // If some networking fails somewhere
    public static final int OTHER_FAIL = 0x6; // Derp
    public static final int ORCH_FAIL = 0x7; // If the orchestrator fails
    public static final int PARAM_STOP = 0x8; // If it stops based on one of the flags
    public static final int VERSION_MISMATCH = 0x9; // The component needs newer Android
    public static final int MISSING_FEATURES = 0xA; // If the device doesn't have features

    public static final int GENERIC_TRIGGER_FAIL = 0x10; // This is very much a special case

    private long id;
    private CompositeService cs;
    private long startTime;
    private long endTime;
    private String message;
    private int status;
    private ArrayList<ComponentLogItem> componentLogs;

    public LogItem(long id, CompositeService cs, long startTime, long endTime, String message, int status) {

        this.id = id;
        this.cs = cs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
        this.status = status;

        componentLogs = new ArrayList<ComponentLogItem>();
    }

    public long getID() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int success) {
        this.status = success;
    }

    public CompositeService getComposite() {
        return cs;
    }

    public void setComposite(CompositeService cs) {
        this.cs = cs;
    }

    public void addComponentLog(ComponentLogItem cli) {
        componentLogs.add(cli);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public ArrayList<ComponentLogItem> getComponentLogs() {
        return componentLogs;
    }
}
