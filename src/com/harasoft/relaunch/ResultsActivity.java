package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ResultsActivity extends Activity {
    final String                  TAG = "Results";
    final int                     CNTXT_MENU_RMFAV = 1;
    final int                     CNTXT_MENU_RMFILE = 2;
    final int                     CNTXT_MENU_CANCEL = 3;
    final int                     CNTXT_MENU_MOVEUP = 4;
    final int                     CNTXT_MENU_MOVEDOWN = 5;
    final int                     CNTXT_MENU_MARK_FINISHED = 6;
    final int                     CNTXT_MENU_MARK_READING = 7;
    final int                     CNTXT_MENU_MARK_FORGET = 8;
    final int                     CNTXT_MENU_RMDIR = 9;
    ReLaunchApp                   app;
    HashMap<String, Drawable>     icons;
    String                        listName;
    String                        title;
    Boolean                       rereadOnStart = true;
    SharedPreferences             prefs;
    FLSimpleAdapter               adapter;
    ListView                      lv;
    List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
    Integer                       currentPosition = -1;
    boolean                       addSView = true;
    boolean                       oldHome;

    static class ViewHolder {
        TextView  tv1;
        TextView  tv2;
        ImageView iv;
    }
    class FLSimpleAdapter extends ArrayAdapter<HashMap<String, String>> {
        FLSimpleAdapter(Context context, int resource, List<HashMap<String, String>> data)
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

            if (position >= itemsArray.size())
            {
                v.setVisibility(View.INVISIBLE);
                tv1.setVisibility(View.INVISIBLE);
                tv2.setVisibility(View.INVISIBLE);
                iv.setVisibility(View.INVISIBLE);
                return v;
            }
            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
                String    fname = item.get("fname");
                String    dname = item.get("dname");
                String    fullName = dname + "/" + fname;
                boolean   setBold = false;
                boolean   useFaces  = prefs.getBoolean("showNew", true);

                if (useFaces)
                {
                    if (app.history.containsKey(fullName))
                    {
                        if (app.history.get(fullName) == app.READING)
                        {
                            tv1.setBackgroundColor(getResources().getColor(R.color.file_reading_bg));
                            tv1.setTextColor(getResources().getColor(R.color.file_reading_fg));
                            tv2.setBackgroundColor(getResources().getColor(R.color.file_reading_bg));
                            tv2.setTextColor(getResources().getColor(R.color.file_reading_fg));
                        }
                        else if (app.history.get(fullName) == app.FINISHED)
                        {
                            tv1.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                            tv1.setTextColor(getResources().getColor(R.color.file_finished_fg));
                            tv2.setBackgroundColor(getResources().getColor(R.color.file_finished_bg));
                            tv2.setTextColor(getResources().getColor(R.color.file_finished_fg));
                        }
                        else
                        {
                            tv1.setBackgroundColor(getResources().getColor(R.color.file_unknown_bg));
                            tv1.setTextColor(getResources().getColor(R.color.file_unknown_fg));
                            tv2.setBackgroundColor(getResources().getColor(R.color.file_unknown_bg));
                            tv2.setTextColor(getResources().getColor(R.color.file_unknown_fg));
                        }
                    }
                    else
                    {
                        tv1.setBackgroundColor(getResources().getColor(R.color.file_new_bg));
                        tv1.setTextColor(getResources().getColor(R.color.file_new_fg));
                        tv2.setBackgroundColor(getResources().getColor(R.color.file_new_bg));
                        tv2.setTextColor(getResources().getColor(R.color.file_new_fg));
                        if (getResources().getBoolean(R.bool.show_new_as_bold))
                            setBold = true;
                    }
                }

                Drawable d = app.specialIcon(fullName, item.get("type").equals("dir"));
                if (d != null)
                    iv.setImageDrawable(d);
                else
                {
                    String rdrName = app.readerName(fname);
                    if (rdrName.equals("Nope"))
                    {
                        File f = new File(fullName);
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
                        if (icons.containsKey(rdrName))
                            iv.setImageDrawable(icons.get(rdrName));
                        else
                            iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
                    }
                }

                if (useFaces)
                {
                    SpannableString s = new SpannableString(fname);
                    s.setSpan(new StyleSpan(setBold ? Typeface.BOLD : Typeface.NORMAL), 0, fname.length(), 0);
                    tv1.setText(dname);
                    tv2.setText(s);
                }
                else
                {
                    tv1.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                    tv1.setTextColor(getResources().getColor(R.color.normal_fg));
                    tv2.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                    tv2.setTextColor(getResources().getColor(R.color.normal_fg));
                    tv1.setText(dname);
                    tv2.setText(fname);
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
                if (app.filterFile(item.get("dname"), item.get("fname")))
                    newItemsArray.add(item);
            }
            itemsArray = newItemsArray;
        }
        adapter.notifyDataSetChanged();
        if (currentPosition != -1)
            lv.setSelection(currentPosition);
    }

    private void start(Intent i)
    {
        if (i != null)
            startActivity(i);
    }

    private void createItemsArray()
    {
        itemsArray = new ArrayList<HashMap<String, String>>();
        for (String[] n : app.getList(listName))
        {
            if (!prefs.getBoolean("filterResults", false)  ||  app.filterFile(n[0], n[1]))
            {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("dname", n[0]);
                item.put("fname", n[1]);
                if (n[1].equals(app.DIR_TAG))
                {
                    int ind = n[0].lastIndexOf('/');
                    if (ind == -1)
                    {
                        item.put("fname", "");
                    }
                    else
                    {
                        item.put("fname", n[0].substring(ind+1));
                        item.put("dname", n[0].substring(0, ind));
                    }
                    item.put("type", "dir");
                }
                else
                    item.put("type", "file");

                itemsArray.add(item);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.results_layout);

        icons = app.getIcons();

        // Recreate readers list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        listName = data.getExtras().getString("list");
        title = data.getExtras().getString("title");
        rereadOnStart = data.getExtras().getBoolean("rereadOnStart");
        int total = data.getExtras().getInt("total", -1);

        ((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { finish(); }});

        final Button up = (Button)findViewById(R.id.goup_btn);
        if (up != null)
            up.setEnabled(false);
        final ImageButton adv = (ImageButton)findViewById(R.id.advanced_btn);
        if (adv != null)
        {
            adv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    Intent i = new Intent(ResultsActivity.this, Advanced.class);
                    startActivity(i);
                }});

        }

        currentPosition = -1;
        lv = (ListView) findViewById(R.id.results_list);
        if (total == -1)
            ((TextView)findViewById(R.id.results_title)).setText(title + " (" + app.getList(listName).size() + ")");
        else
            ((TextView)findViewById(R.id.results_title)).setText(title + " (" + app.getList(listName).size() + "/" + total +")");

        createItemsArray();
        adapter = new FLSimpleAdapter(this, R.layout.results_item, itemsArray);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
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

                LinearLayout ll = (LinearLayout)findViewById(R.id.results_fl);
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

                    HashMap<String, String> item = itemsArray.get(position);

                    String fullName = item.get("dname") + "/" + item.get("fname");
                    currentPosition = parent.getFirstVisiblePosition();
                    if (item.get("type").equals("dir"))
                    {
                        Intent intent = new Intent(ResultsActivity.this, ReLaunch.class);
                        intent.putExtra("dirviewer", true);
                        intent.putExtra("start_dir", fullName);
                        oldHome = ReLaunch.useHome;
                        startActivityForResult(intent, ReLaunch.DIR_ACT);
                    }
                    else
                    {
                        String fileName = item.get("fname");
                        if (!app.specialAction(ResultsActivity.this, fullName))
                        {
                            if (app.readerName(fileName).equals("Nope"))
                                app.defaultAction(ResultsActivity.this, fullName);
                            else
                            {
                                // Launch reader
                                if (app.askIfAmbiguous)
                                {
                                    List<String> rdrs = app.readerNames(item.get("fname"));
                                    if (rdrs.size() < 1)
                                        return;
                                    else if (rdrs.size() == 1)
                                        start(app.launchReader(rdrs.get(0), fullName));
                                    else
                                    {
                                        final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
                                        final String rdr1 = fullName;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
                                        //builder.setTitle("Select application");
                                        builder.setTitle(getResources().getString(R.string.jv_results_select_application));
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
                                    start(app.launchReader(app.readerName(fileName), fullName));
                            }
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
                first += (total * app.scrollStep) / 100;
                if (first > total)
                    first = total;
                lv.setSelection(first);
            }});
    }

    @Override
    protected void onStart() {
        if (rereadOnStart)
            createItemsArray();
        redrawList();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.resultsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
        case R.id.mime_types:
            Intent intent1 = new Intent(ResultsActivity.this, TypesActivity.class);
            startActivityForResult(intent1, ReLaunch.TYPES_ACT);
            return true;
        case R.id.about:
            app.About(this);
            return true;
        case R.id.setting:
            Intent intent3 = new Intent(ResultsActivity.this, PrefsActivity.class);
            startActivity(intent3);
            return true;
        default:
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult, " + requestCode + " " + resultCode);
        switch (requestCode)
        {
        case ReLaunch.TYPES_ACT:
            if (resultCode != Activity.RESULT_OK)
                return;

            String newTypes = ReLaunch.createReadersString(app.getReaders());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("types", newTypes);
            editor.commit();

            redrawList();
            break;
        case ReLaunch.DIR_ACT:
            ReLaunch.useHome = oldHome;
            break;
        default:
            return;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        int pos = info.position;
        HashMap<String, String> i = itemsArray.get(pos);
        final String dr = i.get("dname");
        final String fn = i.get("fname");
        String fullName = dr + "/" + fn;

        if (i.get("type").equals("dir"))
        {
            if (pos > 0)
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, "Move one position up");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_results_move_up));
            if (pos < (itemsArray.size()-1))
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, "Move one position down");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_results_move_down));
            //menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, "Remove from favorites");
            menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources().getString(R.string.jv_results_remove));
            //menu.add(Menu.NONE, CNTXT_MENU_RMDIR, Menu.NONE, "Delete directory");
            menu.add(Menu.NONE, CNTXT_MENU_RMDIR, Menu.NONE, getResources().getString(R.string.jv_results_delete_dir));
            //menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.jv_results_cancel));
        }
        else if (listName.equals("favorites"))
        {
            if (pos > 0)
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, "Move one position up");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_results_move_up));
            if (pos < (itemsArray.size()-1))
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, "Move one position down");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_results_move_down));
            //menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, "Remove from favorites");
            menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources().getString(R.string.jv_results_remove));
            //menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, "Delete file");
            menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, getResources().getString(R.string.jv_results_delete_file));
            //menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.jv_results_cancel));
        }
        else if (listName.equals("lastOpened"))
        {
            if (app.history.containsKey(fullName))
            {
                if (app.history.get(fullName) == app.READING)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_results_mark));
                else if (app.history.get(fullName) == app.FINISHED)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, "Remove \"read\" mark");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, getResources().getString(R.string.jv_results_unmark));
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, "Forget all marks");
                menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, getResources().getString(R.string.jv_results_unmark_all));
            }
            else
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
            	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_results_mark));
            //menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, "Delete file");
            menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, getResources().getString(R.string.jv_results_delete_file));
            //menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.jv_results_cancel));
        }
        else if (listName.equals("searchResults"))
        {
            if (pos > 0)
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, "Move one position up");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE, getResources().getString(R.string.jv_results_move_up));
            if (pos < (itemsArray.size()-1))
                //menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, "Move one position down");
            	menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE, getResources().getString(R.string.jv_results_move_down));
            if (app.history.containsKey(fullName))
            {
                if (app.history.get(fullName) == app.READING)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_results_mark));
                else if (app.history.get(fullName) == app.FINISHED)
                    //menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, "Remove \"read\" mark");
                	menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE, getResources().getString(R.string.jv_results_unmark));
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, "Forget all marks");
                menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE, getResources().getString(R.string.jv_results_unmark_all));
            }
            else
                //menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, "Mark as read");
            	menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE, getResources().getString(R.string.jv_results_mark));
            //menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, "Delete file");
            menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, getResources().getString(R.string.jv_results_delete_file));
            //menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
            menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources().getString(R.string.jv_results_cancel));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getItemId() == CNTXT_MENU_CANCEL)
            return true;

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;
        HashMap<String, String> i = itemsArray.get(pos);
        final String dname = i.get("dname");
        final String fname = i.get("fname");
        String fullName = dname + "/" + fname;

        switch (item.getItemId())
        {
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
        case CNTXT_MENU_RMFAV:
            if (i.get("type").equals("dir"))
                app.removeFromList("favorites", fullName, app.DIR_TAG);
            else
                app.removeFromList("favorites", dname, fname);
            itemsArray.remove(pos);
            redrawList();
            break;
        case CNTXT_MENU_MOVEUP:
            if (pos > 0)
            {
                List<String[]>          f = app.getList(listName);
                HashMap<String, String> it = itemsArray.get(pos);
                String[]                fit = f.get(pos);

                itemsArray.remove(pos);
                f.remove(pos);
                itemsArray.add(pos-1, it);
                f.add(pos-1, fit);
                app.setList(listName, f);
                redrawList();
            }
            break;
        case CNTXT_MENU_MOVEDOWN:
            if (pos < (itemsArray.size()-1))
            {
                List<String[]>          f = app.getList(listName);
                HashMap<String, String> it = itemsArray.get(pos);
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
                redrawList();
            }
            break;
        case CNTXT_MENU_RMFILE:
            if (prefs.getBoolean("confirmFileDelete", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Delete file warning");
                builder.setTitle(getResources().getString(R.string.jv_results_delete_file_title));
                //builder.setMessage("Are you sure to delete file \"" + fname + "\" ?");
                builder.setMessage(getResources().getString(R.string.jv_results_delete_file_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_results_delete_file_text2));
                //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                builder.setPositiveButton(getResources().getString(R.string.jv_results_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            if (app.removeFile(dname, fname))
                            {
                                itemsArray.remove(pos);
                                redrawList();
                            }
                        }});
                //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                builder.setNegativeButton(getResources().getString(R.string.jv_results_no), new DialogInterface.OnClickListener() {
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
        case CNTXT_MENU_RMDIR:
            File d = new File(fullName);
            boolean isEmpty = d.list().length < 1;
            if (isEmpty)
            {
                if (prefs.getBoolean("confirmDirDelete", true))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    //builder.setTitle("Delete empty directory warning");
                    builder.setTitle(getResources().getString(R.string.jv_results_delete_em_dir_title));
                    //builder.setMessage("Are you sure to delete empty directory \"" + fname + "\" ?");
                    builder.setMessage(getResources().getString(R.string.jv_results_delete_em_dir_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_results_delete_em_dir_text2));
                    //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    builder.setPositiveButton(getResources().getString(R.string.jv_results_yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                if (app.removeFile(dname, fname))
                                {
                                    itemsArray.remove(pos);
                                    redrawList();
                                }
                            }});
                    //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    builder.setNegativeButton(getResources().getString(R.string.jv_results_no), new DialogInterface.OnClickListener() {
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
            }
            else
            {
                if (prefs.getBoolean("confirmNonEmptyDirDelete", true))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    //builder.setTitle("Delete non empty directory warning");
                    builder.setTitle(getResources().getString(R.string.jv_results_delete_ne_dir_title));
                    //builder.setMessage("Are you sure to delete non-empty directory \"" + fname + "\" (dangerous) ?");
                    builder.setMessage(getResources().getString(R.string.jv_results_delete_ne_dir_text1) + " \"" + fname + "\" " + getResources().getString(R.string.jv_results_delete_ne_dir_text2));
                    //builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    builder.setPositiveButton(getResources().getString(R.string.jv_results_yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                if (app.removeDirectory(dname, fname))
                                {
                                    itemsArray.remove(pos);
                                    redrawList();
                                }
                            }});
                    //builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    builder.setNegativeButton(getResources().getString(R.string.jv_results_no), new DialogInterface.OnClickListener() {
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
            }
            break;
        }
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG, this);
    }
}
