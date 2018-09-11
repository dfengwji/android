package com.zstart.install;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import com.zstart.action.AppActionProxy;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class InstallService extends Service {
    public class InstallBinder extends Binder {
        public InstallService getService(){
            return InstallService.this;
        }
    }
    private BroadcastReceiver castReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packName = "";
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    packName = intent.getDataString().split(":")[1];
                    LogUtil.v("install:action apk had added:" + packName);
                    if(packName.equals(appPackage)){
                        isInstalling = false;
                        runApp(appPackage);
                    }
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    packName = intent.getDataString().split(":")[1];
                    LogUtil.v("install:action apk had removed:" + packName);
                    break;
                case Intent.ACTION_PACKAGE_CHANGED:
                    LogUtil.v("install:action apk had changed:" + intent.getDataString());

                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    LogUtil.v("install:action apk had replaced:" + intent.getDataString());

                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    LogUtil.v("install: device startup !!!!");
                    break;
                case Intent.ACTION_SHUTDOWN:
                    LogUtil.v("install: device close !!!!");
                    break;
            }
        }
    };

    private InstallBinder binder = new InstallBinder();
    private String appPath = "";
    private String appPackage = "";
    private AppActionProxy actionProxy;
    private boolean isInstalling = false;
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        LogUtil.d("service create!!!!");

        IntentFilter tmpFilter = new IntentFilter();
        tmpFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        tmpFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        tmpFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        tmpFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        //tmpFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        //tmpFilter.addAction(Intent.ACTION_SHUTDOWN);
        tmpFilter.addDataScheme("package");
        this.registerReceiver(castReceiver, tmpFilter);

        actionProxy = new AppActionProxy();
        actionProxy.setContext(this.getApplicationContext());
        //actionProxy.setCallBack(new InstallCallback(this));

        setActivityController();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateTimerTask();
            }
        };
        Timer timer = new Timer(false);
        timer.schedule(timerTask, 1000, 5000);
    }

    private void updateTimerTask() {
        if(isInstalling){
            return;
        }
        String pkg = getForegroundPackage(this.getApplicationContext());
        if(pkg != null && !pkg.equals("com.zps.idle")){
            LogUtil.i("foreground pkg = "+pkg);
            runApp("com.zps.idle");
        }
    }

    public static String getForegroundPackage(Context context){
        final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        try {
            Field field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            if(field == null){
                return "";
            }
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo app : appList) {
                if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                    Integer state = field.getInt(app);
                    if (state == PROCESS_STATE_TOP) {
                        currentInfo = app;
                        break;
                    }
                }
            }
            if (currentInfo != null) {
                return currentInfo.processName;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        LogUtil.d("service start!!!!path = " + appPath);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtil.d("service destroy!!!!");
        super.onDestroy();
    }

    public void installApp(String pkg,String path){
        if(pkg == null || path == null){
            return;
        }
        appPath = path;
        appPackage = pkg;
        LogUtil.d("service: try install app ....pkg = "+ appPackage + ";path = " + path);
        isInstalling = actionProxy.install(path, pkg, MainActivity.SELF_PACKAGE);
    }

    public void runApp(String pkg){
        LogUtil.v("service:run apk ..." + pkg);
        try {
            PackageManager packageManager = this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.getApplicationContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setActivityController() {
        try {
            Class<?> cActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method mGetDefault = cActivityManagerNative.getMethod("getDefault");
            Object oActivityManagerNative = mGetDefault.invoke(null,null);
            Method mSetActivityController = cActivityManagerNative.getMethod(
                    "setActivityController",
                    Class.forName("android.app.IActivityController"));
            mSetActivityController.invoke(oActivityManagerNative,
                    new ActivityController());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
