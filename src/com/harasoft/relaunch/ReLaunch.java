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
	final String                  defReaders = ".fb2,.fb2.zip,.epub:Nomad Reader|.zip:FBReader";
	final static public String    defReader = "Nomad Reader";
	final static public int       TYPES_ACT = 1;
	final static public int       EDIT_ACT = 1;
	String                        currentRoot = "/sdcard";
	Integer                       currentPosition = -1;
	List<String>                  apps;
    List<HashMap<String, String>> readers;
	List<HashMap<String, String>> itemsArray;
	Stack<Integer>                positions = new Stack<Integer>();
    SimpleAdapter                 adapter;
    HashMap<String, Drawable>     icons;
    
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

    public static List<HashMap<String, String>> parseReadersString(String readerList)
    {
    	List<HashMap<String, String>> rc = new ArrayList<HashMap<String,String>>();
        String[] rdrs = readerList.split("\\|");
        for (int i=0; i<rdrs.length; i++)
        {
        	String[] re = rdrs[i].split(":");
        	if (re.length != 2)
        		continue;
        	String rName = re[1];
    		String[] exts = re[0].split(",");
           	for (int j=0; j<exts.length; j++)
        	{
           		String ext = exts[j];
        		HashMap<String, String> r = new HashMap<String, String>();
        		r.put(ext, rName);
        		rc.add(r);
        	}
        }
        return rc;
    }
    
    public static String createReadersString(List<HashMap<String, String>> rdrs)
    {
    	String rc = new String();
    	
       	for (HashMap<String, String> r : rdrs)
    	{
    		for (String key : r.keySet())
    		{
    			if (!rc.equals(""))
    				rc += "|";
    			rc += key + ":" + r.get(key);
    		} 		
    	}
       	return rc;
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

    	currentRoot = root;
    	currentPosition = startPosition;
    	
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

    public static HashMap<String, Drawable> createIconsList(PackageManager pm)
    {
        HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
        
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Drawable d = null;
    		try {
    			d = pm.getApplicationIcon(packageInfo.packageName);
    		} catch(PackageManager.NameNotFoundException e) { }
        
    		if (d != null)
    			rc.put((String)pm.getApplicationLabel(packageInfo), d);
        }
        return rc;
    }
    
    public static List<String> createAppList(PackageManager pm)
    {
    	List<String> rc = new ArrayList<String>();
    	
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Intent aIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);
        	String aLabel = (String)pm.getApplicationLabel(packageInfo);
        	if (aIntent != null  &&  aLabel != null)
        		//appSet.add(aLabel);
        		rc.add(aLabel);
        }
        Collections.sort(rc);
        return rc;
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Readers list
        readers = parseReadersString(defReaders);
        //Log.d(TAG, "Readers string: \"" + createReadersString(readers) + "\"");

        // Create application icons map
        icons = createIconsList(getPackageManager());
        
        // Create applications label list
        apps = createAppList(getPackageManager());
        
        // First directory to get to
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
			case R.id.mime_types:
				Intent intent1 = new Intent(ReLaunch.this, TypesActivity.class);
				intent1.putExtra("types", createReadersString(readers));
		        startActivityForResult(intent1, TYPES_ACT);
		        return true;
			case R.id.about:
				Intent intent2 = new Intent(ReLaunch.this, AboutActivity.class);
		        startActivity(intent2);
				return true;
			default:
				return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null  ||  data.getExtras() == null)
			return;
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
			case TYPES_ACT:
				readers = parseReadersString(data.getExtras().getString("types"));
				drawDirectory(currentRoot, currentPosition);
				break;
			default:
				return;
		}
	}
}