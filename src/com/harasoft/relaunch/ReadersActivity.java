package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class ReadersActivity extends Activity {
    ReLaunchApp                   app;
    RDArrayAdapter                adapter;
    ListView                      lv;
    List<HashMap<String, String>> itemsArray = new ArrayList<HashMap<String, String>>();

    class RDArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
        final Context cntx;

        RDArrayAdapter(Context context, int resource, List<HashMap<String, String>> data)
        {
            super(context, resource, data);
            cntx = context;
        }

        @Override
        public int getCount() {
            return itemsArray.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.readers_item, null);
            }

            Button  rdrName = (Button) v.findViewById(R.id.readers_name);
            Button  typeName = (Button) v.findViewById(R.id.readers_type);
            ImageButton rmBtn = (ImageButton) v.findViewById(R.id.readers_delete);


            if (position >= itemsArray.size())
            {
                rdrName.setVisibility(View.INVISIBLE);
                typeName.setVisibility(View.INVISIBLE);
                rmBtn.setVisibility(View.INVISIBLE);
                return v;
            }

            final HashMap<String, String> item = itemsArray.get(position);
            if (item != null)
            {
                // Setting reader name button
                rdrName.setText(item.get("rdr"));
                rdrName.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
                            builder.setTitle("Reader name");
                            final EditText input = new EditText(cntx);
                            input.setText(item.get("rdr"));
                            builder.setView(input);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String value = input.getText().toString();
                                        if (value.equals(""))
                                            Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
                                        else
                                        {
                                            itemsArray.get(position).put("rdr", value);
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                                });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });

                            builder.show();
                        }
                    });

                // Setting reader name button
                typeName.setText(item.get("type"));
                typeName.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
                            builder.setTitle("Application type");
                            final EditText input = new EditText(cntx);
                            input.setText(item.get("type"));
                            builder.setView(input);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String value = input.getText().toString();
                                        if (value.equals(""))
                                            Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
                                        else
                                        {
                                            itemsArray.get(position).put("type", value);
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                                });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });

                            builder.show();
                        }
                    });

                // Setting remove button
                rmBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            itemsArray.remove(position);
                            //adapter.notifyDataSetChanged();
                            ((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
                        }
                    });
            }
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = ((ReLaunchApp)getApplicationContext());
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.readers_view);

        lv = (ListView) findViewById(R.id.readers_lv);
        itemsArray = new ArrayList<HashMap<String,String>>();
        for (String[] r : app.getList("startReaders"))
        {
            HashMap<String,String> i = new HashMap<String,String>();

            i.put("rdr", r[0]);
            i.put("type", r[1]);
            itemsArray.add(i);
        }
        adapter = new RDArrayAdapter(this, R.layout.readers_item, itemsArray);
        lv.setAdapter(adapter);

        // OK/Save button
        Button    okBtn = (Button) findViewById(R.id.readers_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    List<String[]> rc = new ArrayList<String[]>();
                    for (HashMap<String, String> r : itemsArray)
                    {
                        String[] r1 = new String[2];
                        r1[0] = r.get("rdr");
                        r1[1] = r.get("type");
                        rc.add(r1);
                    }
                    app.setList("startReaders", rc);
                    app.writeFile("startReaders", ReLaunch.RDR_FILE, 0, ":");
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

        // Add new button
        Button    addBtn = (Button) findViewById(R.id.readers_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    HashMap<String,String> i = new HashMap<String,String>();
                    i.put("rdr", ".");
                    i.put("type", "application/");
                    itemsArray.add(i);
                    adapter.notifyDataSetChanged();
                }
            });

        // Revert to defaults button
        Button revertBtn = (Button) findViewById(R.id.readers_revert);
        revertBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    app.setDefault("startReaders");
                    itemsArray = new ArrayList<HashMap<String,String>>();
                    for (String[] r : app.getList("startReaders"))
                    {
                        HashMap<String,String> i = new HashMap<String,String>();

                        i.put("rdr", r[0]);
                        i.put("type", r[1]);
                        itemsArray.add(i);
                    }
                    adapter.notifyDataSetChanged();
                }
            });

        // Cancel button
        Button    cancelBtn = (Button) findViewById(R.id.readers_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
    }
}
