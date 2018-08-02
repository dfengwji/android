package com.idealens.system.status;

/**
 * @file:com.idealens.system.status.DeviceStateType.java
 * @authoer:huangzizhen
 * @date:2017/12/7
 * @version:V1.0
 */

public enum DeviceStateType {
    Unknown(0),
    Battery_Joystick(1),
    Battery_Glasses(2),
    GlassesCharging(3),
    WifiStrength(4),
    Storage_Self(5),
    Storage_TF(6),
    Storage_OTG(7),
    Earphone(8),
    Memory(9),
    Bluetooth(10),
    Server(11);

    private int code;

    DeviceStateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DeviceStateType switchCode(int code) {
        for (DeviceStateType type : DeviceStateType.values()) {
            if (type.getCode() == code)
                return type;
        }
        return DeviceStateType.Unknown;
    }
}
