package com.harasoft.relaunch;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;

public class PrefsActivity extends PreferenceActivity {
    ReLaunchApp                   app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        setContentView(R.layout.prefs_main);

        ((Button)findViewById(R.id.filter_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(PrefsActivity.this, FiltersActivity.class);
                startActivity(intent);
            }});
        ((Button)findViewById(R.id.filter_ok)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                finish();
            }});
    }
}
