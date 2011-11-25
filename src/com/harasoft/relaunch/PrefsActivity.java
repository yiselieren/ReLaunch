package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class PrefsActivity extends PreferenceActivity {
    final String                  TAG = "PreferenceActivity";
    final static public int       TYPES_ACT = 1;

    ReLaunchApp                   app;
    SharedPreferences             prefs;

    static class PrefItem
    {
        PrefItem(String n, boolean value)
        {
            name = n;
            isBoolean = true;
            bValue = value;
        }
        PrefItem(String n, String value)
        {
            name = n;
            isBoolean = false;
            sValue = value;        
        }
        PrefItem(PrefItem o)
        {
            name = o.name;
            isBoolean = o.isBoolean;
            bValue = o.bValue;
            sValue = o.sValue;
        }
        String  name;
        boolean isBoolean;
        boolean bValue;
        String  sValue;
    }
    List<PrefItem> defItems = new ArrayList<PrefItem>();
    List<PrefItem> savedItems;
    
    private void setDefaults()
    {
        // General section
        defItems.add(new PrefItem("filterResults", false));
        defItems.add(new PrefItem("fullScreen", true));
        defItems.add(new PrefItem("showButtons", true));
        defItems.add(new PrefItem("startDir", "/sdcard"));
        defItems.add(new PrefItem("saveDir", true));
        defItems.add(new PrefItem("askAmbig", false));
        defItems.add(new PrefItem("showNew", true));
        defItems.add(new PrefItem("gl16Mode", "5"));

        // Launcher mode settings
        defItems.add(new PrefItem("filterSelf", true));
        defItems.add(new PrefItem("shopMode", true));
        defItems.add(new PrefItem("dateUS", false));
        defItems.add(new PrefItem("libraryMode", true));
        defItems.add(new PrefItem("homeMode", true));

        // Scrollbar appearance settings
        defItems.add(new PrefItem("scrollPerc", "10"));
        defItems.add(new PrefItem("customScroll", app.customScrollDef));
        defItems.add(new PrefItem("scrollWidth", "25"));
        defItems.add(new PrefItem("scrollPad", "10"));

        // Search setting
        defItems.add(new PrefItem("searchSize", "5000"));
        defItems.add(new PrefItem("searchReport", "100"));
        defItems.add(new PrefItem("searchRoot", "/media,/sdcard"));

        // Viewer/editor settings
        defItems.add(new PrefItem("viewerMaxSize", "1048576"));
        defItems.add(new PrefItem("editorMaxSize", "262144"));

        // Maximal size of misc. persistent lists
        defItems.add(new PrefItem("lruSize", "30"));
        defItems.add(new PrefItem("favSize", "30"));
        defItems.add(new PrefItem("appLruSize", "30"));
        defItems.add(new PrefItem("appFavSize", "30"));

        // Confirmations
        defItems.add(new PrefItem("confirmFileDelete", true));
        defItems.add(new PrefItem("confirmDirDelete", true));
        defItems.add(new PrefItem("confirmNonEmptyDirDelete", true));
    }
 
    private void revert()
    {
        SharedPreferences.Editor editor = prefs.edit();
        for (PrefItem p : defItems)
        {
            if (p.isBoolean)
                editor.putBoolean(p.name, p.bValue);
            else
                editor.putString(p.name, p.sValue);
        }
        editor.commit();
    }

    private void cancel()
    {
        SharedPreferences.Editor editor = prefs.edit();
        for (PrefItem p : savedItems)
        {
            if (p.isBoolean)
                editor.putBoolean(p.name, p.bValue);
            else
                editor.putString(p.name, p.sValue);
        }
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        setContentView(R.layout.prefs_main);

        // Save items value
        setDefaults();
        savedItems = new ArrayList<PrefItem>();
        for (PrefItem p : defItems)
            savedItems.add(new PrefItem(p));

        ((Button)findViewById(R.id.filter_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(PrefsActivity.this, FiltersActivity.class);
                startActivity(intent);
            }});
        ((Button)findViewById(R.id.associations)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(PrefsActivity.this, TypesActivity.class);
                startActivityForResult(intent, TYPES_ACT);
            }});
        ((Button)findViewById(R.id.prefs_ok)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                finish();
            }});
        ((Button)findViewById(R.id.restart_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                AlarmManager mgr = (AlarmManager)getSystemService(ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, app.RestartIntent);
                System.exit(0);
            }});
        ((Button)findViewById(R.id.revert_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                revert();
                finish();
            }});
        ((Button)findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                cancel();
                finish();
            }});
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult, " + requestCode + " " + resultCode);
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode)
        {
        case TYPES_ACT:
            String newTypes = ReLaunch.createReadersString(app.getReaders());
            //Log.d(TAG, "New types string: \"" + newTypes + "\"");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("types", newTypes);
            editor.commit();
            break;
        default:
            return;
        }
    }
}
