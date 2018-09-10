package com.zstart.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.zstart.action.util.LogUtil;

public class InstallBroadReceiver extends BroadcastReceiver {

    public InstallBroadReceiver(){
        LogUtil.d("BroadReceiver: init!!!!");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            LogUtil.d("System onReceive: ACTION_BOOT_COMPLETED");
            try {
                PackageManager packageManager = context.getPackageManager();
                Intent tmp = packageManager.getLaunchIntentForPackage(MainActivity.SELF_PACKAGE);
                tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.getApplicationContext().startActivity(tmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
