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
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    boolean                       addSView = true;
    
    WifiManager                   wfm;
    boolean                       wifiOn = false;
    Button                        wifiOnOff;
    Button                        wifiScan;
    List<ScanResult>              wifiNetworks = new ArrayList<ScanResult>();
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
    
    private void Trace(String t, String s)
    {
        Log.d(t, s);
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
                if (item.SSID.equals(winfo.getSSID()))
                {
                    SpannableString s1 = new SpannableString(item.SSID);
                    s1.setSpan(Typeface.BOLD, 0, item.SSID.length(), 0);
                    tv1.setText(s1);
                    SpannableString s2 = new SpannableString(item.capabilities);
                    s2.setSpan(Typeface.BOLD, 0, item.capabilities.length(), 0);
                    tv2.setText(s2);
                    int ipAddress = winfo.getIpAddress();
                    String s = String.format("Connected, IP: %d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));
                    SpannableString s3 = new SpannableString(s);
                    s3.setSpan(Typeface.BOLD, 0, s.length(), 0);
                    tv3.setText(s3);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
               }
                else
                {
                    tv1.setText(item.SSID);
                    tv2.setText(item.capabilities);
                    tv3.setText("Level: " + item.level);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
                }
            }
            return v;
        }
    }

    private String sp(int n)
    {
        String rc = "";
        for (int i=0; i<n; i++)
            rc += "&nbsp;";
        return rc;
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
    
    private void updateWiFiInfo()
    {
        wifiOnOff.setEnabled(true);
        if (wifiOn)
        {
            wifiOnOff.setText("Turn WiFi off");
            wifiOnOff.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.wifi_off),
                    getResources().getDrawable(android.R.drawable.ic_lock_power_off),
                    getResources().getDrawable(R.drawable.wifi_off),
                    null);
            wifiOnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wfm.setWifiEnabled(false);
                    wifiOnOff.setText("Turning WiFi off");   
                    wifiOnOff.setEnabled(false);
               }});
            wifiNetworks = wfm.getScanResults();
            adapter.notifyDataSetChanged();
            wifiScan.setEnabled(true);
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
                    Log.d(TAG, " ---- Turning WiFi on");
                    wifiOnOff.setText("Turning WiFi on");   
                    wifiOnOff.setEnabled(false);
                }});
            wifiNetworks.clear();
            adapter.notifyDataSetChanged();
            wifiScan.setEnabled(false);
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
        ((TextView)findViewById(R.id.results_title)).setText("Advanced functions, info, etc.");
        ((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }});

        // Wifi info
        lv_wifi = (ListView) findViewById(R.id.wifi_lv);
        adapter = new WiFiAdapter(this);
        lv_wifi.setAdapter(adapter);
        if (prefs.getBoolean("customScroll", true))
        {
            int scrollW;
            try {
                scrollW = Integer.parseInt(prefs.getString("scrollWidth", "25"));
            } catch(NumberFormatException e) {
                scrollW = 25;
            }

            if (addSView)
            {
                LinearLayout ll = (LinearLayout)findViewById(R.id.wifi_lv_layout);
                final SView sv = new SView(getBaseContext());
                LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
                sv.setLayoutParams(pars);
                ll.addView(sv);
                lv_wifi.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { 
                        sv.total = totalItemCount;
                        sv.count = visibleItemCount;
                        sv.first = firstVisibleItem;
                        sv.invalidate();
                    }
                    public void onScrollStateChanged(AbsListView view, int scrollState) {                
                    }
                });         
                addSView = false;
            }
        }


        wifiScan = (Button)findViewById(R.id.wifi_scan_btn);
        wifiScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                wfm.startScan();
                wifiScan.setEnabled(false);
            }});

        // Receive broadcast when scan results are available
        i1 = new IntentFilter();
        i1.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        b1 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Trace(TAG, "--- Broadcast receiver B1");
                wifiNetworks = wfm.getScanResults();
                wifiScan.setEnabled(true);
                adapter.notifyDataSetChanged();
            }};
        registerReceiver(b1, i1);

        // Receive broadcast when WiFi status changed
        i2 = new IntentFilter();
        i2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        i2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        i2.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        i2.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        i2.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        b2 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Trace(TAG, "--- Broadcast receiver B2");
                wifiOn = wfm.isWifiEnabled();
                wifiNetworks = wfm.getScanResults();
                wifiScan.setEnabled(true);
                adapter.notifyDataSetChanged();
                updateWiFiInfo();
             }};
        registerReceiver(b2, i2);
        
        wifiOnOff = (Button)findViewById(R.id.wifi_onoff_btn);
        updateWiFiInfo();
            
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
        
        // Web info view
        WebView wv = (WebView)findViewById(R.id.webview1);
        final int ntitle1 = 0;
        final int ntitle2 = 5;
        String s = "<h2><center>Disks/partitions</center></h2><table>";
        s += "<tr>"
            + "<td><b>" + sp(ntitle1) + "Mount point" + sp(ntitle2) + "</b></td>"
            + "<td><b>" + sp(ntitle1) + "FS"          + sp(ntitle2) + "</b></td>"
            + "<td><b>" + sp(ntitle1) + "total"       + sp(ntitle2) + "</b></td>"
            + "<td><b>" + sp(ntitle1) + "used"        + sp(ntitle2) + "</b></td>"
            + "<td><b>" + sp(ntitle1) + "free"        + sp(ntitle2) + "</b></td>"
            + "<td><b>" + sp(ntitle1) + "RO/RW"       + sp(ntitle2) + "</b></td>"
            + "</tr>";
        for (Info i : infos)
            s += "<tr>"
                + "<td><b>" + i.mpoint + "</b></td>"
                + "<td><b>" + i.fs +     "</b></td>"
                + "<td><b>" + i.total +  "</b></td>"
                + "<td><b>" + i.used +   "</b></td>"
                + "<td><b>" + i.free +   "</b></td>"
                + "<td><b>" + (i.ro ? "RO" : "RW") + "</b></td>"
                + "</tr>";
        s += "</table>";
        wv.loadData(s, "text/html", "utf-8");
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
