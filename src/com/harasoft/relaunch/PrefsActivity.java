package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.R.integer;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.os.SystemClock;
import android.view.MotionEvent;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	final Context context = this;
	final String TAG = "PreferenceActivity";
	final static public int TYPES_ACT = 1;
	final static public int FILTS_ACT = 2;

	ReLaunchApp app;
	List<String> applicationsArray;
	CharSequence[] applications;
	CharSequence[] happlications;

	SharedPreferences prefs;
	boolean do_pref_subrequest = true;
	Map<String, ?> oldPrefs;
	Map<String, ?> newPrefs;

	//Clear shared preferences
	private void resetPreferences() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
		do_pref_subrequest = true;
	}

	//Undoes the preference changes
	private void cancel() {
		do_pref_subrequest = false;
		SharedPreferences.Editor editor = prefs.edit();
		Set<String> keys = oldPrefs.keySet();
		for (String key : keys) {
			Object value = oldPrefs.get(key);
			if (value instanceof Boolean) {
				editor.putBoolean(key, ((Boolean) value).booleanValue());
			} else if (value instanceof Float) {
				editor.putFloat(key, ((Float) value).floatValue());
			} else if (value instanceof Integer) {
				editor.putInt(key, ((Integer) value).intValue());
			} else if (value instanceof Long) {
				editor.putLong(key, ((Long) value).longValue());
			} else if (value instanceof String) {
				editor.putString(key, (String) value);
			}
		}
		editor.commit();
		do_pref_subrequest = true;
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
			} else if (p.getKey().equals("appFavButtonST")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("appFavButtonSTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("appFavButtonDT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("appFavButtonDTapp", "%%").split(
						"\\%");
				p.setSummary(listPref.getEntry() + " \"" + appa[2] + "\"");
			} else if (p.getKey().equals("appFavButtonLT")
					&& listPref.getValue().toString().equals("RUN")) {
				String[] appa = prefs.getString("appFavButtonLTapp", "%%").split(
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
		} else if (p instanceof PreferenceScreen) {
			PreferenceScreen pScr = (PreferenceScreen) p;
			for (int i = 0; i < pScr.getPreferenceCount(); i++) {
				initSummary(pScr.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		app = ((ReLaunchApp) getApplicationContext());
//		app.setFullScreenIfNecessary(this);

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

		PreferenceScreen prefAdvancedScreen = (PreferenceScreen) findPreference("screenAdvanced");
		prefAdvancedScreen.setOnPreferenceClickListener(prefScreenListener);

		for (int i = 0; i < prefAdvancedScreen.getPreferenceCount(); i++) {
			Preference p = prefAdvancedScreen.getPreference(i);
			if (p instanceof PreferenceScreen)
				p.setOnPreferenceClickListener(prefScreenListener);
		}

		// Save items value
//		setDefaults();

		oldPrefs = prefs.getAll();

		findViewById(R.id.LLbuttons).setVisibility(View.GONE);

		findPreference("cleanupDatabase").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_cleanup_database_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_cleanup_database_text));
						builder.setPositiveButton(
								getResources().getString(R.string.jv_prefs_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										app.dataBase.resetDb();
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.jv_relaunch_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("cleanupLRU").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_clear_lists_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_clear_lru_text));
						builder.setPositiveButton(
								getResources().getString(R.string.jv_prefs_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										app.setDefault("lastOpened");
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.jv_relaunch_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("cleanupFAV").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						builder.setTitle(getResources().getString(
								R.string.jv_prefs_clear_lists_title));
						builder.setMessage(getResources().getString(
								R.string.jv_prefs_clear_favorites_text));
						builder.setPositiveButton(
								getResources().getString(R.string.jv_prefs_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										app.setDefault("favorites");
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(
										R.string.jv_relaunch_no),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("fileFilter").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						Intent intent = new Intent(PrefsActivity.this,
								FiltersActivity.class);
						startActivityForResult(intent, FILTS_ACT);
						return true;
					}
				});

		findPreference("fileAssociations").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						Intent intent = new Intent(PrefsActivity.this,
								TypesActivity.class);
						startActivityForResult(intent, TYPES_ACT);
						return true;
					}
				});

		findPreference("resetSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								context);
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
										resetPreferences();
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
						return true;
					}
				});

		findPreference("saveSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						boolean ret = app.copyPrefs(app.DATA_DIR,
								app.BACKUP_DIR);
						AlertDialog.Builder builder = new AlertDialog.Builder(
								context);
						if (ret) {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_ok_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_ok_text));
						} else {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_fail_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_fail_text));
						}
						builder.setNeutralButton(
								getResources().getString(R.string.jv_prefs_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("loadSettings").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						boolean ret = app.copyPrefs(app.BACKUP_DIR,
								app.DATA_DIR);
						AlertDialog.Builder builder = new AlertDialog.Builder(
								context);
						if (ret) {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_ok_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_okrst_text));
						} else {
							builder.setTitle(getResources().getString(
									R.string.jv_prefs_rsr_fail_title));
							builder.setMessage(getResources().getString(
									R.string.jv_prefs_rsr_fail_text));
						}
						builder.setNeutralButton(
								getResources().getString(R.string.jv_prefs_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 500,
												app.RestartIntent);
										System.exit(0);
									}
								});
						builder.show();
						return true;
					}
				});

		findPreference("restart").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
						mgr.set(AlarmManager.RTC,
								System.currentTimeMillis() + 500,
								app.RestartIntent);
						System.exit(0);
						return true;
					}
				});

		// final Activity pact = this;

		// back button - work as cancel
		((ImageButton) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (isPreferencesChanged()) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
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
		// if (resultCode == Activity.RESULT_OK) {
		// settings_changed = true;
		// }
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

		final Preference pref = findPreference(key);
		// update summary
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}

		if (do_pref_subrequest) {
			if (key.equals("startMode")) {
				String value = sharedPreferences.getString(key, "UNKNOWN");
				if (value.equals("LAUNCHER")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("fullScreen"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("homeMode"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("libraryMode"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("shopMode"))
							.setChecked(true);
					do_pref_subrequest = false;
				} else if (value.equals("PROGRAM")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("fullScreen"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("homeMode"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("libraryMode"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("shopMode"))
							.setChecked(false);
					do_pref_subrequest = true;
				}
			} else if (key.equals("workMode")) {
				String value = sharedPreferences.getString(key, "UNKNOWN");
				if (value.equals("FILES")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("rowSeparator"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("useFileManagerFunctions"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("openWith"))
					.setChecked(true);
					((CheckBoxPreference) findPreference("showFullDirPath"))
					.setChecked(true);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 0);
					editor.putBoolean("showBookTitles", false);
					editor.putBoolean("rowSeparator", false);
					editor.putBoolean("useFileManagerFunctions", true);
					editor.putBoolean("openWith", true);
					editor.putBoolean("showFullDirPath", true);
					editor.commit();
					do_pref_subrequest = true;
				} else if (value.equals("BOOKS")) {
					do_pref_subrequest = false;
					((CheckBoxPreference) findPreference("showBookTitles"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("rowSeparator"))
							.setChecked(true);
					((CheckBoxPreference) findPreference("useFileManagerFunctions"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("openWith"))
							.setChecked(false);
					((CheckBoxPreference) findPreference("showFullDirPath"))
					.setChecked(false);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("sortMode", 2);
					editor.putBoolean("showBookTitles", true);
					editor.putBoolean("rowSeparator", true);
					editor.putBoolean("useFileManagerFunctions", false);
					editor.putBoolean("openWith", false);
					editor.putBoolean("showFullDirPath", false);
					editor.commit();
					do_pref_subrequest = true;
				}
			} else if (key.equals("screenUpdateMode")) {
				boolean value = sharedPreferences.getBoolean(key, true);
				if (value) {
					((ListPreference) findPreference("einkUpdateMode"))
							.setValueIndex(2);
				} else {
					((ListPreference) findPreference("einkUpdateMode"))
							.setValueIndex(1);
				}
			} else if (key.equals("fileFontSize")) {
				do_pref_subrequest = false;
				String sValue = sharedPreferences.getString("fileFontSize", "20");
				int f1Size = (new Integer(sValue)).intValue();
				int f2Size = f1Size*4/5;
				((EditTextPreference) findPreference("firstLineFontSizePx"))
					.setText(((Integer) f1Size).toString());
				((EditTextPreference) findPreference("secondLineFontSizePx"))
					.setText(((Integer) f2Size).toString());
				do_pref_subrequest = true;
			}

			if ((key.equals("fullScreen")) || (key.equals("homeMode"))
					|| (key.equals("libraryMode")) || (key.equals("shopMode"))) {
				do_pref_subrequest = false;
				((ListPreference) findPreference("startMode")).setValueIndex(2);
				do_pref_subrequest = true;
			}
			if ((key.equals("showBookTitles"))
					|| (key.equals("useFileManagerFunctions"))) {
				do_pref_subrequest = false;
				((ListPreference) findPreference("workMode")).setValueIndex(2);
				do_pref_subrequest = true;
			}

			// special cases
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
			if (key.equals("appFavButtonST")) {
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
									editor.putString("appFavButtonSTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("appFavButtonDT")) {
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
									editor.putString("appFavButtonDTapp",
											(String) applications[i]);
									editor.commit();
									updatePrefSummary(pref);
									dialog.dismiss();
								}
							});
					builder.show();
				}
			}
			if (key.equals("appFavButtonLT")) {
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
									editor.putString("appFavButtonLTapp",
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

	public Preference.OnPreferenceClickListener prefScreenListener = new Preference.OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference pref) {
			final PreferenceScreen prefScreen = (PreferenceScreen) pref;
			LayoutInflater inflater = (LayoutInflater) getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View prefView = inflater.inflate(R.layout.prefs_main, null);
			prefScreen.getDialog().setContentView(prefView);
			final ListView prefListView = (ListView) prefView
					.findViewById(android.R.id.list);
			prefScreen.bind(prefListView);

			EditText tEdit = (EditText) prefView
					.findViewById(R.id.prefernces_title);
			tEdit.setText(pref.getTitle());

			ImageButton b = (ImageButton) prefView.findViewById(R.id.back_btn);
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					prefScreen.getDialog().dismiss();
				}
			});

			ImageButton bu = (ImageButton) prefView
					.findViewById(R.id.btn_scrollup);
			bu.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if (DeviceInfo.EINK_NOOK) {
						MotionEvent ev;
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_DOWN, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis() + 100,
								MotionEvent.ACTION_MOVE, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
						SystemClock.sleep(100);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_UP, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
					} else {
						int first = prefListView.getFirstVisiblePosition();
						int visible = prefListView.getLastVisiblePosition()
								- prefListView.getFirstVisiblePosition() + 1;
						first -= visible;
						if (first < 0)
							first = 0;
						final int finfirst = first;
						prefListView.clearFocus();
						prefListView.post(new Runnable() {

							public void run() {
								prefListView.setSelection(finfirst);
							}
						});
					}
				}
			});

			ImageButton bd = (ImageButton) prefView
					.findViewById(R.id.btn_scrolldown);
			bd.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if (DeviceInfo.EINK_NOOK) {
						MotionEvent ev;
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_DOWN, 200, 200, 0);
						prefListView.dispatchTouchEvent(ev);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis() + 100,
								MotionEvent.ACTION_MOVE, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
						SystemClock.sleep(100);
						ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(),
								MotionEvent.ACTION_UP, 200, 100, 0);
						prefListView.dispatchTouchEvent(ev);
					} else {
						int total = prefListView.getCount();
						int last = prefListView.getLastVisiblePosition();
						if (total == last + 1)
							return;
						int target = last + 1;
						if (target > (total - 1))
							target = total - 1;
						final int ftarget = target;
						prefListView.clearFocus();
						prefListView.post(new Runnable() {
							public void run() {
								prefListView.setSelection(ftarget);
							}
						});
					}

				}
			});

			return false;
		}
	};

	// Check changed preferences or not
	private boolean isPreferencesChanged() {
		boolean changed = false;
		newPrefs = prefs.getAll();
		Set<String> keys = newPrefs.keySet();
		for (String key : keys) {
			Object newObject = newPrefs.get(key);
			Object oldObject = oldPrefs.get(key);
			if (!isEqual(newObject, oldObject)) {
				changed = true;
				break;
			}
		}
		return changed;
	}

	// Compares two preference values
	private boolean isEqual(Object o1, Object o2) {
		boolean equal = true;
		if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
			boolean v1 = ((Boolean) o1).booleanValue();
			boolean v2 = ((Boolean) o2).booleanValue();
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof String) && (o2 instanceof String)) {
			String v1 = (String) o1;
			String v2 = (String) o2;
			if (!v1.equalsIgnoreCase(v2))
				equal = false;
		} else if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
			int v1 = ((Integer) o1).intValue();
			int v2 = ((Integer) o2).intValue();
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof Float) && (o2 instanceof Float)) {
			float v1 = ((Float) o1).floatValue();
			float v2 = ((Float) o2).floatValue();
			if (v1 != v2)
				equal = false;
		} else if ((o1 instanceof Long) && (o2 instanceof Long)) {
			long v1 = ((Long) o1).longValue();
			long v2 = ((Long) o2).longValue();
			if (v1 != v2)
				equal = false;
		}
		return equal;
	}

}
