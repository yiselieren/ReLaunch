package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AllApplications extends Activity {
	ReLaunchApp                   app;
    HashMap<String, Drawable>     icons;
    Boolean                       rereadOnStart = false;
	List<String>                  itemsArray = new ArrayList<String>();
    AppAdapter                    adapter;
    ListView                      lv;
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
    	itemsArray = app.getApps();
    	
    	adapter = new AppAdapter(this, R.layout.applications_item, itemsArray);
    	lv = (ListView) findViewById(R.id.app_list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	String item = itemsArray.get(position);
            	Intent i = app.getIntentByLabel(item);
            	if (i == null)
            		Toast.makeText(AllApplications.this, "Activity \"" + item + "\" not found!", Toast.LENGTH_SHORT).show();
            	else
            		startActivity(i);
            	}
 			});

	}
}
