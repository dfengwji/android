package com.idealens.system.status;

/**
 * @file:com.idealens.system.status.DeviceInfoType.java
 * @authoer:huangzizhen
 * @date:2017/12/19
 * @version:V1.0
 */

public enum DeviceBaseType {
    Unknown(0),
    CPU(1),
    GPU(2),
    Version_Android(3),
    Version_OS(4),
    Module(5),
    Address_Mac(6),
    Address_IP(7),
    Address_Bluetooth(8),
    SN(9),
    Memory(10),
    Storage_Self(11),
    Storage_TF(12),
    Storage_OTG(13);
    private int code;

    DeviceBaseType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DeviceBaseType switchCode(int code) {
        for (DeviceBaseType type : DeviceBaseType.values()) {
            if (type.getCode() == code)
                return type;
        }
        return DeviceBaseType.Unknown;
    }
}
