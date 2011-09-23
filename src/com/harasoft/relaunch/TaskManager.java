package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.harasoft.relaunch.ResultsActivity.FLSimpleAdapter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaskManager extends Activity {
    final String                  TAG = "TaskManager";

    ReLaunchApp                   app;
    SharedPreferences             prefs;

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
    long                          CPUUsage;

    List<Integer>                 taskPids = new ArrayList<Integer>();
    List<Integer>                 servPids = new ArrayList<Integer>();
    ListView                      lv_t;
    TAdapter                      adapter_t;
    ListView                      lv_s;
    SAdapter                      adapter_s;

    // Process info
    static class PInfo {
        PInfo() { prev = 0; curr = 0; usage = 0; mem = 0; }
        long    prev;
        long    curr;
        long    usage;
        int     mem;
        String  name;
        String  extra;
    }
    HashMap<Integer, PInfo> pinfo = new HashMap<Integer, PInfo>();

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
        List<Integer>                 newTask = new ArrayList<Integer>();
        List<Integer>                 newServ = new ArrayList<Integer>();
        HashMap<Integer, PInfo>       newPinfo = new HashMap<Integer, PInfo>();

        /*
         * New tasks list
         */
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l1 = am.getRunningAppProcesses();
        if (l1 != null)
        {
            int[] pids = new int[l1.size()];
            int[] imps = new int[l1.size()];
            for (int i=0; i<l1.size(); i++)
                pids[i] = l1.get(i).pid;
            Debug.MemoryInfo[] mis = am.getProcessMemoryInfo(pids);
            for (int i=0; i<l1.size(); i++)
            {
                ActivityManager.RunningAppProcessInfo pi = l1.get(i);
                String is = "";
                switch (imps[i])
                {
                case ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
                    is = "RUNNING IN BACKGROUND";
                    break;
                case ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY:
                    is = "NOT ACTIVE";
                    break;
                case ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
                    is = "RUNNING IN FOREGROUND";
                    break;
                case ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE:
                    is = "SERVICE";
                    break;
                case ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE:
                    is = "VISIBLE";
                    break;
                }
            
                PInfo p = new PInfo();
                p.name = pi.processName;
                p.extra = is;
                p.mem = mis[i].otherPrivateDirty + mis[i].nativePrivateDirty + mis[i].dalvikSharedDirty;
                newPinfo.put(pids[i], p);
                newTask.add(pids[i]);
            }
        }
        
        /*
         * New services list
         */
        List<ActivityManager.RunningServiceInfo> l2 = am.getRunningServices(1024);
        if(l2 != null){
            int[] pids = new int[l2.size()];
            for(int i=0;i<l2.size();++i)
                pids[i] = l2.get(i).pid;
            Debug.MemoryInfo[] mis = am.getProcessMemoryInfo(pids);

            for(int i=0;i<l2.size();++i)
            {
                ActivityManager.RunningServiceInfo si = l2.get(i);
                String fs = "";
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_FOREGROUND) != 0)
                    fs = fs.equals("") ? "FOREGROUND" : (fs + ",FOREGROUND");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS) != 0)
                    fs = fs.equals("") ? "PERSISTENT" : (fs + ",PERSISTENT");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_STARTED) != 0)
                    fs = fs.equals("") ? "STARTED" : (fs + ",STARTED");
                if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_SYSTEM_PROCESS) != 0)
                    fs = fs.equals("") ? "SYSTEM" : (fs + ",SYSTEM");

                PInfo p = new PInfo();
                p.name = si.clientPackage;
                p.extra = fs;
                p.mem = mis[i].otherPrivateDirty + mis[i].nativePrivateDirty + mis[i].dalvikSharedDirty;
                newPinfo.put(pids[i], p);
                newServ.add(pids[i]);
            }
        }

        /*
         * Merge new pinfo list with the old one
         */
        for (Integer k : pinfo.keySet())
            if (!newPinfo.containsKey(k))
                pinfo.remove(k);
        for (Integer k : pinfo.keySet())
        {
            PInfo n = newPinfo.get(k);
            pinfo.get(k).mem = n.mem;
        }
        for (Integer k : newPinfo.keySet())
            if (!pinfo.containsKey(k))
                pinfo.put(k, newPinfo.get(k));

        /*
         *  Update CPU usage
         *  ----------------
         */
        getCPU();
        for (Integer k : pinfo.keySet())
            pinfo.get(k).curr = getCPU(k);
 
        if (cpuTotal_prev != 0  &&  cpuIdle_prev != 0)
        {
            // CPU total
            long deltaTotal =  cpuTotal - cpuTotal_prev;
            long deltaIdle = cpuIdle - cpuIdle_prev;
            CPUUsage = (deltaTotal != 0) ? (100 * (deltaTotal - deltaIdle)) / deltaTotal : 0;

            // Per process
            for (Integer k : pinfo.keySet())
            {
                PInfo p = pinfo.get(k);
                if (p.prev != 0)
                    p.usage = (deltaTotal != 0) ? (100 * (p.curr - p.prev)) / deltaTotal : 0;
                pinfo.put(k, p);
            }
        }
        
        // Save current values as previous
        for (Integer k : pinfo.keySet())
            pinfo.get(k).prev = pinfo.get(k).curr;
        cpuTotal_prev = cpuTotal;
        cpuIdle_prev = cpuIdle;
        
        taskPids = newTask;
        servPids = newServ;
        adapter_t.notifyDataSetChanged();
        adapter_s.notifyDataSetChanged();

        //// DEBUG+++++
        //Log.d(TAG, "---------------");
        //Log.d(TAG, "CPU usage: " + CPUUsage + "%");
        //Log.d(TAG, "--- Tasks:");
        //for (Integer pid : taskPids)
        //{
        //    PInfo pi = pinfo.get(pid);
        //    Log.d(TAG, pi.name + " " + pi.extra + " CPU:" + pi.usage + "% MEM:" + pi.mem + "K");
        //}
        //Log.d(TAG, "--- Services:");
        //for (Integer pid : taskPids)
        //{
        //    PInfo pi = pinfo.get(pid);
        //    Log.d(TAG, pi.name + " " + pi.extra + " CPU:" + pi.usage + "% MEM:" + pi.mem + "K");
        //}
        //// DEBUG-----

        /*
         *  Next CPUUpdate_proc call
         */
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

    static class ViewHolder {
        TextView  tv1;
        TextView  tv2;
        ImageView iv;
    }
    class TAdapter extends ArrayAdapter<Integer> {
        TAdapter(Context context, int resource)
        {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return taskPids.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View       v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.results_item, null);
                holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.res_dname);
                holder.tv2 = (TextView) v.findViewById(R.id.res_fname);
                holder.iv  = (ImageView) v.findViewById(R.id.res_icon);
                v.setTag(holder);
            }
            else
                holder = (ViewHolder) v.getTag();

            TextView  tv1 = holder.tv1;
            TextView  tv2 = holder.tv2;
            ImageView iv = holder.iv;

            Integer item = taskPids.get(position);
            PInfo   pi = pinfo.get(item);
            if (item != null) {
                tv2.setText(pi.name);
                tv1.setText(Html.fromHtml(pi.extra + ", CPU: <b>" + pi.usage + "%</b>, Mem: <b>" + pi.mem + "K</b>"), TextView.BufferType.SPANNABLE);
            }
            return v;
        }
    }

    class SAdapter extends ArrayAdapter<Integer> {
        SAdapter(Context context, int resource)
        {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return servPids.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View       v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.results_item, null);
                holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.res_dname);
                holder.tv2 = (TextView) v.findViewById(R.id.res_fname);
                holder.iv  = (ImageView) v.findViewById(R.id.res_icon);
                v.setTag(holder);
            }
            else
                holder = (ViewHolder) v.getTag();

            TextView  tv1 = holder.tv1;
            TextView  tv2 = holder.tv2;
            ImageView iv = holder.iv;

            Integer item = servPids.get(position);
            PInfo   pi = pinfo.get(item);
            if (item != null) {
                tv2.setText(pi.name);
                tv1.setText(Html.fromHtml(pi.extra + ", CPU: <b>" + pi.usage + "%</b>, Mem: <b>" + pi.mem + "K</b>"), TextView.BufferType.SPANNABLE);
            }
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.taskmanager_layout);

        lv_t = (ListView) findViewById(R.id.tasks_lv);
        adapter_t = new TAdapter(this, R.layout.results_item);
        lv_t.setAdapter(adapter_t);
        lv_s = (ListView) findViewById(R.id.services_lv);
        adapter_s = new SAdapter(this, R.layout.results_item);
        lv_s.setAdapter(adapter_s);

        startCPUUpdate();
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