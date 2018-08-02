package com.zstart.action.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.zstart.action.util.LogUtil;

import java.util.List;

public class WifiAccessPoint implements Comparable<WifiAccessPoint> {
    private static final String TAG = "_IDS_UWifiAccessPoint";

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

    private int mRssi;
    private int mLevel;
    private int mSecurity;
    private Context mContext;
    private PskType mPskType = PskType.UNKNOWN;
    private String mSSID;
    private String mBSSID;
    private int mNetworkId;
    private WifiInfo mWifiInfo;
    private DetailedState mState;
    private WifiConfiguration mConfig;
    private ScanResult mScanResult;
    private boolean mWpsAvailable = false;

    public WifiAccessPoint(Context context) {
        mContext = context;
    }

    public WifiAccessPoint(Context context, WifiConfiguration config) {
        mContext = context;
        loadConfig(config);
    }

    public WifiAccessPoint(Context context, ScanResult result) {
        mContext = context;
        loadResult(result);
    }

    public int getRssi() {
        return mRssi;
    }

    public String getSSID() {
        return mSSID;
    }

    public String getBSSID() {
        return mBSSID;
    }

    public int getDisableReason() {
        if (mConfig == null) {
            return -1;
        }
        return -1;
    }

    public int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }

//    public String getLevelString(Context context){
//        Resources resources = context.getResources();
//         String[] signal = resources.getStringArray(R.array.wifi_signal);
//        return signal[getLevel()];
//    }

    public String getSpeed() {
        return mWifiInfo.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS;
    }

    public int getSecurity() {
        return mSecurity;
    }

    public int getConfigureState() {
        if (mConfig == null) return -1;
        return mConfig.status;
    }

    public DetailedState getDetailState() {
        return mState;
    }

    public int getNetworkId() {
        return mNetworkId;
    }

    public WifiConfiguration getDefaultWifiConfiguration() {
        return mConfig;
    }

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        /// M: support wapi psk/cert @{
        /*tingkui
        if (config.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
            return SECURITY_WAPI_PSK;
        }

        if (config.allowedKeyManagement.get(KeyMgmt.WAPI_CERT)) {
            return SECURITY_WAPI_CERT;
        }*/

        if (config.wepTxKeyIndex >= 0 && config.wepTxKeyIndex < config.wepKeys.length
                && config.wepKeys[config.wepTxKeyIndex] != null) {
            return SECURITY_WEP;
        }
        ///@}
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

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

    /*
    public String getSecurityString(boolean concise) {
        Context context = mContext;
        switch(mSecurity) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                    context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (mPskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                            context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                            context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                            context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                    context.getString(R.string.wifi_security_wep);
            case SECURITY_WAPI_PSK:
             
                return context.getString(R.string.wifi_security_wapi_psk);
            case SECURITY_WAPI_CERT:
             
                return context.getString(R.string.wifi_security_wapi_certificate);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }*/

    private static PskType getPskType(ScanResult result) {
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


    private void loadConfig(WifiConfiguration config) {
        mSSID = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        mBSSID = config.BSSID;
        mSecurity = getSecurity(config);
        mNetworkId = config.networkId;
        mRssi = Integer.MAX_VALUE;
        mState = null;
        mConfig = config;
    }

    private void loadResult(ScanResult result) {
        mSSID = result.SSID;
        mBSSID = result.BSSID;
        mSecurity = getSecurity(result);
        mWpsAvailable = mSecurity != SECURITY_EAP && result.capabilities.contains("WPS");
        if (mSecurity == SECURITY_PSK)
            mPskType = getPskType(result);
        mNetworkId = -1;
        mRssi = result.level;
        mScanResult = result;
        mState = null;
    }

    public ScanResult getmScanResult() {
        return mScanResult;
    }

    public int compareTo(WifiAccessPoint ap) {

        WifiAccessPoint other = ap;
        // Active one goes first.
        if (mWifiInfo != null && other.mWifiInfo == null) return -1;
        if (mWifiInfo == null && other.mWifiInfo != null) return 1;
        // Reachable one goes before unreachable one.
        if (mRssi != Integer.MAX_VALUE && other.mRssi == Integer.MAX_VALUE) return -1;
        if (mRssi == Integer.MAX_VALUE && other.mRssi != Integer.MAX_VALUE) return 1;
        // Configured one goes before unconfigured one.
        if ((mNetworkId ^ other.mNetworkId) < 0) {
            return (mNetworkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
//        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
//        if (difference != 0) {
//            return difference;
//        }

        ///M: sort by security
//        int securityDiff = other.mSecurity - mSecurity;
//        if (securityDiff != 0) {
//            return securityDiff;
//        }

        // Sort by ssid.
        return mSSID.compareToIgnoreCase(other.mSSID);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WifiAccessPoint)) return false;
        return (this.compareTo((WifiAccessPoint) other) == 0);
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mWifiInfo != null) result += 13 * mWifiInfo.hashCode();
        result += 19 * mRssi;
        result += 23 * mNetworkId;
        result += 29 * mSSID.hashCode();
        return result;
    }

    public boolean update(ScanResult result) {

        if (mSSID.equals(result.SSID) && mSecurity == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                int oldLevel = getLevel();
                mRssi = result.level;
                if (getLevel() != oldLevel) {
                    //notifyChanged();
                }
                mBSSID = result.BSSID;
            }
            // This flag only comes from scans, is not easily saved in config
            if (mSecurity == SECURITY_PSK) {
                mPskType = getPskType(result);
            }
            return true;
        }
        return false;
    }

    public void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;

        if ((info != null && mNetworkId != -1
                && mNetworkId == info.getNetworkId())) {
            LogUtil.d("wifi:WifiAccessPoint Update state:" + info.getSSID());
            reorder = (mWifiInfo == null);
            mRssi = info.getRssi();
            mWifiInfo = info;
            mState = state;
        } else if (mWifiInfo != null) {
            reorder = true;
            mWifiInfo = null;
            mState = null;
        }
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mWifiInfo;
    }

    static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public void generateOpenNetworkConfig() {
        if (mSecurity != SECURITY_NONE)
            throw new IllegalStateException();
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = convertToQuotedString(mSSID);
        mConfig.BSSID = mBSSID;
        mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
    }

    public WifiConfiguration getApConfiguration(Context context, WifiAccessPoint ap) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config.BSSID.equals(ap.getBSSID())) {
                    return config;
                }
            }
        }
        return null;
    }

    public String toString() {
        return "SSID = " + getSSID() + " " +
                "BSSID = " + getBSSID() + " " +
                "configureState =" + getConfigureState() + " " +
                "detailState = " + getDetailState() + " " +
                "security = " + getSecurity() + " ";
    }
}
