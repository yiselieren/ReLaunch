package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ReLaunch extends Activity {

    final String                  TAG = "ReLaunch";
    static public final String    APP_LRU_FILE = "AppLruFile.txt";
    static public final String    APP_FAV_FILE = "AppFavorites.txt";
    static public final String    LRU_FILE = "LruFile.txt";
    static public final String    FAV_FILE = "Favorites.txt";
    static public final String    RDR_FILE = "Readers.txt";
    static public final String    HIST_FILE = "History.txt";
    static public final String    FILT_FILE = "Filters.txt";
    final String                  defReaders = ".fb2,.fb2.zip,.epub:Nomad Reader|.zip:FBReader";
    final static public String    defReader = "Nomad Reader";
    final static public int       TYPES_ACT = 1;
    final static int              CNTXT_MENU_DELETE_F=1;
    final static int              CNTXT_MENU_DELETE_D_EMPTY=2;
    final static int              CNTXT_MENU_DELETE_D_NON_EMPTY=3;
    final static int              CNTXT_MENU_ADD=4;
    final static int              CNTXT_MENU_CANCEL=5;
    final static int              CNTXT_MENU_MARK_READING = 6;
    final static int              CNTXT_MENU_MARK_FINISHED = 7;
    final static int              CNTXT_MENU_MARK_FORGET = 8;
    String                        currentRoot = "/sdcard";
    Integer                       currentPosition = -1;
    List<HashMap<String, String>> itemsArray;
    Stack<Integer>                positions = new Stack<Integer>();
    SimpleAdapter                 adapter;
    SharedPreferences             prefs;
    ReLaunchApp                   app;
    boolean                       useHome = false;
    boolean                       useShop = false;
    boolean                       useDirViewer = false;
    static public boolean         filterMyself = true;
    static public String          selfName = "ReLaunch";


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
            if (re.length != 2)
                continue;
            String rName = re[1];
            String[] exts = re[0].split(",");
            for (int j=0; j<exts.length; j++)
            {
                String ext = exts[j];
                HashMap<String, String> r = new HashMap<String, String>();
                r.put(ext, rName);
                rc.add(r);
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

        TextView  tv = (TextView)findViewById(useDirViewer ? R.id.results_title : R.id.title_txt);
        tv.setText(dir.getAbsolutePath() + " (" + ((allEntries == null) ? 0 : allEntries.length) + ")");

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
        for (String f : dirs)
        {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("name", f);
            item.put("dname", dir.getAbsolutePath());
            if (f.equals(".."))
                item.put("fname", dir.getParent());
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
        ListView lv = (ListView) findViewById(useDirViewer ? R.id.results_list : R.id.fl_list);

        adapter = new FLSimpleAdapter(this, itemsArray, useDirViewer ? R.layout.results_layout : R.layout.flist_layout, from, to);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        if (startPosition != -1)
            lv.setSelection(startPosition);
        lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    HashMap<String, String> item = itemsArray.get(position);

                    if (item.get("name").equals(".."))
                    {
                        // Goto up directory
                        File dir = new File(item.get("dname"));
                        String pdir = dir.getParent();
                        if (pdir != null)
                        {
                            Integer p = -1;
                            if (!positions.empty())
                                p = positions.pop();
                            drawDirectory(pdir, p);
                        }
                    }
                    else if (item.get("type").equals("dir"))
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
                                    builder.setTitle("Select application");
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
    }

    private HashMap<String, Drawable> createIconsList(PackageManager pm)
    {
        HashMap<String, Drawable> rc = new HashMap<String, Drawable>();

        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
            Drawable d = null;
            try {
                d = pm.getApplicationIcon(packageInfo.packageName);
            } catch(PackageManager.NameNotFoundException e) { }

            if (d != null)
                rc.put((String)pm.getApplicationLabel(packageInfo), d);
        }
        return rc;
    }

    static public List<String> createAppList(PackageManager pm)
    {
        List<String> rc = new ArrayList<String>();

        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
            Intent aIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);
            String aLabel = (String)pm.getApplicationLabel(packageInfo);
            if (aIntent != null  &&  aLabel != null)
            {
                if (!filterMyself  ||  !aLabel.equals(selfName))
                    rc.add(aLabel);
            }
        }
        Collections.sort(rc);
        return rc;
    }

    private void start(Intent i)
    {
        if (i != null)
            startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If we called from Home laucher?
        final Intent data = getIntent();
        if (data.getExtras() != null  &&  data.getBooleanExtra("home", false))
            useHome = true;
        if (data.getExtras() != null  &&  data.getBooleanExtra("shop", false))
            useShop = true;
        if (data.getExtras() != null  &&  data.getBooleanExtra("dirviewer", false))
            useDirViewer = true;

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
            app.viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize", "1048576"));
            app.editorMax = Integer.parseInt(prefs.getString("editorMaxSize", "262144"));
        } catch(NumberFormatException e) {
            app.viewerMax = 1048576;
            app.editorMax = 262144;
       }

        filterMyself = prefs.getBoolean("filterSelf", true);
        if (useShop  &&  prefs.getBoolean("shopMode", true))
            useHome = true;

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
        if (!app.readFile("startReaders", RDR_FILE, ":"))
                        app.setDefault("startReaders");
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
                            intent.putExtra("title", "Last recently used applications");
                            startActivity(intent);
                        }
                    });
                ((ImageButton)findViewById(R.id.all_applications_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
                            intent.putExtra("list", "app_all");
                            intent.putExtra("title", "All applications");
                            startActivity(intent);
                        }
                    });
                ((ImageButton)findViewById(R.id.app_favorites)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ReLaunch.this, AllApplications.class);
                            intent.putExtra("list", "app_favorites");
                            intent.putExtra("title", "Favorite applications");
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
                ((ImageButton)findViewById(R.id.types_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuTypes(); }});
                ((ImageButton)findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuSearch(); }});
                ((ImageButton)findViewById(R.id.lru_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuLastopened(); }});
                ((ImageButton)findViewById(R.id.favor_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuFavorites(); }});
                ((ImageButton)findViewById(R.id.readers_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuReaders(); }});
                ((ImageButton)findViewById(R.id.about_btn)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { menuAbout(); }});
            }

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
            case R.id.readers:
                menuReaders();
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
                menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, "Add to favorites");
            if (app.history.containsKey(fullName))
            {
                if (app.history.get(fullName) == app.READING)
                    menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
                else if (app.history.get(fullName) == app.FINISHED)
                    menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, "Remove \"read\" mark");
                menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, "Forget all marks");
            }
            else
                menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
            menu.add(Menu.NONE, CNTXT_MENU_DELETE_F, Menu.NONE, "Delete");
        }
        else
        {
            File   d = new File(fullName);
            String[] allEntries = d.list();
            if (allEntries.length > 0)
                menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_NON_EMPTY, Menu.NONE, "Delete NON-EMPTY directory!");
            else
                menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_EMPTY, Menu.NONE, "Delete empty directory");
        }
        menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
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

        switch (item.getItemId())
        {
        case CNTXT_MENU_ADD:
            app.addToList("favorites", dname, fname, true);
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
        case CNTXT_MENU_DELETE_F:
            if (prefs.getBoolean("confirmFileDelete", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete file warning");
                builder.setMessage("Are you sure to delete file \"" + fname + "\" ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeFile(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                builder.setTitle("Delete empty directory warning");
                builder.setMessage("Are you sure to delete empty directory \"" + fname + "\" ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeFile(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                builder.setTitle("Delete non empty directory warning");
                builder.setMessage("Are you sure to delete non-empty directory \"" + fname + "\" (dangerous) ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeDirectory(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

        // Memory
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        TextView tv = (TextView)findViewById(R.id.mem_level);
        if (tv != null)
            tv.setText(mi.availMem / 1048576L + "M free");

        // Battery
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    context.unregisterReceiver(this);
                    int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        level = (rawlevel * 100) / scale;
                    }
                    TextView tv = (TextView)findViewById(R.id.bat_level);
                    if (tv != null)
                        tv.setText(level + "%");
                }
            };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
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
                .setTitle("This is a launcher!")
                .setMessage("Are you sure you want to quit ?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                .setNegativeButton("NO", null)
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
        intent.putExtra("title", "Last opened");
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);
    }

    private void menuFavorites() {
        Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
        intent.putExtra("list", "favorites");
        intent.putExtra("title", "Favorites");
        intent.putExtra("rereadOnStart", true);
        startActivity(intent);
    }

    private void menuReaders() {
        Intent intent = new Intent(ReLaunch.this, ReadersActivity.class);
        startActivity(intent);
    }

    private void menuAbout() {
        app.About(this);
    }
}
