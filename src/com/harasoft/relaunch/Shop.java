package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Shop extends Activity {
	ReLaunchApp                   app;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	app = ((ReLaunchApp)getApplicationContext());
    	
		Intent intent = new Intent(Shop.this, ReLaunch.class);
		intent.putExtra("shop", true);
        startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}
}
