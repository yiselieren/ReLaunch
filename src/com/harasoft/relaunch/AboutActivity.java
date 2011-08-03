package com.harasoft.relaunch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		
		String vers = getResources().getString(R.string.app_version);
		EditText et  = (EditText)findViewById(R.id.about_txt);
		et.setText("Reader launcher\nVersion: " + vers);
	
		Button ok = (Button)findViewById(R.id.about_ok);
		ok.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				finish();
			}});
	}
}
