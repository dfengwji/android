package com.zstart.install;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import com.zstart.action.AppActionProxy;
import com.zstart.action.util.LogUtil;
import java.lang.reflect.Method;

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
                        run();
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
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        LogUtil.d("install service create!!!!");

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        appPath = intent.getStringExtra("path");
        appPackage = intent.getStringExtra("pkg");
        LogUtil.d("install service start!!!!path = " + appPath);
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtil.d("install service destroy!!!!");
        super.onDestroy();
    }

    public void installApp(String pkg,String path){
        appPath = path;
        appPackage = pkg;
        LogUtil.d("install service: try install app ....pkg = "+ appPackage + ";path = " + path);
        actionProxy.install(path, pkg, MainActivity.SELF_PACKAGE);
    }

    private void run(){
        LogUtil.v("install:run apk ..." + appPackage);
        try {
            PackageManager packageManager = this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(appPackage);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.getApplicationContext().startActivity(intent);
        }catch (Exception e){

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
