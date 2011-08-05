package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class TypesActivity extends Activity {
	final String                 TAG = "Types";
    HashMap<String, Drawable>     icons;
    PackageManager                pm;
    List<HashMap<String, String>> readers;
	ArrayList<HashMap<String,String>> itemsArray;
    TPSimpleAdapter               adapter;

    class TPSimpleAdapter extends SimpleAdapter {
    	TPSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
    	{
    		super(context, data, resource, from, to);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.types_layout, null);
            }
            HashMap<String, String> item = itemsArray.get(position);
            if (item != null)
            {
            	ImageView iv = (ImageView) v.findViewById(R.id.types_img);
            	Button    appBtn = (Button) v.findViewById(R.id.types_btn);
            	EditText  editExt = (EditText)v.findViewById(R.id.types_ext);
            	String    appName = item.get("rdr");
            	editExt.setText(item.get("ext"));
            	appBtn.setText(appName);
            	if(icons.containsKey(appName))
        			iv.setImageDrawable(icons.get(appName));
            	else
            		iv.setImageDrawable(getResources().getDrawable(R.drawable.icon));
            }
            return v;
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.types_view);
 
        // Create application icons map
        icons = ReLaunch.createIconsList(getPackageManager());

        // Recreate readers list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        readers = ReLaunch.parseReadersString(data.getExtras().getString("types"));
        
        // Preliminary fill listview
    	String[] from = new String[] { "ext" };
    	int[]    to   = new int[] { R.id.types_ext };
    	ListView lv = (ListView) findViewById(R.id.types_lv);

    	itemsArray = new ArrayList<HashMap<String,String>>();
    	for (HashMap<String, String> r : readers)
    	{
    		for (String k : r.keySet())
    		{
        		HashMap<String,String> i = new HashMap<String,String>();

    			i.put("ext", k);
    			i.put("rdr", r.get(k));
    			itemsArray.add(i);
    		}
    	}
    	adapter = new TPSimpleAdapter(this, itemsArray, R.layout.types_layout, from, to);
        lv.setAdapter(adapter);
	}
}
