package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ResultsActivity extends Activity {
	final String                  TAG = "Results";
	ReLaunchApp                   app;
    HashMap<String, Drawable>     icons;
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();

    class FLSimpleAdapter extends SimpleAdapter {
    	FLSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
    	{
    		super(context, data, resource, from, to);
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
            		String rdrName = ReLaunch.readerName(fname);
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

    private Intent getIntentByLabel(String label)
    {
        PackageManager pm = getPackageManager();;

        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	if (label.equals(pm.getApplicationLabel(packageInfo)))
        		return pm.getLaunchIntentForPackage(packageInfo.packageName);
        }
        return null;
    }
    
    private void launchReader(String name, String file)
    {
    	if (name.equals("Nomad Reader"))
    	{
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW); 
            i.setDataAndType(Uri.parse("file://" + file), "application/fb2"); 
            startActivity(i);
    	}
    	else
    	{
            Intent i = getIntentByLabel(name);
            if (i == null)
            	Toast.makeText(this, "Activity \"" + name + "\" not found!", Toast.LENGTH_SHORT).show();
            else
            {
            	i.setAction(Intent.ACTION_VIEW); 
            	i.setData(Uri.parse("file://" + file));
            	startActivity(i);
            }
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.main_nobuttons);
    	app = ((ReLaunchApp)getApplicationContext());
    	icons = app.getIcons();
    	
        // Recreate readers list
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        String listName = data.getExtras().getString("list");
        String title = data.getExtras().getString("title");

    	String[] from = new String[] { "fname", "dname" };
    	int[]    to   = new int[] { R.id.res_fname, R.id.res_dname };
    	ListView lv = (ListView) findViewById(R.id.fl_list);
    	((TextView)findViewById(R.id.title_txt)).setText(title);

    	for (String[] n : app.getList(listName))
    	{
       		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("dname", n[0]);
    		item.put("fname", n[1]);
    		Log.d(TAG, n[0] + ":" + n[1]);
    		itemsArray.add(item);
    	}
    	SimpleAdapter adapter = new FLSimpleAdapter(this, itemsArray, R.layout.results_item, from, to);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	HashMap<String, String> item = itemsArray.get(position);      
 
             	String fullName = item.get("dname") + "/" + item.get("fname");
             	String fileName = item.get("fname");
             	if (!ReLaunch.readerName(fileName).equals("Nope"))
            	{
            		// Launch reader
            		if (app.askIfAmbiguous)
            		{
            			List<String> rdrs = ReLaunch.readerNames(item.get("fname"));
            			if (rdrs.size() < 1)
            				return;
            			else if (rdrs.size() == 1)
            				launchReader(rdrs.get(0), fullName);
            			else
            			{
            			   	final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
            			   	final String rdr1 = fullName;
    						AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
    						builder.setTitle("Select application");
    						builder.setSingleChoiceItems(applications, -1, new DialogInterface.OnClickListener() {
    						    public void onClick(DialogInterface dialog, int i) {
    						    	launchReader((String)applications[i], rdr1);
    		            			dialog.dismiss();
    						    }
    						});
    						AlertDialog alert = builder.create();
    						alert.show();

            			}
            		}
            		else
            			launchReader(ReLaunch.readerName(fileName), fullName);
            	}
 			}});
	}

}
