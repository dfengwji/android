package com.zstart.action.constant;

public enum ActionType {
    Unknown(0),
    Install(1),
    Uninstall(2),
    Run(3),
    Check(4),
    Load(5),
    WIFI(6);
    private int code;

    ActionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
