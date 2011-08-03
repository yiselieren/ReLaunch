package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ReLaunch extends Activity {
	
	final String                  TAG = "ReLaunch";
	List<String>                  apps = new ArrayList<String>();
    List<HashMap<String, String>> readers;
	List<HashMap<String, String>> itemsArray;
	Stack<Integer>                positions = new Stack<Integer>();
    SimpleAdapter                 adapter;
    PackageManager                pm;
    HashMap<String, Drawable>     icons = new HashMap<String, Drawable>();
    
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
                v = vi.inflate(R.layout.flist_layout, null);
            }
            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
            	TextView  tv = (TextView) v.findViewById(R.id.fl_text);
            	ImageView iv = (ImageView) v.findViewById(R.id.fl_icon);
            	String    sname = item.get("name");
            	if (tv != null)
            		tv.setText(sname);
            	if(iv != null)
            	{
            		if (item.get("type").equals("dir"))
            			iv.setImageDrawable(getResources().getDrawable(R.drawable.dir_ok));
            		else
            		{
            			String rdrName = item.get("reader");
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
            }
            return v;
    	}
    }

    private Intent getIntentByLabel(String label)
    {
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

    public String readerName(String file)
    {
    	for (HashMap<String, String> r : readers)
    	{
    		for (String key : r.keySet())
    		{
    			if (file.endsWith(key))
    				return r.get(key);
    		} 		
    	}
    	return "Nope";
    }

    private void drawDirectory(String root, Integer startPosition)
    {
    	File         dir = new File(root);
    	File[]       allEntries = dir.listFiles();    	
    	List<String> files = new ArrayList<String>();
    	List<String> dirs = new ArrayList<String>();

    	TextView  tv = (TextView)findViewById(R.id.title_txt);
    	tv.setText(dir.getAbsolutePath());

    	itemsArray = new ArrayList<HashMap<String,String>>();
    	if (dir.getParent() != null)
    		dirs.add("..");
    	if (allEntries != null)
    	{
    		for (File entry : allEntries)
    		{
    			if (entry.isDirectory())
    				dirs.add(entry.getName());
    			else
    				files.add(entry.getName()); 
    		}
    	}
    	Collections.sort(dirs);
    	Collections.sort(files);
    	for (String f : dirs)
    	{
    		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("name", f);
    		item.put("dname", dir.getAbsolutePath());
    		if (f.equals(".."))
    			item.put("fname", dir.getParent());
    		else
    			item.put("fname", dir.getAbsolutePath() + "/" + f);
    		item.put("type", "dir");
    		item.put("reader", "Nope");
    		itemsArray.add(item);
    	}
    	for (String f : files)
    	{
    		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("name", f);
    		item.put("dname", dir.getAbsolutePath());
    		item.put("fname", dir.getAbsolutePath() + "/" + f);
    		item.put("type", "file");
    		item.put("reader", readerName(f));
    		itemsArray.add(item);
    	}
    	
    	String[] from = new String[] { "name" };
    	int[]    to   = new int[] { R.id.fl_text };
    	ListView lv = (ListView) findViewById(R.id.fl_list);

        adapter = new FLSimpleAdapter(this, itemsArray, R.layout.flist_layout, from, to);
        lv.setAdapter(adapter);
        if (startPosition != -1)
        	lv.setSelection(startPosition);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	HashMap<String, String> item = itemsArray.get(position);      
                
            	if (item.get("name").equals(".."))
            	{
            		// Goto up directory
            		File dir = new File(item.get("dname"));
            		String pdir = dir.getParent();
            		if (pdir != null)
            		{
            			Integer p = -1;
            			if (!positions.empty())
            				p = positions.pop();
            			drawDirectory(pdir, p);
            		}
            	}
            	else if (item.get("type").equals("dir"))
            	{
            		// Goto directory
        			positions.push(position);
            		drawDirectory(item.get("fname"), -1);
            	}
            	else if (!item.get("reader").equals("Nope"))
            	{
            		// Launch reader
        			//launchReader(item.get("fname"));
            		launchReader(item.get("reader"), item.get("fname"));
            	}
            }});
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pm = getPackageManager();

        // Readers list
        readers = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> r = new HashMap<String, String>();
        r.put(".fb2.zip", "Nomad Reader");
        r.put(".fb2", "Nomad Reader");
        r.put(".zip", "Nomad Reader");
        r.put(".epub", "Nomad Reader");
        readers.add(r);

        // Create application icons map
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Drawable d = null;
    		try {
    			d = pm.getApplicationIcon(packageInfo.packageName);
    		} catch(PackageManager.NameNotFoundException e) { }
        
    		if (d != null)
    			icons.put((String)pm.getApplicationLabel(packageInfo), d);
        }
        
        // Create applications label list
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Intent aIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);
        	String aLabel = (String)pm.getApplicationLabel(packageInfo);
        	if (aIntent != null  &&  aLabel != null)
        		//appSet.add(aLabel);
        		apps.add(aLabel);
        }
        Collections.sort(apps);
        drawDirectory("/sdcard", -1);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.setting:
            	Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.about:
				Intent intent = new Intent(ReLaunch.this, AboutActivity.class);
		        startActivity(intent);
				return true;
			default:
				return true;
		}
	}
}