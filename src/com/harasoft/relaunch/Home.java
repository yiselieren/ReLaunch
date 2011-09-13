package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Home extends Activity {
	ReLaunchApp                   app;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	app = ((ReLaunchApp)getApplicationContext());
    	
		Intent intent = new Intent(Home.this, ReLaunch.class);
		intent.putExtra("home", true);
        startActivity(intent);
	}
}
