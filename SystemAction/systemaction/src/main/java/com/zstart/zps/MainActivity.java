package com.zstart.zps;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Window;
import android.widget.Toast;

import com.unity3d.player.UnityPlayerActivity;
import com.zstart.action.AppActionProxy;
import com.zstart.action.util.LogUtil;
import com.zstart.action.util.SystemUtil;

import java.io.File;

public class MainActivity extends UnityPlayerActivity {
    private AppActionProxy actionProxy;

   @Override
   protected void onCreate (Bundle savedInstanceState)
   {
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       super.onCreate(savedInstanceState);

       getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy
       mUnityPlayer.requestFocus();
       LogUtil.d("activity:onCreate ....");
       actionProxy = new AppActionProxy();
       actionProxy.setContext(this);
   }

   public String getSN(){
       String sn = SystemUtil.getSerialNumber(this);
       LogUtil.d("activity:get sn = "+ sn);
       return sn;
   }

    public String getMacAddress(){
        String mac = "";
        LogUtil.d("activity:get mac = "+ mac);
        return mac;
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
}
