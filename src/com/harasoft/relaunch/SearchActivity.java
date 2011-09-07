package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends Activity {
	final String             TAG = "Search";
    SharedPreferences        prefs;
    Spinner                  searchAs;
    CheckBox                 searchCase;
    CheckBox                 searchKnown;
    EditText                 searchRoot;
    EditText                 searchTxt;
    Button                   searchButton;
    InputMethodManager       imm;
	ReLaunchApp              app;


    private void addEntries(String root, List<String[]> searchResults, Boolean case_sens,
    		Boolean known_only, Boolean regexp, String pattern)
    {
    	File         dir = new File(root);
       	File[]       allEntries = dir.listFiles();
    	for (File entry : allEntries)
    	{
    		String entryFullName = root + "/" + entry.getName();
    		
			if (entry.isDirectory())
				addEntries(entryFullName, searchResults, case_sens, known_only, regexp, pattern);
			else
			{
				String[] n = new String[2];
				n[0] = root;
				n[1] = entry.getName();

				if (known_only  &&  app.readerName(n[1]).equals("Nope"))
					continue;
				if (regexp)
				{
					// Regular expression
					if (entry.getName().matches(pattern))
						searchResults.add(n); 
				}
				else
				{
					// String
					if (case_sens)
					{
						if (entry.getName().contains(pattern))
							searchResults.add(n); 
					}
					else
					{
						if (entry.getName().toLowerCase().contains(pattern.toLowerCase()))
							searchResults.add(n); 
					}
				}
			}
    	}
    }

    private void executeSearch(boolean all)
    {
		List<String[]> searchResults = new ArrayList<String[]>();

		if (all)
    	{
    		String         root = searchRoot.getText().toString();
    		File         dir = new File(root);
    		if (!dir.isDirectory())
    			return;
    		addEntries(root, searchResults, false, true, true, ".*");    		
    	}
    	else
    	{
    		Boolean        case_sens = searchCase.isChecked();
    		Boolean        known_only = searchKnown.isChecked();
    		Boolean        regexp = searchAs.getSelectedItemPosition() == 1;
    		String         root = searchRoot.getText().toString();
    		String         pattern = searchTxt.getText().toString();

    		// Save all search settings
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putBoolean("searchCase", case_sens);
    		editor.putBoolean("searchKnown", known_only);
    		editor.putInt("searchAs", searchAs.getSelectedItemPosition());
    		editor.putString("searchRoot", root);
    		editor.putString("searchPrev", pattern);
    		editor.commit();
        
    		// Search
    		File         dir = new File(root);
    		if (!dir.isDirectory())
    			return;
    		addEntries(root, searchResults, case_sens, known_only, regexp, pattern);
    	}
    	
    	//// DEBUG
    	//for (String[] r : searchResults)
    	//	Log.d(TAG, "Found dir: \"" + r[0] + "\"; file: \"" + r[1] + "\"");
    	app.setList("searchResults", searchResults);
		Intent intent = new Intent(SearchActivity.this, ResultsActivity.class);
		intent.putExtra("list", "searchResults");
		intent.putExtra("title", "Search results");
		intent.putExtra("rereadOnStart", false);
        startActivity(intent);
     }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        app = ((ReLaunchApp)getApplicationContext());
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        searchCase = (CheckBox)findViewById(R.id.search_case);
        searchKnown = (CheckBox)findViewById(R.id.search_books_only);
        searchAs = (Spinner)findViewById(R.id.search_as);
        searchRoot = (EditText)findViewById(R.id.search_root);
        searchTxt = (EditText)findViewById(R.id.search_txt);
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	               if (actionId == EditorInfo.IME_ACTION_SEARCH) {
	            	   imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	                   executeSearch(false);
	                   return true;
	                }
				return false;
			}});

        // Set clean search text button
		((ImageButton)findViewById(R.id.search_txt_delete)).setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) { searchTxt.setText(""); }});
		
		// Set main search button
		((Button)findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { executeSearch(false); }});	
		
		// Search all button
		((Button)findViewById(R.id.search_all)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { executeSearch(true); }});	
		
		// Cancel button
    	((Button)findViewById(R.id.search_cancel)).setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) { finish(); }});
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// Set case sensitive checkbox
		searchCase.setChecked(prefs.getBoolean("searchCase", true));
		
		// Set search known extensions only checkbox
		searchKnown.setChecked(prefs.getBoolean("searchKnown", true));
		
		// Set search as spinner
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.search_as_values, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    searchAs.setAdapter(adapter);
	    searchAs.setSelection(prefs.getInt("searchAs", 0));
	    
		// set search root
		searchRoot.setText(prefs.getString("searchRoot", "/sdcard"));
		
		// Set search text
		searchTxt.setText(prefs.getString("searchPrev", ""));
		
	}

}
