package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ResultsActivity extends Activity {
	final String                  TAG = "Results";
	final int                     CNTXT_MENU_RMFAV = 1;
	final int                     CNTXT_MENU_RMFILE = 2;
	final int                     CNTXT_MENU_CANCEL = 3;
	ReLaunchApp                   app;
    HashMap<String, Drawable>     icons;
    String                        listName;
    String                        title;
    Boolean                       rereadOnStart = false;
    SharedPreferences             prefs;
    FLSimpleAdapter               adapter;
    ListView                      lv;
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();

	class FLSimpleAdapter extends ArrayAdapter<HashMap<String, String>> {
    	FLSimpleAdapter(Context context, int resource, List<HashMap<String, String>> data)
    	{
    		super(context, resource, data);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		//return super.getView(position, convertView, parent);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.results_item, null);
            }

            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
            	TextView  tv1 = (TextView) v.findViewById(R.id.res_dname);
            	TextView  tv2 = (TextView) v.findViewById(R.id.res_fname);
            	ImageView iv = (ImageView) v.findViewById(R.id.res_icon);
            	String    fname = item.get("fname");
            	String    dname = item.get("dname");
            	if (tv1 != null)
            		tv1.setText(dname);
            	if (tv2 != null)
            		tv2.setText(fname);
            	if (iv != null)
            	{
            		String rdrName = app.readerName(fname);
            		if (rdrName.equals("Nope"))
            			iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
            		else
            		{
            			if (icons.containsKey(rdrName))
            				iv.setImageDrawable(icons.get(rdrName));
            			else
            				iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
            		}
            	}
            }
            return v;
    	}
    }

    private void start(Intent i)
    {
    	if (i != null)
    		startActivity(i);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.results_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	app = ((ReLaunchApp)getApplicationContext());
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

    	((ImageButton)findViewById(R.id.results_btn)).setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) { finish(); }});

    	lv = (ListView) findViewById(R.id.results_list);
    	((TextView)findViewById(R.id.results_title)).setText(title + " (" + app.getList(listName).size() + ")");

    	Log.d(TAG, "listname=" + listName + " title=" + title);
    	for (String[] n : app.getList(listName))
    	{
       		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("dname", n[0]);
    		item.put("fname", n[1]);
    		Log.d(TAG, n[0] + ":" + n[1]);
    		itemsArray.add(item);
    	}
    	adapter = new FLSimpleAdapter(this, R.layout.results_item, itemsArray);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	HashMap<String, String> item = itemsArray.get(position);      
 
             	String fullName = item.get("dname") + "/" + item.get("fname");
             	String fileName = item.get("fname");
             	if (!app.readerName(fileName).equals("Nope"))
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
            			start(app.launchReader(app.readerName(fileName), fullName));
            	}
 			}});
	}

	@Override
	protected void onStart() {
		if (rereadOnStart)
		{
			itemsArray = new ArrayList<HashMap<String, String>>();
			for (String[] n : app.getList(listName))
			{
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("dname", n[0]);
				item.put("fname", n[1]);
				Log.d(TAG, n[0] + ":" + n[1]);
				itemsArray.add(item);
			}
			adapter.notifyDataSetChanged();
		}
		super.onStart();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		if (listName.equals("favorites"))
		{
    		menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, "Remove from favorites");
    		menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, "Delete file");
    		menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
		}
		else if (listName.equals("lastOpened"))
		{
    		menu.add(Menu.NONE, CNTXT_MENU_RMFILE, Menu.NONE, "Delete file");
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
		HashMap<String, String> i = itemsArray.get(pos);
		final String dname = i.get("dname");
		final String fname = i.get("fname");

		switch (item.getItemId())
		{
		case CNTXT_MENU_RMFAV:
		    app.removeFromList("favorites", dname, fname);
			itemsArray.remove(pos);
			adapter.notifyDataSetChanged();
			break;
		case CNTXT_MENU_RMFILE:
			if (prefs.getBoolean("confirmFileDelete", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Are you sure to delete file \"" + fname + "\"?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							if (app.removeFile(dname, fname))
							{
								itemsArray.remove(pos);
								adapter.notifyDataSetChanged();
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
				adapter.notifyDataSetChanged();
			}
			break;
		}
		return true;
	}
}
