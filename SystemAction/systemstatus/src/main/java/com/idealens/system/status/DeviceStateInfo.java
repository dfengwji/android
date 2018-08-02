package com.idealens.system.status;

import java.net.PortUnreachableException;

/**
 * @file:com.idealens.system.status.DeviceStateInfo.java
 * @authoer:huangzizhen
 * @date:2017/12/7
 * @version:V1.0
 */

public final class DeviceStateInfo {
    public int glassesBattery = -1;
    public int joystickBattery = -1;
    public boolean isCharging = false;
    public long memory = 0;
    public long internalStorage = 0;
    public long externalStorage = 0;
    public long otgStorage = 0;
    public int wifiStrength = 0;
    public int bluetooth = 0;
    public boolean earphone = false;

    public boolean updateBluetooth(int percent) {
        if (bluetooth == percent)
            return false;
        bluetooth = percent;
        return true;
    }

    public boolean updateGlassesBattery(int percent) {
        if (glassesBattery == percent) {
            return false;
        }
        glassesBattery = percent;
        return true;
    }

    public boolean updateWifiStrength(int strength) {
        if (wifiStrength == strength) {
            return false;
        }
        wifiStrength = strength;
        return true;
    }

    public boolean updateJoystickBattery(int percent) {
        if (joystickBattery == percent) {
            return false;
        }
        joystickBattery = percent;
        return true;
    }

    public boolean updateMemory(long size) {
        if (memory == size)
            return false;
        memory = size;
        return true;
    }

    public boolean updateInternalStorage(long size) {
        if (internalStorage == size)
            return false;
        internalStorage = size;
        return true;
    }

    public boolean updateExternalStorage(long size) {
        if (externalStorage == size)
            return false;
        externalStorage = size;
        return true;
    }

    @Override
    public String toString() {
        return "device state info:joystick = " + joystickBattery
                + ";battery = " + glassesBattery + ";charging = " + isCharging;
    }
}
