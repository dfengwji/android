package com.zstart.action.common;

import com.zstart.action.model.AppInfo;

import java.util.List;

public interface ICallBack {
    void installBegin(String pkg);
    void installFailed(String pkg,String reason);
    void installSuccess(String pkg);
    void uninstallFailed(String pkg,String reason);
    void uninstallSuccess(String pkg);
    void checkSuccess(String pkg);
    void checkFailed(String pkg,String reason);
    void runFailed(String pkg,String reason);
    void loadComplete(List<AppInfo> list);
    void connectSuccess(String ssid);
    void connectFailed(String reason);
}
