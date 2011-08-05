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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TypesActivity extends Activity {
	final String                 TAG = "Types";
    HashMap<String, Drawable>     icons;
    PackageManager                pm;
    List<HashMap<String, String>> readers;
	List<HashMap<String,String>>  itemsArray;
    TPSimpleAdapter               adapter;

    class TPSimpleAdapter extends SimpleAdapter {
    	TPSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
    	{
    		super(context, data, resource, from, to);
    	}

    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.types_layout, null);
            }
            HashMap<String, String> item = itemsArray.get(position);
            if (item != null)
            {
            	ImageView iv = (ImageView) v.findViewById(R.id.types_img);
            	
            	// Setting up button
            	ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
            	upBtn.setEnabled(position != 0);
            	upBtn.setOnClickListener(new View.OnClickListener() {
            		public void onClick(View v)
            		{
            			HashMap<String,String> i = itemsArray.get(position);
            			itemsArray.remove(position);
            			itemsArray.add(position-1, i);
            			adapter.notifyDataSetChanged();
					}
				});
            	
            	// Setting down button
            	ImageButton downBtn = (ImageButton) v.findViewById(R.id.types_down);
            	downBtn.setEnabled(position != (itemsArray.size()-1));
            	downBtn.setOnClickListener(new View.OnClickListener() {
            		public void onClick(View v)
            		{
            			HashMap<String,String> i = itemsArray.get(position);
            			itemsArray.remove(position);
            			itemsArray.add(position+1, i);
            			adapter.notifyDataSetChanged();
					}
				});
            	
            	// Setting remove button
            	ImageButton rmBtn = (ImageButton) v.findViewById(R.id.types_delete);
            	rmBtn.setEnabled(itemsArray.size() > 1);
            	rmBtn.setOnClickListener(new View.OnClickListener() {
            		public void onClick(View v)
            		{
            			itemsArray.remove(position);
            			adapter.notifyDataSetChanged();
					}
				});

            	// Setting extension
            	EditText  editExt = (EditText)v.findViewById(R.id.types_ext);
            	editExt.setText(item.get("ext"));
            	
            	// Setting application select button
            	Button    appBtn = (Button) v.findViewById(R.id.types_btn);
            	String    appName = item.get("rdr");
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
        
        // Fill listview with our info
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
        
        // OK/Save button
        Button    okBtn = (Button) findViewById(R.id.types_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
    			readers.clear();
    	    	for (HashMap<String, String> r : itemsArray)
    	    	{
    	    		HashMap<String, String> a = new HashMap<String, String>();
    	    		a.put(r.get("ext"), r.get("rdr"));
    	    		readers.add(a);
    	    	}
    			data.putExtra("types", ReLaunch.createReadersString(readers));
              	setResult(Activity.RESULT_OK, data);
            	finish();
   			}
		});
        
        // Add new button
        Button    addBtn = (Button) findViewById(R.id.types_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
    			HashMap<String,String> i = new HashMap<String,String>();
    			i.put("ext", ".");
    			i.put("rdr", ReLaunch.defReader);
    			itemsArray.add(i);
    			adapter.notifyDataSetChanged();
			}
		});
        
        // Cancel button
        Button    cancelBtn = (Button) findViewById(R.id.types_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
            	setResult(Activity.RESULT_CANCELED);
            	finish();
			}
		});
	}
}
