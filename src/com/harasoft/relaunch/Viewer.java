package com.harasoft.relaunch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Viewer extends Activity {
    final String                  TAG = "Viewer";

    final int                     EDITOR_ACT = 1;

    ReLaunchApp                   app;
    ImageButton                   backBtn;
    Button                        editBtn;
    EditText                      editTxt;
    String                        textBuffer;
    String                        fileName;

    private boolean rereadFile(String fname, EditText editTxt)
    {
        StringBuilder  buf = new StringBuilder();
        String         readLine;
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(fname));
        } catch (FileNotFoundException e) { return false; }

        try {
            while ((readLine = br.readLine()) != null)
            {
                buf.append(readLine);
                buf.append("\n");
            }
        } catch (IOException e) { return false; }
        try {
            br.close();
        } catch (IOException e) { }

        // Set text
        textBuffer = buf.toString();
        editTxt.setText(textBuffer);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (ReLaunchApp)getApplicationContext();
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.viewer_layout);

        // Read parameters
        final Intent data = getIntent();
        if (data.getExtras() == null)
            finish();

        final String fname = data.getStringExtra("filename");
        if (fname == null)
            finish();
        fileName = fname;

        // Check file size
        File f = new File(fname);
        if (!f.exists())
            finish();
        final long  fileSize = f.length();
        if (fileSize > app.viewerMax)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("File too big");
            builder.setMessage("File \"" + fname + "\" is too big for viewer (" + f.length() + " bytes)\n"
                    + "Maximal allowed size is " + app.viewerMax + " bytes");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }});
            builder.show();
        }
        else
        {
            // Set edit button
            editBtn = (Button)findViewById(R.id.viewedit_btn);
            editBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        if (fileSize > app.editorMax)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Viewer.this);
                            builder.setTitle("File too big");
                            builder.setMessage("File \"" + fname + "\" is too big for editor (" + fileSize + " bytes)\n"
                                    + "Maximal allowed size is " + app.editorMax + " bytes");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }});
                            builder.show();
                        }
                        else
                        {
                            // Start editor activity
                                Intent intent = new Intent(Viewer.this, Editor.class);
                                intent.putExtra("filename", fname);
                                startActivityForResult(intent, EDITOR_ACT);
                        }
                    }});

            // Set back button
            backBtn = (ImageButton)findViewById(R.id.view_btn);
            backBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { finish(); }});


            // Set title
            ((TextView)findViewById(R.id.view_title)).setText(fname);

            // Read file and set view field
            editTxt = (EditText)findViewById(R.id.view_txt);
            rereadFile(fname, editTxt);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        switch (requestCode)
        {
        case EDITOR_ACT:
            rereadFile(fileName, editTxt);
            break;
        default:
            return;
        }
    }
}
