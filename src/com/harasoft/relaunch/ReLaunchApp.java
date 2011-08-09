package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class ReLaunchApp extends Application {
	final String                            TAG = "ReLaunchApp";
	private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
	private HashMap<String, Drawable>       icons;
    private List<HashMap<String, String>>   readers;
    private List<String>                    apps;



	// Miscellaneous public flags
	public Boolean                          askIfAmbiguous;

	// General lists
	public List<String[]> getList(String name) {
		if (m.containsKey(name))
			return m.get(name);
		else
			return new ArrayList<String[]>();
	}
	public void setList(String name, List<String[]> l) {
		m.put(name, l);
	}
	public void dumpLists()
	{
		for (String k : m.keySet())
		{
			Log.d(TAG, "LIST: \"" + k + "\"");
			for (String[] n : m.get(k))
				Log.d(TAG, "    \"" + n[0] + "\"; \"" + n[1] + "\"");
		}
		
	}
	
	// Icons
    public HashMap<String, Drawable> getIcons()       { return icons; }
    public void setIcons(HashMap<String, Drawable> i) { icons = i;    }

    // Applications
    public List<String>  getApps()      { return apps; }
    public void setApps(List<String> a) { apps = a;    }

    // Readers
    public List<HashMap<String, String>>  getReaders()      { return readers; }
    public void setReaders(List<HashMap<String, String>> r) { readers = r;    }
    public String readerName(String file) {
    	for (HashMap<String, String> r : readers) {
    		for (String key : r.keySet()) {
    			if (file.endsWith(key))
    				return r.get(key);
    		} 		
    	}
    	return "Nope";
    }
    public List<String> readerNames(String file) {
    	List<String> rc = new ArrayList<String>();
    	for (HashMap<String, String> r : readers) {
    		for (String key : r.keySet()) {
    			if (file.endsWith(key)  &&  !rc.contains(r.get(key)))
    				rc.add(r.get(key));
    		} 		
    	}
    	return rc;
    }

    // common utility - get intent by label, null if not found
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
    
    // common utility - launch reader by reader name and full file name
    public Intent launchReader(String name, String file)
    {
    	if (name.equals("Nomad Reader"))
    	{
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW); 
            i.setDataAndType(Uri.parse("file://" + file), "application/fb2"); 
            return i;
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
            	return i;
            }
    	}
    	return null;
    }
}
