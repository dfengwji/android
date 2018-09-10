package com.zstart.action.util;

import android.content.Context;

import com.zstart.action.constant.ExceptionState;
import com.zstart.action.R;

public class ExceptionUtil {

    public static String getExceptionTip(Context context, ExceptionState reason) {
        String tip = "";
        if (reason == ExceptionState.Wifi_Error_PSW)
            tip = context.getString(R.string.wifi_error_psw);
        else if (reason == ExceptionState.Wifi_Error_SSID)
            tip = context.getString(R.string.wifi_error_ssid);
        else if (reason == ExceptionState.WifiError)
            tip = context.getString(R.string.wifi_error_ssid);
        return tip;
    }
}
