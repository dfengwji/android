package com.zps.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zstart.action.AppActionProxy;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

import java.io.File;
import java.lang.reflect.Method;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity{
    private AppActionProxy actionProxy;
    private int brightness = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window _window = getWindow();
        _window.setFormat(PixelFormat.RGBX_8888);
        _window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        _window.setAttributes(params);
        super.onCreate(savedInstanceState);

        mUnityPlayer.requestFocus();
        LogUtil.d("main activity:onCreate ....");
        actionProxy = new AppActionProxy();
        actionProxy.setContext(this);
        brightness = getScreenBrightness();
        SystemUtil.clearMemory(this);
    }

    public String getSN(){
        String sn = SystemUtil.getSerialNumber(this);
        LogUtil.d("activity:get sn = "+ sn);
        return sn;
    }

    public String getMacAddress(){
        String mac = SystemUtil.getMacAddress(this);
        LogUtil.d("activity:get mac = "+ mac);
        return mac;
    }

    public void runApp(String pkg){
        actionProxy.run(pkg);
    }

    public void install(String path,String pkg){
        actionProxy.install(path, pkg,"com.zps.idle");
    }

    public void installSelf(String path){
        LogUtil.d("start install activity...");
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.zps.install");
        intent.putExtra("pkg","com.zps.idle");
        intent.putExtra("path",path);
        this.getApplicationContext().startActivity(intent);
    }

    public void toast(final String message){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public long getStorage(int type){
        File file = Environment.getDataDirectory();
        if(type == 0){
            return getSFSize(file.getPath(), false);
        }else{
            return getSFSize(file.getPath(), true);
        }
    }

    private long getSFSize(String path, boolean free) {
        LogUtil.w("action: getSFSize path = " + path + ", available = " + free);
        StatFs sf = new StatFs(path);
        return free ? sf.getAvailableBytes() : sf.getTotalBytes();
    }

    public void shutdown(){
        SystemUtil.shutDown();
    }

    public void startup(String time){

    }

    public void reboot(){
        SystemUtil.reboot(this);
    }

    private void setBrightness(int brightness) {
        try{
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
            Window localWindow = getWindow();
            WindowManager.LayoutParams params = localWindow.getAttributes();
            params.screenBrightness = brightness / 255.0F;
            localWindow.setAttributes(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setStartupTime() {
        try {
            /*AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_RTC_AUTO_POWER_ON),0);
            am.setExact(7, "", pi);*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getScreenBrightness(){
        int screenBrightness=255;
        try{
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return screenBrightness;
    }

    private void setNavigationBarVisibility(boolean visible) {
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sleep(){
        LogUtil.w("action: ready to sleep!!!");
        PowerManager powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        try {
            Method[] array = powerManager.getClass().getMethods();
            for(int i = 0;i < array.length;i++){
                LogUtil.w(array[i].getName());
            }
            Class<?> classType = powerManager.getClass();
            Method method = classType.getMethod("goToSleep",long.class);
            method.invoke(powerManager, SystemClock.uptimeMillis());
        } catch (Exception e){
            e.printStackTrace();
        }
        /*ComponentName componentName = new ComponentName(this, SystemAdminReceiver.class);
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean isAdmin = manager.isAdminActive(componentName);

        if(isAdmin){
            manager.lockNow();
        }else{
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra (DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "lockNow");
            startActivityForResult(intent, 0);
        }*/
    }

    public void wakeup(){
        LogUtil.w("action: ready to awake!!!");
        PowerManager powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        try {
            Class<?> classType = powerManager.getClass();
            Method method = classType.getMethod("wakeUp");
            method.invoke(powerManager);
        } catch (Exception e){
            e.printStackTrace();
        }
        /*try {
            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isInteractive()) {
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                wakeLock.acquire();
                wakeLock.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }
}
