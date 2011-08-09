package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

public class TypesActivity extends Activity {
	final String                  TAG = "Types";
    HashMap<String, Drawable>     icons;
    PackageManager                pm;
    List<HashMap<String, String>> readers;
    List<String>                  applicationsArray;
   	CharSequence[]                applications;
	List<HashMap<String,String>>  itemsArray;
    TPSimpleAdapter               adapter;
	ReLaunchApp                   app;

    class TPSimpleAdapter extends SimpleAdapter {
    	final Context cntx;
    	
    	TPSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
    	{
    		super(context, data, resource, from, to);
    		cntx = context;
    	}

    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.types_layout, null);
            }
            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null)
            {
            	ImageView iv = (ImageView) v.findViewById(R.id.types_img);
            	
            	// Setting up button
            	ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
            	if (position == 0)
            	{
            		upBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
            		upBtn.setEnabled(false);
            	}
            	else
            	{
            		upBtn.setImageDrawable(getResources().getDrawable(R.drawable.arrow_up));
            		upBtn.setEnabled(true);
            	}
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
            	if (position == (itemsArray.size()-1))
            	{
            		downBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
            		downBtn.setEnabled(false);
            	}
            	else
            	{
            		downBtn.setImageDrawable(getResources().getDrawable(R.drawable.arrow_down));
            		downBtn.setEnabled(true);
            	}

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

            	// Setting extension title
            	TextView ext_title = (TextView) v.findViewById(R.id.types_ext_title);
            	ext_title.setText("Suffix (" + (position+1) + "/" + (itemsArray.size()) + ")");

            	// Setting extension
            	Button  extName = (Button)v.findViewById(R.id.types_ext);
            	extName.setText(item.get("ext"));
            	extName.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
						builder.setTitle("File suffix");
						final EditText input = new EditText(cntx);
						input.setText(item.get("ext"));
						builder.setView(input);

						builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String value = input.getText().toString();
								if (value.equals(""))
									Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
								else
								{
									itemsArray.get(position).put("ext", value);
									adapter.notifyDataSetChanged();
									dialog.dismiss();
								}
							}
						});

						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
		            			dialog.dismiss();
							}
						});

						builder.show();
					}
            	
            	});

            	// Setting application name
            	Button  appName = (Button) v.findViewById(R.id.types_app);
            	String    app = item.get("rdr");
            	appName.setText(app);
            	if(icons.containsKey(app))
        			iv.setImageDrawable(icons.get(app));
            	else
            		iv.setImageDrawable(getResources().getDrawable(R.drawable.icon));
 
            	appName.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
						builder.setTitle("Select application");
						builder.setSingleChoiceItems(applications, -1, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int i) {
						    	itemsArray.get(position).put("rdr", (String)applications[i]);
		            			adapter.notifyDataSetChanged();
		            			dialog.dismiss();
						    	//Toast.makeText(getApplicationContext(), "Selected: " + applications[i], Toast.LENGTH_SHORT).show();
						    }
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
            		
            	});
            	
            }
            return v;
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.types_view);
 
        // Global storage
    	app = ((ReLaunchApp)getApplicationContext());
    	icons = app.getIcons();

        // Recreate readers list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        readers = ReLaunch.parseReadersString(data.getExtras().getString("types"));
        applicationsArray = ReLaunch.createAppList(getPackageManager());
        applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);


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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null  ||  data.getExtras() == null)
			return;
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
			case ReLaunch.EDIT_ACT:
				int pos = data.getExtras().getInt("position");
				itemsArray.get(pos).put("ext", data.getExtras().getString("ext"));
				itemsArray.get(pos).put("rdr", data.getExtras().getString("app"));
    			adapter.notifyDataSetChanged();
				break;
			default:
				return;
		}
	}

}
