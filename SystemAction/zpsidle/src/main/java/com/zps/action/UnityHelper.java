package com.zps.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.zstart.action.AppActionProxy;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

public class UnityHelper {
    private AppActionProxy actionProxy;
    private Context rootContext;
    public void init(Context context){
        LogUtil.d("init unity helper!!!");
        rootContext = context;
        actionProxy = new AppActionProxy();
        actionProxy.setContext(context);
    }

    public void installApp(String path,String pkg){
        actionProxy.install(path, pkg,"com.zps.idle");
    }

    public void runApp(String pkg){
        LogUtil.d("try run app that pkg = " + pkg);
        try{
            PackageManager packageManager = rootContext.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            rootContext.getApplicationContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown(){
        SystemUtil.shutDown();
    }

    public void reboot(){
        SystemUtil.reboot(rootContext);
    }
}
