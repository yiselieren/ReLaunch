package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class ReLaunchApp extends Application {
    final String                            TAG = "ReLaunchApp";

    // booting state
    public boolean							booted = false;
    
    // Reading files
    final int                               FileBufferSize = 1024;
    
    // Pending intent to self restart
    public PendingIntent                    RestartIntent;

    // Miscellaneous public flags/settings
    public boolean                          fullScreen = false;
    public Boolean                          askIfAmbiguous;
    public boolean                          customScrollDef = true;

    // Search values
    final String                            DIR_TAG = ".DIR..";

    // Book status
    final int                               READING=1;
    final int                               FINISHED=2;

    // Max file sizes
    int                                     viewerMax;
    int                                     editorMax;

    // Scrolling related values
    public int                             scrollStep; 

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
    public  HashMap<String, Integer>        columns = new HashMap<String, Integer>();
    private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
    private HashMap<String, Drawable>       icons;
    private List<HashMap<String, String>>   readers;
    private List<String>                    apps;

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

    // save list
    public void saveList(String listName) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	if(listName.equals("favorites")) {
            int favMax = 30;
            try {
                favMax = Integer.parseInt(prefs.getString("favSize", "30"));
            } catch(NumberFormatException e) { }
            this.writeFile("favorites",  ReLaunch.FAV_FILE, favMax);            
    	}
    	if(listName.equals("lastOpened")) {
            int lruMax = 30;
            try {
                lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
            } catch(NumberFormatException e) { }
            this.writeFile("lastOpened", ReLaunch.LRU_FILE, lruMax);            
    	}
    	if(listName.equals("app_last")) {
            int appLruMax = 30;
            try {
                appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
            } catch(NumberFormatException e) { }
            this.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
    	}
    	if(listName.equals("app_favorites")) {
            int appFavMax = 30;
            try {
                appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
            } catch(NumberFormatException e) { }
            this.writeFile("app_favorites", ReLaunch.APP_FAV_FILE, appFavMax, ":");    		
    	}
    	if(listName.equals("history")) {
    		List<String[]> h = new ArrayList<String[]>();
    		for (String k : this.history.keySet())
    		{
    			if (this.history.get(k) == this.READING)
    				h.add(new String[] {k, "READING"});
    			else if (this.history.get(k) == this.FINISHED)
    				h.add(new String[] {k, "FINISHED"});
    		}
    		this.setList("history", h);
    		this.writeFile("history", ReLaunch.HIST_FILE, 0, ":");
    	}
    	if(listName.equals("columns")) {
    		List<String[]> c = new ArrayList<String[]>();
    		for (String k : this.columns.keySet())
    		{
    			c.add(new String[] {k, Integer.toString(this.columns.get(k)) } );
    		}
    		this.setList("columns", c);
    		this.writeFile("columns", ReLaunch.COLS_FILE, 0, ":");
    	}    	
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
            if (fullName.endsWith("/" + DIR_TAG))
            {
                fullName = fullName.substring(0, fullName.length() - DIR_TAG.length() - 1);
                File f = new File(fullName);
                if (!f.exists())
                    return;
                addToList_internal(listName, fullName, DIR_TAG, addToEnd);
            }
            else
            {
                File f = new File(fullName);
                if (!f.exists())
                    return;
                addToList_internal(listName, f.getParent(), f.getName(), addToEnd);
            }
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
                // File f = new File(dname);
                //if (!f.exists())
                    // return;
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
        // saveList(listName);
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
                saveList(listName);
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
        	InputStreamReader insr = null;
        	try {
        		insr = new InputStreamReader(fis, "utf8");
        	}
        	catch(UnsupportedEncodingException e) {
        		return false;
        	}
        	BufferedReader bufr = new BufferedReader(insr,FileBufferSize);
            String l = new String();
            while (true) {
            		try { 
            			l = bufr.readLine();
            		}
            		catch(IOException e) {
            			return false;
            		}
            		if(l==null) break;
                    if (!l.equals("")) {
                        //Log.d(TAG, "ADD (last) \"" + l + "\"");
                        addToList(listName, l, true, delimiter);
                    }
                }
            try {
            	bufr.close();
            	insr.close();
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
        saveList("lastOpened");
        removeFromList("favorites", dr, fn);
        saveList("favorites");
        removeFromList("history", dr, fn);
        history.remove(fullName);
        saveList("history");
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
    	String[] labelp = label.split("\\%");
    	Intent i = new Intent();
    	i.setComponent(new ComponentName(labelp[0], labelp[1]));
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	//i = addSpecialFlags(i, labelp[0]+"%"+labelp[1]);
    	return i;
    }

    // common utility - return intent to launch reader by reader name and full file name. Null if not found
    public Intent launchReader(String name, String file)
    {
        String re[] = name.split(":");
        if (re.length == 2  &&  re[0].equals("Intent"))
        {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //i = addSpecialFlags(i, re[1]);
            i.setDataAndType(Uri.parse("file://" + file), re[1]);
            addToList("lastOpened", file, false);
            saveList("lastOpened");
            if (!history.containsKey(file)  ||  history.get(file) == FINISHED) {
                history.put(file, READING);
                saveList("history");
            	}
            return i;       
        }
        else
        {
            Intent i = getIntentByLabel(name);
            if (i == null)
                //Toast.makeText(this, "Activity \"" + name + "\" not found!", Toast.LENGTH_SHORT).show();
            	Toast.makeText(this, getResources().getString(R.string.jv_rla_activity) + " \"" + name + "\" " + getResources().getString(R.string.jv_rla_not_found), Toast.LENGTH_SHORT).show();
            else
            {
                i.setAction(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); - ALREADY DONE! in getIntentByLabel
                i.setData(Uri.parse("file://" + file));
                addToList("lastOpened", file, false);
                saveList("lastOpened");
                if (!history.containsKey(file)  ||  history.get(file) == FINISHED) {
                    history.put(file, READING);
                    saveList("history");
                	}
                return i;
            }
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
    //      Log.d(TAG, "filterFile1(" + dname + ", " + fname + ", " +method + ", " + value + ")");
    //      boolean rc = filterFile1_(dname, fname, method, value);
    //      Log.d(TAG, "    rc=" + rc);
    //      return rc;
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

    public Drawable specialIcon(String s, boolean isDir)
    {
        if (isDir)
            return getResources().getDrawable(R.drawable.dir_ok);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(intent);
            return true;
        }
        return false;
    }

    public void defaultAction(Activity a, String fname)
    {
        Intent intent = new Intent(a, Viewer.class);
        intent.putExtra("filename", fname);
        a.startActivity(intent);
    }
    
    public void About(final Activity a)
    {
        //String vers = getResources().getString(R.string.app_version);
    	String vers = "<version>";
    	try {
    		vers = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    	}
    	catch(Exception e) { }
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        WebView wv = new WebView(a);
        
        builder.setTitle("ReLaunch");
        //String str = "<h1><center>ReLaunch</center></h1>"
        //    + "<center><b>Reader launcher for Nook Simple Touch</b></center><br>"
        //    + "<center>Version: <b>" + vers + "</b></center><br>"
        //    + "<center>Source code: <a href=\"https://github.com/yiselieren/ReLaunch\">git://github.com/yiselieren/ReLaunch.git</a></center>";
        String str = getResources().getString(R.string.jv_rla_about_prev) + vers + getResources().getString(R.string.jv_rla_about_post);
        wv.loadDataWithBaseURL(null, str, "text/html", "utf-8", null);
        builder.setView(wv);
        //builder.setPositiveButton(("Ok", new DialogInterface.OnClickListener() {
        builder.setPositiveButton(getResources().getString(R.string.jv_rla_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                }});
        //builder.setNegativeButton("See changelog", new DialogInterface.OnClickListener() {
        builder.setNegativeButton(getResources().getString(R.string.jv_rla_see_changelog), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(a);
                WebView wv = new WebView(a);
                wv.loadDataWithBaseURL(null, 
                		getResources().getString(R.string.about_help)+
                		getResources().getString(R.string.about_appr)+
                		getResources().getString(R.string.whats_new)
                		, "text/html", "utf-8", null);
                //builder1.setTitle("What's new");
                builder1.setTitle(getResources().getString(R.string.jv_rla_whats_new_title));
                builder1.setView(wv);
                //builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                builder1.setPositiveButton(getResources().getString(R.string.jv_rla_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }});
                builder1.show();
                }});
        builder.show();
    }
    
    /*
    public Intent addSpecialFlags(Intent i, String checkAppOrInt) {
    	// Log.d(TAG,checkAppOrInt);
    	String specFlags="application/pdf:+FLAG_ACTIVITY_CLEAR_TOP";
        specFlags=specFlags+"|org.ebookdroid%org.ebookdroid.core.RecentActivity:+FLAG_ACTIVITY_CLEAR_TOP";
        // next not working, so - commented
        // specFlags=specFlags+"|com.speedsoftware.rootexplorer%com.speedsoftware.rootexplorer.RootExplorer:+FLAG_ACTIVITY_CLEAR_TOP";        
    	String[] specFlagsA = specFlags.split("\\|");
    	for(int j=0;j<specFlagsA.length;j++) {
    		String[] specFlag = specFlagsA[j].split("\\:");
    		if(specFlag[0].equals(checkAppOrInt)) {
    			if(specFlag[1].equals("FLAG_ACTIVITY_CLEAR_TOP")) { // now only one flag
    				// Log.d(TAG,"FLAG_ACTIVITY_CLEAR_TOP");
    				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				}
    			if(specFlag[1].equals("+FLAG_ACTIVITY_CLEAR_TOP")) { // now only one flag
    				// Log.d(TAG,"+FLAG_ACTIVITY_CLEAR_TOP");
    				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				}    			
    			if(specFlag[1].equals("-FLAG_ACTIVITY_CLEAR_TOP")) { // now only one flag
    				// Log.d(TAG,"-FLAG_ACTIVITY_CLEAR_TOP");
    				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				i.setFlags(i.getFlags() & ~Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				}    			
    			if(specFlag[1].equals("FLAG_ACTIVITY_NO_HISTORY")) { // now only one flag
    				// Log.d(TAG,"FLAG_ACTIVITY_NO_HISTORY");
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    				}    			
    			if(specFlag[1].equals("+FLAG_ACTIVITY_NO_HISTORY")) { // now only one flag
    				// Log.d(TAG,"+FLAG_ACTIVITY_NO_HISTORY");
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    				}
    			if(specFlag[1].equals("-FLAG_ACTIVITY_NO_HISTORY")) { // now only one flag
    				// Log.d(TAG,"-FLAG_ACTIVITY_NO_HISTORY");
    				i.setFlags(i.getFlags() & ~Intent.FLAG_ACTIVITY_NO_HISTORY);
    				}
    			if(specFlag[1].equals("FLAG_ACTIVITY_NEW_TASK")) { // now only one flag
    				// Log.d(TAG,"FLAG_ACTIVITY_NEW_TASK");
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
    				}    			
    			if(specFlag[1].equals("+FLAG_ACTIVITY_NEW_TASK")) { // now only one flag
    				// Log.d(TAG,"+FLAG_ACTIVITY_NEW_TASK");
    				i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
    				}
    			if(specFlag[1].equals("-FLAG_ACTIVITY_NEW_TASK")) { // now only one flag
    				// Log.d(TAG,"-FLAG_ACTIVITY_NEW_TASK");
    				i.setFlags(i.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
    				}    			
    			}
    		}
    	return i;
    }
    */
    public void setFullScreenIfNecessary(Activity a)
    {
        if (fullScreen)
        {
            a.requestWindowFeature(Window.FEATURE_NO_TITLE);
            a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    
    public void generalOnResume(String name, Activity a)
    {
        Log.d(TAG, "--- onResume(" + name + ")");
    }
}
