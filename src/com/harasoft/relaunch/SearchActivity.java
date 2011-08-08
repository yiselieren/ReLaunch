package com.harasoft.relaunch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class SearchActivity extends Activity {
    SharedPreferences        prefs;
    Spinner                  searchAs;
    CheckBox                 searchCase;
    EditText                 searchRoot;
    EditText                 searchTxt;
    Button                   searchButton;

    private void executeSearch()
    {
    	// Save all search settings
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("searchCase", searchCase.isChecked());
        editor.putInt("searchAs", searchAs.getSelectedItemPosition());
        editor.putString("searchRoot", searchRoot.getText().toString());
        editor.putString("searchPrev", searchTxt.getText().toString());
        editor.commit();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        searchCase = (CheckBox)findViewById(R.id.search_case);
        searchAs = (Spinner)findViewById(R.id.search_as);
        searchRoot = (EditText)findViewById(R.id.search_root);
        searchTxt = (EditText)findViewById(R.id.search_txt);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// Set case sensitive checkbox
		searchCase.setChecked(prefs.getBoolean("searchCase", true));
		
		// Set search as spinner
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.search_as_values, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    searchAs.setAdapter(adapter);
	    searchAs.setSelection(prefs.getInt("searchAs", 0));
	    
		// set search root
		searchRoot.setText(prefs.getString("searchRoot", "/sdcard"));
		
		// set search text
		searchTxt.setText(prefs.getString("searchPrev", ""));
		
		// set clean search text button
		((ImageButton)findViewById(R.id.searc_txt_delete)).setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) { searchTxt.setText(""); }});
		
		// set main search button
		((Button)findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {executeSearch(); }});		
	}

}
