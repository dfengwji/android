package com.zstart.action.constant;

public enum ExceptionState {
    None(0),
    LowBattery(1),
    MemoryInsufficient(2),
    InstallFailed(3),
    DownloadFailed(4),
    MD5CheckFailed(5),
    DownloadWaiting(6),
    WifiError(7),
    NetworkError(8),
    ServerError(9),
    Wifi_Error_SSID(10),
    Wifi_Error_PSW(11),
    UnknownError(12);

    private int code;

    ExceptionState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
