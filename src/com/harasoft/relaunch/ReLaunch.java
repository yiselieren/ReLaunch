package com.harasoft.relaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ReLaunch extends Activity {
	
	final String                  TAG = "ReLaunch";
	final String                  LRU_FILE = "LruFile.txt";
	final String                  FAV_FILE = "Favorites.txt";
	final String                  RDR_FILE = "Readers.txt";
	final String                  defReaders = ".fb2,.fb2.zip,.epub:Nomad Reader|.zip:FBReader";
	final static public String    defReader = "Nomad Reader";
	final static public int       TYPES_ACT = 1;
	final static int              CNTXT_MENU_DELETE_F=1;
	final static int              CNTXT_MENU_DELETE_D_EMPTY=2;
	final static int              CNTXT_MENU_DELETE_D_NON_EMPTY=3;
	final static int              CNTXT_MENU_ADD=4;
	final static int              CNTXT_MENU_CANCEL=5;
	String                        currentRoot = "/sdcard";
	Integer                       currentPosition = -1;
	List<HashMap<String, String>> itemsArray;
	Stack<Integer>                positions = new Stack<Integer>();
    SimpleAdapter                 adapter;
    SharedPreferences             prefs;
	ReLaunchApp                   app;

	class FLSimpleAdapter extends SimpleAdapter {
    	FLSimpleAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to)
    	{
    		super(context, data, resource, from, to);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		//return super.getView(position, convertView, parent);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.flist_layout, null);
            }
            HashMap<String, String> item = itemsArray.get(position);
            if (item != null) {
            	TextView  tv = (TextView) v.findViewById(R.id.fl_text);
            	ImageView iv = (ImageView) v.findViewById(R.id.fl_icon);
            	String    sname = item.get("name");
            	if (tv != null)
            		tv.setText(sname);
            	if(iv != null)
            	{
            		if (item.get("type").equals("dir"))
            			iv.setImageDrawable(getResources().getDrawable(R.drawable.dir_ok));
            		else
            		{
            			String rdrName = item.get("reader");
            			if (rdrName.equals("Nope"))
            				iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
            			else
            			{
            				if (app.getIcons().containsKey(rdrName))
            					iv.setImageDrawable(app.getIcons().get(rdrName));
            				else
            					iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
            			}
            		}
            	}
            }
            return v;
    	}
    }

    public static List<HashMap<String, String>> parseReadersString(String readerList)
    {
    	List<HashMap<String, String>> rc = new ArrayList<HashMap<String,String>>();
        String[] rdrs = readerList.split("\\|");
        for (int i=0; i<rdrs.length; i++)
        {
        	String[] re = rdrs[i].split(":");
        	if (re.length != 2)
        		continue;
        	String rName = re[1];
    		String[] exts = re[0].split(",");
           	for (int j=0; j<exts.length; j++)
        	{
           		String ext = exts[j];
        		HashMap<String, String> r = new HashMap<String, String>();
        		r.put(ext, rName);
        		rc.add(r);
        	}
        }
        return rc;
    }
    
    public static String createReadersString(List<HashMap<String, String>> rdrs)
    {
    	String rc = new String();
    	
       	for (HashMap<String, String> r : rdrs)
    	{
    		for (String key : r.keySet())
    		{
    			if (!rc.equals(""))
    				rc += "|";
    			rc += key + ":" + r.get(key);
    		} 		
    	}
       	return rc;
    }


    private void drawDirectory(String root, Integer startPosition)
    {
    	File         dir = new File(root);
    	File[]       allEntries = dir.listFiles();    	
    	List<String> files = new ArrayList<String>();
    	List<String> dirs = new ArrayList<String>();

    	currentRoot = root;
    	currentPosition = startPosition;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastdir", currentRoot);
        editor.commit();
    	
    	TextView  tv = (TextView)findViewById(R.id.title_txt);
    	tv.setText(dir.getAbsolutePath());

    	itemsArray = new ArrayList<HashMap<String,String>>();
    	if (dir.getParent() != null)
    		dirs.add("..");
    	if (allEntries != null)
    	{
    		for (File entry : allEntries)
    		{
    			if (entry.isDirectory())
    				dirs.add(entry.getName());
    			else
    				files.add(entry.getName()); 
    		}
    	}
    	Collections.sort(dirs);
    	Collections.sort(files);
    	for (String f : dirs)
    	{
    		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("name", f);
    		item.put("dname", dir.getAbsolutePath());
    		if (f.equals(".."))
    			item.put("fname", dir.getParent());
    		else
    			item.put("fname", dir.getAbsolutePath() + "/" + f);
    		item.put("type", "dir");
    		item.put("reader", "Nope");
    		itemsArray.add(item);
    	}
    	for (String f : files)
    	{
    		HashMap<String, String> item = new HashMap<String, String>();
    		item.put("name", f);
    		item.put("dname", dir.getAbsolutePath());
    		item.put("fname", dir.getAbsolutePath() + "/" + f);
    		item.put("type", "file");
    		item.put("reader", app.readerName(f));
    		itemsArray.add(item);
    	}
    	
    	String[] from = new String[] { "name" };
    	int[]    to   = new int[] { R.id.fl_text };
    	ListView lv = (ListView) findViewById(R.id.fl_list);

        adapter = new FLSimpleAdapter(this, itemsArray, R.layout.flist_layout, from, to);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        if (startPosition != -1)
        	lv.setSelection(startPosition);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
             	HashMap<String, String> item = itemsArray.get(position);      
                
            	if (item.get("name").equals(".."))
            	{
            		// Goto up directory
            		File dir = new File(item.get("dname"));
            		String pdir = dir.getParent();
            		if (pdir != null)
            		{
            			Integer p = -1;
            			if (!positions.empty())
            				p = positions.pop();
            			drawDirectory(pdir, p);
            		}
            	}
            	else if (item.get("type").equals("dir"))
            	{
            		// Goto directory
        			positions.push(position);
            		drawDirectory(item.get("fname"), -1);
            	}
            	else if (!item.get("reader").equals("Nope"))
            	{
            		// Launch reader
            		if (app.askIfAmbiguous)
            		{
            			List<String> rdrs = app.readerNames(item.get("fname"));
            			if (rdrs.size() < 1)
            				return;
            			else if (rdrs.size() == 1)
            				start(app.launchReader(rdrs.get(0), item.get("fname")));
            			else
            			{
            			   	final CharSequence[] applications = rdrs.toArray(new CharSequence[rdrs.size()]);
            			   	final String rdr1 = item.get("fname");
    						AlertDialog.Builder builder = new AlertDialog.Builder(ReLaunch.this);
    						builder.setTitle("Select application");
    						builder.setSingleChoiceItems(applications, -1, new DialogInterface.OnClickListener() {
    						    public void onClick(DialogInterface dialog, int i) {
    						    	start(app.launchReader((String)applications[i], rdr1));
    		            			dialog.dismiss();
    						    }
    						});
    						AlertDialog alert = builder.create();
    						alert.show();

            			}
            		}
            		else
            			start(app.launchReader(item.get("reader"), item.get("fname")));
            	}
            }});
    }

	private HashMap<String, Drawable> createIconsList(PackageManager pm)
    {
        HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
        
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Drawable d = null;
    		try {
    			d = pm.getApplicationIcon(packageInfo.packageName);
    		} catch(PackageManager.NameNotFoundException e) { }
        
    		if (d != null)
    			rc.put((String)pm.getApplicationLabel(packageInfo), d);
        }
        return rc;
    }
    
    private List<String> createAppList(PackageManager pm)
    {
    	List<String> rc = new ArrayList<String>();
    	
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA))
        {
        	Intent aIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);
        	String aLabel = (String)pm.getApplicationLabel(packageInfo);
        	if (aIntent != null  &&  aLabel != null)
        		//appSet.add(aLabel);
        		rc.add(aLabel);
        }
        Collections.sort(rc);
        return rc;
    }
 
    private void start(Intent i)
    {
    	if (i != null)
    		startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create global storage with values
        app = (ReLaunchApp)getApplicationContext();

		// Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String typesString = prefs.getString("types", defReaders);
        //Log.d(TAG, "Types string: \"" + typesString + "\"");

       // Create application icons map
        app.setIcons(createIconsList(getPackageManager()));
        
        // Create applications label list
        app.setApps(createAppList(getPackageManager()));
        
        // Readers list
        app.setReaders(parseReadersString(typesString));
        //Log.d(TAG, "Readers string: \"" + createReadersString(app.getReaders()) + "\"");

        // Miscellaneous lists list
        app.readFile("lastOpened", LRU_FILE);
        app.readFile("favorites", FAV_FILE);
        app.readFile("startReaders", RDR_FILE, ":");
        if (app.getList("startReaders").size() < 1)
        {
        	// Default list for start readers
        	List<String[]> nl = new ArrayList<String[]>();
        	nl.add(new String[] {"Nomad Reader", "application/fb2"});
        	nl.add(new String[] {"EBookDroid",   "application/djvu"});
        	app.setList("startReaders", nl);
        }

        // Main layout
        if (prefs.getBoolean("showButtons", true))
        {
        	setContentView(R.layout.main);
        	((ImageButton)findViewById(R.id.settings_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuSettings(); }});
        	((ImageButton)findViewById(R.id.types_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuTypes(); }});
        	((ImageButton)findViewById(R.id.search_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuSearch(); }});
        	((ImageButton)findViewById(R.id.lru_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuLastopened(); }});
        	((ImageButton)findViewById(R.id.favor_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuFavorites(); }});
        	((ImageButton)findViewById(R.id.readers_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuReaders(); }});
        	((ImageButton)findViewById(R.id.about_btn)).setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) { menuAbout(); }});
        }
        else
        	setContentView(R.layout.main_nobuttons);

        // First directory to get to
        if (prefs.getBoolean("saveDir", true))
        	drawDirectory(prefs.getString("lastdir", "/sdcard"), -1);
        else
        	drawDirectory(prefs.getString("startDir", "/sdcard"), -1);
    }
    
    @Override
	protected void onStart() {
		super.onStart();

		// Reread preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String typesString = prefs.getString("types", defReaders);
        //Log.d(TAG, "Types string: \"" + typesString + "\"");

        // Recreate readers list
        app.setReaders(parseReadersString(typesString));
        //Log.d(TAG, "Readers string: \"" + createReadersString(readers) + "\"");
        
        app.askIfAmbiguous = prefs.getBoolean("askAmbig", false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.search:
				menuSearch();
				return true;
			case R.id.mime_types:
				menuTypes();
		        return true;
			case R.id.about:
				menuAbout();
				return true;
			case R.id.readers:
				menuReaders();
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
		//Log.d(TAG, "onActivityResult, " + requestCode + " " + resultCode);
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
			case TYPES_ACT:
				String newTypes = createReadersString(app.getReaders());
		        //Log.d(TAG, "New types string: \"" + newTypes + "\"");

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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		HashMap<String, String> i = itemsArray.get(info.position);
		String fn = i.get("name");
		String dr = i.get("dname");
		String tp  = i.get("type");

		if (tp.equals("file"))
		{
			if (!app.contains("favorites", dr, fn))
				menu.add(Menu.NONE, CNTXT_MENU_ADD, Menu.NONE, "Add to favorites");
			menu.add(Menu.NONE, CNTXT_MENU_DELETE_F, Menu.NONE, "Delete");
		}
		else
		{
	    	File   d = new File(dr + "/" + fn);
	    	String[] allEntries = d.list();
	    	if (allEntries.length > 0)
	    		menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_NON_EMPTY, Menu.NONE, "Delete NON-EMPTY directory!");
	    	else
	    		menu.add(Menu.NONE, CNTXT_MENU_DELETE_D_EMPTY, Menu.NONE, "Delete empty directory");
		}
		menu.add(Menu.NONE, CNTXT_MENU_CANCEL, Menu.NONE, "Cancel");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CNTXT_MENU_CANCEL)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final int pos = info.position;
		HashMap<String, String> i = itemsArray.get(pos);
		final String fname = i.get("name");
		final String dname = i.get("dname");

		switch (item.getItemId())
		{
		case CNTXT_MENU_ADD:
			app.addToList("favorites", dname, fname, true);
			break;
		case CNTXT_MENU_DELETE_F:
			if (prefs.getBoolean("confirmFileDelete", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Are you sure to delete file \"" + fname + "\"?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							if (app.removeFile(dname, fname))
							{
								itemsArray.remove(pos);
								adapter.notifyDataSetChanged();
							}
						}});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
            			dialog.dismiss();
					}});
				builder.show();
			}
			else if (app.removeFile(dname, fname))
			{
				itemsArray.remove(pos);
				adapter.notifyDataSetChanged();
			}
			break;
		case CNTXT_MENU_DELETE_D_EMPTY:
			if (prefs.getBoolean("confirmDirDelete", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Are you sure to delete empty directory \"" + fname + "\"?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							if (app.removeFile(dname, fname))
							{
								itemsArray.remove(pos);
								adapter.notifyDataSetChanged();
							}
						}});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
            			dialog.dismiss();
					}});
				builder.show();
			}
			else if (app.removeFile(dname, fname))
			{
				itemsArray.remove(pos);
				adapter.notifyDataSetChanged();
			}
			break;
		case CNTXT_MENU_DELETE_D_NON_EMPTY:
			if (prefs.getBoolean("confirmNonEmptyDirDelete", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Are you sure to delete non-empty directory \"" + fname + "\" (dangerous) ?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							if (app.removeDirectory(dname, fname))
							{
								itemsArray.remove(pos);
								adapter.notifyDataSetChanged();
							}
						}});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
            			dialog.dismiss();
					}});
				builder.show();
			}
			else if (app.removeDirectory(dname, fname))
			{
				itemsArray.remove(pos);
				adapter.notifyDataSetChanged();
			}
			break;
		}
		return true;
	}

	@Override
	protected void onStop() {
		int lruMax = 30;
		int favMax = 30;
		try {
			lruMax = Integer.parseInt(prefs.getString("lruSize", "30"));
			favMax = Integer.parseInt(prefs.getString("favSize", "30"));
		} catch(NumberFormatException e) { }
		app.writeFile("lastOpened", LRU_FILE, lruMax);
		app.writeFile("favorites",  FAV_FILE, favMax);
        app.writeFile("startReaders", RDR_FILE, 0, ":");
		super.onStop();
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
		intent.putExtra("title", "Last opened");
		intent.putExtra("rereadOnStart", true);
        startActivity(intent);

	}
	
	private void menuFavorites() {
		Intent intent = new Intent(ReLaunch.this, ResultsActivity.class);
		intent.putExtra("list", "favorites");
		intent.putExtra("title", "Favorites");
		intent.putExtra("rereadOnStart", true);
        startActivity(intent);
	}
	
	private void menuReaders() {
		Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
	}
	
	private void menuAbout() {
		String vers = getResources().getString(R.string.app_version);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("ReLaunch");
		builder.setMessage("Reader launcher\nVersion: " + vers);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}});
		builder.show();
	}
}