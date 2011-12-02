package com.harasoft.relaunch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	final static String           TAG = "BootReceiver";
	
	ReLaunchApp	app;
	
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
        	// Create global storage with values
        	Log.d(TAG,"Boot event received");
            app = (ReLaunchApp) context.getApplicationContext();
            app.readFile("lastOpened", ReLaunch.LRU_FILE);
            app.readFile("favorites", ReLaunch.FAV_FILE);
            app.readFile("filters", ReLaunch.FILT_FILE, ":");
            app.readFile("history", ReLaunch.HIST_FILE, ":");
            app.history.clear();
            for (String[] r : app.getList("history"))
            {
                if (r[1].equals("READING"))
                    app.history.put(r[0], app.READING);
                else if (r[1].equals("FINISHED"))
                    app.history.put(r[0], app.FINISHED);
            }
        }
    }
}