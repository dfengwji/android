package com.zstart.action.util;

import android.content.Context;

import com.unity3d.player.UnityPlayer;
import com.zps.idle.R;
import com.zstart.action.constant.ActionState;
import com.zstart.action.constant.ActionType;
import com.zstart.action.constant.ExceptionState;

public class UnityUtil {

    public static void sendActionNotify(ActionType type, ActionState status, String message) {
        if (UnityPlayer.currentActivity != null) {
            String tmp = type.getCode() + "|" + status.getCode() + "|" + message;
            if (status != ActionState.Update)
                LogUtil.v("sendActionNotify.....param = " + tmp);
            UnityPlayer.UnitySendMessage("AndroidNotifyReceiver", "OnActionUpdate", tmp);
        }
    }

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
