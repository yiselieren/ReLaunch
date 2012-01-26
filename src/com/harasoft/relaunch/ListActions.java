package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ListActions {
	String	TAG = "ListActions";
	List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();
	ReLaunchApp app;
	Activity act;
	SharedPreferences prefs;
	String listName;
	
	private void createItemsArray() {
		itemsArray = new ArrayList<HashMap<String, String>>();
		for (String[] n : app.getList(listName)) {
			if (!prefs.getBoolean("filterResults", false)
					|| app.filterFile(n[0], n[1])) {
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

    private void start(Intent i)
    {
        if (i != null)
            try {
                act.startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(act, app.getResources().getString(R.string.jv_results_activity_not_found), Toast.LENGTH_LONG).show();
            }
    }
	
	public ListActions(ReLaunchApp application, Activity activity) {
		app = application;
		act = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(app.getBaseContext());
	}
	
	public void showMenu(String listName) {
		this.listName = listName;
		createItemsArray();
		if(itemsArray.size()>0) {
			// exts dirs sorter
            final class ExtsComparator implements java.util.Comparator<String>
            {
                public int compare(String a, String b)
                {
                if(a==null && b==null) return 0;
                if(a==null && b!=null) return 1;
                if(a!=null && b==null) return -1;
                if(a.length() < b.length()) return 1;
                if(a.length() > b.length()) return -1;
                return a.compareTo(b);
                }
            }
            // known extensions
            List<HashMap<String, String>> rc;
            ArrayList<String> exts = new ArrayList<String>();
            if(prefs.getBoolean("hideKnownExts", false)) {
            	rc = app.getReaders();
            	Set<String> tkeys = new HashSet<String>();
            	for(int i=0;i<rc.size();i++) {
            		Object[] keys = rc.get(i).keySet().toArray();
            		for(int j=0;j<keys.length;j++) {
            			tkeys.add(keys[j].toString());
            		}
            	}
            	exts = new ArrayList<String>(tkeys);
            	Collections.sort(exts,new ExtsComparator());
            	}
            // known dirs
            ArrayList<String> dirs = new ArrayList<String>();
            if(prefs.getBoolean("hideKnownDirs", false)) {
            	String[] home_dirs = prefs.getString("startDir", "/sdcard,/media/My Files").split("\\,"); 
            	for(int i=0;i<home_dirs.length;i++) dirs.add(home_dirs[i]);
            	Collections.sort(dirs,new ExtsComparator());
            	}
            
            //if (item != null) {
			//	String fname = item.get("fname");
			//	String sname = item.get("fname");
			//	String dname = item.get("dname");
			//	String sdname = item.get("dname");
			//	String fullName = dname + "/" + fname;
			// clean extension, if needed
            
			//if(prefs.getBoolean("hideKnownExts", false)) {
			//	for(int i=0;i<exts.size();i++) {
			//		if(sname.endsWith(exts.get(i))) {
			//			sname = sname.substring(0, sname.length()-exts.get(i).length());
			//		}
			//	}
			//}
            final CharSequence[] lnames = new CharSequence[itemsArray.size()];
            for(int i=0;i<itemsArray.size();i++) {
            	HashMap<String, String> item = itemsArray.get(i);
            	String fname = item.get("fname");
            	String sname = item.get("fname");
            	String dname = item.get("dname");
            	// clean extension, if needed
				if(prefs.getBoolean("hideKnownExts", false)) {
					for(int j=0;j<exts.size();j++) {
						if(sname.endsWith(exts.get(j))) {
							sname = sname.substring(0, sname.length()-exts.get(j).length());
						}
					}
				}
				if (dname.equals("")) {
					dname = "/";
					if (fname.equals("")) {
						fname = "/";
						sname = "/";
						dname = "";
					}
				}
				lnames[i] = sname;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            //builder.setTitle("Select home directory");
            final String flistName = listName;
            builder.setTitle("Выберите");
            builder.setSingleChoiceItems(lnames, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                    	// action
                    	runItem(flistName, i);
                        dialog.dismiss();
                    }
                });
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }});
            builder.show();
            
		}
	}
	
	public void runItem(String listName, int position) {
		
	this.listName = listName;
	createItemsArray();
	
	if(position<0 || position>itemsArray.size()-1) return;
	
	HashMap<String, String> item = itemsArray.get(position);

	String fullName = item.get("dname") + "/" + item.get("fname");
	if (item.get("type").equals("dir")) {
		Intent intent = new Intent(act,
				ReLaunch.class);
		intent.putExtra("dirviewer", false);
		intent.putExtra("start_dir", fullName);
		intent.putExtra("home", ReLaunch.useHome);
		intent.putExtra("home1", ReLaunch.useHome1);
		intent.putExtra("shop", ReLaunch.useShop);
		intent.putExtra("library", ReLaunch.useLibrary);
		// oldHome = ReLaunch.useHome;
		act.startActivityForResult(intent, ReLaunch.DIR_ACT);
		// startActivity(intent);
	} else {
		String fileName = item.get("fname");
		if (!app.specialAction(act, fullName)) {
			if (app.readerName(fileName).equals("Nope"))
				app.defaultAction(act, fullName);
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
								act);
						// builder.setTitle("Select application");
						builder.setTitle(app.getResources()
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
	}
}
}
