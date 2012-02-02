package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ResultsActivity extends Activity {
	final String TAG = "Results";
	final int CNTXT_MENU_RMFAV = 1;
	final int CNTXT_MENU_RMFILE = 2;
	final int CNTXT_MENU_CANCEL = 3;
	final int CNTXT_MENU_MOVEUP = 4;
	final int CNTXT_MENU_MOVEDOWN = 5;
	final int CNTXT_MENU_MARK_FINISHED = 6;
	final int CNTXT_MENU_MARK_READING = 7;
	final int CNTXT_MENU_MARK_FORGET = 8;
	final int CNTXT_MENU_RMDIR = 9;
	ReLaunchApp app;
	HashMap<String, Drawable> icons;
	String listName;
	String title;
	Boolean rereadOnStart = true;
	SharedPreferences prefs;
	FLSimpleAdapter adapter;
	ListView lv;
	Integer currentColsNum = -1;
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
	Integer currentPosition = -1;
	boolean addSView = true;
	boolean oldHome;

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

	static class ViewHolder {
		TextView tv1;
		TextView tv2;
		LinearLayout tvHolder;
		ImageView iv;
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

	class FLSimpleAdapter extends ArrayAdapter<HashMap<String, String>> {
		FLSimpleAdapter(Context context, int resource,
				List<HashMap<String, String>> data) {
			super(context, resource, data);
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
				v = vi.inflate(R.layout.results_item, null);
				holder = new ViewHolder();
				holder.tv1 = (TextView) v.findViewById(R.id.res_dname);
				holder.tv2 = (TextView) v.findViewById(R.id.res_fname);
				holder.tvHolder = (LinearLayout) v
						.findViewById(R.id.res_holder);
				holder.iv = (ImageView) v.findViewById(R.id.res_icon);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			// exts dirs sorter
			final class ExtsComparator implements java.util.Comparator<String> {
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
				Collections.sort(exts, new ExtsComparator());
			}
			// known dirs
			ArrayList<String> dirs = new ArrayList<String>();
			if (prefs.getBoolean("hideKnownDirs", false)) {
				String[] home_dirs = prefs.getString("startDir",
						"/sdcard,/media/My Files").split("\\,");
				for (int i = 0; i < home_dirs.length; i++)
					dirs.add(home_dirs[i]);
				Collections.sort(dirs, new ExtsComparator());
			}

			TextView tv1 = holder.tv1;
			TextView tv2 = holder.tv2;

			tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, Integer.parseInt(prefs
					.getString("firstLineFontSizePx", "20")));
			tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, Integer.parseInt(prefs
					.getString("secondLineFontSizePx", "16")));

			LinearLayout tvHolder = holder.tvHolder;
			ImageView iv = holder.iv;

			if (position >= itemsArray.size()) {
				v.setVisibility(View.INVISIBLE);
				tv1.setVisibility(View.INVISIBLE);
				tv2.setVisibility(View.INVISIBLE);
				iv.setVisibility(View.INVISIBLE);
				return v;
			}
			HashMap<String, String> item = itemsArray.get(position);
			if (item != null) {
				String fname = item.get("fname");
				String sname = item.get("fname");
				String dname = item.get("dname");
				String sdname = item.get("dname");
				String fullName = dname + "/" + fname;
				boolean setBold = false;
				boolean useFaces = prefs.getBoolean("showNew", true);

				// clean extension, if needed
				if (prefs.getBoolean("hideKnownExts", false)) {
					for (int i = 0; i < exts.size(); i++) {
						if (sname.endsWith(exts.get(i))) {
							sname = sname.substring(0, sname.length()
									- exts.get(i).length());
						}
					}
				}
				// clean start prefixes, if need
				if (prefs.getBoolean("hideKnownDirs", false)) {
					for (int i = 0; i < dirs.size(); i++) {
						if (sdname.startsWith(dirs.get(i))) {
							sdname = "~"
									+ sdname.substring(dirs.get(i).length());
						}
					}
				}

				if (useFaces) {
					if (app.history.containsKey(fullName)) {
						if (app.history.get(fullName) == app.READING) {
							tvHolder.setBackgroundColor(getResources()
									.getColor(R.color.file_reading_bg));
							tv1.setTextColor(getResources().getColor(
									R.color.file_reading_fg));
							tv2.setTextColor(getResources().getColor(
									R.color.file_reading_fg));
						} else if (app.history.get(fullName) == app.FINISHED) {
							tvHolder.setBackgroundColor(getResources()
									.getColor(R.color.file_finished_bg));
							tv1.setTextColor(getResources().getColor(
									R.color.file_finished_fg));
							tv2.setTextColor(getResources().getColor(
									R.color.file_finished_fg));
						} else {
							tvHolder.setBackgroundColor(getResources()
									.getColor(R.color.file_unknown_bg));
							tv1.setTextColor(getResources().getColor(
									R.color.file_unknown_fg));
							tv2.setTextColor(getResources().getColor(
									R.color.file_unknown_fg));
						}
					} else {
						tvHolder.setBackgroundColor(getResources().getColor(
								R.color.file_new_bg));
						tv1.setTextColor(getResources().getColor(
								R.color.file_new_fg));
						tv2.setTextColor(getResources().getColor(
								R.color.file_new_fg));
						if (getResources().getBoolean(R.bool.show_new_as_bold))
							setBold = true;
					}
				}

				// setup icon
				if (prefs.getString("firstLineIconSizePx", "48").equals("0")) {
					iv.setVisibility(View.GONE);
				} else {
					Drawable d = app.specialIcon(fullName, item.get("type")
							.equals("dir"));
					if (d != null)
						iv.setImageBitmap(scaleDrawable(d, Integer
								.parseInt(prefs.getString(
										"firstLineIconSizePx", "48"))));
					else {
						String rdrName = app.readerName(fname);
						if (rdrName.equals("Nope")) {
							File f = new File(fullName);
							if (f.length() > app.viewerMax)
								iv.setImageBitmap(scaleDrawableById(
										R.drawable.file_notok, Integer
												.parseInt(prefs.getString(
														"firstLineIconSizePx",
														"48"))));
							else
								iv.setImageBitmap(scaleDrawableById(
										R.drawable.file_ok, Integer
												.parseInt(prefs.getString(
														"firstLineIconSizePx",
														"48"))));
						} else if (rdrName.startsWith("Intent:"))
							iv.setImageBitmap(scaleDrawableById(
									R.drawable.icon, Integer.parseInt(prefs
											.getString("firstLineIconSizePx",
													"48"))));
						else {
							if (icons.containsKey(rdrName))
								iv.setImageBitmap(scaleDrawable(icons
										.get(rdrName),
										Integer.parseInt(prefs.getString(
												"firstLineIconSizePx", "48"))));
							else
								iv.setImageBitmap(scaleDrawableById(
										R.drawable.file_ok, Integer
												.parseInt(prefs.getString(
														"firstLineIconSizePx",
														"48"))));
						}
					}
				}

				// special cases in dname & fname
				// dname empty - in root dir
				// fname empty with dname empty - root dir as is
				if (dname.equals("")) {
					dname = "/";
					sdname = "/";
					if (fname.equals("")) {
						fname = "/";
						sname = "/";
						dname = "";
						sdname = "";
					}
				}
				if (useFaces) {
					SpannableString s = new SpannableString(sname);
					s.setSpan(new StyleSpan(setBold ? Typeface.BOLD
							: Typeface.NORMAL), 0, sname.length(), 0);
					tv1.setText(sdname);
					tv2.setText(s);
				} else {
					tvHolder.setBackgroundColor(getResources().getColor(
							R.color.normal_bg));
					tv1.setTextColor(getResources().getColor(R.color.normal_fg));
					tv2.setTextColor(getResources().getColor(R.color.normal_fg));
					tv1.setText(sdname);
					tv2.setText(sname);
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
				tmp.add(itemsArray.get(i).get("fname").length());
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
				if (app.filterFile(item.get("dname"), item.get("fname"))
						|| item.get("type").equals("dir"))
					newItemsArray.add(item);
			}
			itemsArray = newItemsArray;
		}
		Integer colsNum = -1;
		if (listName.equals("homeList")) {
			colsNum = Integer
					.parseInt(prefs.getString("columnsHomeList", "-1"));
		}
		if (listName.equals("favorites")) {
			colsNum = Integer.parseInt(prefs.getString("columnsFAV", "-1"));
		}
		if (listName.equals("lastOpened")) {
			colsNum = Integer.parseInt(prefs.getString("columnsLRU", "-1"));
		}
		if (listName.equals("searchResults")) {
			colsNum = Integer.parseInt(prefs.getString("columnsSearch", "-1"));
		}
		// override auto (not working fine in adnroid)
		if (colsNum == -1) {
			colsNum = getAutoColsNum();
		}
		currentColsNum = colsNum;
		final GridView gv = (GridView) findViewById(R.id.results_list);
		gv.setNumColumns(colsNum);
		adapter.notifyDataSetChanged();
		if (currentPosition != -1)
			gv.setSelection(currentPosition);
	}

	private void start(Intent i) {
		if (i != null)
			try {
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(
						ResultsActivity.this,
						getResources().getString(
								R.string.jv_results_activity_not_found),
						Toast.LENGTH_LONG).show();
			}
	}

	private void createItemsArray() {
		itemsArray = new ArrayList<HashMap<String, String>>();
		for (String[] n : app.getList(listName)) {
			if (!prefs.getBoolean("filterResults", false)
					|| (prefs.getBoolean("filterResults", false) && app
							.filterFile(n[0], n[1]))
					|| (n[1].equals(app.DIR_TAG))) {
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("dname", n[0]);
				item.put("fname", n[1]);
				if (n[1].equals(app.DIR_TAG)) {
					int ind = n[0].lastIndexOf('/');
					if (ind == -1) {
						item.put("fname", "");
					} else {
						item.put("fname", n[0].substring(ind + 1));
						item.put("dname", n[0].substring(0, ind));
					}
					item.put("type", "dir");
				} else
					item.put("type", "file");
				itemsArray.add(item);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		setEinkController();

		app = ((ReLaunchApp) getApplicationContext());
		app.setFullScreenIfNecessary(this);
		setContentView(R.layout.results_layout);

		icons = app.getIcons();

		// Recreate readers list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		listName = data.getExtras().getString("list");
		title = data.getExtras().getString("title");
		rereadOnStart = data.getExtras().getBoolean("rereadOnStart");
		int total = data.getExtras().getInt("total", -1);

		((ImageButton) findViewById(R.id.results_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		// set results icon
		ImageView results_icon = (ImageView) findViewById(R.id.results_icon);
		if (listName.equals("homeList")) {
			results_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_home));
		}
		if (listName.equals("favorites")) {
			results_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_fav));
		}
		if (listName.equals("lastOpened")) {
			results_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_lre));
		}
		if (listName.equals("searchResults")) {
			results_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_search));
		}
		// may be "dead end" of code now(?) now UP functionality in this
		// screens?
		// so force to DISABLED
		final Button up = (Button) findViewById(R.id.goup_btn);
		up.setEnabled(false);
		// end of (possible) "dead end" code
		final ImageButton adv = (ImageButton) findViewById(R.id.advanced_btn);
		if (adv != null) {
			class advSimpleOnGestureListener extends SimpleOnGestureListener {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					Intent i = new Intent(ResultsActivity.this, Advanced.class);
					startActivity(i);
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					if (adv.hasWindowFocus()) {

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

		currentPosition = -1;
		final GridView gv = (GridView) findViewById(R.id.results_list);
		gv.setHorizontalSpacing(0);
		Button rt = (Button) findViewById(R.id.results_title);
		if (total == -1)
			rt.setText(title + " (" + app.getList(listName).size() + ")");
		else
			rt.setText(title + " (" + app.getList(listName).size() + "/"
					+ total + ")");
		rt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String[] columns = getResources().getStringArray(
						R.array.output_columns_names);
				final CharSequence[] columnsmode = new CharSequence[columns.length];
				for (int i = 0; i < columns.length; i++) {
					columnsmode[i] = columns[i];
				}
				boolean show_select = false;
				Integer checked = -1;
				if (listName.equals("homeList")) {
					checked = Integer.parseInt(prefs.getString(
							"columnsHomeList", "-1"));
					if (checked == -1)
						checked = 0;
					show_select = true;
				}
				if (listName.equals("favorites")) {
					checked = Integer.parseInt(prefs.getString("columnsFAV",
							"-1"));
					if (checked == -1)
						checked = 0;
					show_select = true;
				}
				if (listName.equals("lastOpened")) {
					checked = Integer.parseInt(prefs.getString("columnsLRU",
							"-1"));
					if (checked == -1)
						checked = 0;
					show_select = true;
				}
				if (listName.equals("searchResults")) {
					checked = Integer.parseInt(prefs.getString("columnsSearch",
							"-1"));
					if (checked == -1)
						checked = 0;
					show_select = true;
				}
				// get checked
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ResultsActivity.this);
				// "Select application"
				builder.setTitle(getResources().getString(
						R.string.jv_relaunch_select_columns));
				builder.setSingleChoiceItems(columnsmode, checked,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i) {
								if (i == 0) {
									if (listName.equals("homeList")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsHomeList",
												"-1");
										editor.commit();
									}
									if (listName.equals("favorites")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsFAV", "-1");
										editor.commit();
									}
									if (listName.equals("lastOpened")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsLRU", "-1");
										editor.commit();
									}
									if (listName.equals("searchResults")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsSearch", "-1");
										editor.commit();
									}
								} else {
									if (listName.equals("homeList")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsHomeList",
												Integer.toString(i));
										editor.commit();
									}
									if (listName.equals("favorites")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsFAV",
												Integer.toString(i));
										editor.commit();
									}
									if (listName.equals("lastOpened")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsLRU",
												Integer.toString(i));
										editor.commit();
									}
									if (listName.equals("searchResults")) {
										SharedPreferences.Editor editor = prefs
												.edit();
										editor.putString("columnsSearch",
												Integer.toString(i));
										editor.commit();
									}
								}
								redrawList();
								dialog.dismiss();
							}
						});
				if (show_select) {
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});

		createItemsArray();
		adapter = new FLSimpleAdapter(this, R.layout.results_item, itemsArray);
		gv.setAdapter(adapter);
		registerForContextMenu(gv);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth",
							"25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}

				LinearLayout ll = (LinearLayout) findViewById(R.id.results_fl);
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
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> item = itemsArray.get(position);

				String fullName = item.get("dname") + "/" + item.get("fname");
				currentPosition = parent.getFirstVisiblePosition();
				if (item.get("type").equals("dir")) {
					Intent intent = new Intent(ResultsActivity.this,
							ReLaunch.class);
					intent.putExtra("dirviewer", false);
					intent.putExtra("start_dir", fullName);
					intent.putExtra("home", ReLaunch.useHome);
					intent.putExtra("home1", ReLaunch.useHome1);
					intent.putExtra("shop", ReLaunch.useShop);
					intent.putExtra("library", ReLaunch.useLibrary);
					oldHome = ReLaunch.useHome;
					startActivityForResult(intent, ReLaunch.DIR_ACT);
				} else {
					String fileName = item.get("fname");
					if (!app.specialAction(ResultsActivity.this, fullName)) {
						if (app.readerName(fileName).equals("Nope"))
							app.defaultAction(ResultsActivity.this, fullName);
						else {
							// Launch reader
							if (app.askIfAmbiguous) {
								List<String> rdrs = app.readerNames(item
										.get("fname"));
								if (rdrs.size() < 1)
									return;
								else if (rdrs.size() == 1)
									start(app.launchReader(rdrs.get(0),
											fullName));
								else {
									final CharSequence[] applications = rdrs
											.toArray(new CharSequence[rdrs
													.size()]);
									CharSequence[] happlications = app
											.getApps().toArray(
													new CharSequence[app
															.getApps().size()]);
									for (int j = 0; j < happlications.length; j++) {
										String happ = (String) happlications[j];
										String[] happp = happ.split("\\%");
										happlications[j] = happp[2];
									}
									final String rdr1 = fullName;
									AlertDialog.Builder builder = new AlertDialog.Builder(
											ResultsActivity.this);
									// "Select application"
									builder.setTitle(getResources()
											.getString(
													R.string.jv_results_select_application));
									builder.setSingleChoiceItems(
											happlications,
											-1,
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
								start(app.launchReader(
										app.readerName(fileName), fullName));
						}
					}
					// close in needed
					if (prefs.getBoolean("returnFileToMain", false))
						finish();
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
				final GridView gv = (GridView) findViewById(R.id.results_list);
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
						if (target <= last)
							target = last + 1; // Special for NOOK, otherwise it
												// won't redraw the listview
						if (target > (total - 1))
							target = total - 1;
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
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onStart() {
		if (rereadOnStart)
			createItemsArray();
		redrawList();
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.resultsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mime_types:
			Intent intent1 = new Intent(ResultsActivity.this,
					TypesActivity.class);
			startActivityForResult(intent1, ReLaunch.TYPES_ACT);
			return true;
		case R.id.about:
			app.About(this);
			return true;
		case R.id.setting:
			Intent intent3 = new Intent(ResultsActivity.this,
					PrefsActivity.class);
			startActivity(intent3);
			return true;
		default:
			return true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ReLaunch.TYPES_ACT:
			if (resultCode != Activity.RESULT_OK)
				return;

			String newTypes = ReLaunch.createReadersString(app.getReaders());

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("types", newTypes);
			editor.commit();

			redrawList();
			break;
		case ReLaunch.DIR_ACT:
			ReLaunch.useHome = oldHome;
			break;
		default:
			return;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String dr = i.get("dname");
		final String fn = i.get("fname");
		String fullName = dr + "/" + fn;

		if (listName.equals("homeList")) {
			return;
		} else if (i.get("type").equals("dir")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,
						getResources().getString(R.string.jv_results_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,
						getResources().getString(R.string.jv_results_move_down));
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources()
					.getString(R.string.jv_results_remove));
			// "Delete directory"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(Menu.NONE, CNTXT_MENU_RMDIR, Menu.NONE, getResources()
						.getString(R.string.jv_results_delete_dir));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_results_cancel));
		} else if (listName.equals("favorites")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,
						getResources().getString(R.string.jv_results_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,
						getResources().getString(R.string.jv_results_move_down));
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources()
					.getString(R.string.jv_results_remove));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_results_cancel));
		} else if (listName.equals("lastOpened")) {
			if (app.history.containsKey(fullName)) {
				if (app.history.get(fullName) == app.READING)
					// "Mark as read"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
							getResources().getString(R.string.jv_results_mark));
				else if (app.history.get(fullName) == app.FINISHED)
					// "Remove \"read\" mark"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE,
							getResources()
									.getString(R.string.jv_results_unmark));
				// "Forget all marks"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE,
						getResources()
								.getString(R.string.jv_results_unmark_all));
			} else
				// "Mark as read"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
						getResources().getString(R.string.jv_results_mark));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_results_cancel));
		} else if (listName.equals("searchResults")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,
						getResources().getString(R.string.jv_results_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,
						getResources().getString(R.string.jv_results_move_down));
			if (app.history.containsKey(fullName)) {
				if (app.history.get(fullName) == app.READING)
					// "Mark as read"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
							getResources().getString(R.string.jv_results_mark));
				else if (app.history.get(fullName) == app.FINISHED)
					// "Remove \"read\" mark"
					menu.add(Menu.NONE, CNTXT_MENU_MARK_READING, Menu.NONE,
							getResources()
									.getString(R.string.jv_results_unmark));
				// "Forget all marks"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FORGET, Menu.NONE,
						getResources()
								.getString(R.string.jv_results_unmark_all));
			} else
				// "Mark as read"
				menu.add(Menu.NONE, CNTXT_MENU_MARK_FINISHED, Menu.NONE,
						getResources().getString(R.string.jv_results_mark));
			// "Delete file"
			if (prefs.getBoolean("useFileManagerFunctions", true))
				menu.add(
						Menu.NONE,
						CNTXT_MENU_RMFILE,
						Menu.NONE,
						getResources().getString(
								R.string.jv_results_delete_file));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_results_cancel));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CNTXT_MENU_CANCEL)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String dname = i.get("dname");
		final String fname = i.get("fname");
		String fullName = dname + "/" + fname;

		switch (item.getItemId()) {
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
		case CNTXT_MENU_RMFAV:
			if (i.get("type").equals("dir")) {
				app.removeFromList("favorites", fullName, app.DIR_TAG);
				app.saveList("favorites");
			} else {
				app.removeFromList("favorites", dname, fname);
				app.saveList("favorites");
			}
			itemsArray.remove(pos);
			redrawList();
			break;
		case CNTXT_MENU_MOVEUP:
			if (pos > 0) {
				List<String[]> f = app.getList(listName);
				HashMap<String, String> it = itemsArray.get(pos);
				String[] fit = f.get(pos);

				itemsArray.remove(pos);
				f.remove(pos);
				itemsArray.add(pos - 1, it);
				f.add(pos - 1, fit);
				app.setList(listName, f);
				redrawList();
			}
			break;
		case CNTXT_MENU_MOVEDOWN:
			if (pos < (itemsArray.size() - 1)) {
				List<String[]> f = app.getList(listName);
				HashMap<String, String> it = itemsArray.get(pos);
				String[] fit = f.get(pos);

				int size = itemsArray.size();
				itemsArray.remove(pos);
				f.remove(pos);
				if (pos + 1 >= size - 1) {
					itemsArray.add(it);
					f.add(fit);
				} else {
					itemsArray.add(pos + 1, it);
					f.add(pos + 1, fit);
				}
				app.setList(listName, f);
				redrawList();
			}
			break;
		case CNTXT_MENU_RMFILE:
			if (prefs.getBoolean("confirmFileDelete", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// "Delete file warning"
				builder.setTitle(getResources().getString(
						R.string.jv_results_delete_file_title));
				// "Are you sure to delete file \"" + fname + "\" ?");
				builder.setMessage(getResources().getString(
						R.string.jv_results_delete_file_text1)
						+ " \""
						+ fname
						+ "\" "
						+ getResources().getString(
								R.string.jv_results_delete_file_text2));
				// "Yes"
				builder.setPositiveButton(
						getResources().getString(R.string.jv_results_yes),
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
						getResources().getString(R.string.jv_results_no),
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
		case CNTXT_MENU_RMDIR:
			File d = new File(fullName);
			boolean isEmpty = d.list().length < 1;
			if (isEmpty) {
				if (prefs.getBoolean("confirmDirDelete", true)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					// "Delete empty directory warning"
					builder.setTitle(getResources().getString(
							R.string.jv_results_delete_em_dir_title));
					// "Are you sure to delete empty directory \"" + fname +
					// "\" ?");
					builder.setMessage(getResources().getString(
							R.string.jv_results_delete_em_dir_text1)
							+ " \""
							+ fname
							+ "\" "
							+ getResources().getString(
									R.string.jv_results_delete_em_dir_text2));
					// "Yes"
					builder.setPositiveButton(
							getResources().getString(R.string.jv_results_yes),
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
							getResources().getString(R.string.jv_results_no),
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
			} else {
				if (prefs.getBoolean("confirmNonEmptyDirDelete", true)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					// "Delete non empty directory warning"
					builder.setTitle(getResources().getString(
							R.string.jv_results_delete_ne_dir_title));
					// "Are you sure to delete non-empty directory \"" + fname +
					// "\" (dangerous) ?");
					builder.setMessage(getResources().getString(
							R.string.jv_results_delete_ne_dir_text1)
							+ " \""
							+ fname
							+ "\" "
							+ getResources().getString(
									R.string.jv_results_delete_ne_dir_text2));
					// "Yes"
					builder.setPositiveButton(
							getResources().getString(R.string.jv_results_yes),
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
							getResources().getString(R.string.jv_results_no),
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
	}
}
