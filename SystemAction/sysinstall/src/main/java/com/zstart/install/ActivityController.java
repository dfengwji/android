package com.zstart.install;

import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;

import com.zstart.action.util.LogUtil;

public class ActivityController extends IActivityController.Stub {
    @Override
    public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
        LogUtil.w("activityStarting..."+pkg);
        return false;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        LogUtil.w("activityResuming..."+pkg);
        return false;
    }

    @Override
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
        LogUtil.w("appCrashed..."+processName);
        return false;
    }

    @Override
    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        return 0;
    }

    @Override
    public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
        return 0;
    }

    @Override
    public int systemNotResponding(String msg) throws RemoteException {
        return 0;
    }
}
