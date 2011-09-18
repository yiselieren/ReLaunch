package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Viewer extends Activity {
    final String                  TAG = "Viewer";

    SharedPreferences             prefs;
    Button                        backBtn;
    Button                        editBtn;
    EditText                      editTxt;
    String                        textBuffer;
    int                           viewerMax;
    int                           editorMax;

    public void saveChanges(String newBuf, final String fname, final long fsize)
    {
        // Open
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(fname));
        } catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
            builder.setTitle("Can't open \"" + fname + "\"");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switchToView(fname, fsize);
                    }});
            builder.show();
            return;
        }

        // Write
        try {
            bw.write(newBuf, 0, newBuf.length());
        } catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
            builder.setTitle("Can't write to \"" + fname + "\"");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switchToView(fname, fsize);
                    }});
            builder.show();
            return;
        }

        // Close
        try {
            bw.close();
        } catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
            builder.setTitle("Can't close file \"" + fname + "\"");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switchToView(fname, fsize);
                    }});
            builder.show();
       }
    }

    public void switchToView(final String fname, final long fsize)
    {
    	editTxt.setFocusable(false);
    	//editTxt.setFilters(new InputFilter[] {
    	//	    new InputFilter() {
    	//	    	public CharSequence filter(CharSequence source, int start,
    	//	    			int end, Spanned dest, int dstart, int dend) { return source; }
    	//	    }});

    	// Set edit button
        editBtn.setText("Edit");
        editBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    if (fsize > editorMax)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
                        builder.setTitle("File \"" + fname + "\" is too big for editor (" + fsize + " bytes) " + editorMax);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }});
                        builder.show();
                    }
                    else
                        switchToEdit(fname, fsize);
                }});

        // Set back button
        backBtn.setText("Back");
        backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { finish(); }});

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
        try {
            br.close();
        } catch (IOException e) { }

        // Set text
        textBuffer = buf.toString();
        editTxt.setText(textBuffer);

    }

    public void switchToEdit(final String fname, final long fsize)
    {
    	editTxt.setFocusable(true);
    	//editTxt.setFilters(new InputFilter[] {
    	//	    new InputFilter() {
    	//	    	public CharSequence filter(CharSequence source, int start,
    	//	    			int end, Spanned dest, int dstart, int dend) { return null; }
    	//	    }});

    	editBtn.setText("Save");
        editBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    final String newBuf = editTxt.getText().toString();
                    saveChanges(newBuf, fname, fsize);
                    switchToView(fname, fsize);
               }});

        backBtn.setText("Cancel");
        backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    final String newBuf = editTxt.getText().toString();
                    if (newBuf.equals(textBuffer))
                    	// No changes
                    	switchToView(fname, fsize);
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
                        builder.setTitle("Save changes?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    saveChanges(newBuf, fname, fsize);
                                    switchToView(fname, fsize);
                                }});
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    switchToView(fname, fsize);
                                }});
                        builder.show();
                    }
                }});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        try {
            viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize", "5242880"));
            editorMax = Integer.parseInt(prefs.getString("editorMaxSize", "1048576"));
        } catch(NumberFormatException e) {
            viewerMax = 5242880;
            editorMax = 1048576;
        }

        // Read parameters
        final Intent data = getIntent();
        if (data.getExtras() == null)
            finish();

        final String fname = data.getStringExtra("filename");
        if (fname == null)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        // Check file size
        File f = new File(fname);
        if (!f.exists())
            finish();
        final long  fileSize = f.length();
        if (fileSize > viewerMax)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("File \"" + fname + "\" is too big for viewer (" + f.length() + " bytes)");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }});
            builder.show();
        }
        else
        {
            editBtn = (Button)findViewById(R.id.viewedit_btn);
            backBtn = (Button)findViewById(R.id.view_back);
            editTxt = (EditText)findViewById(R.id.view_txt);

            // Set title
            ((TextView)findViewById(R.id.view_title)).setText(fname);
            switchToView(fname, fileSize);
        }
    }
}
