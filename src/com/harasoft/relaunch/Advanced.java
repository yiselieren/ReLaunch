package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Advanced extends Activity {
    final static String           TAG = "Advanced";

    SharedPreferences             prefs;
    ReLaunchApp                   app;
    Button                        rebootBtn;
    Button                        suBtn;
    String[]                      ingnoreFs;
    List<Info>                    infos;
    int                           myId = -1;;

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
    
    private boolean cmd_su(String cmd, String title, String descr)
    {
        String r1 = "";
        Log.d(TAG, "===== LOG 1 ID:" + getMyId());
        //List<String> r = execFg(cmd);
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e1) { }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) { }
        Log.d(TAG, "===== LOG 2 ID:" + getMyId());
       if (getMyId() == 0)
            return true;

        /*
        for (int i=0; i<r.size(); i++)
            r1 = r1 + r.get(i) + "\n";
        AlertDialog.Builder builder = new AlertDialog.Builder(Advanced.this);
        builder.setTitle(title);
        builder.setMessage("Can't " + descr + ":\n" + ((r.size() > 0) ? r1 : "Can't execute \"su\""));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }});
        builder.show();
        */
        return false;
    }

    private boolean cmd1(String cmd, String title, String descr)
    {
        String r1 = "";
        List<String> r = execFg(cmd);
        if (r.size() < 1)
            return true;

        for (int i=0; i<r.size(); i++)
            r1 = r1 + r.get(i) + "\n";
        AlertDialog.Builder builder = new AlertDialog.Builder(Advanced.this);
        builder.setTitle(title);
        builder.setMessage("Can't " + descr + ":\n" + r1);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }});
        builder.show();
        return false;
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
        
        myId = getMyId();
        Log.d(TAG, "ID=" + myId);
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
        for (int i=0; i<infos.size(); i++)
            infos.get(i).dump("#" + i);
        ((TextView)findViewById(R.id.results_title)).setText("Advanced functions");
        ((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }});

        rebootBtn = (Button)findViewById(R.id.reboot_btn);
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Advanced.this);
                builder.setTitle("Reboot confirmation");
                builder.setMessage("Are you sure to reboot your device ? ");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (cmd_su("su", "Obtaining superuser permissions failure", "obtain superuser permissions"))
                            try {
                                Runtime.getRuntime().exec("reboot");
                            } catch (IOException e) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(Advanced.this);
                                builder1.setTitle("Reboot failure");
                                builder1.setMessage("Can't reboot device");
                                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }});
                                builder1.show();
                            }

                    }});
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }});
             
                builder.show();
            }});
        
        suBtn = (Button)findViewById(R.id.su_btn);
        suBtn.setEnabled(myId != 0);
        suBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                suBtn.setEnabled(!cmd_su("su", "Obtaining superuser permissions failure", "obtain superuser permissions"));
            }});
    }
}
