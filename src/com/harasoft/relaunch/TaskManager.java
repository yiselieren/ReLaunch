package com.harasoft.relaunch;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;

public class TaskManager extends Activity {
    final String                  TAG = "TaskManager";

    static class PInfo {
        String           name;
        int              pid;
        Debug.MemoryInfo mi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        /*
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        if (l != null)
        {
            int[] pids = new int[l.size()];
            String[] names = new String[l.size()];
            for (int i=0; i<l.size(); i++)
            {
                ActivityManager.RunningAppProcessInfo pi = l.get(i);
                pids[i] = pi.pid;
                names[i] = pi.processName;
                //Log.d(TAG, "Process=" + pi.processName + " PID:" + pi.pid);
                //int pid = android.os.Process.getUidForName("com.android.email");
                //android.os.Process.killProcess(pid);
            }
            
            PInfo[] pinfo = new PInfo[l.size()];
            Debug.MemoryInfo[] mis = am.getProcessMemoryInfo(pids);
            for (int i=0; i<l.size(); i++)
            {
                //pinfo[i].name = names[i];
                //pinfo[i].pid = pids[i];
                //pinfo[i].mi = mis[i];
                
                //Log.d(TAG, "Process=" + pinfo[i].name + " PID:" + pinfo[i].pid + " MEM=" + pinfo[i].mi.getTotalPrivateDirty() + "K");
                Log.d(TAG, "Process=" + names[i] + " PID:" + pids[i] + " MEM=" + mis[i].getTotalPss() + "K");
            }
        }
        */
        {
            List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
            if(list != null) {
                for(int i=0;i<list.size();++i)
                    Log.d(TAG, "Running app:" + list.get(i).processName );
            }
        }

        {
            List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(1024);
            if(list != null){
                for(int i=0;i<list.size();++i)
                    Log.d(TAG, "Service:" + list.get(i).service.getClassName());
            }
        }

        {
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1024);
            if(list != null){
                for(int i=0;i<list.size();++i)
                    Log.d(TAG, "Running task: " + list.get(i).baseActivity.toString() + "; State: " + list.get(i).description);
            }
        }
        
        finish();
    }
}