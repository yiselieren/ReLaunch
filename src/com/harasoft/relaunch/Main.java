package com.harasoft.relaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity {
    final String                 TAG = "Main";
    ReLaunchApp                  app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = ((ReLaunchApp)getApplicationContext());

        Intent intent = new Intent(Main.this, ReLaunch.class);
        intent.putExtra("home", false);
        intent.putExtra("shop", false);
        intent.putExtra("library", false);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }
}
