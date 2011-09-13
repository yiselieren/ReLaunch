package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AllApplications extends Activity {
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
    ListView                      lv;
    String                        listName;
    //SharedPreferences             prefs;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_applications);
        
        //prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	app = ((ReLaunchApp)getApplicationContext());
    	icons = app.getIcons();
    	
        // Create applications list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        listName = data.getExtras().getString("list");

        if (listName.equals("app_all"))
        	itemsArray = app.getApps();
        else
        {
        	List<String[]> lit = app.getList(listName);
        	itemsArray = new ArrayList<String>();
        	for (String[] r : lit)
        		itemsArray.add(r[0]);
        }
    	
    	adapter = new AppAdapter(this, R.layout.applications_item, itemsArray);
    	lv = (ListView) findViewById(R.id.app_list);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	String item = itemsArray.get(position);
            	Intent i = app.getIntentByLabel(item);
            	if (i == null)
            		Toast.makeText(AllApplications.this, "Activity \"" + item + "\" not found!", Toast.LENGTH_SHORT).show();
            	else
            	{
            		boolean ok = true;
            		try {
            			startActivity(i);
            		} catch (ActivityNotFoundException e) {
            			Toast.makeText(AllApplications.this, "Activity \"" + item + "\" not found!", Toast.LENGTH_SHORT).show();
            			ok = false;
            		}
            		if (ok)
            			app.addToList("app_last", item, "", false);

            	}
 			}});
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
		String i = itemsArray.get(pos);

		switch (item.getItemId())
		{
		case CNTXT_MENU_MOVEUP:
			break;
		case CNTXT_MENU_MOVEDOWN:
			break;
		case CNTXT_MENU_RMFAV:
			break;
		case CNTXT_MENU_ADDFAV:
			app.addToList("app_favorites", i, "", true);
			break;
		case CNTXT_MENU_UNINSTALL:
			break;
		}
		return true;
	}
}
