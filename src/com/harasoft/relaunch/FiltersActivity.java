package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class FiltersActivity  extends Activity {
	final String                  TAG = "Filters";
	ReLaunchApp                   app;
    FTArrayAdapter                adapter;
    ListView                      lv;
	List<String[]>                itemsArray = new ArrayList<String[]>();

	
    public class myOnItemSelectedListener implements OnItemSelectedListener {
    	private boolean isFirst = true;
    	final int     position;
    	final int     spos;
    	myOnItemSelectedListener(int p, int s)
    	{
    		position = p;
    		spos = s;
    	}

		public void onItemSelected(AdapterView<?> parent, View v1, int pos, long row) {
			if (isFirst)
				isFirst = false;
			else
			{
				Log.d(TAG, "position=" + position + " pos=" + pos + " spos=" + spos + " row=" + row);
				if (position >=0  &&  position < itemsArray.size())
				{
					itemsArray.set(position, new String[] { Integer.toString(pos), itemsArray.get(position)[1]});
					adapter.notifyDataSetChanged();
				}
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
    }

    static class ViewHolder {
        Spinner        method;
        ImageButton    rm;
        Button         val;
        TextView       cond;
    }
	class FTArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
    	final Context cntx;

    	FTArrayAdapter(Context context, int resource)
    	{
    		super(context, resource);
    		cntx = context;
    	}

    	@Override
		public int getCount() {
    		return itemsArray.size();
		}

		@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
            View       v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.filters_item, null);
                holder = new ViewHolder();
                holder.method = (Spinner) v.findViewById(R.id.filters_method);
                holder.rm     = (ImageButton) v.findViewById(R.id.filters_delete);
                holder.val    = (Button) v.findViewById(R.id.filters_type);
                holder.cond   = (TextView)v.findViewById(R.id.filters_condition);
                v.setTag(holder);
            }
            else
            	holder = (ViewHolder) v.getTag();

            Spinner      methodSpn = holder.method;
            ImageButton  rmBtn = holder.rm;
            Button       valBtn = holder.val;
            TextView     condTxt = holder.cond;
            
            final String[] item = itemsArray.get(position);
            if (item == null)
            	return v;

    		// Set  spinner
            Integer spos = 0;
    		try {
    			spos = Integer.parseInt(item[0]);
    		} catch(NumberFormatException e) { }

    	    ArrayAdapter<CharSequence> sadapter = ArrayAdapter.createFromResource(
    	    		cntx, R.array.filter_values, android.R.layout.simple_spinner_item);
    	    sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	    methodSpn.setAdapter(sadapter);
    	    methodSpn.setSelection(spos);
			Log.d(TAG, "-----------");
			Log.d(TAG, " getView: size=" + itemsArray.size() + " position=" + position);
			app.dumpList("itemsArray", itemsArray);
			methodSpn.setOnItemSelectedListener(new myOnItemSelectedListener(position, spos));
    	    
    	    // Set remove button
        	rmBtn.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v)
        		{
        			itemsArray.remove(position);
        			adapter.notifyDataSetChanged();
				}});

        	// Set value button
        	if (spos == app.FLT_SELECT  ||  spos == app.FLT_NEW  ||  spos == app.FLT_NEW_AND_READING)
        	{
        		valBtn.setText("");
        		valBtn.setEnabled(false);
        	}
        	else
        	{
        		valBtn.setText(item[1]);
        		valBtn.setEnabled(true);
        		valBtn.setOnClickListener(new View.OnClickListener()
        		{
        			public void onClick(View v) {
        				AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
        				builder.setTitle("Filter value:");
        				final EditText input = new EditText(cntx);
        				input.setText(item[1]);
        				builder.setView(input);

        				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int whichButton) {
        						String value = input.getText().toString();
        						if (value.equals(""))
        							Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
        						else
        						{
        							itemsArray.set(position, new String[] { item[0], value});
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
        			}});
        	}
        	
        	// Set condition text
        	if (position >= itemsArray.size()-1)
        		condTxt.setText("");
        	else if (app.filters_and)
        		condTxt.setText("AND");
        	else
        		condTxt.setText("OR");

        	return v;
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.filters_view);
 
        // Create global storage with values
        app = (ReLaunchApp)getApplicationContext();

    	lv = (ListView) findViewById(R.id.filters_lv);
    	itemsArray = app.getList("filters");
    	adapter = new FTArrayAdapter(this, R.layout.filters_item);
        lv.setAdapter(adapter);

        // OK/Save button
        Button    okBtn = (Button) findViewById(R.id.filters_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
    	    	app.setList("filters", itemsArray);
    			app.writeFile("filters", ReLaunch.FILT_FILE, 0, ":");
            	setResult(Activity.RESULT_OK);
            	finish();
   			}
		});
        
        // Add new button
        Button    addBtn = (Button) findViewById(R.id.filters_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
    			itemsArray.add(new String[] { "0", ""});
    			adapter.notifyDataSetChanged();
   			}
		});
        
        // AND/OR button
        final Button    andorBtn = (Button) findViewById(R.id.filters_andor);
		if (app.filters_and)
			andorBtn.setText("OR");
		else
			andorBtn.setText("AND");
        andorBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
    			app.filters_and = ! app.filters_and;
    			if (app.filters_and)
    				andorBtn.setText("OR");
    			else
    				andorBtn.setText("AND");
    			adapter.notifyDataSetChanged();
   			}
		});
        
        // Cancel button
        Button    cancelBtn = (Button) findViewById(R.id.filters_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v)
    		{
            	setResult(Activity.RESULT_CANCELED);
            	finish();
			}
		});

	}
}
