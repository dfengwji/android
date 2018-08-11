package com.zstart.zps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.zstart.action.util.LogUtil;

public class SystemReceiver extends BroadcastReceiver {

    public SystemReceiver(){
        LogUtil.d("SystemReceiver: init!!!!");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Toast.makeText(context,action, Toast.LENGTH_SHORT).show();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            LogUtil.d("System onReceive: ACTION_BOOT_COMPLETED");
        }
    }

}
