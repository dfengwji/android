package com.zstart.action.constant;

public enum ActionState {
    Unknown(0),
    Ready(1),
    Begin(2),
    Update(3),
    Pause(4),
    Resume(5),
    Success(6),
    Complete(7),
    Checking(8),
    Installing(9),
    Failed(10),
    Exit(11),
    Waiting(12);

    private int code;

    ActionState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
