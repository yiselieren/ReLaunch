package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	final String TAG = "PreferenceActivity";
	final static public int TYPES_ACT = 1;

	ReLaunchApp app;
	List<String> applicationsArray;
	CharSequence[] applications;
	CharSequence[] happlications;

	SharedPreferences prefs;

	boolean do_pref_subrequest = true;

	static class PrefItem {
		PrefItem(String n, boolean value) {
			name = n;
			isBoolean = true;
			bValue = value;
		}

		PrefItem(String n, String value) {
			name = n;
			isBoolean = false;
			sValue = value;
		}

		PrefItem(PrefItem o) {
			name = o.name;
			isBoolean = o.isBoolean;
			bValue = o.bValue;
			sValue = o.sValue;
		}

		String name;
		boolean isBoolean;
		boolean bValue;
		String sValue;
	}

	List<PrefItem> defItems = new ArrayList<PrefItem>();
	List<PrefItem> savedItems;

	private void setDefaults() {
		// General section
		defItems.add(new PrefItem("filterResults", false));
		defItems.add(new PrefItem("fullScreen", true));
		defItems.add(new PrefItem("showButtons", true));
		defItems.add(new PrefItem("startDir", "/sdcard,/media/My Files"));
		defItems.add(new PrefItem("notLeaveStartDir", false));
		defItems.add(new PrefItem("saveDir", true));
		defItems.add(new PrefItem("returnFileToMain", false));
		defItems.add(new PrefItem("askAmbig", false));
		defItems.add(new PrefItem("showNew", true));
		defItems.add(new PrefItem("hideKnownExts", false));
		defItems.add(new PrefItem("hideKnownDirs", false));
		defItems.add(new PrefItem("openWith", true));
		defItems.add(new PrefItem("createIntent", true));
		defItems.add(new PrefItem("gl16Mode", "5"));
		defItems.add(new PrefItem("screenOrientation", "NO"));
		defItems.add(new PrefItem("einkUpdateMode", "1"));
		defItems.add(new PrefItem("einkUpdateInterval", "10"));

		// Columns settings
		defItems.add(new PrefItem("firstLineFontSize", "8"));
		defItems.add(new PrefItem("secondLineFontSize", "6"));
		defItems.add(new PrefItem("columnsDirsFiles", "-1"));
		defItems.add(new PrefItem("columnsHomeList", "-1"));
		defItems.add(new PrefItem("columnsLRU", "-1"));
		defItems.add(new PrefItem("columnsFAV", "-1"));
		defItems.add(new PrefItem("columnsSearch", "-1"));
		defItems.add(new PrefItem("columnsAlgIntensity", "70 3:5 7:4 15:3 48:2"));

		// Buttons settings
		defItems.add(new PrefItem("homeButtonST", "OPENN"));
		defItems.add(new PrefItem("homeButtonSTopenN", "1"));
		defItems.add(new PrefItem("homeButtonDT", "OPENMENU"));
		defItems.add(new PrefItem("homeButtonDTopenN", "1"));
		defItems.add(new PrefItem("homeButtonLT", "OPENSCREEN"));
		defItems.add(new PrefItem("homeButtonLTopenN", "1"));
		defItems.add(new PrefItem("advancedButtonST", "RELAUNCH"));
		defItems.add(new PrefItem("advancedButtonSTapp", "%%"));
		defItems.add(new PrefItem("advancedButtonDT", "NOTHING"));
		defItems.add(new PrefItem("advancedButtonDTapp", "%%"));
		defItems.add(new PrefItem("advancedButtonLT", "NOTHING"));
		defItems.add(new PrefItem("advancedButtonLTapp", "%%"));
		defItems.add(new PrefItem("memButtonST", "RELAUNCH"));
		defItems.add(new PrefItem("memButtonSTapp", "%%"));
		defItems.add(new PrefItem("memButtonDT", "NOTHING"));
		defItems.add(new PrefItem("memButtonDTapp", "%%"));
		defItems.add(new PrefItem("memButtonLT", "NOTHING"));
		defItems.add(new PrefItem("memButtonLTapp", "%%"));
		defItems.add(new PrefItem("batButtonST", "RELAUNCH"));
		defItems.add(new PrefItem("batButtonSTapp", "%%"));
		defItems.add(new PrefItem("batButtonDT", "NOTHING"));
		defItems.add(new PrefItem("batButtonDTapp", "%%"));
		defItems.add(new PrefItem("batButtonLT", "NOTHING"));
		defItems.add(new PrefItem("batButtonLTapp", "%%"));

		// Launcher mode settings
		defItems.add(new PrefItem("homeMode", true));
		defItems.add(new PrefItem("libraryMode", true));
		defItems.add(new PrefItem("shopMode", true));
		defItems.add(new PrefItem("returnToMain", false));
		defItems.add(new PrefItem("filterSelf", true));
		defItems.add(new PrefItem("dateUS", false));

		// Scrollbar appearance settings
		defItems.add(new PrefItem("scrollPerc", "10"));
		// defItems.add(new PrefItem("customScroll", true));
		defItems.add(new PrefItem("customScroll", app.customScrollDef));
		defItems.add(new PrefItem("scrollWidth", "25"));
		defItems.add(new PrefItem("scrollPad", "10"));

		// Search setting
		defItems.add(new PrefItem("searchSize", "5000"));
		defItems.add(new PrefItem("searchReport", "100"));
		defItems.add(new PrefItem("searchRoot", "/sdcard,/media/My Files"));

		// Viewer/editor settings
		defItems.add(new PrefItem("viewerMaxSize", "1048576"));
		defItems.add(new PrefItem("editorMaxSize", "262144"));

		// Maximal size of misc. persistent lists
		defItems.add(new PrefItem("lruSize", "30"));
		defItems.add(new PrefItem("favSize", "30"));
		defItems.add(new PrefItem("appLruSize", "30"));
		defItems.add(new PrefItem("appFavSize", "30"));

		// Confirmations
		defItems.add(new PrefItem("useFileManagerFunctions", true));
		defItems.add(new PrefItem("confirmFileDelete", true));
		defItems.add(new PrefItem("confirmDirDelete", true));
		defItems.add(new PrefItem("confirmNonEmptyDirDelete", true));
	}

	private void revert() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		for (PrefItem p : defItems) {
			if (p.isBoolean)
				editor.putBoolean(p.name, p.bValue);
			else
				editor.putString(p.name, p.sValue);
		}
		editor.commit();
	}

	private void cancel() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		for (PrefItem p : savedItems) {
			if (p.isBoolean)
				editor.putBoolean(p.name, p.bValue);
			else
				editor.putString(p.name, p.sValue);
		}
		editor.commit();
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			if (p.getKey().equals("homeButtonST")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("homeButtonSTopenN", "1"));
			} else if (p.getKey().equals("homeButtonDT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("homeButtonDTopenN", "1"));
			} else if (p.getKey().equals("homeButtonLT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("homeButtonLTopenN", "1"));
			} else if (p.getKey().equals("advancedButtonST")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("advancedButtonSTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("advancedButtonDT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("advancedButtonDTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("advancedButtonLT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("advancedButtonLTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("memButtonST")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("memButtonSTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("memButtonDT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("memButtonDTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("memButtonLT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("memButtonLTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("batButtonST")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("batButtonSTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("batButtonDT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("batButtonDTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("batButtonLT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("batButtonLTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else {
				p.setSummary(listPref.getEntry());
			}
		}
		// if (p instanceof EditTextPreference) {
		// EditTextPreference editTextPref = (EditTextPreference) p;
		// p.setSummary(editTextPref.getText());
		// }
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		app = ((ReLaunchApp) getApplicationContext());
		app.setFullScreenIfNecessary(this);

		applicationsArray = app.getApps();
		applications = applicationsArray
				.toArray(new CharSequence[applicationsArray.size()]);
		happlications = app.getApps().toArray(
				new CharSequence[app.getApps().size()]);
		for (int j = 0; j < happlications.length; j++) {
			String happ = (String) happlications[j];
			String[] happp = happ.split("\\%");
			happlications[j] = happp[2];
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		setContentView(R.layout.prefs_main);

		// Save items value
		setDefaults();

		savedItems = new ArrayList<PrefItem>();
		for (PrefItem p : defItems) {
			PrefItem s = new PrefItem(p);
			if (prefs.contains(p.name)) {
				if (p.isBoolean) {
					s.bValue = prefs.getBoolean(p.name, p.bValue);
				} else {
					s.sValue = prefs.getString(p.name, p.sValue);
				}
			}
			savedItems.add(s);
		}

		((Button) findViewById(R.id.filter_settings))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(PrefsActivity.this,
								FiltersActivity.class);
						startActivity(intent);
					}
				});
		((Button) findViewById(R.id.associations))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(PrefsActivity.this,
								TypesActivity.class);
						startActivityForResult(intent, TYPES_ACT);
					}
				});
		// ((Button) findViewById(R.id.prefs_ok))
		// .setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// finish();
		// }
		// });
		((Button) findViewById(R.id.restart_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
						mgr.set(AlarmManager.RTC,
								System.currentTimeMillis() + 500,
								app.RestartIntent);
						System.exit(0);
					}
				});
		final Activity pact = this;
		((Button) findViewById(R.id.revert_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								pact);
						// builder.setTitle("Default settings warning");
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_default_settings_title));
						// builder.setMessage("Are you sure to restore default settings?");
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_default_settings_text));
						// builder.setPositiveButton("Yes", new
						// DialogInterface.OnClickListener() {
						builder.setPositiveButton(
								getResources().getString(R.string.jv_prefs_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										revert();
										AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 500,
												app.RestartIntent);
										System.exit(0);
									}
								});
						// builder.setNegativeButton("No", new
						// DialogInterface.OnClickListener() {
						builder.setNegativeButton(
								getResources().getString(R.string.jv_prefs_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
					}
				});
		// ((Button) findViewById(R.id.cancel_btn))
		// .setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// cancel();
		// finish();
		// }
		// });
		// back button - work as cancel
		// final Activity pact = this;
		((ImageButton) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								pact);
						// builder.setTitle("Decline changes warning");
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_decline_changes_title));
						// builder.setMessage("Are you sure to decline changes?");
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_decline_changes_text));
						// builder.setPositiveButton("Yes", new
						// DialogInterface.OnClickListener() {
						builder.setPositiveButton(
								getResources().getString(R.string.jv_prefs_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 500,
												app.RestartIntent);
										System.exit(0);
									}
								});
						// builder.setNegativeButton("No", new
						// DialogInterface.OnClickListener() {
						builder.setNegativeButton(
								getResources().getString(
										R.string.jv_prefs_cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						// builder.setNeutralButton("Cancel", new
						// DialogInterface.OnClickListener() {
						builder.setNeutralButton(
								getResources().getString(R.string.jv_prefs_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										cancel();
										finish();
									}
								});
						builder.show();
					}
				});

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		app.generalOnResume(TAG, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.d(TAG, "onActivityResult, " + requestCode + " " + resultCode);
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = ReLaunch.createReadersString(app.getReaders());
			// Log.d(TAG, "New types string: \"" + newTypes + "\"");

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();
			break;
		default:
			return;
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		final Preference pref = findPreference(key);
		// update summary
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}

		// special cases
		if (do_pref_subrequest) {
			if (key.equals("homeButtonST")) {
				if (sharedPreferences.getString(key, "OPENN").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder1.setTitle("Intent type");
					// builder1.setTitle("Select number");
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonSTopenN", "1"));
					builder1.setView(input);
					// builder1.setPositiveButton("OK",
					builder1.setPositiveButton(
							getResources().getString(R.string.jv_prefs_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// input.getText().toString()
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("homeButtonSTopenN", input
											.getText().toString());
									editor.commit();
									updatePrefSummary(pref);
								}
							});
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					builder1.show();
				}
			}
			if (key.equals("homeButtonDT")) {
				if (sharedPreferences.getString(key, "OPENMENU")
						.equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder1.setTitle("Intent type");
					builder1.setTitle("Select num");
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonDTopenN", "1"));
					builder1.setView(input);
					// builder1.setPositiveButton("Ok", new
					// DialogInterface.OnClickListener() {
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// input.getText().toString()
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("homeButtonDTopenN", input
											.getText().toString());
									editor.commit();
									updatePrefSummary(pref);
								}
							});
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					builder1.show();
				}
			}
			if (key.equals("homeButtonLT")) {
				if (sharedPreferences.getString(key, "OPENSCREEN").equals(
						"OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder1.setTitle("Intent type");
					builder1.setTitle("Select num");
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonLTopenN", "1"));
					builder1.setView(input);
					// builder1.setPositiveButton("Ok", new
					// DialogInterface.OnClickListener() {
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// input.getText().toString()
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("homeButtonLTopenN", input
											.getText().toString());
									editor.commit();
									updatePrefSummary(pref);
								}
							});
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					builder1.show();
				}
			}
			if (key.equals("advancedButtonST")) {
				if (sharedPreferences.getString(key, "RELAUNCH").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("advancedButtonSTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("advancedButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("advancedButtonDTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("advancedButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("advancedButtonLTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("memButtonST")) {
				if (sharedPreferences.getString(key, "RELAUNCH").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("memButtonSTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("memButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("memButtonDTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("memButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("memButtonLTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("batButtonST")) {
				if (sharedPreferences.getString(key, "RELAUNCH").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("batButtonSTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("batButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("batButtonDTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("batButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// builder2.setTitle("Select application");
					builder.setTitle(getResources().getString(R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("batButtonLTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}			
		}
	}
}
