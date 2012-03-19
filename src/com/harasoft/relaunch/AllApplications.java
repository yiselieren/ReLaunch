package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class AllApplications extends Activity {
	final String TAG = "AllApps";

	final int UNINSTALL_ACT = 1;

	final int CNTXT_MENU_RMFAV = 1;
	final int CNTXT_MENU_ADDFAV = 2;
	final int CNTXT_MENU_UNINSTALL = 3;
	final int CNTXT_MENU_CANCEL = 4;
	final int CNTXT_MENU_MOVEUP = 5;
	final int CNTXT_MENU_MOVEDOWN = 6;

	ReLaunchApp app;
	HashMap<String, Drawable> icons;
	Boolean rereadOnStart = false;
	List<String> itemsArray = new ArrayList<String>();
	AppAdapter adapter;
	GridView lv;
	String listName;
	String title;
	SharedPreferences prefs;
	boolean addSView = true;
	int gcols = 2;

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
		TextView tv;
		ImageView iv;
	}

	class AppAdapter extends ArrayAdapter<String> {
		AppAdapter(Context context, int resource, List<String> data) {
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
				v = vi.inflate(R.layout.applications_item, null);
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.app_name);
				holder.iv = (ImageView) v.findViewById(R.id.app_icon);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			TextView tv = holder.tv;
			ImageView iv = holder.iv;

			String item = itemsArray.get(position);

			if (item != null) {
				String[] itemp = item.split("\\%");
				tv.setText(itemp[2]);
				iv.setImageDrawable(app.getIcons().get(item));
			}
			return v;
		}
	}

	private void saveLast() {
		int appLruMax = 30;
		try {
			appLruMax = Integer.parseInt(prefs.getString("appLruSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("app_last", ReLaunch.APP_LRU_FILE, appLruMax, ":");
	}

	private void saveFav() {
		int appFavMax = 30;
		try {
			appFavMax = Integer.parseInt(prefs.getString("appFavSize", "30"));
		} catch (NumberFormatException e) {
		}
		app.writeFile("app_favorites", ReLaunch.APP_FAV_FILE, appFavMax, ":");
	}

	private void checkListByName(String lName, List<String> master) {
		List<String[]> rc = app.getList(lName);
		List<String[]> rc1 = new ArrayList<String[]>();

		for (String[] r : rc) {
			boolean inMaster = false;
			for (String m : master) {
				if (m.equals(r[0])) {
					inMaster = true;
					break;
				}
			}
			if (inMaster)
				rc1.add(r);
		}
		app.setList(lName, rc1);

	}

	private List<String> checkList(List<String> lst, List<String> master) {
		List<String> rc = new ArrayList<String>();
		for (String s : lst) {
			boolean inMaster = false;
			for (String m : master) {
				if (m.equals(s)) {
					inMaster = true;
					break;
				}
			}
			if (inMaster)
				rc.add(s);
		}
		return rc;
	}

	// REREAD application list, check that favorites and last lists don't
	// contain extra applications
	private void rereadAppList() {
		app.setApps(ReLaunch.createAppList(getPackageManager()));
		checkListByName("app_last", app.getApps());
		checkListByName("app_favorites", app.getApps());
		itemsArray = checkList(itemsArray, app.getApps());
		saveLast();
		saveFav();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		setEinkController();
		app = ((ReLaunchApp) getApplicationContext());
		app.setFullScreenIfNecessary(this);
		setContentView(R.layout.all_applications);
		icons = app.getIcons();

		// Create applications list
		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		listName = data.getExtras().getString("list");
		title = data.getExtras().getString("title");

		// set app icon
		ImageView app_icon = (ImageView) findViewById(R.id.app_icon);
		if (listName.equals("app_all")) {
			app_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_grid));
			String cols = prefs.getString("columnsAppAll", "-1");
			gcols = Integer.parseInt(cols);
		}
		if (listName.equals("app_last")) {
			app_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_lrea));
		}
		if (listName.equals("app_favorites")) {
			app_icon.setImageDrawable(getResources().getDrawable(
					R.drawable.ci_fava));
			String cols = prefs.getString("columnsAppFav", "-1");
			gcols = Integer.parseInt(cols);

		}
		((ImageButton) findViewById(R.id.app_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		if (listName.equals("app_all")) {
			app.setApps(ReLaunch.createAppList(getPackageManager()));
			checkListByName("app_last", app.getApps());
			checkListByName("app_favorites", app.getApps());
			saveLast();
			saveFav();
			itemsArray = app.getApps();
		} else {
			List<String[]> lit = app.getList(listName);
			itemsArray = new ArrayList<String>();
			for (String[] r : lit)
				itemsArray.add(r[0]);
		}
		((TextView) findViewById(R.id.app_title)).setText(title + " ("
				+ itemsArray.size() + ")");

		adapter = new AppAdapter(this, R.layout.applications_item, itemsArray);
		lv = (GridView) findViewById(R.id.app_grid);
		if (gcols <= 0)
			gcols = 2;
		lv.setNumColumns(gcols);
		lv.setAdapter(adapter);
		registerForContextMenu(lv);
		if (prefs.getBoolean("customScroll", app.customScrollDef)) {
			if (addSView) {
				int scrollW;
				try {
					scrollW = Integer.parseInt(prefs.getString("scrollWidth",
							"25"));
				} catch (NumberFormatException e) {
					scrollW = 25;
				}

				LinearLayout ll = (LinearLayout) findViewById(R.id.app_grid_layout);
				final SView sv = new SView(getBaseContext());
				LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
						scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
				sv.setLayoutParams(pars);
				ll.addView(sv);
				lv.setOnScrollListener(new AbsListView.OnScrollListener() {
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
			lv.setOnScrollListener(new AbsListView.OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					setEinkController();
				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
				}
			});
		}
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String item = itemsArray.get(position);
				Intent i = app.getIntentByLabel(item);
				if (i == null)
					// "Activity \"" + item + "\" not found!"
					Toast.makeText(
							AllApplications.this,
							getResources().getString(
									R.string.jv_allapp_activity)
									+ " \""
									+ item
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
						if (prefs.getBoolean("returnToMain", false))
							finish();
					} catch (ActivityNotFoundException e) {
						// "Activity \"" + item + "\" not found!"
						Toast.makeText(
								AllApplications.this,
								getResources().getString(
										R.string.jv_allapp_activity)
										+ " \""
										+ item
										+ "\" "
										+ getResources().getString(
												R.string.jv_allapp_not_found),
								Toast.LENGTH_LONG).show();
						ok = false;
					}
					if (ok) {
						app.addToList("app_last", item, "X", false);
						saveLast();
					}
				}
			}
		});
		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		setEinkController();
		if (listName.equals("app_all"))
			rereadAppList();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setEinkController();
		if (listName.equals("app_all"))
			rereadAppList();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setEinkController();
		if (listName.equals("app_all"))
			rereadAppList();
		app.generalOnResume(TAG, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int pos = info.position;
		String i = itemsArray.get(pos);

		if (listName.equals("app_favorites")) {
			if (pos > 0)
				// "Move one position up"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEUP, Menu.NONE,
						getResources().getString(R.string.jv_allapp_move_up));
			if (pos < (itemsArray.size() - 1))
				// "Move one position down"
				menu.add(Menu.NONE, CNTXT_MENU_MOVEDOWN, Menu.NONE,
						getResources().getString(R.string.jv_allapp_move_down));
			// "Remove from favorites"
			menu.add(Menu.NONE, CNTXT_MENU_RMFAV, Menu.NONE, getResources()
					.getString(R.string.jv_allapp_remove));
			// "Uninstall"
			menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, getResources()
					.getString(R.string.jv_allapp_uninstall));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_allapp_cancel));
		} else {
			List<String[]> lit = app.getList("app_favorites");
			boolean in_fav = false;
			for (String[] r : lit) {
				if (r[0].equals(i)) {
					in_fav = true;
					break;
				}
			}
			if (!in_fav)
				// "Add to favorites"
				menu.add(Menu.NONE, CNTXT_MENU_ADDFAV, Menu.NONE,
						getResources().getString(R.string.jv_allapp_add));
			// "Uninstall"
			menu.add(Menu.NONE, CNTXT_MENU_UNINSTALL, Menu.NONE, getResources()
					.getString(R.string.jv_allapp_uninstall));
			// "Cancel"
			menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, getResources()
					.getString(R.string.jv_allapp_cancel));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CNTXT_MENU_CANCEL)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final int pos = info.position;
		String it = itemsArray.get(pos);

		switch (item.getItemId()) {
		case CNTXT_MENU_MOVEUP:
			if (pos > 0) {
				List<String[]> f = app.getList(listName);
				String[] fit = f.get(pos);

				itemsArray.remove(pos);
				f.remove(pos);
				itemsArray.add(pos - 1, it);
				f.add(pos - 1, fit);
				app.setList(listName, f);
				saveFav();
				adapter.notifyDataSetChanged();
			}
			break;
		case CNTXT_MENU_MOVEDOWN:
			if (pos < (itemsArray.size() - 1)) {
				List<String[]> f = app.getList(listName);
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
				saveFav();
				adapter.notifyDataSetChanged();
			}
			break;
		case CNTXT_MENU_RMFAV:
			app.getList(listName).remove(pos);
			itemsArray.remove(pos);
			saveFav();
			adapter.notifyDataSetChanged();
			break;
		case CNTXT_MENU_ADDFAV:
			app.addToList("app_favorites", it, "X", true);
			saveFav();
			break;
		case CNTXT_MENU_UNINSTALL:
			PackageManager pm = getPackageManager();
			PackageInfo pi = null;
			String[] itp = it.split("\\%");
			try {
				pi = pm.getPackageInfo(itp[0], 0);
			} catch (Exception e) {
			}
			if (pi == null)
				// "PackageInfo not found for label \"" + it + "\""
				Toast.makeText(
						AllApplications.this,
						getResources().getString(
								R.string.jv_allapp_package_info_not_found)
								+ " \"" + itp[2] + "\"", Toast.LENGTH_LONG)
						.show();
			else {
				// "Package name is \"" + pi.packageName + "\" for label \"" +
				// it + "\""
				Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
						"package", pi.packageName, null));
				try {
					startActivityForResult(intent, UNINSTALL_ACT);
				} catch (ActivityNotFoundException e) {
					// "Activity \"" + pi.packageName + "\" not found"
					Toast.makeText(
							AllApplications.this,
							getResources().getString(
									R.string.jv_allapp_activity)
									+ " \""
									+ pi.packageName
									+ "\" "
									+ getResources().getString(
											R.string.jv_allapp_not_found),
							Toast.LENGTH_LONG).show();
					return true;
				}
			}
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case UNINSTALL_ACT:
			rereadAppList();
			break;
		default:
			return;
		}
	}
}
