package com.zstart.keycomm;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.zstart.vrapp.FastBitmapDrawable;
import com.zstart.vrapp.util.APKUtil;
import com.zstart.vrapp.util.BitmapUtil;

/**
 * Created by xmh on 2017/11/23.
 */

public class SelfIcon {
    public class AppIconInfo
    {
        public int width;
        public int height;
        public byte[] iconsource;
    }
    private Activity m_context;
    private PackageManager pm;
    private boolean isOver;
    private AppIconInfo m_AppIconInfo;
    public SelfIcon(Activity context)
    {
        m_context = context;
        pm = context.getPackageManager();

    }

    public void GetApplicationIcon()
    {
        isOver = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_AppIconInfo = getAppInfo();
                isOver = true;
            }
        }).run();
    }
    private AppIconInfo getAppInfo()
    {
        AppIconInfo info = new AppIconInfo();
        Drawable drawable = m_context.getApplicationInfo().loadIcon(pm);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        info.width = width;
        info.height = height;
        info.iconsource = APKUtil.getBytesFromDrawable(new FastBitmapDrawable(BitmapUtil.createIconBitmap(drawable,m_context)));
        Log.d("getApplicationIcon", width+","+height);
        return info;
    }
}
