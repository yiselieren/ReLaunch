package com.harasoft.relaunch;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        setContentView(R.layout.prefs_main);

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
        ((Button)findViewById(R.id.readers_start)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(PrefsActivity.this, ReadersActivity.class);
                startActivity(intent);
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
            //Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(i);
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
