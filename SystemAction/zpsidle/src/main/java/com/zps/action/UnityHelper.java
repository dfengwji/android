package com.zps.action;

import android.content.Context;

import com.zstart.action.AppActionProxy;
import com.zstart.action.util.SystemUtil;

public class UnityHelper {
    private static AppActionProxy actionProxy;
    private static Context rootContext;
    public static void init(Context context){
        rootContext = context;
        actionProxy = new AppActionProxy();
        actionProxy.setContext(context);
    }

    public static void installApp(String path,String pkg){
        actionProxy.install(path, pkg,"com.zps.idle");
    }

    public static void runApp(String pkg){
        actionProxy.run(pkg);
    }

    public void shutdown(){
        SystemUtil.shutDown();
    }

    public void reboot(){
        SystemUtil.reboot(rootContext);
    }
}
