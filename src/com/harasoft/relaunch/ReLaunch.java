package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ReLaunch extends Activity {

    final static String           TAG = "ReLaunch";
    static public final String    APP_LRU_FILE = "AppLruFile.txt";
    static public final String    APP_FAV_FILE = "AppFavorites.txt";
    static public final String    LRU_FILE = "LruFile.txt";
    static public final String    FAV_FILE = "Favorites.txt";
    static public final String    HIST_FILE = "History.txt";
    static public final String    FILT_FILE = "Filters.txt";
    final String                  defReaders = ".fb2,.fb2.zip:org.coolreader%org.coolreader.CoolReader%Cool Reader|.epub:Intent:application/epub|.jpg,.jpeg:Intent:image/jpeg"
        + "|.png:Intent:image/png|.pdf:Intent:application/pdf";
    final static public String    defReader = "Cool Reader";
    final static public int       TYPES_ACT = 1;
    final static public int       DIR_ACT = 2;
    final static int              CNTXT_MENU_DELETE_F=1;
    final static int              CNTXT_MENU_DELETE_D_EMPTY=2;
    final static int              CNTXT_MENU_DELETE_D_NON_EMPTY=3;
    final static int              CNTXT_MENU_ADD=4;
    final static int              CNTXT_MENU_CANCEL=5;
    final static int              CNTXT_MENU_MARK_READING = 6;
    final static int              CNTXT_MENU_MARK_FINISHED = 7;
    final static int              CNTXT_MENU_MARK_FORGET = 8;
    final static int              CNTXT_MENU_INTENT = 9;
    final static int              CNTXT_MENU_OPENWITH = 10;
    String                        currentRoot = "/sdcard";
    Integer                       currentPosition = -1;
    List<HashMap<String, String>> itemsArray;
    Stack<Integer>                positions = new Stack<Integer>();
    SimpleAdapter                 adapter;
    SharedPreferences             prefs;
    ReLaunchApp                   app;
    static public boolean         useHome = false;
    boolean                       useHome1 = false;
    boolean                       useShop = false;
    boolean                       useLibrary = false;
    boolean                       useDirViewer = false;
    static public boolean         filterMyself = true;
    //static public String          selfName = "ReLaunch";
    static public String          selfName = "com.harasoft.relaunch.Main";
    String[]                      allowedModels;
    String[]                      allowedDevices;
    String[]                      allowedManufacts;
    String[]                      allowedProducts;
    boolean                       addSView = true;
    
    // Bottom info panel
    BroadcastReceiver             batteryLevelReceiver = null;
    boolean                       batteryLevelRegistered = false;
    Button                        memTitle;
    Button                        memLevel;
    Button                        battTitle;
    Button                        battLevel;
    IntentFilter                  batteryLevelFilter;
    
    private boolean checkField(String[] a, String f)
    {
        for (int i=0; i<a.length; i++)
            if (a[i].equals("*")  ||  a[i].equals(f))
                return true;
        return false;
    }
    private void checkDevice(String dev, String man, String model, String product)
    {
        if (checkField(allowedModels, model))
            return;
        if (checkField(allowedDevices, dev))
            return;
        if (checkField(allowedManufacts, man))
            return;
        if (checkField(allowedProducts, product))
            return;
            
        if (!prefs.getBoolean("allowDevice", false))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            WebView wv = new WebView(this);
            wv.loadDataWithBaseURL(null,getResources().getString(R.string.model_warning), "text/html", "utf-8",null);
            //builder.setTitle("Wrong model !");
            builder.setTitle(getResources().getString(R.string.jv_relaunch_wrong_model));            
            builder.setView(wv);
            //builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("allowDevice", true);
                    editor.commit();
                    dialog.dismiss();
                }});
            //builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }});
         
            builder.show();
        }
     }

    static class ViewHolder {
        TextView  tv;
        ImageView iv;
    }
    class FLSimpleAdapter extends SimpleAdapter {

        FLSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
        {
            super(context, data, resource, from, to);
        }

        @Override
        public int getCount() {
            return itemsArray.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View       v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.flist_layout, null);
                holder = new ViewHolder();
                holder.tv = (TextView) v.findViewById(R.id.fl_text);
                holder.iv  = (ImageView) v.findViewById(R.id.fl_icon);
                v.setTag(holder);
            }
            else
                holder = (ViewHolder) v.getTag();

            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                TextView  tv = holder.tv;
                ImageView iv = holder.iv;
                String    sname = item.get("name");
                String    fname = item.get("fname");
                boolean   setBold = false;
                boolean   useFaces  = prefs.getBoolean("showNew", true);


                if (item.get("type").equals("dir"))
                {
                    if (useFaces)
                    {
                        tv.setBackgroundColor(getResources().getColor(R.color.dir_bg));
                        tv.setTextColor(getResources().getColor(R.color.dir_fg));
                    }
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.dir_ok));
                }
                else
                {
                    if (useFaces)
                    {
                        if (app.history.containsKey(fname))
                        {
                            if (app.history.get(fname) == app.READING)
                            {
                                tv.setBackgroundColor(getResources().getColor(R.color.file_reading_bg));
                                tv.setTextColor(getResources().getColor(R.color.file_reading_fg));
                            }
                            else if (app.history.get(fname) == app.FINISHED)
                            {
                                tv.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                                tv.setTextColor(getResources().getColor(R.color.file_finished_fg));
                            }
                            else
                            {
                                tv.setBackgroundColor(getResources().getColor(R.color.file_unknown_bg));
                                tv.setTextColor(getResources().getColor(R.color.file_unknown_fg));
                            }
                        }
                        else
                        {
                            tv.setBackgroundColor(getResources().getColor(R.color.file_new_bg));
                            tv.setTextColor(getResources().getColor(R.color.file_new_fg));
                            if (getResources().getBoolean(R.bool.show_new_as_bold))
                                setBold = true;
                        }
                    }
                    Drawable d = app.specialIcon(item.get("fname"), false);
                    if (d != null)
                        iv.setImageDrawable(d);
                    else
                    {
                        String rdrName = item.get("reader");
                        if (rdrName.equals("Nope"))
                        {
                            File f = new File(item.get("fname"));
                            if (f.length() > app.viewerMax)
                                iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
                            else
                                iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
                        }
                        else if (rdrName.startsWith("Intent:"))
                            //iv.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_view));
                            iv.setImageDrawable(getResources().getDrawable(R.drawable.icon));
                        else
                        {
                            if (app.getIcons().containsKey(rdrName))
                                iv.setImageDrawable(app.getIcons().get(rdrName));
                            else
                                iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
                        }
                    }
                }

                if (useFaces)
                {
                    SpannableString s = new SpannableString(sname);
                    s.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.NORMAL), 0, sname.length(), 0);
                    tv.setText(s);
                }
                else
                {
                    tv.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                    tv.setTextColor(getResources().getColor(R.color.normal_fg));
                    tv.setText(sname);
                }
            }
            return v;
        }
    }

    private void redrawList()
    {
        if (prefs.getBoolean("filterResults", false))
        {
            List<HashMap<String, String>> newItemsArray = new ArrayList<HashMap<String, String>>();

            for (HashMap<String, String> item : itemsArray)
            {
                if (item.get("type").equals("dir")   ||  app.filterFile(item.get("dname"), item.get("name")))
                    newItemsArray.add(item);
            }
            itemsArray = newItemsArray;
        }
        adapter.notifyDataSetChanged();
    }
    private static List<HashMap<String, String>> parseReadersString(String readerList)
    {
        List<HashMap<String, String>> rc = new ArrayList<HashMap<String,String>>();
        String[] rdrs = readerList.split("\\|");
        for (int i=0; i<rdrs.length; i++)
        {
            String[] re = rdrs[i].split(":");
            switch (re.length)
            {
            case 2:
                String rName = re[1];
                String[] exts = re[0].split(",");
                for (int j=0; j<exts.length; j++)
                {
                    String ext = exts[j];
                    HashMap<String, String> r = new HashMap<String, String>();
                    r.put(ext, rName);
                    rc.add(r);
                }
                break;
            case 3:
                if (re[1].equals("Intent"))
                {
                    String iType = re[2];
                    String[] exts1 = re[0].split(",");
                    for (int j=0; j<exts1.length; j++)
                    {
                        String ext = exts1[j];
                        HashMap<String, String> r = new HashMap<String, String>();
                        r.put(ext, "Intent:" + iType);
                        rc.add(r);
                    }
               }
                break;
            }
        }
        return rc;
    }

    public static String createReadersString(List<HashMap<String, String>> rdrs)
    {
        String rc = new String();

        for (HashMap<String, String> r : rdrs)
        {
            for (String key : r.keySet())
            {
                if (!rc.equals(""))
                    rc += "|";
                rc += key + ":" + r.get(key);
            }
        }
        return rc;
    }

    private void pushCurrentPos(AdapterView<?> parent, boolean push_to_stack)
    {
        Integer p1 = parent.getFirstVisiblePosition();
        if (push_to_stack)
            positions.push(p1);
        currentPosition = p1;
    }
    
    private void setUpButton(Button up, final String upDir)
    {
        if (up != null)
        {
            up.setEnabled(!upDir.equals(""));
            up.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    if (!upDir.equals(""))
                    {
                        Integer p = -1;
                        if (!positions.empty())
                            p = positions.pop();
                        drawDirectory(upDir, p);
                    }
                }});
        }
    }

    private void refreshBottomInfo()
    {
        // Date
        Log.d(TAG, "memTitle:" + (memTitle==null) + " memLevel:" + (memLevel==null));
        String d;
        Calendar c = Calendar.getInstance();
        if (prefs.getBoolean("dateUS", false))
            d = String.format("%02d:%02d%s %02d/%02d/%02d",
                    c.get(Calendar.HOUR),
                    c.get(Calendar.MINUTE),
                    ((c.get(Calendar.AM_PM) == 0) ? "AM" : "PM"),
                    c.get(Calendar.MONTH)+1,
                    c.get(Calendar.DAY_OF_MONTH),
                    (c.get(Calendar.YEAR)-2000));
        else
            d = String.format("%02d:%02d %02d/%02d/%02d",
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    c.get(Calendar.DAY_OF_MONTH),
                    c.get(Calendar.MONTH)+1,
                    (c.get(Calendar.YEAR)-2000));
        if (memTitle != null)
            memTitle.setText(d);

        // Memory
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        if (memLevel != null)
        {
            //memLevel.setText(mi.availMem / 1048576L + "M free");
        	memLevel.setText(mi.availMem / 1048576L + getResources().getString(R.string.jv_relaunch_m_free));
            memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ram), null);
        }


        // Wifi status
        WifiManager wfm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (battTitle != null)
        {
            if (wfm.isWifiEnabled())
            {
            	String nowConnected = wfm.getConnectionInfo().getSSID();
            	if(nowConnected!=null && !nowConnected.equals("")) {
            		battTitle.setText(nowConnected);
            	}
            	else {
            		battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_on));
            	}
                battTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.wifi_on), null, null, null);
            }
            else
            {
                //battTitle.setText("WiFi is off");
            	battTitle.setText(getResources().getString(R.string.jv_relaunch_wifi_is_off));
                battTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.wifi_off), null, null, null);
            }
        }

        // Battery
        if (batteryLevelReceiver == null)
        {
            batteryLevelReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    context.unregisterReceiver(this);
                    batteryLevelRegistered = false;
                    int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        level = (rawlevel * 100) / scale;
                    }
                    if (battLevel != null)
                    {
                        battLevel.setText(level + "%");
                        if (level < 25)
                            battLevel.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bat1), null, null, null);
                        else if (level < 50)
                            battLevel.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bat2), null, null, null);
                        else if (level < 75)
                            battLevel.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bat3), null, null, null);
                        else
                            battLevel.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.bat4), null, null, null);
                    }

                }
            };
        }
        if (!batteryLevelRegistered)
        {
            registerReceiver(batteryLevelReceiver, batteryLevelFilter);
            batteryLevelRegistered = true;
        }
    }

    private void drawDirectory(String root, Integer startPosition)
    {
        File         dir = new File(root);
        File[]       allEntries = dir.listFiles();
        List<String> files = new ArrayList<String>();
        List<String> dirs = new ArrayList<String>();

        currentRoot = root;
        currentPosition = (startPosition == -1) ? 0 : startPosition;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastdir", currentRoot);
        editor.commit();

        final EditText  tv = (EditText)findViewById(useDirViewer ? R.id.results_title : R.id.title_txt);
        final String dirAbsPath = dir.getAbsolutePath();
        tv.setText(dirAbsPath + " (" + ((allEntries == null) ? 0 : allEntries.length) + ")");
        final Button up = (Button)findViewById(R.id.goup_btn);
        final ImageButton adv = (ImageButton)findViewById(R.id.advanced_btn);
        if (adv != null)
        {
            adv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    Intent i = new Intent(ReLaunch.this, Advanced.class);
                    startActivity(i);
                }});
        }

        itemsArray = new ArrayList<HashMap<String,String>>();
        if (dir.getParent() != null)
            dirs.add("..");
        if (allEntries != null)
        {
            for (File entry : allEntries)
            {
                if (entry.isDirectory())
                    dirs.add(entry.getName());
                else if (!prefs.getBoolean("filterResults", false)  ||  app.filterFile(dir.getAbsolutePath(), entry.getName()))
                    files.add(entry.getName());
            }
        }
        Collections.sort(dirs);
        Collections.sort(files);
        String upDir = "";
        for (String f : dirs)
        {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("name", f);
            item.put("dname", dir.getAbsolutePath());
            if (f.equals(".."))
            {
                upDir = dir.getParent();
                continue;
            }
            else
                item.put("fname", dir.getAbsolutePath() + "/" + f);
            item.put("type", "dir");
            item.put("reader", "Nope");
            itemsArray.add(item);
        }
        for (String f : files)
        {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("name", f);
            item.put("dname", dir.getAbsolutePath());
            item.put("fname", dir.getAbsolutePath() + "/" + f);
            item.put("type", "file");
            item.put("reader", app.readerName(f));
            itemsArray.add(item);
        }

        String[] from = new String[] { "name" };
        int[]    to   = new int[] { R.id.fl_text };
        setUpButton(up, upDir);
        
        final ListView lv = (ListView) findViewById(useDirViewer ? R.id.results_list : R.id.fl_list);
        adapter = new FLSimpleAdapter(this, itemsArray, useDirViewer ? R.layout.results_layout : R.layout.flist_layout, from, to);
        lv.setAdapter(adapter);
        //if (prefs.getBoolean("customScroll", true))
        if (prefs.getBoolean("customScroll", app.customScrollDef))	
        {
            if (addSView)
            {
                int scrollW;
                try {
                    scrollW = Integer.parseInt(prefs.getString("scrollWidth", "25"));
                } catch(NumberFormatException e) {
                    scrollW = 25;
                }

                LinearLayout ll = (LinearLayout)findViewById(useDirViewer ? R.id.results_fl : R.id.fl_layout);
                final SView sv = new SView(getBaseContext());
                LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
                sv.setLayoutParams(pars);
                ll.addView(sv);
                lv.setOnScrollListener(new AbsListView.OnScrollListener() {
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


        registerForContextMenu(lv);
        if (startPosition != -1)
            lv.setSelection(startPosition);
        lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    HashMap<String, String> item = itemsArray.get(position);

                    if (item.get("type").equals("dir"))
                    {
                        // Goto directory
                        pushCurrentPos(parent, true);
                        drawDirectory(item.get("fname"), -1);
                    }
                    else if (app.specialAction(ReLaunch.this, item.get("fname")))
                        pushCurrentPos(parent, false);
                    else
                    {
                        pushCurrentPos(parent, false);
                        if (item.get("reader").equals("Nope"))
                            app.defaultAction(ReLaunch.this, item.get("fname"));
                        else
                        {
                            // Launch reader
                            if (app.askIfAmbiguous)
                            {
                                List<String> rdrs = app.readerNames(item.get("fname"));
                                if (rdrs.size() < 1)
                                    return;
                                else if (rdrs.size() == 1)
                                    start(app.launchReader(rdrs.get(0), item.get("fname")));
                                else
                                {
                                    final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
                                    final String rdr1 = item.get("fname");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
                                    //builder.setTitle("Select application");
                                    builder.setTitle(getResources().getString(R.string.jv_relaunch_select_application));
                                    builder.setSingleChoiceItems(applications, -1, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int i) {
                                                start(app.launchReader((String)applications[i], rdr1));
                                                dialog.dismiss();
                                            }
                                        });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                            else
                                start(app.launchReader(item.get("reader"), item.get("fname")));
                        }
                    }
                }});

        final Button upScroll = (Button)findViewById(R.id.upscroll_btn);
        upScroll.setText(app.scrollStep + "%");
        upScroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                int first = lv.getFirstVisiblePosition();
                int total = itemsArray.size();
                first -= (total * app.scrollStep) / 100;
                if (first < 0)
                    first = 0;
                lv.setSelection(first);
            }});

        final Button downScroll = (Button)findViewById(R.id.downscroll_btn);
        downScroll.setText(app.scrollStep + "%");
        downScroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                int first = lv.getFirstVisiblePosition();
                int total = itemsArray.size();
                int last = lv.getLastVisiblePosition();
                //Log.d(TAG, "1 -- first=" + first + " last=" + last + " total=" + total);
                first += (total * app.scrollStep) / 100;
                if (first <= last)
                    first = last+1;  // Special for NOOK, otherwise it won't redraw the listview
                if (first > (total-1))
                    first = total-1;
                lv.setSelection(first);
            }});
        
        refreshBottomInfo();
    }

    private HashMap<String, Drawable> createIconsList(PackageManager pm)
    {
    	Drawable d = null;
        HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname="";
        String aname="";
        String hname="";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                	if(ri.activityInfo.labelRes!=0) {
                		hname=(String)ri.activityInfo.loadLabel(pm);
                		}
                	else {
                		hname=(String)ri.loadLabel(pm);
                		}
                	if(ri.activityInfo.icon!=0) {
                		d=ri.activityInfo.loadIcon(pm);
                		}
                	else {
                		d=ri.loadIcon(pm);                		
                	}
                }
                catch(Exception e) { 
                }
                if (d != null)	{ 
                	rc.put(pname + "%" + aname + "%" + hname,d);
                    }
                }
            }
        return rc;
    }

    private static class AppComparator implements java.util.Comparator<String>
    {
        public int compare(String a, String b)
        {
        	if(a==null && b==null) {
        		return 0;
        	}
        	if(a==null && b!=null) {
        		return 1;
        	}
        	if(a!=null && b==null) {
        		return -1;
        	}
        	String[] ap = a.split("\\%");
        	String[] bp = b.split("\\%");
            return ap[2].compareToIgnoreCase(bp[2]);
        }
    }
    
    static public List<String> createAppList(PackageManager pm)
    {
        List<String> rc = new ArrayList<String>();
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname="";
        String aname="";
        String hname="";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                	if(ri.activityInfo.labelRes!=0) {
                		hname=(String)ri.activityInfo.loadLabel(pm);
                		}
                	else {
                		hname=(String)ri.loadLabel(pm);
                		}
                	}
                catch(Exception e) { 
                }
                if (!filterMyself  ||  !aname.equals(selfName))
                	rc.add(pname + "%" + aname + "%" + hname);
                }
        	}
        Collections.sort(rc,new AppComparator());
        return rc;
    }

    private void start(Intent i)
    {
        if (i != null)
            try {
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(ReLaunch.this, "Activity not found", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If we called from Home launcher?
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
            useHome = false;
            useHome1 = false;
            useShop = false;
            useLibrary = false;
            useDirViewer = false;
        }
        else
        {
            useHome = data.getBooleanExtra("home", false);
            useHome1 = data.getBooleanExtra("home1", false);
            useShop = data.getBooleanExtra("shop", false);
            useLibrary = data.getBooleanExtra("library", false);
            useDirViewer = data.getBooleanExtra("dirviewer", false);
        }

        // Global arrays
        allowedModels = getResources().getStringArray(R.array.allowed_models);
        allowedDevices = getResources().getStringArray(R.array.allowed_devices);
        allowedManufacts = getResources().getStringArray(R.array.allowed_manufacturers);
        allowedProducts = getResources().getStringArray(R.array.allowed_products);

        // Create global storage with values
        app = (ReLaunchApp)getApplicationContext();

        app.FLT_SELECT = getResources().getInteger(R.integer.FLT_SELECT);
        app.FLT_STARTS = getResources().getInteger(R.integer.FLT_STARTS);
        app.FLT_ENDS = getResources().getInteger(R.integer.FLT_ENDS);
        app.FLT_CONTAINS = getResources().getInteger(R.integer.FLT_CONTAINS);
        app.FLT_MATCHES = getResources().getInteger(R.integer.FLT_MATCHES);
        app.FLT_NEW = getResources().getInteger(R.integer.FLT_NEW);
        app.FLT_NEW_AND_READING = getResources().getInteger(R.integer.FLT_NEW_AND_READING);

        // Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String typesString = prefs.getString("types", defReaders);
        try {
            app.scrollStep = Integer.parseInt(prefs.getString("scrollPerc", "10"));
            app.viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize", "1048576"));
            app.editorMax = Integer.parseInt(prefs.getString("editorMaxSize", "262144"));
        } catch(NumberFormatException e) {
            app.scrollStep = 10;
            app.viewerMax = 1048576;
            app.editorMax = 262144;
       }
        if (app.scrollStep < 1)
            app.scrollStep = 1;
        if (app.scrollStep > 100)
            app.scrollStep = 100;

        filterMyself = prefs.getBoolean("filterSelf", true);
        if (useHome1  &&  prefs.getBoolean("homeMode", true))
            useHome = true;
        if (useShop  &&  prefs.getBoolean("shopMode", true))
            useHome = true;
        if (useLibrary  &&  prefs.getBoolean("libraryMode", true))
            useHome = true;
        app.fullScreen = prefs.getBoolean("fullScreen", true);
        app.setFullScreenIfNecessary(this);

       // Create application icons map
        app.setIcons(createIconsList(getPackageManager()));

        // Create applications label list
        app.setApps(createAppList(getPackageManager()));

        // Readers list
        app.setReaders(parseReadersString(typesString));
        //Log.d(TAG, "Readers string: \"" + createReadersString(app.getReaders()) + "\"");

        // Miscellaneous lists list
        app.readFile("lastOpened", LRU_FILE);
        app.readFile("favorites", FAV_FILE);
        app.readFile("filters", FILT_FILE, ":");
        app.filters_and = prefs.getBoolean("filtersAnd", true);
        app.readFile("history", HIST_FILE, ":");
        app.history.clear();
        for (String[] r : app.getList("history"))
        {
            if (r[1].equals("READING"))
                app.history.put(r[0], app.READING);
            else if (r[1].equals("FINISHED"))
                app.history.put(r[0], app.FINISHED);
        }

        if (useDirViewer)
        {
            String start_dir = null;
            setContentView(R.layout.results_layout);
            if (data.getExtras() != null)
                start_dir = data.getStringExtra("start_dir");
            ((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { finish(); }});
            if (start_dir != null)
                drawDirectory(start_dir, -1);
        }
        else
        {
            // Main layout
            if (useHome)
            {
                app.readFile("app_last", APP_LRU_FILE, ":");
                app.readFile("app_favorites", APP_FAV_FILE, ":");
                setContentView(prefs.getBoolean("showButtons", true) ? R.layout.main_launcher : R.layout.main_launcher_nb);
                ((ImageButton)findViewById(R.id.app_last)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
                            intent.putExtra("list", "app_last");
                            //intent.putExtra("title", "Last recently used applications");
                            intent.putExtra("title", getResources().getString(R.string.jv_relaunch_lru_a));
                            startActivity(intent);
                        }
                    });
                ((ImageButton)findViewById(R.id.all_applications_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
                            intent.putExtra("list", "app_all");
                            //intent.putExtra("title", "All applications");
                            intent.putExtra("title", getResources().getString(R.string.jv_relaunch_all_a));
                            startActivity(intent);
                        }
                    });
                ((ImageButton)findViewById(R.id.app_favorites)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
                            intent.putExtra("list", "app_favorites");
                            //intent.putExtra("title", "Favorite applications");
                            intent.putExtra("title", getResources().getString(R.string.jv_relaunch_fav_a));
                            startActivity(intent);
                        }
                    });
            }
            else
                setContentView(prefs.getBoolean("showButtons", true) ? R.layout.main : R.layout.main_nobuttons);
            if (prefs.getBoolean("showButtons", true))
            {
                ((ImageButton)findViewById(R.id.settings_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuSettings(); }});
                ((ImageButton)findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuSearch(); }});
                ((ImageButton)findViewById(R.id.lru_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuLastopened(); }});
                ((ImageButton)findViewById(R.id.favor_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuFavorites(); }});
                ((ImageButton)findViewById(R.id.about_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuAbout(); }});
            }

            // Memory buttons (task manager activity
            memLevel = (Button)findViewById(R.id.mem_level);
            if (memLevel != null)
                memLevel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ReLaunch.this, TaskManager.class);
                        startActivity(intent);
                    }});
            memTitle = (Button)findViewById(R.id.mem_title);
            if (memTitle != null)
                memTitle.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ReLaunch.this, TaskManager.class);
                        startActivity(intent);
                    }});

            // Battery buttons
            battLevel = (Button)findViewById(R.id.bat_level);
            if (battLevel != null)
                battLevel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                        startActivity(intent);
                    }});
            battTitle = (Button)findViewById(R.id.bat_title);
            if (battTitle != null)
                battTitle.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                        startActivity(intent);
                    }});
            batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

            checkDevice(Build.DEVICE, Build.MANUFACTURER, Build.MODEL, Build.PRODUCT);

            // What's new processing
            final int latestVersion = prefs.getInt("latestVersion", 0);
            //final int currentVersion = getResources().getInteger(R.integer.app_iversion);
            int tCurrentVersion = 0;
            try {
            	tCurrentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        	}
        	catch(Exception e) { }
            final int currentVersion = tCurrentVersion;
            if (currentVersion > latestVersion)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                WebView wv = new WebView(this);
                wv.loadDataWithBaseURL(null,getResources().getString(R.string.whats_new), "text/html", "utf-8",null);
                //builder.setTitle("What's new");
                builder.setTitle(getResources().getString(R.string.jv_relaunch_whats_new));
                builder.setView(wv);
                builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("latestVersion", currentVersion);
                        editor.commit();
                        dialog.dismiss();
                    }});
                builder.show();
            }
    
            int gl16Mode;
            try {
                gl16Mode = Integer.parseInt(prefs.getString("gl16Mode", "5"));
            } catch (NumberFormatException e) {
                gl16Mode = 5;
            }
            N2EpdController.setGL16Mode(gl16Mode);
             // First directory to get to
            if (prefs.getBoolean("saveDir", true))
                drawDirectory(prefs.getString("lastdir", "/sdcard"), -1);
            else
                drawDirectory(prefs.getString("startDir", "/sdcard"), -1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

                // Reread preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String typesString = prefs.getString("types", defReaders);
        //Log.d(TAG, "Types string: \"" + typesString + "\"");

        // Recreate readers list
        app.setReaders(parseReadersString(typesString));
        //Log.d(TAG, "Readers string: \"" + createReadersString(readers) + "\"");

        app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
        drawDirectory(currentRoot, currentPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
            {
            case R.id.search:
                menuSearch();
                return true;
            case R.id.mime_types:
                menuTypes();
                return true;
            case R.id.about:
                menuAbout();
                return true;
            case R.id.setting:
                menuSettings();
                return true;
            case R.id.lastopened:
                menuLastopened();
                return true;
            case R.id.favorites:
                menuFavorites();
                return true;
            default:
                return true;
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult, " + requestCode + " " + resultCode);
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode)
        {
        case TYPES_ACT:
            String newTypes = createReadersString(app.getReaders());
            //Log.d(TAG, "New types string: \"" + newTypes + "\"");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("types", newTypes);
            editor.commit();

            drawDirectory(currentRoot, currentPosition);
            break;
        default:
            return;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        HashMap<String, String> i = itemsArray.get(info.position);
        String fn = i.get("name");
        String dr = i.get("dname");
        String tp  = i.get("type");
        String fullName = dr + "/" + fn;

        if (tp.equals("file"))
        {
            if (!app.contains("favorites", dr, fn))
                //menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, "Add to favorites");
            	menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, getResources().getString(R.string.jv_relaunch_add));
            if (app.history.containsKey(fullName))
            {
                if (app.history.get(fullName) == app.READING)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_relaunch_mark));
                else if (app.history.get(fullName) == app.FINISHED)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, "Remove \"read\" mark");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, getResources().getString(R.string.jv_relaunch_unmark));
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, "Forget all marks");
                menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, getResources().getString(R.string.jv_relaunch_unmarkall));
            }
            else
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
            	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_relaunch_mark));
            if (prefs.getBoolean("openWith", true))
                //menu.add(Menu.NONE, CNTXT_MENU_OPENWITH, Menu.NONE, "Open with ...");
            	menu.add(Menu.NONE, CNTXT_MENU_OPENWITH, Menu.NONE, getResources().getString(R.string.jv_relaunch_openwith));
            if (prefs.getBoolean("createIntent", true))
                //menu.add(Menu.NONE, CNTXT_MENU_INTENT, Menu.NONE, "Create Intent ...");
            	menu.add(Menu.NONE, CNTXT_MENU_INTENT, Menu.NONE, getResources().getString(R.string.jv_relaunch_createintent));
            //menu.add(Menu.NONE, CNTXT_MENU_DELETE_F, Menu.NONE, "Delete");
            menu.add(Menu.NONE, CNTXT_MENU_DELETE_F, Menu.NONE, getResources().getString(R.string.jv_relaunch_delete));
        }
        else
        {
            File   d = new File(fullName);
            String[] allEntries = d.list();
            if (!app.contains("favorites", fullName, app.DIR_TAG))
                //menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, "Add to favorites");
            	menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, getResources().getString(R.string.jv_relaunch_add));
            if (allEntries.length > 0)
                //menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_NON_EMPTY, Menu.NONE, "Delete NON-EMPTY directory!");
            	menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_NON_EMPTY, Menu.NONE, getResources().getString(R.string.jv_relaunch_delete_non_emp_dir));
            else
                //menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_EMPTY, Menu.NONE, "Delete empty directory");
            	menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_EMPTY, Menu.NONE, getResources().getString(R.string.jv_relaunch_delete_emp_dir));
        }
        //menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
        menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.jv_relaunch_cancel));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CNTXT_MENU_CANCEL)
            return true;

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;
        HashMap<String, String> i = itemsArray.get(pos);
        final String fname = i.get("name");
        final String dname = i.get("dname");
        final String fullName = dname + "/" + fname;
        final String type  = i.get("type");

        switch (item.getItemId())
        {
        case CNTXT_MENU_ADD:
            if (type.equals("file"))
                app.addToList("favorites", dname, fname, false);
            else
                app.addToList("favorites", fullName, app.DIR_TAG, false);
            break;
        case CNTXT_MENU_MARK_READING:
            app.history.put(fullName, app.READING);
            redrawList();
            break;
        case CNTXT_MENU_MARK_FINISHED:
            app.history.put(fullName, app.FINISHED);
            redrawList();
            break;
        case CNTXT_MENU_MARK_FORGET:
            app.history.remove(fullName);
            redrawList();
            break;
        case CNTXT_MENU_OPENWITH:
        {
            final CharSequence[] applications = app.getApps().toArray(new CharSequence[app.getApps().size()]);
            CharSequence[] happlications = app.getApps().toArray(new CharSequence[app.getApps().size()]);
            for(int j=0;j<happlications.length;j++) {
            	String happ = (String)happlications[j];
            	String[] happp = happ.split("\\%"); 
            	happlications[j] = happp[2];
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
            //builder.setTitle("Select application");
            builder.setTitle(getResources().getString(R.string.jv_relaunch_select_application));
            builder.setSingleChoiceItems(happlications, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        start(app.launchReader((String)applications[i], fullName));
                        dialog.dismiss();
                    }
                });
            builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }});
            builder.show();
            break;
        }
        case CNTXT_MENU_INTENT:
        {
            String re[] = fname.split("\\.");
            for (int j=0; j<re.length; j++)
                Log.d(TAG, "re[" + j + "]=" + re[j]);
            List<String> ilist = new ArrayList<String>();
            for (int j=1; j<re.length; j++)
            {
                String act = "application/";
                String typ = re[j];
                if (typ.equals("jpg"))
                    typ = "jpeg";
                if (typ.equals("jpeg")  ||  typ.equals("png"))
                    act = "image/";
                ilist.add(act + typ);
                if (re.length > 2)
                {
                    for (int k = j+1; k < re.length; k++)
                    {
                        String x = "";
                        for (int l = k; l < re.length; l++)
                            x += "+" + re[l];
                        ilist.add(act + typ + x);
                    }
                }
            }

            final CharSequence[] intents = ilist.toArray(new CharSequence[ilist.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
            //builder.setTitle("Select intent type");
            builder.setTitle(getResources().getString(R.string.jv_relaunch_select_intent_type));
            builder.setSingleChoiceItems(intents, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    Intent in = new Intent();
                    in.setAction(Intent.ACTION_VIEW);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    in.setDataAndType(Uri.parse("file://" + fullName), (String) intents[i]);
                    dialog.dismiss();
                    try {
                        startActivity(in);
                    } catch (ActivityNotFoundException e) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ReLaunch.this);
                        //builder1.setTitle("Activity not found");
                        builder1.setTitle(getResources().getString(R.string.jv_relaunch_activity_not_found_title));
                        //builder1.setMessage("Activity for file \"" + fullName + "\" with type \"" + intents[i] + "\" not found");
                        builder1.setMessage(getResources().getString(R.string.jv_relaunch_activity_not_found_text1) + " \"" + fullName + "\" " + getResources().getString(R.string.jv_relaunch_activity_not_found_text2) + " \"" + intents[i] + "\" " + getResources().getString(R.string.jv_relaunch_activity_not_found_text3));
                        builder1.setPositiveButton(getResources().getString(R.string.jv_relaunch_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }});
                        builder1.show();
                    }
                }
            });
            //builder.setPositiveButton("Other", new DialogInterface.OnClickListener() {
            builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_other), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ReLaunch.this);
                    //builder1.setTitle("Intent type");
                    builder1.setTitle(getResources().getString(R.string.jv_relaunch_intent_type));
                    final EditText input = new EditText(ReLaunch.this);
                    input.setText("application/");
                    builder1.setView(input);
                    //builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    builder1.setPositiveButton(getResources().getString(R.string.jv_relaunch_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent in = new Intent();
                            in.setAction(Intent.ACTION_VIEW);
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            in.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            in.setDataAndType(Uri.parse("file://" + fullName), input.getText().toString());
                            dialog.dismiss();
                            try {
                                startActivity(in);
                            } catch (ActivityNotFoundException e) {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ReLaunch.this);
                                //builder2.setTitle("Activity not found");
                                builder2.setTitle(getResources().getString(R.string.jv_relaunch_activity_not_found_title));
                                //builder2.setMessage("Activity for file \"" + fullName + "\" with type \"" + input.getText() + "\" not found");
                                builder2.setMessage(getResources().getString(R.string.jv_relaunch_activity_not_found_text1) + " \"" + fullName + "\" " + getResources().getString(R.string.jv_relaunch_activity_not_found_text2) + " \"" + input.getText() + "\" " + getResources().getString(R.string.jv_relaunch_activity_not_found_text3));
                                //builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                builder2.setPositiveButton(getResources().getString(R.string.jv_relaunch_ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }});
                                builder2.show();
                            }
                        }});
                    builder1.show();
                }});
            //builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }});

            builder.show();
            break;
        }
        case CNTXT_MENU_DELETE_F:
            if (prefs.getBoolean("confirmFileDelete", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Delete file warning");
                builder.setTitle(getResources().getString(R.string.jv_relaunch_del_file_title));
                //builder.setMessage("Are you sure to delete file \"" + fname + "\" ?");
                builder.setMessage(getResources().getString(R.string.jv_relaunch_del_file_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_relaunch_del_file_text2));
                //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeFile(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }});
                builder.show();
            }
            else if (app.removeFile(dname, fname))
            {
                itemsArray.remove(pos);
                redrawList();
            }
            break;
        case CNTXT_MENU_DELETE_D_EMPTY:
            if (prefs.getBoolean("confirmDirDelete", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Delete empty directory warning");
                builder.setTitle(getResources().getString(R.string.jv_relaunch_del_em_dir_title));
                //builder.setMessage("Are you sure to delete empty directory \"" + fname + "\" ?");
                builder.setMessage(getResources().getString(R.string.jv_relaunch_del_em_dir_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_relaunch_del_em_dir_text2));
                //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeFile(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }});
                builder.show();
            }
            else if (app.removeFile(dname, fname))
            {
                itemsArray.remove(pos);
                redrawList();
            }
            break;
        case CNTXT_MENU_DELETE_D_NON_EMPTY:
            if (prefs.getBoolean("confirmNonEmptyDirDelete", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Delete non empty directory warning");
                builder.setTitle(getResources().getString(R.string.jv_relaunch_del_ne_dir_title));
                //builder.setMessage("Are you sure to delete non-empty directory \"" + fname + "\" (dangerous) ?");
                builder.setMessage(getResources().getString(R.string.jv_relaunch_del_ne_dir_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_relaunch_del_ne_dir_text2));
                //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                builder.setPositiveButton(getResources().getString(R.string.jv_relaunch_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeDirectory(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                builder.setNegativeButton(getResources().getString(R.string.jv_relaunch_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }});
                builder.show();
            }
            else if (app.removeDirectory(dname, fname))
            {
                itemsArray.remove(pos);
                redrawList();
            }
            break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG, this);

        refreshBottomInfo();
    }

    @Override
    protected void onStop() {
        int lruMax = 30;
        int favMax = 30;
        int appLruMax = 30;
        int appFavMax = 30;
        try {
            lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
            favMax = Integer.parseInt(prefs.getString("favSize", "30"));
            appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
            appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
        } catch(NumberFormatException e) { }
        app.writeFile("lastOpened", LRU_FILE, lruMax);
        app.writeFile("favorites",  FAV_FILE, favMax);
        app.writeFile("app_last", APP_LRU_FILE, appLruMax, ":");
        app.writeFile("app_favorites", APP_FAV_FILE, appFavMax, ":");

        List<String[]> h = new ArrayList<String[]>();
        for (String k : app.history.keySet())
        {
            if (app.history.get(k) == app.READING)
                h.add(new String[] {k, "READING"});
            else if (app.history.get(k) == app.FINISHED)
                h.add(new String[] {k, "FINISHED"});
        }
        app.setList("history", h);
        app.writeFile("history", HIST_FILE, 0, ":");
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!useHome)
            return super.onKeyDown(keyCode, event);
        if(keyCode == KeyEvent.KEYCODE_HOME)
            return true;
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                //.setTitle("This is a launcher!")
                .setTitle(getResources().getString(R.string.jv_relaunch_launcher))
                //.setMessage("Are you sure you want to quit ?")
                .setMessage(getResources().getString(R.string.jv_relaunch_launcher_text))
                //.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                .setPositiveButton(getResources().getString(R.string.jv_relaunch_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                //.setNegativeButton("NO", null)
                .setNegativeButton(getResources().getString(R.string.jv_relaunch_no), null)
                .show();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void menuSearch() {
        Intent intent = new Intent(ReLaunch.this, SearchActivity.class);
        startActivity(intent);
    }

    private void menuTypes() {
        Intent intent = new Intent(ReLaunch.this, TypesActivity.class);
        startActivityForResult(intent, TYPES_ACT);
    }

    private void menuSettings() {
        Intent intent = new Intent(ReLaunch.this, PrefsActivity.class);
        startActivity(intent);
    }

    private void menuLastopened() {
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "lastOpened");
        //intent.putExtra("title", "Last opened");
        intent.putExtra("title", getResources().getString(R.string.jv_relaunch_lru));
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);
    }

    private void menuFavorites() {
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "favorites");
        //intent.putExtra("title", "Favorites");
        intent.putExtra("title", getResources().getString(R.string.jv_relaunch_fav));
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);
    }

    private void menuAbout() {
        app.About(this);
    }
}
