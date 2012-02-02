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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Editor extends Activity implements TextWatcher {
	final String TAG = "Viewer";

	ReLaunchApp app;
	Button saveBtn;
	Button cancelBtn;
	EditText editTxt;
	String textBuffer;
	SharedPreferences prefs;

	private boolean rereadFile(String fname, EditText editTxt) {
		StringBuilder buf = new StringBuilder();
		String readLine;
		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(fname));
		} catch (FileNotFoundException e) {
			return false;
		}

		try {
			while ((readLine = br.readLine()) != null) {
				buf.append(readLine);
				buf.append("\n");
			}
		} catch (IOException e) {
			return false;
		}
		try {
			br.close();
		} catch (IOException e) {
		}

		// Set text
		textBuffer = buf.toString();
		editTxt.setText(textBuffer);
		return true;
	}

	private boolean saveChanges(String newBuf, final String fname) {
		// Open
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(fname));
		} catch (IOException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Editor.this);
			// "Open failure"
			builder.setTitle(getResources().getString(
					R.string.jv_editor_openerr_title));
			// "Can't open file \"" + fname + "\" for writting"
			builder.setMessage(getResources().getString(
					R.string.jv_editor_openerr_text1)
					+ " \""
					+ fname
					+ "\" "
					+ getResources()
							.getString(R.string.jv_editor_openerr_text2));
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_editor_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(Activity.RESULT_CANCELED);
							finish();
						}
					});
			builder.show();
			return false;
		}

		// Write
		try {
			bw.write(newBuf, 0, newBuf.length());
		} catch (IOException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Editor.this);
			// "Write failure"
			builder.setTitle(getResources().getString(
					R.string.jv_editor_writerr_title));
			// "Can't write to file \"" + fname + "\""
			builder.setMessage(getResources().getString(
					R.string.jv_editor_writerr_text)
					+ " \"" + fname + "\"");
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_editor_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(Activity.RESULT_OK);
							finish();
						}
					});
			builder.show();
			return false;
		}

		// Close
		try {
			bw.close();
		} catch (IOException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Editor.this);
			// "Close failure"
			builder.setTitle(getResources().getString(
					R.string.jv_editor_closerr_title));
			// "Can't close file \"" + fname + "\""
			builder.setMessage(getResources().getString(
					R.string.jv_editor_closerr_text)
					+ " \"" + fname + "\"");
			// "OK"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_editor_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(Activity.RESULT_OK);
							finish();
						}
					});
			builder.show();
			return false;
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		app = (ReLaunchApp) getApplicationContext();
		app.setFullScreenIfNecessary(this);
		setContentView(R.layout.editor_layout);

		// Read parameters
		final Intent data = getIntent();
		if (data.getExtras() == null)
			finish();

		final String fname = data.getStringExtra("filename");
		if (fname == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		// Check file size
		File f = new File(fname);
		if (!f.exists())
			finish();
		final long fileSize = f.length();
		if (fileSize > app.editorMax) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// "File too big"
			builder.setTitle(getResources().getString(
					R.string.jv_editor_file_too_big));
			// "File \"" + fname + "\" is too big for editor (" + f.length() +
			// " bytes)\n"
			// + "Maximal allowed size is " + app.editorMax + " bytes"
			builder.setMessage(getResources()
					.getString(R.string.jv_editor_file)
					+ " \""
					+ fname
					+ "\" "
					+ getResources().getString(R.string.jv_editor_too_big)
					+ " ("
					+ f.length()
					+ " "
					+ getResources().getString(R.string.jv_editor_bytes)
					+ ")\n"
					+ getResources().getString(R.string.jv_editor_maximum)
					+ " "
					+ app.editorMax
					+ " "
					+ getResources().getString(R.string.jv_editor_bytes));
			builder.setPositiveButton(
					getResources().getString(R.string.jv_editor_ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							setResult(Activity.RESULT_CANCELED);
							finish();
						}
					});
			builder.show();
		} else {
			saveBtn = (Button) findViewById(R.id.edit_save);
			saveBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final String newBuf = editTxt.getText().toString();
					if (newBuf.equals(textBuffer)) {
						// No changes
						setResult(Activity.RESULT_CANCELED);
						finish();
					} else if (saveChanges(newBuf, fname)) {
						setResult(Activity.RESULT_OK);
						finish();
					}
				}
			});

			cancelBtn = (Button) findViewById(R.id.edit_cancel);
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final String newBuf = editTxt.getText().toString();
					if (newBuf.equals(textBuffer)) {
						// No changes
						setResult(Activity.RESULT_CANCELED);
						finish();
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								Editor.this);
						// "Save changes warning"
						builder.setTitle(getResources().getString(
								R.string.jv_editor_save_title));
						// "Do you want to save changes?"
						builder.setMessage(getResources().getString(
								R.string.jv_editor_save_text));
						// "YES"
						builder.setPositiveButton(
								getResources()
										.getString(R.string.jv_editor_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										if (saveChanges(newBuf, fname)) {
											setResult(Activity.RESULT_OK);
											finish();
										}
									}
								});
						// "NO"
						builder.setNegativeButton(
								getResources().getString(R.string.jv_editor_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										setResult(Activity.RESULT_CANCELED);
										finish();
									}
								});
						builder.show();
					}
				}
			});
			editTxt = (EditText) findViewById(R.id.edit_txt);
			editTxt.addTextChangedListener(this);

			// Set title
			((EditText) findViewById(R.id.edit_title)).setText(fname);
			rereadFile(fname, editTxt);
		}
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		app.generalOnResume(TAG, this);
	}

	public void afterTextChanged(Editable s) {
		final String newBuf = editTxt.getText().toString();
		if (newBuf.equals(textBuffer)) {
			saveBtn.setEnabled(false);
			// "Back"
			cancelBtn
					.setText(getResources().getString(R.string.jv_editor_back));
		} else {
			saveBtn.setEnabled(true);
			// "Cancel"
			cancelBtn.setText(getResources().getString(
					R.string.jv_editor_cancel));
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
