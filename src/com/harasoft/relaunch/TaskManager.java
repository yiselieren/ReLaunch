package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;

public class TaskManager extends Activity {
    final String                  TAG = "TaskManager";
    final int                     CPUUpdate_i = 5000;
    long                          cpuTotal = 0;
    long                          cpuTotal_prev = 0;
    long                          cpuIdle = 0;
    long                          cpuIdle_prev = 0;
    boolean                       CPUUpdate_active = false;
    Handler                       CPUUpdate_h = new Handler();
    Runnable                      CPUUpdate = new Runnable() {
         public void run() { CPUUpdate_proc(); }
    };

    // Process info
    
    
    // Process map by pids
    static class Pids {
        Pids() { prev = 0; curr = 0; usage = 0; }
        long  prev;
        long  curr;
        long  usage;
    }
    HashMap<Integer, Pids> pidInfo = new HashMap<Integer, Pids>();
    long                   CPUUsage;

    private void stopCPUUpdate()
    {
        CPUUpdate_active = false;
        CPUUpdate_h.removeCallbacks(CPUUpdate);
    }

    private void startCPUUpdate()
    {
        if (!CPUUpdate_active)
        {
            CPUUpdate_active = true;
            CPUUpdate_proc();
        }
    }

    private void CPUUpdate_proc()
    {
        // Update current
        getCPU();
        for (Integer pid : pidInfo.keySet())
            pidInfo.get(pid).curr = getCPU(pid);
        if (cpuTotal_prev != 0  &&  cpuIdle_prev != 0)
        {
            // CPU total
            long deltaTotal =  cpuTotal - cpuTotal_prev;
            long deltaIdle = cpuIdle - cpuIdle_prev;
            CPUUsage = (deltaTotal != 0) ? (100 * (deltaTotal - deltaIdle)) / deltaTotal : 0;
            //Log.d(TAG, "CPU usage: " + CPUUsage + "%");

            // Per process
            for (Integer pid : pidInfo.keySet())
            {
                long curr = getCPU(pid);
                long prev = pidInfo.get(pid).prev;
                if (prev != 0)
                {
                    long us = (deltaTotal != 0) ? (100 * (curr - prev)) / deltaTotal : 0;
                    pidInfo.get(pid).usage = us;
                    //Log.d(TAG, "PID:" + pid + ", Usage: " + us + "%");
                }
            }
        }
        
        // Save current values as previous
        for (Integer pid : pidInfo.keySet())
            pidInfo.get(pid).prev = pidInfo.get(pid).curr;
        cpuTotal_prev = cpuTotal;
        cpuIdle_prev = cpuIdle;

          // Next CPUUpdate_proc call
        if (CPUUpdate_active)
            CPUUpdate_h.postDelayed(CPUUpdate, CPUUpdate_i);
    }
    
    private void getCPU()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();     

            String[] toks = load.split("\\s+");

            cpuIdle =  Long.parseLong(toks[4]) + Long.parseLong(toks[5]);
            cpuTotal = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + cpuIdle
                + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
        }
        catch(IOException ex) { }
    }
    private long getCPU(int pid)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();     

            String[] toks = load.split("\\s+");
            return Long.parseLong(toks[13]) + Long.parseLong(toks[14]);
        }
        catch(IOException ex) { return 0; }  
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        Log.d(TAG, "-------------------- getRunningAppProcesses in details:");
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        if (l != null)
        {
            int[] pids = new int[l.size()];
            int[] imps = new int[l.size()];
            String[] names = new String[l.size()];
            for (int i=0; i<l.size(); i++)
            {
                ActivityManager.RunningAppProcessInfo pi = l.get(i);
                pids[i] = pi.pid;
                names[i] = pi.processName;
                imps[i] = pi.importance;
                //Log.d(TAG, "Process=" + pi.processName + " PID:" + pi.pid);
                //int pid = android.os.Process.getUidForName("com.android.email");
                //android.os.Process.killProcess(pid);
                
                pidInfo.put(pi.pid, new Pids());
            }
            
            Debug.MemoryInfo[] mis = am.getProcessMemoryInfo(pids);
            for (int i=0; i<l.size(); i++)
            {
                 //pinfo[i].name = names[i];
                 //pinfo[i].pid = pids[i];
                 //pinfo[i].mi = mis[i];
                 String is = "Unknown";
                 switch (imps[i])
                 {
                 case ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
                     is = "IMPORTANCE_BACKGROUND";
                     break;
                 case ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY:
                     is = "IMPORTANCE_EMPTY";
                     break;
                 case ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
                     is = "IMPORTANCE_FOREGROUND";
                     break;
                 case ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE:
                     is = "IMPORTANCE_SERVICE";
                     break;
                 case ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE:
                     is = "IMPORTANCE_VISIBLE";
                     break;
                 }
            
                 Log.d(TAG, "Process=" + names[i] + " -- PID:" + pids[i] + " -- MEM=" + mis[i].otherPrivateDirty
                         + " / " + mis[i].nativePrivateDirty + " / " + mis[i].dalvikPrivateDirty
                         + " = " + (mis[i].otherPrivateDirty + mis[i].nativePrivateDirty + mis[i].dalvikSharedDirty )
                         + " OR " + (mis[i].otherPss + mis[i].nativePss + mis[i].dalvikPss)
                         + " Importance: " + imps[i] + "(" + is + ")");
            }
        }
  
        Log.d(TAG, "-------------------- getRunningServices:");
        List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(1024);
        if(list != null){
            for(int i=0;i<list.size();++i)
            {
                ActivityManager.RunningServiceInfo si = list.get(i);
                String fs = "";
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_FOREGROUND) != 0)
                    fs = fs.equals("") ? "FOREGROUND" : (fs + ",FOREGROUND");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS) != 0)
                    fs = fs.equals("") ? "PERSISTENT" : (fs + ",PERSISTENT");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_STARTED) != 0)
                    fs = fs.equals("") ? "STARTED" : (fs + ",STARTED");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_SYSTEM_PROCESS) != 0)
                    fs = fs.equals("") ? "SYSTEM" : (fs + ",SYSTEM");

                Log.d(TAG, "Service:" + si.service.getClassName() + ", " + fs + " foreground=" + si.foreground);
                pidInfo.put(si.pid, new Pids());

            }
        }
        startCPUUpdate();
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCPUUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCPUUpdate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startCPUUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCPUUpdate();
   }

    @Override
    protected void onStart() {
        super.onStart();
        startCPUUpdate();
  }

    @Override
    protected void onStop() {
        super.onStop();
        stopCPUUpdate();
 }
    
    
}