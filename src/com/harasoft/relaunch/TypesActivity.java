package com.harasoft.relaunch;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class TypesActivity extends Activity {
    HashMap<String, Drawable>     icons = new HashMap<String, Drawable>();
    PackageManager                pm;
    List<HashMap<String, String>> readers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.types_layout);
 
        // Create application icons map
        pm = getPackageManager();
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Drawable d = null;
    		try {
    			d = pm.getApplicationIcon(packageInfo.packageName);
    		} catch(PackageManager.NameNotFoundException e) { }
        
    		if (d != null)
    			icons.put((String)pm.getApplicationLabel(packageInfo), d);
        }

        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        readers = ReLaunch.parseReadersString(data.getExtras().getString("types"));
	}

}
