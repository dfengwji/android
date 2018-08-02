package com.zstart.action.wifi;


import android.content.Context;


/**
 * Created by i11 on 15-6-23.
 */
public class WifiAccessApInfo {

    private WifiAccessPoint mAccessAp;
    private int mLevel;
    private String mBSSID;
    private int mSecurity;
    private String mSpeed;
    private int mNetId;
    private String mName;
    private String mAddress;

    public WifiAccessApInfo(Context context, WifiAccessPoint ap) {

        mAccessAp = ap;
        mLevel = mAccessAp.getLevel();
        mBSSID = mAccessAp.getBSSID();
        mSecurity = mAccessAp.getSecurity();
        mSpeed = mAccessAp.getSpeed();
        mNetId = mAccessAp.getNetworkId();
        mName = mAccessAp.getSSID();
    }

    public String getSpeed() {
        return mSpeed;
    }

    public String getBSSID() {
        return mBSSID;
    }

    public int getSecurity() {
        return mSecurity;
    }

    public int getLevel() {
        return mLevel;
    }

    public int getNetId() {
        return mNetId;
    }

    public String getName() {
        return mName;
    }
}
