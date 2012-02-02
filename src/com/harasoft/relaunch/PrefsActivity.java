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
import android.widget.ListView;
import android.os.SystemClock;
import android.view.MotionEvent;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	final String TAG = "PreferenceActivity";
	final static public int TYPES_ACT = 1;
	final static public int FILTS_ACT = 2;

	boolean settings_changed = false;

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
		defItems.add(new PrefItem("firstLineFontSizePx", "20"));
		defItems.add(new PrefItem("secondLineFontSizePx", "16"));
		defItems.add(new PrefItem("firstLineIconSizePx", "48"));
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
		defItems.add(new PrefItem("lruButtonST", "OPENSCREEN"));
		defItems.add(new PrefItem("lruButtonSTopenN", "1"));
		defItems.add(new PrefItem("lruButtonDT", "NOTHING"));
		defItems.add(new PrefItem("lruButtonDTopenN", "1"));
		defItems.add(new PrefItem("lruButtonLT", "NOTHING"));
		defItems.add(new PrefItem("lruButtonLTopenN", "1"));
		defItems.add(new PrefItem("favButtonST", "OPENSCREEN"));
		defItems.add(new PrefItem("favButtonSTopenN", "1"));
		defItems.add(new PrefItem("favButtonDT", "NOTHING"));
		defItems.add(new PrefItem("favButtonDTopenN", "1"));
		defItems.add(new PrefItem("favButtonLT", "NOTHING"));
		defItems.add(new PrefItem("favButtonLTopenN", "1"));
		defItems.add(new PrefItem("settingsButtonST", "RELAUNCH"));
		defItems.add(new PrefItem("settingsButtonSTapp", "%%"));
		defItems.add(new PrefItem("settingsButtonDT", "NOTHING"));
		defItems.add(new PrefItem("settingsButtonDTapp", "%%"));
		defItems.add(new PrefItem("settingsButtonLT", "NOTHING"));
		defItems.add(new PrefItem("settingsButtonLTapp", "%%"));
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
		defItems.add(new PrefItem("disableScrollJump", true));
		defItems.add(new PrefItem("scrollPerc", "10"));
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
			} else if (p.getKey().equals("lruButtonST")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("lruButtonSTopenN", "1"));
			} else if (p.getKey().equals("lruButtonDT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("lruButtonDTopenN", "1"));
			} else if (p.getKey().equals("lruButtonLT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("lruButtonLTopenN", "1"));
			} else if (p.getKey().equals("favButtonST")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("favButtonSTopenN", "1"));
			} else if (p.getKey().equals("favButtonDT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("favButtonDTopenN", "1"));
			} else if (p.getKey().equals("favButtonLT")
					&& listPref.getValue().toString().equals("OPENN")) {
				p.setSummary(listPref.getEntry()
						+ prefs.getString("favButtonLTopenN", "1"));
			} else if (p.getKey().equals("settingsButtonST")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("settingsButtonSTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("settingsButtonDT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("settingsButtonDTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("settingsButtonLT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("settingsButtonLTapp", "%%")
						.split("\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
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
		// For future - even more intellectual Settings
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
						startActivityForResult(intent, FILTS_ACT);
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
						// "Default settings warning"
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_default_settings_title));
						// "Are you sure to restore default settings?"
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_default_settings_text));
						// "Yes"
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
						// "No"
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
		// back button - work as cancel
		((ImageButton) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (settings_changed) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									pact);
							// "Decline changes warning"
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_decline_changes_title));
							// "Are you sure to decline changes?"
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_decline_changes_text));
							// "Yes"
							builder.setPositiveButton(
									getResources().getString(
											R.string.jv_prefs_yes),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
											mgr.set(AlarmManager.RTC,
													System.currentTimeMillis() + 500,
													app.RestartIntent);
											System.exit(0);
										}
									});
							// "No"
							builder.setNegativeButton(
									getResources().getString(
											R.string.jv_prefs_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
										}
									});
							// "Cancel"
							builder.setNeutralButton(
									getResources().getString(
											R.string.jv_prefs_no),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
											cancel();
											finish();
										}
									});
							builder.show();
						} else {
							finish();
						}
					}
				});

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		ImageButton upScroll = (ImageButton) findViewById(R.id.btn_scrollup);
		upScroll.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (DeviceInfo.EINK_NOOK) {
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 100, 0);
					getListView().dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 200, 0);
					getListView().dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 200, 0);
					getListView().dispatchTouchEvent(ev);
				} else {
					final ListView lv = (ListView) getListView();
					int first = lv.getFirstVisiblePosition();
					int visible = lv.getLastVisiblePosition()
							- lv.getFirstVisiblePosition() + 1;
					first -= visible;
					if (first < 0)
						first = 0;
					final int finfirst = first;
					lv.clearFocus();
					lv.post(new Runnable() {

						public void run() {
							lv.setSelection(finfirst);
						}
					});
				}
			}
		});

		ImageButton downScroll = (ImageButton) findViewById(R.id.btn_scrolldown);
		downScroll.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (DeviceInfo.EINK_NOOK) {
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 200, 0);
					getListView().dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 100, 0);
					getListView().dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 100, 0);
					getListView().dispatchTouchEvent(ev);
				} else {
					final ListView lv = (ListView) getListView();
					int total = lv.getCount();
					int last = lv.getLastVisiblePosition();
					if (total == last + 1)
						return;
					int target = last + 1;
					if (target > (total - 1))
						target = total - 1;
					final int ftarget = target;
					lv.clearFocus();
					lv.post(new Runnable() {
						public void run() {
							lv.setSelection(ftarget);
						}
					});
				}

			}
		});

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
		if (resultCode == Activity.RESULT_OK) {
			settings_changed = true;
		}
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = ReLaunch.createReadersString(app.getReaders());
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

		settings_changed = true;

		final Preference pref = findPreference(key);
		// update summary
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}

		// special cases
		if (do_pref_subrequest) {
			// HOME
			if (key.equals("homeButtonST")) {
				if (sharedPreferences.getString(key, "OPENN").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonSTopenN", "1"));
					builder1.setView(input);
					// "OK"
					builder1.setPositiveButton(
							getResources().getString(R.string.jv_prefs_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
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
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonDTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
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
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"homeButtonLTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
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
			// LRU
			if (key.equals("lruButtonST")) {
				if (sharedPreferences.getString(key, "OPENSCREEN").equals(
						"OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"lruButtonSTopenN", "1"));
					builder1.setView(input);
					// "OK"
					builder1.setPositiveButton(
							getResources().getString(R.string.jv_prefs_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("lruButtonSTopenN", input
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
			if (key.equals("lruButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"lruButtonDTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("lruButtonDTopenN", input
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
			if (key.equals("lruButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"lruButtonLTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("lruButtonLTopenN", input
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
			// FAV
			if (key.equals("favButtonST")) {
				if (sharedPreferences.getString(key, "OPENSCREEN").equals(
						"OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"favButtonSTopenN", "1"));
					builder1.setView(input);
					// "OK"
					builder1.setPositiveButton(
							getResources().getString(R.string.jv_prefs_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("favButtonSTopenN", input
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
			if (key.equals("favButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"favButtonDTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("favButtonDTopenN", input
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
			if (key.equals("favButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("OPENN")) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select number"
					builder1.setTitle(getResources().getString(
							R.string.jv_prefs_select_number));
					final EditText input = new EditText(PrefsActivity.this);
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					input.setText(sharedPreferences.getString(
							"favButtonLTopenN", "1"));
					builder1.setView(input);
					// "Ok"
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									dialog.dismiss();
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("favButtonLTopenN", input
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
			if (key.equals("settingsButtonST")) {
				if (sharedPreferences.getString(key, "RELAUNCH").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("settingsButtonSTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("settingsButtonDT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("settingsButtonDTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("settingsButtonLT")) {
				if (sharedPreferences.getString(key, "NOTHING").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
					builder.setSingleChoiceItems(happlications, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int i) {
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("settingsButtonLTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("advancedButtonST")) {
				if (sharedPreferences.getString(key, "RELAUNCH").equals("RUN")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PrefsActivity.this);
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
					// "Select application"
					builder.setTitle(getResources().getString(
							R.string.jv_prefs_select_application));
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
