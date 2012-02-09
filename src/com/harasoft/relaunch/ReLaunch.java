package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ReLaunch extends Activity {

	final static String TAG = "ReLaunch";
	static public final String APP_LRU_FILE = "AppLruFile.txt";
	static public final String APP_FAV_FILE = "AppFavorites.txt";
	static public final String LRU_FILE = "LruFile.txt";
	static public final String FAV_FILE = "Favorites.txt";
	static public final String HIST_FILE = "History.txt";
	static public final String FILT_FILE = "Filters.txt";
	static public final String COLS_FILE = "Columns.txt";
	final String defReaders = ".fb2,.fb2.zip:org.coolreader%org.coolreader.CoolReader%Cool Reader|.epub:Intent:application/epub|.jpg,.jpeg:Intent:image/jpeg"
			+ "|.png:Intent:image/png|.pdf:Intent:application/pdf";
	final static public String defReader = "org.coolreader%org.coolreader.CoolReader%Cool Reader";
	final static public int TYPES_ACT = 1;
	final static public int DIR_ACT = 2;
	final static int CNTXT_MENU_DELETE_F = 1;
	final static int CNTXT_MENU_DELETE_D_EMPTY = 2;
	final static int CNTXT_MENU_DELETE_D_NON_EMPTY = 3;
	final static int CNTXT_MENU_ADD = 4;
	final static int CNTXT_MENU_CANCEL = 5;
	final static int CNTXT_MENU_MARK_READING = 6;
	final static int CNTXT_MENU_MARK_FINISHED = 7;
	final static int CNTXT_MENU_MARK_FORGET = 8;
	final static int CNTXT_MENU_INTENT = 9;
	final static int CNTXT_MENU_OPENWITH = 10;
	final static int CNTXT_MENU_COPY = 11;
	final static int CNTXT_MENU_MOVE = 12;
	final static int CNTXT_MENU_PASTE = 13;
	String currentRoot = "/sdcard";
	Integer currentPosition = -1;
	List<HashMap<String, String>> itemsArray;
	Stack<Integer> positions = new Stack<Integer>();
	SimpleAdapter adapter;
	SharedPreferences prefs;
	ReLaunchApp app;
	static public boolean useHome = false;
	static boolean useHome1 = false;
	static boolean useShop = false;
	static boolean useLibrary = false;
	boolean useDirViewer = false;
	static public boolean filterMyself = true;
	static public String selfName = "com.harasoft.relaunch.Main";
	String[] allowedModels;
	String[] allowedDevices;
	String[] allowedManufacts;
	String[] allowedProducts;
	boolean addSView = true;

	// multicolumns per directory configuration
	List<String[]> columnsArray = new ArrayList<String[]>();
	Integer currentColsNum = -1;

	// Bottom info panel
	BroadcastReceiver batteryLevelReceiver = null;
	boolean batteryLevelRegistered = false;
	boolean mountReceiverRegistered = false;
	boolean powerReceiverRegistered = false;
	boolean wifiReceiverRegistered = false;
	TextView memTitle;
	TextView memLevel;
	TextView battTitle;
	TextView battLevel;
	IntentFilter batteryLevelFilter;
	
	BooksBase dataBase;
	String fileOpFile;
	String fileOpDir;
	int fileOp;

	private void actionSwitchWiFi() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			// "WiFi is off"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunch_turning_wifi_off),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(false);
		} else {
			// "WiFi is ON"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(
							R.string.jv_relaunch_turning_wifi_on),
					Toast.LENGTH_SHORT).show();
			wifiManager.setWifiEnabled(true);
		}
	}

	private void actionLock() {
		PowerFunctions.actionLock(ReLaunch.this);
	}

	private void actionPowerOff() {
		PowerFunctions.actionPowerOff(ReLaunch.this);
	}

	private void saveLast() {
		int appLruMax = 30;
		try {
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
	}

	private void actionRun(String appspec) {
		Intent i = app.getIntentByLabel(appspec);
		if (i == null)
			// "Activity \"" + item + "\" not found!"
			Toast.makeText(
					ReLaunch.this,
					getResources().getString(R.string.jv_allapp_activity)
							+ " \""
							+ appspec
							+ "\" "
							+ getResources().getString(
									R.string.jv_allapp_not_found),
					Toast.LENGTH_LONG).show();
		else {
			boolean ok = true;
			try {
				i.setAction(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				// "Activity \"" + item + "\" not found!"
				Toast.makeText(
						ReLaunch.this,
						getResources().getString(R.string.jv_allapp_activity)
								+ " \""
								+ appspec
								+ "\" "
								+ getResources().getString(
										R.string.jv_allapp_not_found),
						Toast.LENGTH_LONG).show();
				ok = false;
			}
			if (ok) {
				app.addToList("app_last", appspec, "X", false);
				saveLast();
			}
		}
	}

	private void setEinkController() {
		if (prefs != null) {
			Integer einkUpdateMode = 1;
			try {
				einkUpdateMode = Integer.parseInt(prefs.getString(
						"einkUpdateMode", "1"));
			} catch (Exception e) {
				einkUpdateMode = 1;
			}
			if (einkUpdateMode < -1 || einkUpdateMode > 2)
				einkUpdateMode = 1;
			if (einkUpdateMode >= 0) {
				EinkScreen.UpdateMode = einkUpdateMode;

				Integer einkUpdateInterval = 10;
				try {
					einkUpdateInterval = Integer.parseInt(prefs.getString(
							"einkUpdateInterval", "10"));
				} catch (Exception e) {
					einkUpdateInterval = 10;
				}
				if (einkUpdateInterval < 0 || einkUpdateInterval > 100)
					einkUpdateInterval = 10;
				EinkScreen.UpdateModeInterval = einkUpdateInterval;

				EinkScreen.PrepareController(null, false);
			}
		}
	}

	private boolean checkField(String[] a, String f) {
		for (int i = 0; i < a.length; i++)
			if (a[i].equals("*") || a[i].equals(f))
				return true;
		return false;
	}

	private void checkDevice(String dev, String man, String model,
			String product) {
		if (checkField(allowedModels, model))
			return;
		if (checkField(allowedDevices, dev))
			return;
		if (checkField(allowedManufacts, man))
			return;
		if (checkField(allowedProducts, product))
			return;

		if (!prefs.getBoolean("allowDevice", false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			WebView wv = new WebView(this);
			wv.loadDataWithBaseURL(null,
					getResources().getString(R.string.model_warning),
					"text/html", "utf-8", null);
			// "Wrong model !"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunch_wrong_model));
			builder.setView(wv);
			// "YES"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunch_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putBoolean("allowDevice", true);
							editor.commit();
							dialog.dismiss();
						}
					});
			// "NO"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunch_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
					});

			builder.show();
		}
	}

	static class ViewHolder {
		TextView tv;
		ImageView iv;
		LinearLayout tvHolder;
	}

	private Bitmap scaleDrawableById(int id, int size) {
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), id), size, size,
				true);
	}

	private Bitmap scaleDrawable(Drawable d, int size) {
		return Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),
				size, size, true);
	}

	class FLSimpleAdapter extends SimpleAdapter {

		FLSimpleAdapter(Context context, List<HashMap<String, String>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public int getCount() {
			return itemsArray.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.flist_layout, null);
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.fl_text);
				holder.iv = (ImageView) v.findViewById(R.id.fl_icon);
				holder.tvHolder = (LinearLayout) v.findViewById(R.id.fl_holder);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			// known extensions
			List<HashMap<String, String>> rc;
			ArrayList<String> exts = new ArrayList<String>();

			if (prefs.getBoolean("hideKnownExts", false)) {
				rc = app.getReaders();
				Set<String> tkeys = new HashSet<String>();
				for (int i = 0; i < rc.size(); i++) {
					Object[] keys = rc.get(i).keySet().toArray();
					for (int j = 0; j < keys.length; j++) {
						tkeys.add(keys[j].toString());
					}
				}
				exts = new ArrayList<String>(tkeys);
				final class ExtsComparator implements
						java.util.Comparator<String> {
					public int compare(String a, String b) {
						if (a == null && b == null)
							return 0;
						if (a == null && b != null)
							return 1;
						if (a != null && b == null)
							return -1;
						if (a.length() < b.length())
							return 1;
						if (a.length() > b.length())
							return -1;
						return a.compareTo(b);
					}
				}
				Collections.sort(exts, new ExtsComparator());
			}

			HashMap<String, String> item = itemsArray.get(position);
			if (item != null) {
				TextView tv = holder.tv;
				LinearLayout tvHolder = holder.tvHolder;
				ImageView iv = holder.iv;
				String sname = item.get("name");
				// clean extension, if needed
				if (prefs.getBoolean("hideKnownExts", false)) {
					for (int i = 0; i < exts.size(); i++) {
						if (sname.endsWith(exts.get(i))) {
							sname = sname.substring(0, sname.length()
									- exts.get(i).length());
						}
					}
				}

				String fname = item.get("fname");
				boolean setBold = false;
				boolean useFaces = prefs.getBoolean("showNew", true);

				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Integer
						.parseInt(prefs.getString("firstLineFontSizePx", "20")));

				if (item.get("type").equals("dir")) {
					if (useFaces) {
						tvHolder.setBackgroundColor(getResources().getColor(
								R.color.dir_bg));
						tv.setTextColor(getResources().getColor(R.color.dir_fg));
					}
					if (prefs.getString("firstLineIconSizePx", "48")
							.equals("0")) {
						iv.setVisibility(View.GONE);
					} else {
						iv.setImageBitmap(scaleDrawableById(R.drawable.dir_ok,
								Integer.parseInt(prefs.getString(
										"firstLineIconSizePx", "48"))));
					}
				} else {
					if (useFaces) {
						if (app.history.containsKey(fname)) {
							if (app.history.get(fname) == app.READING) {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_reading_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_reading_fg));
							} else if (app.history.get(fname) == app.FINISHED) {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_finished_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_finished_fg));
							} else {
								tvHolder.setBackgroundColor(getResources()
										.getColor(R.color.file_unknown_bg));
								tv.setTextColor(getResources().getColor(
										R.color.file_unknown_fg));
							}
						} else {
							tvHolder.setBackgroundColor(getResources()
									.getColor(R.color.file_new_bg));
							tv.setTextColor(getResources().getColor(
									R.color.file_new_fg));
							if (getResources().getBoolean(
									R.bool.show_new_as_bold))
								setBold = true;
						}
					}

					// setup icon
					if (prefs.getString("firstLineIconSizePx", "48")
							.equals("0")) {
						iv.setVisibility(View.GONE);
					} else {
						Drawable d = app.specialIcon(item.get("fname"), false);
						if (d != null)
							iv.setImageBitmap(scaleDrawable(d, Integer
									.parseInt(prefs.getString(
											"firstLineIconSizePx", "48"))));
						else {
							String rdrName = item.get("reader");
							if (rdrName.equals("Nope")) {
								File f = new File(item.get("fname"));
								if (f.length() > app.viewerMax)
									iv.setImageBitmap(scaleDrawableById(
											R.drawable.file_notok,
											Integer.parseInt(prefs
													.getString(
															"firstLineIconSizePx",
															"48"))));
								else
									iv.setImageBitmap(scaleDrawableById(
											R.drawable.file_ok,
											Integer.parseInt(prefs
													.getString(
															"firstLineIconSizePx",
															"48"))));
							} else if (rdrName.startsWith("Intent:"))
								iv.setImageBitmap(scaleDrawableById(
										R.drawable.icon, Integer.parseInt(prefs
												.getString(
														"firstLineIconSizePx",
														"48"))));
							else {
								if (app.getIcons().containsKey(rdrName))
									iv.setImageBitmap(scaleDrawable(
											app.getIcons().get(rdrName),
											Integer.parseInt(prefs
													.getString(
															"firstLineIconSizePx",
															"48"))));
								else
									iv.setImageBitmap(scaleDrawableById(
											R.drawable.file_ok,
											Integer.parseInt(prefs
													.getString(
															"firstLineIconSizePx",
															"48"))));
							}
						}
					}
				}

				if (useFaces) {
					SpannableString s = new SpannableString(sname);
					s.setSpan(new StyleSpan(setBold ? Typeface.BOLD
							: Typeface.NORMAL), 0, sname.length(), 0);
					tv.setText(s);
				} else {
					tvHolder.setBackgroundColor(getResources().getColor(
							R.color.normal_bg));
					tv.setTextColor(getResources().getColor(R.color.normal_fg));
					tv.setText(sname);
				}
			}
			// fixes on rows height in grid
			if (currentColsNum != 1) {
				GridView pgv = (GridView) parent;
				Integer gcols = currentColsNum;
				Integer between_columns = 0; // configure ???
				Integer after_row_space = 0; // configure ???
				Integer colw = (pgv.getWidth() - (gcols - 1) * between_columns)
						/ gcols;
				Integer recalc_num = position;
				Integer recalc_height = 0;
				while (recalc_num % gcols != 0) {
					recalc_num = recalc_num - 1;
					View temp_v = getView(recalc_num, null, parent);
					temp_v.measure(MeasureSpec.EXACTLY | colw,
							MeasureSpec.UNSPECIFIED);
					Integer p_height = temp_v.getMeasuredHeight();
					if (p_height > recalc_height)
						recalc_height = p_height;
				}
				if (recalc_height > 0) {
					v.setMinimumHeight(recalc_height + after_row_space);
				}
			}
			return v;
		}
	}

	private Integer Percentile(ArrayList<Integer> values, Integer Quantile)
	// not fully "mathematical proof", but not too difficult and working
	{
		Collections.sort(values);
		Integer index = (values.size() * Quantile) / 100;
		return values.get(index);
	}

	private Integer getAutoColsNum() {
		// implementation - via percentiles len
		Integer auto_cols_num = 1;
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		if (itemsArray.size() > 0) {
			Integer factor = 0;
			for (Integer i = 0; i < itemsArray.size(); i++) {
				tmp.add(itemsArray.get(i).get("name").length());
			}
			String pattern = prefs.getString("columnsAlgIntensity",
					"70 3:5 7:4 15:3 48:2"); // default - medium
			String[] spat = pattern.split("[\\s\\:]+");
			Integer quantile = Integer.parseInt(spat[0]);
			factor = Percentile(tmp, quantile);
			for (Integer i = 1; i < spat.length; i = i + 2) {
				try {
					double fval = Double.parseDouble(spat[i]);
					int cval = Integer.parseInt(spat[i + 1]);
					if (factor <= fval) {
						auto_cols_num = cval;
						break;
					}
				} catch (Exception e) {
				}
			}
		}
		if (auto_cols_num > itemsArray.size())
			auto_cols_num = itemsArray.size();
		return auto_cols_num;
	}

	private void redrawList() {
		setEinkController();

		if (prefs.getBoolean("filterResults", false)) {
			List<HashMap<String, String>> newItemsArray = new ArrayList<HashMap<String, String>>();

			for (HashMap<String, String> item : itemsArray) {
				if (item.get("type").equals("dir")
						|| app.filterFile(item.get("dname"), item.get("name")))
					newItemsArray.add(item);
			}
			itemsArray = newItemsArray;
		}
		adapter.notifyDataSetChanged();
	}

	private static List<HashMap<String, String>> parseReadersString(
			String readerList) {
		List<HashMap<String, String>> rc = new ArrayList<HashMap<String, String>>();
		String[] rdrs = readerList.split("\\|");
		for (int i = 0; i < rdrs.length; i++) {
			String[] re = rdrs[i].split(":");
			switch (re.length) {
			case 2:
				String rName = re[1];
				String[] exts = re[0].split(",");
				for (int j = 0; j < exts.length; j++) {
					String ext = exts[j];
					HashMap<String, String> r = new HashMap<String, String>();
					r.put(ext, rName);
					rc.add(r);
				}
				break;
			case 3:
				if (re[1].equals("Intent")) {
					String iType = re[2];
					String[] exts1 = re[0].split(",");
					for (int j = 0; j < exts1.length; j++) {
						String ext = exts1[j];
						HashMap<String, String> r = new HashMap<String, String>();
						r.put(ext, "Intent:" + iType);
						rc.add(r);
					}
				}
				break;
			}
		}
		return rc;
	}

	public static String createReadersString(List<HashMap<String, String>> rdrs) {
		String rc = new String();

		for (HashMap<String, String> r : rdrs) {
			for (String key : r.keySet()) {
				if (!rc.equals(""))
					rc += "|";
				rc += key + ":" + r.get(key);
			}
		}
		return rc;
	}

	private void pushCurrentPos(AdapterView<?> parent, boolean push_to_stack) {
		Integer p1 = parent.getFirstVisiblePosition();
		if (push_to_stack)
			positions.push(p1);
		currentPosition = p1;
	}

	private void setUpButton(final Button up, final String upDir, String currDir) {
		if (up != null) {
			// more versatile check against home, if needed
			boolean enabled = !upDir.equals("");
			if (enabled && !currDir.equals("/")
					&& prefs.getBoolean("notLeaveStartDir", false)) {
				enabled = false;
				String[] homes = prefs.getString("startDir",
						"/sdcard,/media/My Files").split("\\,");
				for (int i = 0; i < homes.length; i++) {
					if (homes[i].length() < currDir.length()
							&& currDir.startsWith(homes[i])) {
						enabled = true;
						break;
					}
				}
			}
			up.setEnabled(enabled);
			// gesture listener
			class UpSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (!upDir.equals("")) {
						Integer p = -1;
						if (!positions.empty())
							p = positions.pop();
						drawDirectory(upDir, p);
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					if (up.hasWindowFocus()) {

					}
				}
			}
			;
			UpSimpleOnGestureListener up_gl = new UpSimpleOnGestureListener();
			final GestureDetector up_gd = new GestureDetector(up_gl);
			up.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					up_gd.onTouchEvent(event);
					return false;
				}
			});
		}
	}

	private void refreshBottomInfo() {
		// Date
		String d;
		Calendar c = Calendar.getInstance();
		if (prefs.getBoolean("dateUS", false))
			d = String.format("%02d:%02d%s %02d/%02d/%02d",
					c.get(Calendar.HOUR), c.get(Calendar.MINUTE),
					((c.get(Calendar.AM_PM) == 0) ? "AM" : "PM"),
					c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
					(c.get(Calendar.YEAR) - 2000));
		else
			d = String.format("%02d:%02d %02d/%02d/%02d",
					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
					c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
					(c.get(Calendar.YEAR) - 2000));
		if (memTitle != null)
			memTitle.setText(d);

		// Memory
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		if (memLevel != null) {
			// "M free"
			memLevel.setText(mi.availMem / 1048576L
					+ getResources().getString(R.string.jv_relaunch_m_free));
			memLevel.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.ram), null);
		}

		// Wifi status
		WifiManager wfm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (battTitle != null) {
			if (wfm.isWifiEnabled()) {
				String nowConnected = wfm.getConnectionInfo().getSSID();
				if (nowConnected != null && !nowConnected.equals("")) {
					battTitle.setText(nowConnected);
				} else {
					battTitle.setText(getResources().getString(
							R.string.jv_relaunch_wifi_is_on));
				}
				battTitle.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.wifi_on), null,
						null, null);
			} else {
				// "WiFi is off"
				battTitle.setText(getResources().getString(
						R.string.jv_relaunch_wifi_is_off));
				battTitle.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.wifi_off), null,
						null, null);
			}
		}

		// Battery
		if (batteryLevelReceiver == null) {
			batteryLevelReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					context.unregisterReceiver(this);
					batteryLevelRegistered = false;
					int rawlevel = intent.getIntExtra(
							BatteryManager.EXTRA_LEVEL, -1);
					int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,
							-1);
					int plugged = intent.getIntExtra(
							BatteryManager.EXTRA_PLUGGED, -1);
					int level = -1;
					if (rawlevel >= 0 && scale > 0) {
						level = (rawlevel * 100) / scale;
					}
					if (battLevel != null) {
						String add_text = "";
						if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
							add_text = " AC";
						} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
							add_text = " USB";
						}
						battLevel.setText(level + "%" + add_text);

						if (level < 25)
							battLevel
									.setCompoundDrawablesWithIntrinsicBounds(
											getResources().getDrawable(
													R.drawable.bat1), null,
											null, null);
						else if (level < 50)
							battLevel
									.setCompoundDrawablesWithIntrinsicBounds(
											getResources().getDrawable(
													R.drawable.bat2), null,
											null, null);
						else if (level < 75)
							battLevel
									.setCompoundDrawablesWithIntrinsicBounds(
											getResources().getDrawable(
													R.drawable.bat3), null,
											null, null);
						else
							battLevel
									.setCompoundDrawablesWithIntrinsicBounds(
											getResources().getDrawable(
													R.drawable.bat4), null,
											null, null);
					}

				}
			};
		}
		if (!batteryLevelRegistered) {
			registerReceiver(batteryLevelReceiver, batteryLevelFilter);
			batteryLevelRegistered = true;
		}
	}

	private void drawDirectory(String root, Integer startPosition) {
		File dir = new File(root);
		File[] allEntries = dir.listFiles();
		List<String> files = new ArrayList<String>();
		List<String> dirs = new ArrayList<String>();

		setEinkController();

		currentRoot = root;
		currentPosition = (startPosition == -1) ? 0 : startPosition;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("lastdir", currentRoot);
		editor.commit();

		final Button tv = (Button) findViewById(useDirViewer ? R.id.results_title
				: R.id.title_txt);
		final String dirAbsPath = dir.getAbsolutePath();
		tv.setText(dirAbsPath + " ("
				+ ((allEntries == null) ? 0 : allEntries.length) + ")");
		tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String[] columns = getResources().getStringArray(
						R.array.output_columns_names);
				final CharSequence[] columnsmode = new CharSequence[columns.length + 1];
				columnsmode[0] = getResources().getString(
						R.string.jv_relaunch_default);
				for (int i = 0; i < columns.length; i++) {
					columnsmode[i + 1] = columns[i];
				}
				Integer checked = -1;
				if (app.columns.containsKey(currentRoot)) {
					if (app.columns.get(currentRoot) == -1) {
						checked = 1;
					} else {
						checked = app.columns.get(currentRoot) + 1;
					}
				} else {
					checked = 0;
				}
				// get checked
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ReLaunch.this);
				// "Select application"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_select_columns));
				builder.setSingleChoiceItems(columnsmode, checked,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i) {
								if (i == 0) {
									app.columns.remove(currentRoot);
								} else {
									if (i == 1) {
										app.columns.put(currentRoot, -1);
									} else {
										app.columns.put(currentRoot, i - 1);
									}
								}
								app.saveList("columns");
								drawDirectory(currentRoot, currentPosition);
								dialog.dismiss();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		final Button up = (Button) findViewById(R.id.goup_btn);
		final ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
		if (adv != null) {
			class advSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (prefs.getString("advancedButtonST", "RELAUNCH").equals(
							"RELAUNCH")) {
						Intent i = new Intent(ReLaunch.this, Advanced.class);
						startActivity(i);
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("LOCK")) {
						actionLock();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("POWEROFF")) {
						actionPowerOff();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("SWITCHWIFI")) {
						actionSwitchWiFi();
					} else if (prefs.getString("advancedButtonST", "RELAUNCH")
							.equals("RUN")) {
						actionRun(prefs.getString("advancedButtonSTapp", "%%"));
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					if (prefs.getString("advancedButtonDT", "NOTHING").equals(
							"RELAUNCH")) {
						Intent i = new Intent(ReLaunch.this, Advanced.class);
						startActivity(i);
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("LOCK")) {
						actionLock();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("POWEROFF")) {
						actionPowerOff();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("SWITCHWIFI")) {
						actionSwitchWiFi();
					} else if (prefs.getString("advancedButtonDT", "NOTHING")
							.equals("RUN")) {
						actionRun(prefs.getString("advancedButtonDTapp", "%%"));
					}
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					if (adv.hasWindowFocus()) {
						if (prefs.getString("advancedButtonLT", "NOTHING")
								.equals("RELAUNCH")) {
							Intent i = new Intent(ReLaunch.this, Advanced.class);
							startActivity(i);
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("advancedButtonLT",
								"NOTHING").equals("RUN")) {
							actionRun(prefs.getString("advancedButtonLTapp",
									"%%"));
						}
					}
				}
			}
			;
			advSimpleOnGestureListener adv_gl = new advSimpleOnGestureListener();
			final GestureDetector adv_gd = new GestureDetector(adv_gl);
			adv.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					adv_gd.onTouchEvent(event);
					return false;
				}
			});
		}

		itemsArray = new ArrayList<HashMap<String, String>>();
		if (dir.getParent() != null)
			dirs.add("..");
		if (allEntries != null) {
			for (File entry : allEntries) {
				if (entry.isDirectory())
					dirs.add(entry.getName());
				else if (!prefs.getBoolean("filterResults", false)
						|| app.filterFile(dir.getAbsolutePath(),
								entry.getName()))
					files.add(entry.getName());
			}
		}
		Collections.sort(dirs);
		Collections.sort(files);
		String upDir = "";
		for (String f : dirs) {
			if ((f.charAt(0) == '.') && (f.charAt(1) != '.') && (!prefs.getBoolean("showHidden", false)))
				continue;
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("name", f);
			item.put("dname", dir.getAbsolutePath());
			if (f.equals("..")) {
				upDir = dir.getParent();
				continue;
			} else
				item.put("fname", dir.getAbsolutePath() + "/" + f);
			item.put("type", "dir");
			item.put("reader", "Nope");
			itemsArray.add(item);
		}
		for (String f : files) {
			if ((f.startsWith(".")) && (!prefs.getBoolean("showHidden", false)))
				continue;
			HashMap<String, String> item = new HashMap<String, String>();
			if (prefs.getBoolean("showBookTitles", false))
				item.put("name", getEbookName(dir.getAbsolutePath(), f));
			else
				item.put("name", f);
			item.put("dname", dir.getAbsolutePath());
			item.put("fname", dir.getAbsolutePath() + "/" + f);
			item.put("type", "file");
			item.put("reader", app.readerName(f));
			itemsArray.add(item);
		}

		String[] from = new String[] { "name" };
		int[] to = new int[] { R.id.fl_text };
		setUpButton(up, upDir, currentRoot);

		final GridView gv = (GridView) findViewById(useDirViewer ? R.id.results_list
				: R.id.gl_list);
		adapter = new FLSimpleAdapter(this, itemsArray,
				useDirViewer ? R.layout.results_layout : R.layout.flist_layout,
				from, to);
		gv.setAdapter(adapter);
		gv.setHorizontalSpacing(0);
		Integer colsNum = -1;
		if (getDirectoryColumns(currentRoot) != 0) {
			colsNum = getDirectoryColumns(currentRoot);
		} else {
			colsNum = Integer.parseInt(prefs
					.getString("columnsDirsFiles", "-1"));
		}
		// override auto (not working fine in adnroid)
		if (colsNum == -1) {
			colsNum = getAutoColsNum();
		}
		currentColsNum = colsNum;
		gv.setNumColumns(colsNum);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth",
							"25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}
				LinearLayout ll = (LinearLayout) findViewById(useDirViewer ? R.id.results_fl
						: R.id.gl_layout);
				final SView sv = new SView(getBaseContext());
				LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
						scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
				sv.setLayoutParams(pars);
				ll.addView(sv);
				gv.setOnScrollListener(new AbsListView.OnScrollListener() {
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						sv.total = totalItemCount;
						sv.count = visibleItemCount;
						sv.first = firstVisibleItem;
						setEinkController();
						sv.invalidate();
					}

					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
					}
				});
				addSView = false;
			}
		} else {
			gv.setOnScrollListener(new AbsListView.OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					setEinkController();
				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
				}
			});
		}

		registerForContextMenu(gv);
		if (startPosition != -1)
			gv.setSelection(startPosition);
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> item = itemsArray.get(position);

				if (item.get("type").equals("dir")) {
					// Goto directory
					pushCurrentPos(parent, true);
					drawDirectory(item.get("fname"), -1);
				} else if (app.specialAction(ReLaunch.this, item.get("fname")))
					pushCurrentPos(parent, false);
				else {
					pushCurrentPos(parent, false);
					if (item.get("reader").equals("Nope"))
						app.defaultAction(ReLaunch.this, item.get("fname"));
					else {
						// Launch reader
						if (app.askIfAmbiguous) {
							List<String> rdrs = app.readerNames(item
									.get("fname"));
							if (rdrs.size() < 1)
								return;
							else if (rdrs.size() == 1)
								start(app.launchReader(rdrs.get(0),
										item.get("fname")));
							else {
								final CharSequence[] applications = rdrs
										.toArray(new CharSequence[rdrs.size()]);
								final String rdr1 = item.get("fname");
								AlertDialog.Builder builder = new AlertDialog.Builder(
										ReLaunch.this);
								// "Select application"
								builder.setTitle(getResources()
										.getString(
												R.string.jv_relaunch_select_application));
								builder.setSingleChoiceItems(applications, -1,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int i) {
												start(app
														.launchReader(
																(String) applications[i],
																rdr1));
												dialog.dismiss();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
						} else
							start(app.launchReader(item.get("reader"),
									item.get("fname")));
					}
				}
			}
		});

		final Button upScroll = (Button) findViewById(R.id.upscroll_btn);
		if (prefs.getBoolean("disableScrollJump", true) == false) {
			upScroll.setText(app.scrollStep + "%");
		} else {
			upScroll.setText(getResources()
					.getString(R.string.jv_relaunch_prev));
		}
		class upScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (DeviceInfo.EINK_NOOK) { // nook
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 100, 0);
					gv.dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 200, 0);
					gv.dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 200, 0);
					gv.dispatchTouchEvent(ev);
				} else { // other devices
					int first = gv.getFirstVisiblePosition();
					int visible = gv.getLastVisiblePosition()
							- gv.getFirstVisiblePosition() + 1;
					int total = itemsArray.size();
					first -= visible;
					if (first < 0)
						first = 0;
					gv.setSelection(first);
					// some hack workaround against not scrolling in some cases
					if (total > 0) {
						gv.requestFocusFromTouch();
						gv.setSelection(first);
					}
				}
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (prefs.getBoolean("disableScrollJump", true) == false) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					first -= (total * app.scrollStep) / 100;
					if (first < 0)
						first = 0;
					gv.setSelection(first);
					// some hack workaround against not scrolling in some cases
					if (total > 0) {
						gv.requestFocusFromTouch();
						gv.setSelection(first);
					}
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (upScroll.hasWindowFocus()) {
					if (prefs.getBoolean("disableScrollJump", true) == false) {
						int first = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						first = 0;
						gv.setSelection(first);
						// some hack workaround against not scrolling in some
						// cases
						if (total > 0) {
							gv.requestFocusFromTouch();
							gv.setSelection(first);
						}
					}
				}
			}
		}
		;
		upScrlSimpleOnGestureListener upscrl_gl = new upScrlSimpleOnGestureListener();
		final GestureDetector upscrl_gd = new GestureDetector(upscrl_gl);
		upScroll.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				upscrl_gd.onTouchEvent(event);
				return false;
			}
		});

		class RepeatedDownScroll {
			public void doIt(int first, int target, int shift) {
				final GridView gv = (GridView) findViewById(R.id.gl_list);
				int total = gv.getCount();
				int last = gv.getLastVisiblePosition();
				if (total == last + 1)
					return;
				final int ftarget = target + shift;
				gv.clearFocus();
				gv.post(new Runnable() {
					public void run() {
						gv.setSelection(ftarget);
					}
				});
				final int ffirst = first;
				final int fshift = shift;
				gv.postDelayed(new Runnable() {
					public void run() {
						int nfirst = gv.getFirstVisiblePosition();
						if (nfirst == ffirst) {
							RepeatedDownScroll ds = new RepeatedDownScroll();
							ds.doIt(ffirst, ftarget, fshift + 1);
						}
					}
				}, 150);
			}
		}

		final Button downScroll = (Button) findViewById(R.id.downscroll_btn);
		if (prefs.getBoolean("disableScrollJump", true) == false) {
			downScroll.setText(app.scrollStep + "%");
		} else {
			downScroll.setText(getResources().getString(
					R.string.jv_relaunch_next));
		}
		class dnScrlSimpleOnGestureListener extends SimpleOnGestureListener {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (DeviceInfo.EINK_NOOK) { // nook special
					MotionEvent ev;
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 200, 200, 0);
					gv.dispatchTouchEvent(ev);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis() + 100,
							MotionEvent.ACTION_MOVE, 200, 100, 0);
					gv.dispatchTouchEvent(ev);
					SystemClock.sleep(100);
					ev = MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							200, 100, 0);
					gv.dispatchTouchEvent(ev);
				} else { // other devices
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					int last = gv.getLastVisiblePosition();
					if (total == last + 1)
						return true;
					int target = last + 1;
					if (target > (total - 1))
						target = total - 1;
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
				}
				return true;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (prefs.getBoolean("disableScrollJump", true) == false) {
					int first = gv.getFirstVisiblePosition();
					int total = itemsArray.size();
					int last = gv.getLastVisiblePosition();
					if (total == last + 1)
						return true;
					int target = first + (total * app.scrollStep) / 100;
					if (target <= last)
						target = last + 1; // Special for NOOK, otherwise it
											// won't redraw the listview
					if (target > (total - 1))
						target = total - 1;
					RepeatedDownScroll ds = new RepeatedDownScroll();
					ds.doIt(first, target, 0);
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (downScroll.hasWindowFocus()) {
					if (prefs.getBoolean("disableScrollJump", true) == false) {
						int first = gv.getFirstVisiblePosition();
						int total = itemsArray.size();
						int last = gv.getLastVisiblePosition();
						if (total == last + 1)
							return;
						int target = total - 1;
						RepeatedDownScroll ds = new RepeatedDownScroll();
						ds.doIt(first, target, 0);
					}
				}
			}
		}
		;
		dnScrlSimpleOnGestureListener dnscrl_gl = new dnScrlSimpleOnGestureListener();
		final GestureDetector dnscrl_gd = new GestureDetector(dnscrl_gl);
		downScroll.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				dnscrl_gd.onTouchEvent(event);
				return false;
			}
		});

		refreshBottomInfo();
	}

	private HashMap<String, Drawable> createIconsList(PackageManager pm) {
		Drawable d = null;
		HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent,
				0);
		String pname = "";
		String aname = "";
		String hname = "";
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				pname = ri.activityInfo.packageName;
				aname = ri.activityInfo.name;
				try {
					if (ri.activityInfo.labelRes != 0) {
						hname = (String) ri.activityInfo.loadLabel(pm);
					} else {
						hname = (String) ri.loadLabel(pm);
					}
					if (ri.activityInfo.icon != 0) {
						d = ri.activityInfo.loadIcon(pm);
					} else {
						d = ri.loadIcon(pm);
					}
				} catch (Exception e) {
				}
				if (d != null) {
					rc.put(pname + "%" + aname + "%" + hname, d);
				}
			}
		}
		return rc;
	}

	private static class AppComparator implements java.util.Comparator<String> {
		public int compare(String a, String b) {
			if (a == null && b == null) {
				return 0;
			}
			if (a == null && b != null) {
				return 1;
			}
			if (a != null && b == null) {
				return -1;
			}
			String[] ap = a.split("\\%");
			String[] bp = b.split("\\%");
			return ap[2].compareToIgnoreCase(bp[2]);
		}
	}

	static public List<String> createAppList(PackageManager pm) {
		List<String> rc = new ArrayList<String>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent,
				0);
		String pname = "";
		String aname = "";
		String hname = "";
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				pname = ri.activityInfo.packageName;
				aname = ri.activityInfo.name;
				try {
					if (ri.activityInfo.labelRes != 0) {
						hname = (String) ri.activityInfo.loadLabel(pm);
					} else {
						hname = (String) ri.loadLabel(pm);
					}
				} catch (Exception e) {
				}
				if (!filterMyself || !aname.equals(selfName))
					rc.add(pname + "%" + aname + "%" + hname);
			}
		}
		Collections.sort(rc, new AppComparator());
		return rc;
	}

	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(
						ReLaunch.this,
						getResources().getString(
								R.string.jv_relaunch_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}

	private BroadcastReceiver SDCardChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Code to react to SD mounted goes here
			Intent i = new Intent(context, ReLaunch.class);
			i.putExtra("home", useHome);
			i.putExtra("home1", useHome1);
			i.putExtra("shop", useShop);
			i.putExtra("library", useLibrary);
			startActivity(i);
		}
	};

	private BroadcastReceiver PowerChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshBottomInfo();
		}
	};

	private BroadcastReceiver WiFiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshBottomInfo();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If we called from Home launcher?
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			useHome = false;
			useHome1 = false;
			useShop = false;
			useLibrary = false;
			useDirViewer = false;
		} else {
			useHome = data.getBooleanExtra("home", false);
			useHome1 = data.getBooleanExtra("home1", false);
			useShop = data.getBooleanExtra("shop", false);
			useLibrary = data.getBooleanExtra("library", false);
			useDirViewer = data.getBooleanExtra("dirviewer", false);
		}

		dataBase = new BooksBase(this);

		// Global arrays
		allowedModels = getResources().getStringArray(R.array.allowed_models);
		allowedDevices = getResources().getStringArray(R.array.allowed_devices);
		allowedManufacts = getResources().getStringArray(
				R.array.allowed_manufacturers);
		allowedProducts = getResources().getStringArray(
				R.array.allowed_products);

		// Create global storage with values
		app = (ReLaunchApp) getApplicationContext();

		app.FLT_SELECT = getResources().getInteger(R.integer.FLT_SELECT);
		app.FLT_STARTS = getResources().getInteger(R.integer.FLT_STARTS);
		app.FLT_ENDS = getResources().getInteger(R.integer.FLT_ENDS);
		app.FLT_CONTAINS = getResources().getInteger(R.integer.FLT_CONTAINS);
		app.FLT_MATCHES = getResources().getInteger(R.integer.FLT_MATCHES);
		app.FLT_NEW = getResources().getInteger(R.integer.FLT_NEW);
		app.FLT_NEW_AND_READING = getResources().getInteger(
				R.integer.FLT_NEW_AND_READING);

		// Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String typesString = prefs.getString("types", defReaders);
		try {
			app.scrollStep = Integer.parseInt(prefs.getString("scrollPerc",
					"10"));
			app.viewerMax = Integer.parseInt(prefs.getString("viewerMaxSize",
					"1048576"));
			app.editorMax = Integer.parseInt(prefs.getString("editorMaxSize",
					"262144"));
		} catch (NumberFormatException e) {
			app.scrollStep = 10;
			app.viewerMax = 1048576;
			app.editorMax = 262144;
		}
		if (app.scrollStep < 1)
			app.scrollStep = 1;
		if (app.scrollStep > 100)
			app.scrollStep = 100;

		filterMyself = prefs.getBoolean("filterSelf", true);
		if (useHome1 && prefs.getBoolean("homeMode", true))
			useHome = true;
		if (useShop && prefs.getBoolean("shopMode", true))
			useHome = true;
		if (useLibrary && prefs.getBoolean("libraryMode", true))
			useHome = true;
		app.fullScreen = prefs.getBoolean("fullScreen", true);
		app.setFullScreenIfNecessary(this);

		// Create application icons map
		app.setIcons(createIconsList(getPackageManager()));

		// Create applications label list
		app.setApps(createAppList(getPackageManager()));

		// Readers list
		app.setReaders(parseReadersString(typesString));

		// Miscellaneous lists list
		app.readFile("lastOpened", LRU_FILE);
		app.readFile("favorites", FAV_FILE);
		app.readFile("filters", FILT_FILE, ":");
		app.filters_and = prefs.getBoolean("filtersAnd", true);
		app.readFile("columns", COLS_FILE, ":");
		app.columns.clear();
		for (String[] r : app.getList("columns")) {
			app.columns.put(r[0], Integer.parseInt(r[1]));
		}
		app.readFile("history", HIST_FILE, ":");
		app.history.clear();
		for (String[] r : app.getList("history")) {
			if (r[1].equals("READING"))
				app.history.put(r[0], app.READING);
			else if (r[1].equals("FINISHED"))
				app.history.put(r[0], app.FINISHED);
		}

		if (useDirViewer) {
			String start_dir = null;
			setContentView(R.layout.results_layout);
			if (data.getExtras() != null)
				start_dir = data.getStringExtra("start_dir");
			((ImageButton) findViewById(R.id.results_btn))
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							finish();
						}
					});
			if (start_dir != null)
				drawDirectory(start_dir, -1);
		} else {
			// Main layout
			setContentView(R.layout.main);
			if (!prefs.getBoolean("showButtons", true)) {
				hideLayout(R.id.linearLayoutTop);
			}
			if (useHome) {
				app.readFile("app_last", APP_LRU_FILE, ":");
				app.readFile("app_favorites", APP_FAV_FILE, ":");

				final ImageButton lrua_button = ((ImageButton) findViewById(R.id.app_last));
				class LruaSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						Intent intent = new Intent(ReLaunch.this,
								AllApplications.class);
						intent.putExtra("list", "app_last");
						// "Last recently used applications"
						intent.putExtra(
								"title",
								getResources().getString(
										R.string.jv_relaunch_lru_a));
						startActivity(intent);
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (lrua_button.hasWindowFocus()) {
						}
					}
				}
				;
				LruaSimpleOnGestureListener lrua_gl = new LruaSimpleOnGestureListener();
				final GestureDetector lrua_gd = new GestureDetector(lrua_gl);
				lrua_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						lrua_gd.onTouchEvent(event);
						return false;
					}
				});
				final ImageButton alla_button = ((ImageButton) findViewById(R.id.all_applications_btn));
				class AllaSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						Intent intent = new Intent(ReLaunch.this,
								AllApplications.class);
						intent.putExtra("list", "app_all");
						// "All applications"
						intent.putExtra(
								"title",
								getResources().getString(
										R.string.jv_relaunch_all_a));
						startActivity(intent);
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (alla_button.hasWindowFocus()) {

						}
					}
				}
				;
				AllaSimpleOnGestureListener alla_gl = new AllaSimpleOnGestureListener();
				final GestureDetector alla_gd = new GestureDetector(alla_gl);
				alla_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						alla_gd.onTouchEvent(event);
						return false;
					}
				});
				final ImageButton fava_button = ((ImageButton) findViewById(R.id.app_favorites));
				class FavaSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						Intent intent = new Intent(ReLaunch.this,
								AllApplications.class);
						intent.putExtra("list", "app_favorites");
						// "Favorite applications"
						intent.putExtra(
								"title",
								getResources().getString(
										R.string.jv_relaunch_fav_a));
						startActivity(intent);
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (fava_button.hasWindowFocus()) {

						}
					}
				}
				;
				FavaSimpleOnGestureListener fava_gl = new FavaSimpleOnGestureListener();
				final GestureDetector fava_gd = new GestureDetector(fava_gl);
				fava_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						fava_gd.onTouchEvent(event);
						return false;
					}
				});
			} else {
				hideLayout(R.id.linearLayoutBottom);
			}

			if (prefs.getBoolean("showButtons", true)) {

				final ImageButton home_button = (ImageButton) findViewById(R.id.home_btn);
				class HomeSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("homeButtonST", "OPENN").equals(
								"OPENN")) {
							openHome(Integer.parseInt(prefs.getString(
									"homeButtonSTopenN", "1")));
						} else if (prefs.getString("homeButtonST", "OPENN")
								.equals("OPENMENU")) {
							menuHome();
						} else if (prefs.getString("homeButtonST", "OPENN")
								.equals("OPENSCREEN")) {
							screenHome();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("homeButtonDT", "OPENMENU").equals(
								"OPENN")) {
							openHome(Integer.parseInt(prefs.getString(
									"homeButtonDTopenN", "1")));
						} else if (prefs.getString("homeButtonDT", "OPENMENU")
								.equals("OPENMENU")) {
							menuHome();
						} else if (prefs.getString("homeButtonDT", "OPENMENU")
								.equals("OPENSCREEN")) {
							screenHome();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (home_button.hasWindowFocus()) {
							if (prefs.getString("homeButtonLT", "OPENSCREEN")
									.equals("OPENN")) {
								openHome(Integer.parseInt(prefs.getString(
										"homeButtonLTopenN", "1")));
							} else if (prefs.getString("homeButtonLT",
									"OPENSCREEN").equals("OPENMENU")) {
								menuHome();
							} else if (prefs.getString("homeButtonLT",
									"OPENSCREEN").equals("OPENSCREEN")) {
								screenHome();
							}
						}
					}
				}
				;
				HomeSimpleOnGestureListener home_gl = new HomeSimpleOnGestureListener();
				final GestureDetector home_gd = new GestureDetector(home_gl);
				home_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						home_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton settings_button = (ImageButton) findViewById(R.id.settings_btn);
				class SettingsSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("settingsButtonST", "RELAUNCH")
								.equals("RELAUNCH")) {
							menuSettings();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("settingsButtonST",
								"RELAUNCH").equals("RUN")) {
							actionRun(prefs.getString("settingsButtonSTapp",
									"%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("settingsButtonDT", "RELAUNCH")
								.equals("RELAUNCH")) {
							menuSettings();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("settingsButtonDT",
								"RELAUNCH").equals("RUN")) {
							actionRun(prefs.getString("settingsButtonDTapp",
									"%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (settings_button.hasWindowFocus()) {
							if (prefs.getString("settingsButtonLT", "RELAUNCH")
									.equals("RELAUNCH")) {
								menuSettings();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("LOCK")) {
								actionLock();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs.getString("settingsButtonLT",
									"RELAUNCH").equals("RUN")) {
								actionRun(prefs.getString(
										"settingsButtonLTapp", "%%"));
							}
						}
					}
				}
				;
				SettingsSimpleOnGestureListener settings_gl = new SettingsSimpleOnGestureListener();
				final GestureDetector settings_gd = new GestureDetector(
						settings_gl);
				settings_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						settings_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton search_button = (ImageButton) findViewById(R.id.search_btn);
				class SearchSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						menuSearch();
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (search_button.hasWindowFocus()) {
						}
					}
				}
				;
				SearchSimpleOnGestureListener search_gl = new SearchSimpleOnGestureListener();
				final GestureDetector search_gd = new GestureDetector(search_gl);
				search_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						search_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton lru_button = (ImageButton) findViewById(R.id.lru_btn);
				class LruSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("lastOpened", Integer.parseInt(prefs
									.getString("lruButtonSTopenN", "1")) - 1);
						} else if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("lastOpened");
						} else if (prefs.getString("lruButtonST", "OPENSCREEN")
								.equals("OPENSCREEN")) {
							menuLastopened();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("lruButtonDT", "NOTHING").equals(
								"OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("lastOpened", Integer.parseInt(prefs
									.getString("lruButtonDTopenN", "1")) - 1);
						} else if (prefs.getString("lruButtonDT", "NOTHING")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("lastOpened");
						} else if (prefs.getString("lruButtonDT", "NOTHING")
								.equals("OPENSCREEN")) {
							menuLastopened();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (lru_button.hasWindowFocus()) {
							if (prefs.getString("lruButtonLT", "NOTHING")
									.equals("OPENN")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.runItem("lastOpened",
										Integer.parseInt(prefs.getString(
												"lruButtonLTopenN", "1")) - 1);
							} else if (prefs
									.getString("lruButtonLT", "NOTHING")
									.equals("OPENMENU")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.showMenu("lastOpened");
							} else if (prefs
									.getString("lruButtonLT", "NOTHING")
									.equals("OPENSCREEN")) {
								menuLastopened();
							}
						}
					}
				}
				;
				LruSimpleOnGestureListener lru_gl = new LruSimpleOnGestureListener();
				final GestureDetector lru_gd = new GestureDetector(lru_gl);
				lru_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						lru_gd.onTouchEvent(event);
						return false;
					}
				});

				final ImageButton fav_button = (ImageButton) findViewById(R.id.favor_btn);
				class FavSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("favorites", Integer.parseInt(prefs
									.getString("favButtonSTopenN", "1")) - 1);
						} else if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("favorites");
						} else if (prefs.getString("favButtonST", "OPENSCREEN")
								.equals("OPENSCREEN")) {
							menuFavorites();
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("favButtonDT", "NOTHING").equals(
								"OPENN")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.runItem("favorites", Integer.parseInt(prefs
									.getString("favButtonDTopenN", "1")) - 1);
						} else if (prefs.getString("favButtonDT", "NOTHING")
								.equals("OPENMENU")) {
							ListActions la = new ListActions(app, ReLaunch.this);
							la.showMenu("favorites");
						} else if (prefs.getString("favButtonDT", "NOTHING")
								.equals("OPENSCREEN")) {
							menuFavorites();
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (fav_button.hasWindowFocus()) {
							if (prefs.getString("favButtonLT", "NOTHING")
									.equals("OPENN")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.runItem("favorites",
										Integer.parseInt(prefs.getString(
												"favButtonLTopenN", "1")) - 1);
							} else if (prefs
									.getString("favButtonLT", "NOTHING")
									.equals("OPENMENU")) {
								ListActions la = new ListActions(app,
										ReLaunch.this);
								la.showMenu("favorites");
							} else if (prefs
									.getString("favButtonLT", "NOTHING")
									.equals("OPENSCREEN")) {
								menuFavorites();
							}
						}
					}
				}
				;
				FavSimpleOnGestureListener fav_gl = new FavSimpleOnGestureListener();
				final GestureDetector fav_gd = new GestureDetector(fav_gl);
				fav_button.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						fav_gd.onTouchEvent(event);
						return false;
					}
				});
			}

			// Memory buttons (task manager activity)
			final LinearLayout mem_l = (LinearLayout) findViewById(R.id.mem_layout);
			if (mem_l != null) {
				class MemlSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("memButtonST", "RELAUNCH").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(ReLaunch.this,
									TaskManager.class);
							startActivity(intent);
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("memButtonST", "RELAUNCH")
								.equals("RUN")) {
							actionRun(prefs.getString("memButtonSTapp", "%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("memButtonDT", "NOTHING").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(ReLaunch.this,
									TaskManager.class);
							startActivity(intent);
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("memButtonDT", "NOTHING")
								.equals("RUN")) {
							actionRun(prefs.getString("memButtonDTapp", "%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (mem_l.hasWindowFocus()) {
							if (prefs.getString("memButtonLT", "NOTHING")
									.equals("RELAUNCH")) {
								Intent intent = new Intent(ReLaunch.this,
										TaskManager.class);
								startActivity(intent);
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("LOCK")) {
								actionLock();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs
									.getString("memButtonLT", "NOTHING")
									.equals("RUN")) {
								actionRun(prefs.getString("memButtonLTapp",
										"%%"));
							}
						}
					}
				}
				;
				MemlSimpleOnGestureListener meml_gl = new MemlSimpleOnGestureListener();
				final GestureDetector meml_gd = new GestureDetector(meml_gl);
				mem_l.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						meml_gd.onTouchEvent(event);
						return false;
					}
				});
			}
			memLevel = (TextView) findViewById(R.id.mem_level);
			memTitle = (TextView) findViewById(R.id.mem_title);

			// Battery Layout
			final LinearLayout bat_l = (LinearLayout) findViewById(R.id.bat_layout);
			if (bat_l != null) {
				class BatlSimpleOnGestureListener extends
						SimpleOnGestureListener {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (prefs.getString("batButtonST", "RELAUNCH").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(
									Intent.ACTION_POWER_USAGE_SUMMARY);
							startActivity(intent);
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("batButtonST", "RELAUNCH")
								.equals("RUN")) {
							actionRun(prefs.getString("batButtonSTapp", "%%"));
						}
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (prefs.getString("batButtonDT", "NOTHING").equals(
								"RELAUNCH")) {
							Intent intent = new Intent(
									Intent.ACTION_POWER_USAGE_SUMMARY);
							startActivity(intent);
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("LOCK")) {
							actionLock();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("POWEROFF")) {
							actionPowerOff();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("SWITCHWIFI")) {
							actionSwitchWiFi();
						} else if (prefs.getString("batButtonDT", "NOTHING")
								.equals("RUN")) {
							actionRun(prefs.getString("batButtonDTapp", "%%"));
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						if (mem_l.hasWindowFocus()) {
							if (prefs.getString("batButtonLT", "NOTHING")
									.equals("RELAUNCH")) {
								Intent intent = new Intent(
										Intent.ACTION_POWER_USAGE_SUMMARY);
								startActivity(intent);
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("LOCK")) {
								actionLock();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("POWEROFF")) {
								actionPowerOff();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("SWITCHWIFI")) {
								actionSwitchWiFi();
							} else if (prefs
									.getString("batButtonLT", "NOTHING")
									.equals("RUN")) {
								actionRun(prefs.getString("batButtonLTapp",
										"%%"));
							}
						}
					}
				}
				;
				BatlSimpleOnGestureListener batl_gl = new BatlSimpleOnGestureListener();
				final GestureDetector batl_gd = new GestureDetector(batl_gl);
				bat_l.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						batl_gd.onTouchEvent(event);
						return false;
					}
				});
			}
			// Battery buttons
			battLevel = (TextView) findViewById(R.id.bat_level);
			battTitle = (TextView) findViewById(R.id.bat_title);
			batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

			// What's new processing
			final int latestVersion = prefs.getInt("latestVersion", 0);
			int tCurrentVersion = 0;
			try {
				tCurrentVersion = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;
			} catch (Exception e) {
			}
			final int currentVersion = tCurrentVersion;
			if (currentVersion > latestVersion) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				WebView wv = new WebView(this);
				wv.loadDataWithBaseURL(null,
						getResources().getString(R.string.about_help)
								+ getResources().getString(R.string.about_appr)
								+ getResources().getString(R.string.whats_new),
						"text/html", "utf-8", null);
				// "What's new"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_whats_new));
				builder.setView(wv);
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunch_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SharedPreferences.Editor editor = prefs.edit();
								editor.putInt("latestVersion", currentVersion);
								editor.commit();
								dialog.dismiss();
							}
						});
				builder.show();
			}

			// incorrect device warning
			checkDevice(Build.DEVICE, Build.MANUFACTURER, Build.MODEL,
					Build.PRODUCT);

			setEinkController();

			// First directory to get to
			if (data.getExtras() != null
					&& data.getExtras().getString("start_dir") != null) {
				drawDirectory(data.getExtras().getString("start_dir"), -1);
			} else {
				if (prefs.getBoolean("saveDir", true))
					drawDirectory(prefs.getString("lastdir", "/sdcard"), -1);
				else {
					String[] startDirs = prefs.getString("startDir",
							"/sdcard,/media/My Files").split("\\,");
					drawDirectory(startDirs[0], -1);
				}
			}
		}

		app.booted = true;

		if (!mountReceiverRegistered) {
			IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
			filter.addDataScheme("file");
			registerReceiver(this.SDCardChangeReceiver,
					new IntentFilter(filter));
			mountReceiverRegistered = true;
		}

		if (!powerReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_POWER_CONNECTED);
			filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(this.PowerChangeReceiver, new IntentFilter(filter));
			powerReceiverRegistered = true;
		}

		if (!wifiReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(this.WiFiChangeReceiver, new IntentFilter(filter));
			wifiReceiverRegistered = true;
		}

		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Reread preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String typesString = prefs.getString("types", defReaders);
		// Recreate readers list
		app.setReaders(parseReadersString(typesString));
		app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
		drawDirectory(currentRoot, currentPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			menuSearch();
			return true;
		case R.id.mime_types:
			menuTypes();
			return true;
		case R.id.about:
			menuAbout();
			return true;
		case R.id.setting:
			menuSettings();
			return true;
		case R.id.lastopened:
			menuLastopened();
			return true;
		case R.id.favorites:
			menuFavorites();
			return true;
		default:
			return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case TYPES_ACT:
			String newTypes = createReadersString(app.getReaders());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();
			drawDirectory(currentRoot, currentPosition);
			break;
		default:
			return;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		HashMap<String, String> i = itemsArray.get(info.position);
		String fn = i.get("name");
		String dr = i.get("dname");
		String tp = i.get("type");
		String fullName = dr + "/" + fn;

		if (tp.equals("file")) {
			if (!app.contains("favorites", dr, fn))
				// "Add to favorites"
				menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, getResources()
						.getString(R.string.jv_relaunch_add));
			if (app.history.containsKey(fullName)) {
				if (app.history.get(fullName) == app.READING)
					// "Mark as read"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
							getResources().getString(R.string.jv_relaunch_mark));
				else if (app.history.get(fullName) == app.FINISHED)
					// "Remove \"read\" mark"
					menu.add(
							Menu.NONE,
							CNTXT_MENU_MARK_READING,
							Menu.NONE,
							getResources().getString(
									R.string.jv_relaunch_unmark));
				// "Forget all marks"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE,
						getResources()
								.getString(R.string.jv_relaunch_unmarkall));
			} else
				// "Mark as read"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
						getResources().getString(R.string.jv_relaunch_mark));
			if (prefs.getBoolean("openWith", true))
				// "Open with ..."
				menu.add(Menu.NONE, CNTXT_MENU_OPENWITH, Menu.NONE,
						getResources().getString(R.string.jv_relaunch_openwith));
			if (prefs.getBoolean("createIntent", true))
				// "Create Intent ..."
				menu.add(
						Menu.NONE,
						CNTXT_MENU_INTENT,
						Menu.NONE,
						getResources().getString(
								R.string.jv_relaunch_createintent));
			// "Delete"
			if (prefs.getBoolean("useFileManagerFunctions", true)) {
				menu.add(Menu.NONE, CNTXT_MENU_COPY, Menu.NONE,
						getResources().getString(R.string.jv_relaunch_copy));
				menu.add(Menu.NONE, CNTXT_MENU_MOVE, Menu.NONE,
						getResources().getString(R.string.jv_relaunch_move));
				if (fileOp != 0)
					menu.add(Menu.NONE, CNTXT_MENU_PASTE, Menu.NONE,
							getResources().getString(R.string.jv_relaunch_paste));
				menu.add(Menu.NONE, CNTXT_MENU_DELETE_F, Menu.NONE,
						getResources().getString(R.string.jv_relaunch_delete));
			}
		} else {
			File d = new File(fullName);
			String[] allEntries = d.list();
			if (!app.contains("favorites", fullName, app.DIR_TAG))
				// "Add to favorites"
				menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, getResources()
						.getString(R.string.jv_relaunch_add));
			if (allEntries != null && allEntries.length > 0) {
				// "Delete NON-EMPTY directory!"
				if (prefs.getBoolean("useFileManagerFunctions", true))
					menu.add(
							Menu.NONE,
							CNTXT_MENU_DELETE_D_NON_EMPTY,
							Menu.NONE,
							getResources().getString(
									R.string.jv_relaunch_delete_non_emp_dir));
			} else {
				// "Delete empty directory"
				if (prefs.getBoolean("useFileManagerFunctions", true))
					menu.add(
							Menu.NONE,
							CNTXT_MENU_DELETE_D_EMPTY,
							Menu.NONE,
							getResources().getString(
									R.string.jv_relaunch_delete_emp_dir));
			}
			if (prefs.getBoolean("useFileManagerFunctions", true) && (fileOp != 0)) {
					menu.add(Menu.NONE, CNTXT_MENU_PASTE, Menu.NONE,
							getResources().getString(R.string.jv_relaunch_paste));
			}

		}
		// "Cancel"
		menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
				.getString(R.string.jv_relaunch_cancel));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CNTXT_MENU_CANCEL)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String fname = i.get("name");
		final String dname = i.get("dname");
		final String fullName = dname + "/" + fname;
		final String type = i.get("type");

		switch (item.getItemId()) {
		case CNTXT_MENU_ADD:
			if (type.equals("file"))
				app.addToList("favorites", dname, fname, false);
			else
				app.addToList("favorites", fullName, app.DIR_TAG, false);
			break;
		case CNTXT_MENU_MARK_READING:
			app.history.put(fullName, app.READING);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_MARK_FINISHED:
			app.history.put(fullName, app.FINISHED);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_MARK_FORGET:
			app.history.remove(fullName);
			app.saveList("history");
			redrawList();
			break;
		case CNTXT_MENU_OPENWITH: {
			final CharSequence[] applications = app.getApps().toArray(
					new CharSequence[app.getApps().size()]);
			CharSequence[] happlications = app.getApps().toArray(
					new CharSequence[app.getApps().size()]);
			for (int j = 0; j < happlications.length; j++) {
				String happ = (String) happlications[j];
				String[] happp = happ.split("\\%");
				happlications[j] = happp[2];
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select application"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunch_select_application));
			builder.setSingleChoiceItems(happlications, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							start(app.launchReader((String) applications[i],
									fullName));
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunch_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			builder.show();
			break;
		}
		case CNTXT_MENU_INTENT: {
			String re[] = fname.split("\\.");
			List<String> ilist = new ArrayList<String>();
			for (int j = 1; j < re.length; j++) {
				String act = "application/";
				String typ = re[j];
				if (typ.equals("jpg"))
					typ = "jpeg";
				if (typ.equals("jpeg") || typ.equals("png"))
					act = "image/";
				ilist.add(act + typ);
				if (re.length > 2) {
					for (int k = j + 1; k < re.length; k++) {
						String x = "";
						for (int l = k; l < re.length; l++)
							x += "+" + re[l];
						ilist.add(act + typ + x);
					}
				}
			}

			final CharSequence[] intents = ilist.toArray(new CharSequence[ilist
					.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
			// "Select intent type"
			builder.setTitle(getResources().getString(
					R.string.jv_relaunch_select_intent_type));
			builder.setSingleChoiceItems(intents, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							Intent in = new Intent();
							in.setAction(Intent.ACTION_VIEW);
							in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TOP);
							in.setDataAndType(Uri.parse("file://" + fullName),
									(String) intents[i]);
							dialog.dismiss();
							try {
								startActivity(in);
							} catch (ActivityNotFoundException e) {
								AlertDialog.Builder builder1 = new AlertDialog.Builder(
										ReLaunch.this);
								// "Activity not found"
								builder1.setTitle(getResources()
										.getString(
												R.string.jv_relaunch_activity_not_found_title));
								// "Activity for file \"" + fullName +
								// "\" with type \"" + intents[i] +
								// "\" not found"
								builder1.setMessage(getResources()
										.getString(
												R.string.jv_relaunch_activity_not_found_text1)
										+ " \""
										+ fullName
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunch_activity_not_found_text2)
										+ " \""
										+ intents[i]
										+ "\" "
										+ getResources()
												.getString(
														R.string.jv_relaunch_activity_not_found_text3));
								builder1.setPositiveButton(getResources()
										.getString(R.string.jv_relaunch_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
											}
										});
								builder1.show();
							}
						}
					});
			// "Other"
			builder.setPositiveButton(
					getResources().getString(R.string.jv_relaunch_other),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							AlertDialog.Builder builder1 = new AlertDialog.Builder(
									ReLaunch.this);
							// "Intent type"
							builder1.setTitle(getResources().getString(
									R.string.jv_relaunch_intent_type));
							final EditText input = new EditText(ReLaunch.this);
							input.setText("application/");
							builder1.setView(input);
							// "Ok"
							builder1.setPositiveButton(getResources()
									.getString(R.string.jv_relaunch_ok),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											Intent in = new Intent();
											in.setAction(Intent.ACTION_VIEW);
											in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_CLEAR_TOP);
											in.setDataAndType(
													Uri.parse("file://"
															+ fullName), input
															.getText()
															.toString());
											dialog.dismiss();
											try {
												startActivity(in);
											} catch (ActivityNotFoundException e) {
												AlertDialog.Builder builder2 = new AlertDialog.Builder(
														ReLaunch.this);
												// "Activity not found"
												builder2.setTitle(getResources()
														.getString(
																R.string.jv_relaunch_activity_not_found_title));
												// "Activity for file \"" +
												// fullName + "\" with type \""
												// + input.getText() +
												// "\" not found"
												builder2.setMessage(getResources()
														.getString(
																R.string.jv_relaunch_activity_not_found_text1)
														+ " \""
														+ fullName
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunch_activity_not_found_text2)
														+ " \""
														+ input.getText()
														+ "\" "
														+ getResources()
																.getString(
																		R.string.jv_relaunch_activity_not_found_text3));
												// "OK"
												builder2.setPositiveButton(
														getResources()
																.getString(
																		R.string.jv_relaunch_ok),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {
															}
														});
												builder2.show();
											}
										}
									});
							builder1.show();
						}
					});
			// "Cancel"
			builder.setNegativeButton(
					getResources().getString(R.string.jv_relaunch_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});

			builder.show();
			break;
		}
		case CNTXT_MENU_DELETE_F:
			if (prefs.getBoolean("confirmFileDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete file warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_del_file_title));
				// "Are you sure to delete file \"" + fname + "\" ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunch_del_file_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunch_del_file_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunch_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeFile(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunch_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeFile(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;
		case CNTXT_MENU_DELETE_D_EMPTY:
			if (prefs.getBoolean("confirmDirDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete empty directory warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_del_em_dir_title));
				// "Are you sure to delete empty directory \"" + fname + "\" ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunch_del_em_dir_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunch_del_em_dir_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunch_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeFile(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunch_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeFile(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;
		case CNTXT_MENU_DELETE_D_NON_EMPTY:
			if (prefs.getBoolean("confirmNonEmptyDirDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete non empty directory warning"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_del_ne_dir_title));
				// "Are you sure to delete non-empty directory \"" + fname +
				// "\" (dangerous) ?"
				builder.setMessage(getResources().getString(
						R.string.jv_relaunch_del_ne_dir_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_relaunch_del_ne_dir_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_relaunch_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								if (app.removeDirectory(dname, fname)) {
									itemsArray.remove(pos);
									redrawList();
								}
							}
						});
				// "No"
				builder.setNegativeButton(
						getResources().getString(R.string.jv_relaunch_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			} else if (app.removeDirectory(dname, fname)) {
				itemsArray.remove(pos);
				redrawList();
			}
			break;
			
		case CNTXT_MENU_COPY:
			fileOpFile = fname;
			fileOpDir = dname;
			fileOp = CNTXT_MENU_COPY;
			break;

		case CNTXT_MENU_MOVE:
			fileOpFile = fname;
			fileOpDir = dname;
			fileOp = CNTXT_MENU_MOVE;
			break;

		case CNTXT_MENU_PASTE:
			String src = fileOpDir + "/" + fileOpFile;
			String dst = dname + "/" + fileOpFile;
			boolean retCode = false;
			if (fileOp == CNTXT_MENU_COPY)
				retCode = app.copyFile(src, dst);
			else if (fileOp == CNTXT_MENU_MOVE)
				retCode = app.moveFile(src, dst);
			if (retCode) {
				fileOp = 0;
				HashMap<String, String> fitem = new HashMap<String, String>();
				fitem.put("name", fileOpFile);
				fitem.put("dname", dname);
				fitem.put("fname", dname + "/" + fileOpFile);
				fitem.put("type", "file");
				fitem.put("reader", app.readerName(fileOpFile));
				itemsArray.add(fitem);
				redrawList();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_paste_fail_title));
				builder.setMessage(getResources().getString(
						R.string.jv_relaunch_paste_fail_text)
						+ " " + fileOpFile);
				builder.setNeutralButton(
						getResources().getString(R.string.jv_relaunch_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						});
				builder.show();
			}
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		setEinkController();
		super.onResume();
		app.generalOnResume(TAG, this);
		refreshBottomInfo();
		redrawList();
	}

	@Override
	protected void onStop() {

		int lruMax = 30;
		int favMax = 30;
		int appLruMax = 30;
		int appFavMax = 30;
		try {
			lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
			favMax = Integer.parseInt(prefs.getString("favSize", "30"));
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
			appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("lastOpened", LRU_FILE, lruMax);
		app.writeFile("favorites", FAV_FILE, favMax);
		app.writeFile("app_last", APP_LRU_FILE, appLruMax, ":");
		app.writeFile("app_favorites", APP_FAV_FILE, appFavMax, ":");
		List<String[]> h = new ArrayList<String[]>();
		for (String k : app.history.keySet()) {
			if (app.history.get(k) == app.READING)
				h.add(new String[] { k, "READING" });
			else if (app.history.get(k) == app.FINISHED)
				h.add(new String[] { k, "FINISHED" });
		}
		app.setList("history", h);
		app.writeFile("history", HIST_FILE, 0, ":");
		List<String[]> c = new ArrayList<String[]>();
		for (String k : app.columns.keySet()) {
			c.add(new String[] { k, Integer.toString(app.columns.get(k)) });
		}
		app.setList("columns", c);
		app.writeFile("columns", ReLaunch.COLS_FILE, 0, ":");
		// unregisterReceiver(this.SDReceiver);
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!useHome)
			return super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_HOME)
			return true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Ask the user if they want to quit
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					// "This is a launcher!"
					.setTitle(
							getResources().getString(
									R.string.jv_relaunch_launcher))
					// "Are you sure you want to quit ?"
					.setMessage(
							getResources().getString(
									R.string.jv_relaunch_launcher_text))
					// "YES"
					.setPositiveButton(
							getResources().getString(R.string.jv_relaunch_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							})
					// "NO"
					.setNegativeButton(
							getResources().getString(R.string.jv_relaunch_no),
							null).show();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void menuSearch() {
		Intent intent = new Intent(ReLaunch.this, SearchActivity.class);
		startActivity(intent);
	}

	private void menuTypes() {
		Intent intent = new Intent(ReLaunch.this, TypesActivity.class);
		startActivityForResult(intent, TYPES_ACT);
	}

	private void menuSettings() {
		Intent intent = new Intent(ReLaunch.this, PrefsActivity.class);
		startActivity(intent);
	}

	private void menuLastopened() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "lastOpened");
		// "Last opened"
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunch_lru));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void menuFavorites() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "favorites");
		// "Favorites"
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunch_fav));
		intent.putExtra("rereadOnStart", true);
		setEinkController(); // ??? not needed
		startActivity(intent);
	}

	private void openHome(Integer order_num) {
		String[] startDirs = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		if (order_num > 0 && order_num <= startDirs.length) {
			drawDirectory(startDirs[order_num - 1], -1);
		}
	}

	private void menuHome() {
		final String[] homesList = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		final CharSequence[] hhomes = new CharSequence[homesList.length];
		for (int j = 0; j < homesList.length; j++) {
			int ind = homesList[j].lastIndexOf('/');
			if (ind == -1) {
				hhomes[j] = "";
			} else {
				hhomes[j] = homesList[j].substring(ind + 1);
				if (hhomes[j].equals("")) {
					hhomes[j] = "/";
				}
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
		// "Select home directory"
		builder.setTitle(R.string.jv_relaunch_home);
		builder.setSingleChoiceItems(hhomes, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						drawDirectory(homesList[i], -1);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(
				getResources().getString(R.string.jv_relaunch_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void screenHome() {
		// make home list
		List<String[]> homeList = new ArrayList<String[]>();
		String[] startDirs = prefs.getString("startDir",
				"/sdcard,/media/My Files").split("\\,");
		for (Integer i = 0; i < startDirs.length; i++) {
			String[] homeEl = new String[2];
			homeEl[0] = startDirs[i];
			homeEl[1] = app.DIR_TAG;
			homeList.add(homeEl);
		}
		app.setList("homeList", homeList);
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "homeList");
		intent.putExtra("title",
				getResources().getString(R.string.jv_relaunch_home));
		intent.putExtra("rereadOnStart", true);
		startActivity(intent);
	}

	private void menuAbout() {
		app.About(this);
	}

	private Integer getDirectoryColumns(String dir) {
		Integer columns = 0;
		if (app.columns.containsKey(dir)) {
			columns = app.columns.get(dir);
		}
		return columns;
	}

	private String getEbookName(String dir, String file) {
		EBook eBook;
		String fileName = dir + "/" + file;
		eBook = dataBase.getBookByFileName(dir + "/" + file);
		if (!eBook.isOk) {
			Parser parser = new InstantParser();
			eBook = parser.parse(fileName);
			if (eBook.isOk)
				dataBase.addBook(eBook);
		}
		if (eBook.isOk) {
			String output = prefs.getString("bookTitleFormat", "%a. %t");
			if (eBook.authors.size() > 0) {
				String author = "";
				if (eBook.authors.get(0).firstName != null)
					author += eBook.authors.get(0).firstName;
				if (eBook.authors.get(0).lastName != null)
					author += " " + eBook.authors.get(0).lastName;
					output = output.replace("%a", author);
			}
			if (eBook.title != null)
				output = output.replace("%t", eBook.title);
			if (eBook.sequenceName != null)
				output = output.replace("%s", eBook.sequenceName);
			else
				output = output.replace("%s", "");
			if (eBook.sequenceNumber != null)
				output = output.replace("%n", eBook.sequenceNumber);
			else
				output = output.replace("%n", "");
			return output;
		} else
			return file;
	}

	private void hideLayout(int id) {
		View v = (View) findViewById(id);
		LayoutParams p = (LayoutParams) v.getLayoutParams();
		p.height = 0;
		p.width = 0;
		v.setLayoutParams(p);
	}

}
