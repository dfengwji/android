package com.zstart.action.wifi;

import android.net.wifi.ScanResult;

public class WifiConstant {
    enum PskType {
        UNKNOWN, WPA, WPA2, WPA_WPA2
    }

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_WPA_PSK = 3;
    public static final int SECURITY_WPA2_PSK = 4;
    public static final int SECURITY_EAP = 5;
    public static final int SECURITY_WAPI_PSK = 6;
    public static final int SECURITY_WAPI_CERT = 7;

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WAPI-PSK")) {
            /// M:  WAPI_PSK
            return SECURITY_WAPI_PSK;
        } else if (result.capabilities.contains("WAPI-CERT")) {
            /// M: WAPI_CERT
            return SECURITY_WAPI_CERT;
        } else if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            return PskType.UNKNOWN;
        }
    }
}
