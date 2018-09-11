package com.zstart.install;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.unity3d.player.UnityPlayerActivity;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends UnityPlayerActivity {
    public static final String SELF_PACKAGE = "com.zps.install";

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

        LogUtil.setTag("ZStartInstall");
        LogUtil.d("activity:onCreate ....");
        startService();
    }

    @Override
    protected void onResume(){
        super.onResume();
        String packageName = getIntent().getStringExtra("pkg");
        String filePath = getIntent().getStringExtra("path");
        LogUtil.d("activity:resume ....pkg = "+ packageName + ";path = " + filePath);
        if(service != null){
            service.installApp(packageName, filePath);
        }
    }

    private InstallService service;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            InstallService.InstallBinder myBinder = (InstallService.InstallBinder)binder;
            service = myBinder.getService();
            if(service != null){
                service.runApp("com.zps.idle");
                String packageName = getIntent().getStringExtra("pkg");
                String filePath = getIntent().getStringExtra("path");
                if(packageName != null && filePath != null){
                    service.installApp(packageName, filePath);
                }
            }
            LogUtil.i("onServiceConnected..."+service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i("onServiceConnected");
        }
    };

    private void startService(){
        Intent intent = new Intent(this, InstallService.class);
        this.getApplicationContext().startService(intent);
        bindService(intent,conn,BIND_AUTO_CREATE);
        this.startService(intent);
    }
}
