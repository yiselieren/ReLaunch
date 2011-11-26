package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AllApplications extends Activity {
    final String                  TAG = "AllApps";

    final int                     UNINSTALL_ACT = 1;

    final int                     CNTXT_MENU_RMFAV = 1;
    final int                     CNTXT_MENU_ADDFAV = 2;
    final int                     CNTXT_MENU_UNINSTALL = 3;
    final int                     CNTXT_MENU_CANCEL = 4;
    final int                     CNTXT_MENU_MOVEUP = 5;
    final int                     CNTXT_MENU_MOVEDOWN = 6;

    ReLaunchApp                   app;
    HashMap<String, Drawable>     icons;
    Boolean                       rereadOnStart = false;
    List<String>                  itemsArray = new ArrayList<String>();
    AppAdapter                    adapter;
    GridView                      lv;
    //ListView                      lv;
    String                        listName;
    String                        title;
    SharedPreferences             prefs;
    boolean                       addSView = true;

    static class ViewHolder {
        TextView  tv;
        ImageView iv;
    }
    class AppAdapter extends ArrayAdapter<String> {
        AppAdapter(Context context, int resource, List<String> data)
        {
            super(context, resource, data);
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
                v = vi.inflate(R.layout.applications_item, null);
                holder = new ViewHolder();
                holder.tv = (TextView) v.findViewById(R.id.app_name);
                holder.iv  = (ImageView) v.findViewById(R.id.app_icon);
                v.setTag(holder);
            }
            else
                holder = (ViewHolder) v.getTag();

                TextView  tv = holder.tv;
                ImageView iv = holder.iv;

                String item = itemsArray.get(position);
            if (item != null)
            {
                tv.setText(item);
                        iv.setImageDrawable(app.getIcons().get(item));
            }
            return v;
        }
    }

    private void saveLast()
    {
        int appLruMax = 30;
        try {
            appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
        } catch(NumberFormatException e) { }
        app.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
    }

    private void saveFav()
    {
        int appFavMax = 30;
        try {
            appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
        } catch(NumberFormatException e) { }
        app.writeFile("app_favorites", ReLaunch.APP_FAV_FILE, appFavMax, ":");
    }

    private void checkListByName(String lName, List<String> master)
    {
        List<String[]> rc = app.getList(lName);
        List<String[]> rc1 = new ArrayList<String[]>();

        for (String[] r : rc)
        {
            boolean inMaster = false;
            for (String m : master)
            {
                if (m.equals(r[0]))
                {
                    inMaster = true;
                    break;
                }
            }
            if (inMaster)
                rc1.add(r);
        }
        app.setList(lName, rc1);

    }

    private List<String> checkList(List<String> lst, List<String> master)
    {
        List<String> rc = new ArrayList<String>();
        for (String s : lst)
        {
            boolean inMaster = false;
            for (String m : master)
            {
                if (m.equals(s))
                {
                    inMaster = true;
                    break;
                }
            }
            if (inMaster)
                rc.add(s);
        }
        return rc;
    }

        // REREAD application list, check that  favorites and last lists don't contain extra applications
    private void rereadAppList()
    {
        app.setApps(ReLaunch.createAppList(getPackageManager()));
        checkListByName("app_last", app.getApps());
        checkListByName("app_favorites", app.getApps());
        itemsArray = checkList(itemsArray, app.getApps());
        saveLast();
        saveFav();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.all_applications);
        icons = app.getIcons();

        // Create applications list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        listName = data.getExtras().getString("list");
        title = data.getExtras().getString("title");

        ((ImageButton)findViewById(R.id.app_btn)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { finish(); }});

        if (listName.equals("app_all"))
        {
            app.setApps(ReLaunch.createAppList(getPackageManager()));
            checkListByName("app_last", app.getApps());
            checkListByName("app_favorites", app.getApps());
            saveLast();
            saveFav();
            itemsArray = app.getApps();
        }
        else
        {
            List<String[]> lit = app.getList(listName);
            itemsArray = new ArrayList<String>();
            for (String[] r : lit)
                itemsArray.add(r[0]);
        }
        ((TextView)findViewById(R.id.app_title)).setText(title + " (" + itemsArray.size() + ")");

        adapter = new AppAdapter(this, R.layout.applications_item, itemsArray);
        lv = (GridView) findViewById(R.id.app_grid);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
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

                LinearLayout ll = (LinearLayout)findViewById(R.id.app_grid_layout);
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
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = itemsArray.get(position);
                Intent i = app.getIntentByLabel(item);
                if (i == null)
                    Toast.makeText(AllApplications.this, "Activity \"" + item + "\" not found!", Toast.LENGTH_LONG).show();
                else
                {
                    boolean ok = true;
                    try {
                        startActivity(i);
                        if (prefs.getBoolean("returnToMain", false))
                        	finish();
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(AllApplications.this, "Activity \"" + item + "\" not found!", Toast.LENGTH_LONG).show();
                        ok = false;
                    }
                    if (ok)
                    {
                        app.addToList("app_last", item, "X", false);
                        saveLast();
                    }
                }
            }});
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (listName.equals("app_all"))
            rereadAppList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (listName.equals("app_all"))
            rereadAppList();
    }

    @Override
        protected void onResume() {
        super.onResume();
        if (listName.equals("app_all"))
            rereadAppList();
        app.generalOnResume(TAG, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        int pos = info.position;
        String i = itemsArray.get(pos);

        if (listName.equals("app_favorites"))
        {
            if (pos > 0)
                menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, "Move one position up");
            if (pos < (itemsArray.size()-1))
                menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, "Move one position down");
            menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, "Remove from favorites");
            menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, "Uninstall");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
        }
        else
        {
            List<String[]> lit = app.getList("app_favorites");
            boolean in_fav = false;
            for (String[] r : lit)
            {
                if (r[0].equals(i))
                {
                    in_fav = true;
                    break;
                }
            }
            if (!in_fav)
                menu.add(Menu.NONE, CNTXT_MENU_ADDFAV, Menu.NONE, "Add to favorites");
            menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, "Uninstall");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getItemId() == CNTXT_MENU_CANCEL)
            return true;

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;
        String it = itemsArray.get(pos);

        switch (item.getItemId())
        {
        case CNTXT_MENU_MOVEUP:
            if (pos > 0)
            {
                List<String[]>   f = app.getList(listName);
                String[]         fit = f.get(pos);

                itemsArray.remove(pos);
                f.remove(pos);
                itemsArray.add(pos-1, it);
                f.add(pos-1, fit);
                app.setList(listName, f);
                saveFav();
                adapter.notifyDataSetChanged();
            }
            break;
        case CNTXT_MENU_MOVEDOWN:
            if (pos < (itemsArray.size()-1))
            {
                List<String[]>          f = app.getList(listName);
                String[]                fit = f.get(pos);

                int size = itemsArray.size();
                itemsArray.remove(pos);
                f.remove(pos);
                if (pos+1 >= size-1)
                {
                    itemsArray.add(it);
                    f.add(fit);
                }
                else
                {
                    itemsArray.add(pos+1, it);
                    f.add(pos+1, fit);
                }
                app.setList(listName, f);
                saveFav();
                adapter.notifyDataSetChanged();
            }
            break;
        case CNTXT_MENU_RMFAV:
            app.getList(listName).remove(pos);
            itemsArray.remove(pos);
            saveFav();
            adapter.notifyDataSetChanged();
            break;
        case CNTXT_MENU_ADDFAV:
            app.addToList("app_favorites", it, "X", true);
            saveFav();
            break;
        case CNTXT_MENU_UNINSTALL:
            //Toast.makeText(AllApplications.this, "Uninstall is not implemented yet", Toast.LENGTH_LONG).show();
            PackageManager pm = getPackageManager();
            PackageInfo    pi = null;

            //try {
            //      pi = pm.getPackageInfo(it, PackageManager.GET_ACTIVITIES);
            //} catch (PackageManager.NameNotFoundException e) {
            //      pi = null;
            //}
            for (PackageInfo packageInfo : pm.getInstalledPackages(0))
            {
                if (it.equals(pm.getApplicationLabel(packageInfo.applicationInfo)))
                {
                    pi = packageInfo;
                    break;
                }
            }

            if (pi == null)
                Toast.makeText(AllApplications.this, "PackageInfo not found for label \"" + it + "\"", Toast.LENGTH_LONG).show();
            else
            {
                //Toast.makeText(AllApplications.this, "Package name is \"" + pi.packageName + "\" for label \"" + it + "\"", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", pi.packageName, null));
                try {
                    startActivityForResult(intent, UNINSTALL_ACT);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(AllApplications.this, "Activity \"" + pi.packageName + "\" not found", Toast.LENGTH_LONG).show();
                    return true;
                }
            }

            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult, requestCode=" + requestCode + ",  resultCode=" + resultCode);
        switch (requestCode)
        {
        case UNINSTALL_ACT:
            rereadAppList();
            break;
        default:
            return;
        }
    }
}
