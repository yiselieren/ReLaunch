package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Home extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Intent intent = new Intent(Home.this, ReLaunch.class);
		intent.putExtra("home", true);
        startActivity(intent);
	}
}
