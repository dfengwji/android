package com.zstart.action.common;

import com.zstart.action.model.AppInfo;

import java.util.List;

public class ActionCallBack implements ICallBack {
    @Override
    public void installBegin(String pkg) {

    }

    @Override
    public void installFailed(String pkg, String reason) {

    }

    @Override
    public void installSuccess(String pkg) {

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
