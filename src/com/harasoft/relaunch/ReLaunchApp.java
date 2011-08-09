package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ReLaunchApp extends Application {
	final String                            TAG = "ReLaunchApp";
	private HashMap<String, List<String[]>> m = new HashMap<String, List<String[]>>();
	private HashMap<String, Drawable>       icons;

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
	public void dump()
	{
		for (String k : m.keySet())
		{
			Log.d(TAG, "LIST: \"" + k + "\"");
			for (String[] n : m.get(k))
				Log.d(TAG, "    \"" + n[0] + "\"; \"" + n[1] + "\"");
		}
		
	}
	
	// Icons
    public HashMap<String, Drawable> getIcons() {
    	return icons;
    }
    public void setIcons(HashMap<String, Drawable> i) {
    	icons = i;
    }

}
