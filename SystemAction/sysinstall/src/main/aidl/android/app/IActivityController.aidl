// IActivityController.aidl
package android.app;

import android.content.Intent;

interface IActivityController {
   boolean activityStarting(in Intent intent, String pkg);
   boolean activityResuming(String pkg);
   boolean appCrashed(String processName, int pid,
                String shortMsg, String longMsg,
                long timeMillis, String stackTrace);
   int appEarlyNotResponding(String processName, int pid, String annotation);
   int appNotResponding(String processName,int pid,String processStats);
   int systemNotResponding(String msg);
}
