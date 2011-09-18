package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Viewer extends Activity {
	final String                  TAG = "Viewer";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_layout);
		

		// Read parameters
        final Intent data = getIntent();
        if (data.getExtras() == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }
        String fname = data.getStringExtra("filename");
        if (fname == null)
        {
        	setResult(Activity.RESULT_CANCELED);
        	finish();
        }

		// Set back button and title
		((ImageButton)findViewById(R.id.view_btn)).setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) { finish(); }});
    	((TextView)findViewById(R.id.view_title)).setText(fname);

    			StringBuilder buf = new StringBuilder();
		String        readLine;

		// Read file
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
		} catch (FileNotFoundException e) { br = null; }
		if (br == null)
			return;
		
		try {
			while ((readLine = br.readLine()) != null)
			{
				buf.append(readLine);
				buf.append("\n");
			}
		} catch (IOException e) { }

		// Set text
    	((EditText)findViewById(R.id.view_txt)).setText(buf.toString());
	}

}
