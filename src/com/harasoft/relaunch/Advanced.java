package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.harasoft.relaunch.ResultsActivity.ViewHolder;
import com.harasoft.relaunch.TypesActivity.TPAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Advanced extends Activity {
    final static String           TAG = "Advanced";

    SharedPreferences             prefs;
    ReLaunchApp                   app;
    Button                        rebootBtn;
    String[]                      ingnoreFs;
    List<Info>                    infos;
    int                           myId = -1;
    
    WifiManager                   wfm;
    boolean                       wifiOn = false;
    Button                        wifiOnOff;
    Button                        wifiScan;
    List<ScanResult>              wifiNetworks;
    WiFiAdapter                   adapter;
    ListView                      lv_wifi;
    IntentFilter                  i1;
    IntentFilter                  i2;
    BroadcastReceiver             b1;
    BroadcastReceiver             b2;

    static class Info {
        String  dev;
        String  mpoint;
        String  fs;
        String  total;
        String  used;
        String  free;
        boolean ro;
        public void dump(String p)
        {
            Log.d(TAG, p + " dev=\"" + dev + "\" mpoint=\"" + mpoint + "\" fs=\"" + fs
                    + "\" used=\"" + used + " total=\"" + total
                    + "\" free=\"" + free + "\" ro=" + ro); 
        }
    }
    
    static class ViewHolder {
        TextView  tv1;
        TextView  tv2;
        TextView  tv3;
        ImageView iv;
    }
    class WiFiAdapter extends BaseAdapter {
        final Context cntx;

        WiFiAdapter(Context context)
        {
            cntx = context;
        }

        public int getCount() {
            return wifiNetworks.size();
        }

        public Object getItem(int position) {
            return wifiNetworks.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View      v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.adv_wifi_item, null);
                holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.wf_ssid);
                holder.tv2 = (TextView) v.findViewById(R.id.wf_capabilities);
                holder.tv3 = (TextView) v.findViewById(R.id.wf_other);
                holder.iv = (ImageView)v.findViewById(R.id.wf_icon);
                v.setTag(holder);
            }
            else
                holder = (ViewHolder) v.getTag();
            TextView  tv1 = holder.tv1;
            TextView  tv2 = holder.tv2;
            TextView  tv3 = holder.tv3;
            ImageView iv = holder.iv;

            final WifiInfo  winfo = wfm.getConnectionInfo();
            final ScanResult item = wifiNetworks.get(position);
            if (item != null)
            {
                tv1.setText(item.SSID);
                tv2.setText(item.capabilities);
                if (item.SSID.equals(winfo.getSSID()))
                {
                    int ipAddress = winfo.getIpAddress();
                    tv3.setText(String.format("IP: %d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff)));
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
               }
                else
                {
                    tv3.setText("Level: " + item.level);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
                }
            }
            return v;
        }
    }

    private int getMyId()
    {
        int rc = -1;

        for (String l : execFg("id"))
        {
            Pattern p = Pattern.compile("uid=(\\d+)\\(");
            Matcher m = p.matcher(l);
            if (m.find())
            {
                try {
                    rc = Integer.parseInt(m.group(1));
                } catch (NumberFormatException e) {
                    rc=-1;
                }
                if (rc != -1)
                    return rc;
            }
        }
        return rc;
    }
 
    // Read file and return result as list of strings
    private List<String> readFile(String fname)
    {
        BufferedReader br;
        String         readLine;
        List<String>   rc = new ArrayList<String>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fname)), 1000);
        } catch (FileNotFoundException e) {
            return rc;
        }
        try {
            while ((readLine = br.readLine()) != null)
                rc.add(readLine);
        } catch (IOException e) {
            try {
                br.close();
            } catch (IOException e1) { }
            return rc;
        }
        try {
            br.close();
        } catch (IOException e) { }

       
        return rc;
    }
    
    // Execute foreground command and return result as list of strings
    private List<String> execFg(String cmd)
    {
        List<String>   rc = new ArrayList<String>();
        try {
            
            Process p = Runtime.getRuntime().exec(cmd);
            try {
                DataInputStream ds = new DataInputStream(p.getInputStream());
                String line;
                while (true)
                {
                    line = ds.readLine();
                    if (line == null)
                        break;
                    rc.add(line);
                }
            } finally {
                p.destroy();
            }
        } catch (IOException e) { }
        return rc;
    }

    private void createInfo()
    {
        // Filesystem
        infos = new ArrayList<Info>();
        for (String s : readFile("/proc/mounts"))
        {
            String[] f = s.split("\\s+");
            if (f.length < 4)
                continue;
            String fs = f[2];
            String flags = f[3];
            String[] f1 = flags.split(",");
            boolean ignore = false;
            for (int i=0; i<ingnoreFs.length; i++)
                if (ingnoreFs[i].equals(fs))
                {
                    ignore = true;
                    break;
                }
            if (ignore)
                continue;
            Info in = new Info();
            in.dev = f[0];
            in.mpoint = f[1];
            in.fs = fs;
            for (int i=0; i<f1.length; i++)
                if (f1[i].equals("ro"))
                {
                    in.ro = true;
                    break;
                }
                else if (f1[i].equals("rw"))
                {
                    in.ro = false;
                    break;
                }
            in.total = "0";
            in.used = "0";
            in.free = "0";
            for (String l : execFg("df " + in.mpoint))
            {
                String[] e = l.split("\\s+");
                if (e.length < 6)
                    continue;
                in.total = e[1];
                in.used = e[3];
                in.free = e[5];
            }
            
            infos.add(in);
        }
        
        // UID
        myId = getMyId();
        
        // Wifi
        wfm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wfm.isWifiEnabled())
        {
            wifiOn = true;
            wifiNetworks = wfm.getScanResults();
        }
        else
        {
            wifiOn = false;
            wifiNetworks = new ArrayList<ScanResult>();
        }
    }
    
    private void updateWiFiOnOffButton()
    {
        if (wifiOn)
        {
            wifiOnOff.setText("Turn WiFi off");
            wifiOnOff.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.wifi_off),
                    getResources().getDrawable(android.R.drawable.ic_lock_power_off),
                    getResources().getDrawable(R.drawable.wifi_off),
                    null);
            wfm.startScan();            
            wifiOnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wfm.setWifiEnabled(false);
                }});
        }
        else
        {
            wifiOnOff.setText("Turn WiFi on");
            wifiOnOff.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.wifi_on),
                    getResources().getDrawable(android.R.drawable.ic_lock_power_off),
                    getResources().getDrawable(R.drawable.wifi_on),
                    null);                
            wifiOnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wfm.setWifiEnabled(true);
                }});
            wifiNetworks.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ingnoreFs = getResources().getStringArray(R.array.filesystems_to_ignore);

        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.advanced_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        createInfo();
        ((TextView)findViewById(R.id.results_title)).setText("Advanced functions");
        ((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }});

        // Wifi info
        lv_wifi = (ListView) findViewById(R.id.wifi_lv);
        adapter = new WiFiAdapter(this);
        lv_wifi.setAdapter(adapter);

        // Receive broadcast when scan results are available
        i1 = new IntentFilter();
        i1.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        b1 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiNetworks = wfm.getScanResults();
                adapter.notifyDataSetChanged();
            }};
        registerReceiver(b1, i1);

        // Receive broadcast when WiFi status changed
        i2 = new IntentFilter();
        i2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        b2 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiOn = wfm.isWifiEnabled();
                updateWiFiOnOffButton();
             }};
        registerReceiver(b2, i2);
        
        wifiOnOff = (Button)findViewById(R.id.wifi_onoff_btn);
        updateWiFiOnOffButton();
            
        wifiScan = (Button)findViewById(R.id.wifi_scan_btn);
        wifiScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                wfm.startScan();
            }});

        // Reboot button
        rebootBtn = (Button)findViewById(R.id.reboot_btn);
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Advanced.this);
                builder.setTitle("Reboot confirmation");
                builder.setMessage("Are you sure to reboot your device ? ");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Process p = Runtime.getRuntime().exec(getResources().getString(R.string.shell));
                            try {
                                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                                os.writeChars("su\n");
                                SystemClock.sleep(100);
                                os.writeChars("reboot\n");
                            } finally {
                                p.destroy();
                            }
                        } catch (IOException e) { }
                    }});
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }});
             
                builder.show();
            }});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(b1);
        unregisterReceiver(b2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG, this);
    }
}
