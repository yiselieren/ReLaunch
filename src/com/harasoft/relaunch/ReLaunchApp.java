package com.harasoft.relaunch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class ReLaunchApp extends Application {
	final String                            TAG = "ReLaunchApp";

	final int                               FileBufferSize = 1024;

	// Book status
	final int                               READING=1;
	final int                               FINISHED=2;
	
	// Filter values
	public int                        FLT_SELECT;
	public int                        FLT_STARTS;
	public int                        FLT_ENDS;
	public int                        FLT_CONTAINS;
	public int                        FLT_MATCHES;
	public int                        FLT_NEW;
	public int                        FLT_NEW_AND_READING;
	public boolean                    filters_and;
	
	public  HashMap<String, Integer>        history = new HashMap<String, Integer>();
	private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
	private HashMap<String, Drawable>       icons;
    private List<HashMap<String, String>>   readers;
    private List<String>                    apps;

	// Miscellaneous public flags
	public Boolean                          askIfAmbiguous;
	
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
    public List<String> extNames() {
    	List<String> rc = new ArrayList<String>();
    	for (HashMap<String, String> r : readers) {
    		for (String key : r.keySet()) {
    			if (!rc.contains(key))
    				rc.add(key);
    		} 		
    	}
    	return rc;
    }

    /*
     * Misc lists management (list is identified by name)
     * --------------------------------------------------
     */

    // get list by name
	public List<String[]> getList(String name) {
		if (m.containsKey(name))
			return m.get(name);
		else
			return new ArrayList<String[]>();
	}

	// set list by name
	public void setList(String name, List<String[]> l) {
		m.put(name, l);
	}

	public void setDefault(String name)
    {
    	// Set list default
    	List<String[]> nl = new ArrayList<String[]>();
    	if (name.equals("lastOpened"))
    	{
    	}
    	else if (name.equals("favorites"))
    	{
    	}
    	else if (name.equals("startReaders"))
    	{
    		nl.add(new String[] {"Nomad Reader", "application/fb2"});
    		nl.add(new String[] {"EBookDroid",   "application/djvu"});
    	}

    	m.put(name, nl);
    }

	// dump all lists to log (debug)
	public void dumpLists()
	{
		for (String k : m.keySet())
		{
			Log.d(TAG, "LIST: \"" + k + "\"");
			for (String[] n : m.get(k))
				Log.d(TAG, "    \"" + n[0] + "\"; \"" + n[1] + "\"");
		}
		
	}

	// dump specified lists to log (debug)
	public void dumpList(String name)
	{
		Log.d(TAG, "LIST: \"" + name + "\"");
		for (String[] n : m.get(name))
			Log.d(TAG, "    \"" + n[0] + "\"; \"" + n[1] + "\"");		
	}

	// dump lists to log (debug)
	public void dumpList(String name, List<String[]> l)
	{
		Log.d(TAG, "LIST: \"" + name + "\"");
		for (String[] n : l)
			Log.d(TAG, "    \"" + n[0] + "\"; \"" + n[1] + "\"");		
	}

    // Add to list
    public void addToList(String listName, String dr, String fn, Boolean addToEnd)
    {
    	//Log.d(TAG, "addToList(" + listName + ", " + dr + ":" + fn + ", " + addToEnd + ")");
 
    	addToList_internal(listName, dr, fn, addToEnd);
    }
    public void addToList(String listName, String fullName, Boolean addToEnd)
    {
    	addToList(listName, fullName, addToEnd, "/");
    }
    public void addToList(String listName, String fullName, Boolean addToEnd, String delimiter)
    {
    	//Log.d(TAG, "addToList(" + listName + ", " + fullName + ", " + addToEnd + ", " + delimiter + ")");

    	if (delimiter.equals("/"))
    	{
    		File f = new File(fullName); 	
    		if (!f.exists())
    			return;
    		addToList_internal(listName, f.getParent(), f.getName(), addToEnd);
    	}
    	else
    	{
    		int ind = fullName.indexOf(delimiter);
    		if (ind < 0)
    			return;
    		if (ind+delimiter.length() >= fullName.length())
    			return;
    		String dname = fullName.substring(0, ind);
    		String fname = fullName.substring(ind+delimiter.length());
    		if (listName.equals("history"))
    		{	
    			// Special case - delimiter is not "/" but we need to check file existence anyway.
    			// directory name is a full file name in such case. filename is a READ/NEW mark
    			File f = new File(dname);
    	   		if (!f.exists())
        			return;
    			addToList_internal(listName, dname, fname, addToEnd);
    		}
    		else
    			addToList_internal(listName, dname, fname, addToEnd);
    	}

    }
    public void addToList_internal(String listName, String dr, String fn, Boolean addToEnd)
    {
    	//Log.d(TAG, "addToList_internal(" + listName + ", " + dr + ":" + fn  + ", " + addToEnd + ")");

		if (!m.containsKey(listName))
			m.put(listName, new ArrayList<String[]>());
		List<String[]> resultList = m.get(listName);

    	String [] entry = new String[] {dr, fn};
    	for (int i=0; i<resultList.size(); i++)
    	{
    		if (resultList.get(i)[0].equals(dr)  &&  resultList.get(i)[1].equals(fn))
    		{
    			resultList.remove(i);
    			break;
    		}
    	}
    	if (addToEnd)
    		resultList.add(entry);
    	else
    		resultList.add(0, entry);
    }

    // Remove from list
    public void removeFromList(String listName, String dr, String fn)
    {
    	//Log.d(TAG, "removeFromList(" + listName + ", " + dr + ":" + fn + ")");

    	removeFromList_internal(listName, dr, fn);
    }
    public void removeFromList(String listName, String fullName)
    {
    	//Log.d(TAG, "removeFromList(" + listName + ", " + fullName + ")");
 
    	File f = new File(fullName); 	
    	if (!f.exists())
    		return;
    	removeFromList_internal(listName, f.getParent(), f.getName());

    }
    public void removeFromList_internal(String listName, String dr, String fn)
    {
    	//Log.d(TAG, "removeFromList_internal(" + listName + ", " + dr + ":" + fn  + ")");

		if (!m.containsKey(listName))
			return;
		List<String[]>  resultList = m.get(listName);
    	for (int i=0; i<resultList.size(); i++)
    	{
    		if (resultList.get(i)[0].equals(dr)  &&  resultList.get(i)[1].equals(fn))
    		{
    			resultList.remove(i);
    			return;
    		}
    	}
    }

    // If list contains
    public boolean contains(String listName, String dr, String fn)
    {
		if (!m.containsKey(listName))
			return false;
		List<String[]> resultList = m.get(listName);

    	for (int i=0; i<resultList.size(); i++)
    	{
    		if (resultList.get(i)[0].equals(dr)  &&  resultList.get(i)[1].equals(fn))
	    		return true;
    	}
    	return false;
    }
    
    // Read misc. lists
    public boolean readFile(String listName, String fileName)
    {
    	return readFile(listName, fileName, "/");
    }

    public boolean readFile(String listName, String fileName, String delimiter)
    {
    	FileInputStream fis = null;
    	try {
    		fis = openFileInput(fileName);
    	} catch (FileNotFoundException e) { }
    	if (fis == null)
    		return false;
    	else
    	{
    		String l = new String();

    		while (true)
    		{
    			byte[] buf = new byte[FileBufferSize];
    			int rc = -1;
    			try {
    				rc = fis.read(buf, 0, FileBufferSize);
    			} catch (IOException e)
    			{
    				rc = -1;
    			}
 
    			if (rc < 0)
    			{
    				if (!l.equals(""))
    				{
    					//Log.d(TAG, "ADD (last) \"" + l + "\"");
    					addToList(listName, l, true, delimiter);
    				}
					break;
    			}

    			for (int i=0; i<rc; i++)
    			{				
    				byte ch = buf[i];
    				if (ch == '\n')
    				{
    					if (!l.equals(""))
    					{
    						//Log.d(TAG, "ADD \"" + l + "\"");
    						addToList(listName, l, true, delimiter);
    						l = new String();
    					}
    				}
    				else
    					l = l + (char)ch;
    			}
    		}

    		try {
    			fis.close();
    		} catch (IOException e) { }
    	}
    	return true;
    }
    
    // Save to file miscellaneous lists
    public void writeFile(String listName, String fileName, int maxEntries)
    {
    	writeFile(listName, fileName, maxEntries, "/");
    }
    public void writeFile(String listName, String fileName, int maxEntries, String delimiter)
    {
		if (!m.containsKey(listName))
			return;

		List<String[]> resultList = m.get(listName);
    	FileOutputStream fos = null;
		try {
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			return;
		}
		for (int i=0; i<resultList.size(); i++)
		{
			if (maxEntries != 0  &&  i >= maxEntries)
				break;
			String line = resultList.get(i)[0] + delimiter + resultList.get(i)[1] + "\n";
			try {
				fos.write(line.getBytes());
			} catch (IOException e) { }		
		}
		try {
			fos.close();
		} catch (IOException e) { }
    }

    // Remove file
    public boolean removeFile(String fullname)
    {
    	File f = new File(fullname);
    	return removeFile(f.getParent(), f.getName());
    }
    public boolean removeFile(String dr, String fn)
    {
    	boolean rc = false;
    	String fullName = dr + "/" + fn;
    	removeFromList("lastOpened", dr, fn);
    	removeFromList("favorites", dr, fn);
    	removeFromList("history", dr, fn);
    	history.remove(fullName);
    	File f = new File(fullName);
    	if (f.exists())
    	{
    		try {
    			rc = f.delete();
    		} catch (SecurityException e) { }		
    	}
    	return rc;
    }
    
    // Remove directory
    public boolean removeDirectory(String dr, String fn)
    {
    	boolean rc = false;
    	String dname = dr + "/" + fn;
    	File   d = new File(dname);
    	File[] allEntries = d.listFiles();
    	for (File f : allEntries)
    	{
    		if (f.isDirectory())
    		{
    			//Log.d(TAG, "removeDirectory(" + dname + ", " + f.getName() + ")");
    		    if (!removeDirectory(dname, f.getName()))
    		    	return false;
    		}
    		else
    		{
    			//Log.d(TAG, "removeFile(" + dname + ", " + f.getName() + ")");
    			if (!removeFile(dname, f.getName()))
    				return false;
    		}
    	}
		try {
			rc = d.delete();
		} catch (SecurityException e) { }
    	return rc;
    }

    // common utility - get intent by label, null if not found
    public Intent getIntentByLabel(String label)
    {
        PackageManager pm = getPackageManager();;

        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	if (label.equals(pm.getApplicationLabel(packageInfo)))
        		return pm.getLaunchIntentForPackage(packageInfo.packageName);
        }
        return null;
    }
    
    // common utility - return intent to launch reader by reader name and full file name. Null if not found
    public Intent launchReader(String name, String file)
    {
    	for (String[] r : getList("startReaders"))
    	{
    		if (r[0].equals(name))
    		{
    			Intent i = new Intent();
    			i.setAction(Intent.ACTION_VIEW);
    			i.setDataAndType(Uri.parse("file://" + file), r[1]);
    			addToList("lastOpened", file, false);
        		if (!history.containsKey(file)  ||  history.get(file) == FINISHED)
        			history.put(file, READING);
    			return i;
    		}
    	}
    	Intent i = getIntentByLabel(name);
    	if (i == null)
    		Toast.makeText(this, "Activity \"" + name + "\" not found!", Toast.LENGTH_SHORT).show();
    	else
    	{
    		i.setAction(Intent.ACTION_VIEW); 
    		i.setData(Uri.parse("file://" + file));
    		addToList("lastOpened", file, false);
    		if (!history.containsKey(file)  ||  history.get(file) == FINISHED)
    			history.put(file, READING);
    		return i;
    	}
    	return null;
    }
    
    // compare by second element of String[]
    public class o1Comparator implements java.util.Comparator<String[]>
    {
    	public int compare(String[] o1, String[] o2)
    	{
    		int rc = o1[1].compareTo(o2[1]);
    		if (rc == 0)
    			return o1[0].compareTo(o2[0]);
    		else
    			return rc;
    	}
    }
    public o1Comparator getO1Comparator() { return new o1Comparator(); }
    
    // FILTER
	//public boolean filterFile1(String dname, String fname, Integer method, String value)
	//{
	//	Log.d(TAG, "filterFile1(" + dname + ", " + fname + ", " +method + ", " + value + ")");
	//	boolean rc = filterFile1_(dname, fname, method, value);
	//	Log.d(TAG, "    rc=" + rc);
	//	return rc;
	//}
	public boolean filterFile1(String dname, String fname, Integer method, String value)
	{
		if (method == FLT_STARTS)
			return fname.startsWith(value);
		else if (method ==  FLT_ENDS)
			return fname.endsWith(value);
		else if (method ==  FLT_CONTAINS)
			return fname.contains(value);
		else if (method ==  FLT_MATCHES)
			return fname.matches(value);
		else if (method ==  FLT_NEW)
			return ! history.containsKey(dname + "/" + fname);
		else if (method ==  FLT_NEW_AND_READING)
		{
			String fullName = dname + "/" + fname;
			if (history.containsKey(fullName))
				return history.get(fullName) != FINISHED;
			else
				return true;
		}
		else
			return false;
	}

	public boolean filterFile(String dname, String fname)
	{
		List<String[]> filters = getList("filters");
		if (filters.size() > 0)
		{
			for (String[] f : filters)
			{
				Integer filtMethod = 0;
				try {
					filtMethod = Integer.parseInt(f[0]);
				} catch(NumberFormatException e) { }
				if (filters_and)
				{
					// AND all filters
					if (!filterFile1(dname, fname, filtMethod, f[1]))
						return false;
				}
				else
				{
					// OR all filters
					if (filterFile1(dname, fname, filtMethod, f[1]))
						return true;
				}
			}
			return filters_and ? true : false;
		}
		else
			return true;
	}
	
	public Drawable specialIcon(String s)
	{
		if (s.endsWith(".apk"))
			return getResources().getDrawable(R.drawable.install);
		return null;
	}

	public boolean specialAction(Activity a, String s)
	{
		if (s.endsWith(".apk"))
		{
			// Install application
			Intent intent = new Intent(Intent.ACTION_VIEW);           
			intent.setDataAndType(Uri.parse("file://" + s), "application/vnd.android.package-archive");
			a.startActivity(intent);
			return true;
		}
		return false;
	}
}
