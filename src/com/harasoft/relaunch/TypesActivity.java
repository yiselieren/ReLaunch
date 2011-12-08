package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TypesActivity extends Activity {
    final String                  TAG = "Types";
    final String                  INTENT_PREFIX = "Intent:";
    HashMap<String, Drawable>     icons;
    PackageManager                pm;
    List<String>                  applicationsArray;
    CharSequence[]                applications;
    CharSequence[]                happlications;    
    List<HashMap<String,String>>  itemsArray;
    TPAdapter                     adapter;
    ReLaunchApp                   app;

    class TPAdapter extends BaseAdapter {
        final Context cntx;

        TPAdapter(Context context)
        {
            cntx = context;
        }

        public int getCount() {
            return itemsArray.size();
        }

        public Object getItem(int position) {
            return itemsArray.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.types_layout, null);
            }
            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null)
            {
                ImageView iv = (ImageView) v.findViewById(R.id.types_img);

                // Setting up button
                ImageButton upBtn = (ImageButton) v.findViewById(R.id.types_up);
                if (position == 0)
                {
                    upBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
                    upBtn.setEnabled(false);
                }
                else
                {
                    upBtn.setImageDrawable(getResources().getDrawable(R.drawable.arrow_up));
                    upBtn.setEnabled(true);
                }
                upBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            HashMap<String,String> i = itemsArray.get(position);
                            itemsArray.remove(position);
                            itemsArray.add(position-1, i);
                            adapter.notifyDataSetChanged();
                        }
                    });

                // Setting down button
                ImageButton downBtn = (ImageButton) v.findViewById(R.id.types_down);
                if (position == (itemsArray.size()-1))
                {
                    downBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.checkbox_off_background));
                    downBtn.setEnabled(false);
                }
                else
                {
                    downBtn.setImageDrawable(getResources().getDrawable(R.drawable.arrow_down));
                    downBtn.setEnabled(true);
                }
                downBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            HashMap<String,String> i = itemsArray.get(position);
                            itemsArray.remove(position);
                            itemsArray.add(position+1, i);
                            adapter.notifyDataSetChanged();
                        }
                    });

                // Setting remove button
                ImageButton rmBtn = (ImageButton) v.findViewById(R.id.types_delete);
                rmBtn.setEnabled(itemsArray.size() > 1);
                rmBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            itemsArray.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });

                // Setting extension title
                TextView ext_title = (TextView) v.findViewById(R.id.types_ext_title);
                //ext_title.setText("Suffix (" + (position+1) + "/" + (itemsArray.size()) + ")");
                ext_title.setText(getResources().getString(R.string.jv_types_suffix) + " (" + (position+1) + "/" + (itemsArray.size()) + ")");

                // Setting extension
                Button  extName = (Button)v.findViewById(R.id.types_ext);
                extName.setText(item.get("ext"));
                extName.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
                            //builder.setTitle("File suffix");
                            builder.setTitle(getResources().getString(R.string.jv_types_file_suffix));
                            final EditText input = new EditText(cntx);
                            input.setText(item.get("ext"));
                            builder.setView(input);

                            //builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            builder.setPositiveButton(getResources().getString(R.string.jv_types_ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String value = input.getText().toString();
                                        if (value.equals(""))
                                            //Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
                                        	Toast.makeText(cntx, getResources().getString(R.string.jv_types_cant_be_empty), Toast.LENGTH_LONG).show();
                                        else
                                        {
                                            itemsArray.get(position).put("ext", value);
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                                });

                            //builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            builder.setNegativeButton(getResources().getString(R.string.jv_types_cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });

                            builder.show();
                        }
                    });

                // Setting application name
                Button  appName = (Button) v.findViewById(R.id.types_app);
                String    app = item.get("rdr");
                String[]  appp = app.split("\\%");
                if(appp.length>2) {
                	appName.setText(appp[2]);
                }
                else {
                	appName.setText(app);
                }
                if(icons.containsKey(app))
                    iv.setImageDrawable(icons.get(app));
                else
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.icon));

                appName.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(cntx);
                            //builder1.setTitle("Explicit application or general intent?");
                            builder1.setTitle(getResources().getString(R.string.jv_types_app_or_int_title));
                            //builder1.setMessage("When you tap on file with specified suffix ReLaunch"
                            //        + " may call explicit application or just generate intent with"
                            //        + " application type you specify (ACTION_VIEW). \n\nWhich method do you want?");
                            builder1.setMessage(getResources().getString(R.string.jv_types_app_or_int_text));
                            //builder1.setPositiveButton("Explicit application", new DialogInterface.OnClickListener() {
                            builder1.setPositiveButton(getResources().getString(R.string.jv_types_explicit_application), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {                                    
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(cntx);
                                    //builder2.setTitle("Select application");
                                    builder2.setTitle(getResources().getString(R.string.jv_types_select_application));
                                    builder2.setSingleChoiceItems(happlications, -1, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int i) {
                                                itemsArray.get(position).put("rdr", (String)applications[i]);
                                                adapter.notifyDataSetChanged();
                                                dialog.dismiss();
                                            }
                                        });
                                        builder2.show();
                                }});

                            //builder1.setNeutralButton("General intent", new DialogInterface.OnClickListener() {
                            builder1.setNeutralButton(getResources().getString(R.string.jv_types_general_intent), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {    
                                    AlertDialog.Builder builder3 = new AlertDialog.Builder(cntx);
                                    //builder3.setTitle("Intent type");
                                    builder3.setTitle(getResources().getString(R.string.jv_types_intent_type));
                                    final EditText input = new EditText(cntx);
                                    String v = item.get("rdr");
                                    if (v.startsWith(INTENT_PREFIX))
                                        v = v.substring(INTENT_PREFIX.length());
                                    else
                                        v = "application/";
                                    input.setText(v);
                                    builder3.setView(input);
                                    //builder3.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    builder3.setPositiveButton(getResources().getString(R.string.jv_types_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                String value = input.getText().toString();
                                                if (value.equals(""))
                                                    //Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
                                                	Toast.makeText(cntx, getResources().getString(R.string.jv_types_cant_be_empty), Toast.LENGTH_LONG).show();
                                                else
                                                {
                                                    itemsArray.get(position).put("rdr", INTENT_PREFIX + value);
                                                    adapter.notifyDataSetChanged();
                                                    dialog.dismiss();
                                                }
                                            }
                                        });

                                    //builder3.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    builder3.setNegativeButton(getResources().getString(R.string.jv_types_cancel), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                dialog.dismiss();
                                            }
                                        });

                                    builder3.show();
                                }});

                            //builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            builder1.setNegativeButton(getResources().getString(R.string.jv_types_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }});

                            builder1.show();
                            
                            /*
                            */
                        }
                    });

            }
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Global storage
        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.types_view);
        icons = app.getIcons();

        applicationsArray = app.getApps();
        applications = applicationsArray.toArray(new CharSequence[applicationsArray.size()]);
        happlications = app.getApps().toArray(new CharSequence[app.getApps().size()]);
        for(int j=0;j<happlications.length;j++) {
        	String happ = (String)happlications[j];
        	String[] happp = happ.split("\\%"); 
        	happlications[j] = happp[2];
        }

        // Fill listview with our info
        ListView lv = (ListView) findViewById(R.id.types_lv);

        itemsArray = new ArrayList<HashMap<String,String>>();
        for (HashMap<String, String> r : app.getReaders())
        {
            for (String k : r.keySet())
            {
                HashMap<String,String> i = new HashMap<String,String>();

                i.put("ext", k);
                i.put("rdr", r.get(k));
                itemsArray.add(i);
            }
        }
        adapter = new TPAdapter(this);
        lv.setAdapter(adapter);

        // OK/Save button
        Button    okBtn = (Button) findViewById(R.id.types_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    List<HashMap<String, String>> readers = new ArrayList<HashMap<String, String>>();
                    for (HashMap<String, String> r : itemsArray)
                    {
                        HashMap<String, String> a = new HashMap<String, String>();
                        a.put(r.get("ext"), r.get("rdr"));
                        readers.add(a);
                    }
                    app.setReaders(readers);
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

        // Add new button
        Button    addBtn = (Button) findViewById(R.id.types_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    HashMap<String,String> i = new HashMap<String,String>();
                    i.put("ext", ".");
                    i.put("rdr", ReLaunch.defReader);
                    itemsArray.add(i);
                    adapter.notifyDataSetChanged();
                }
            });

        // Cancel button
        Button    cancelBtn = (Button) findViewById(R.id.types_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
        
        // back btn - work as cancel
        ImageButton    backBtn = (ImageButton) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        app.generalOnResume(TAG, this);
    }
}
