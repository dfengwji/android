package com.zstart.install;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.zstart.action.common.ICallBack;
import com.zstart.action.model.AppInfo;
import com.zstart.action.util.LogUtil;

import java.util.List;

public class InstallCallback implements ICallBack {

    private Context context;
    public InstallCallback(Context ctx){
        context = ctx;
    }
    @Override
    public void installBegin(String pkg) {

    }

    @Override
    public void installFailed(String pkg, String reason) {

    }

    @Override
    public void installSuccess(String pkg) {
        LogUtil.w("install success...");
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void uninstallFailed(String pkg, String reason) {

    }

    @Override
    public void uninstallSuccess(String pkg) {

    }

    @Override
    public void checkSuccess(String pkg) {

    }

    @Override
    public void checkFailed(String pkg, String reason) {

    }

    @Override
    public void runFailed(String pkg, String reason) {

    }

    @Override
    public void loadComplete(List<AppInfo> list) {

    }

    @Override
    public void connectSuccess(String ssid) {

    }

    @Override
    public void connectFailed(String reason) {

    }
}
