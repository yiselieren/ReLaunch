package com.harasoft.relaunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class FiltersActivity  extends Activity {
    final String                   TAG = "Filters";
    ReLaunchApp                    app;
    FTArrayAdapter                 adapter;
    ListView                       lv;
    List<String[]>                 itemsArray = new ArrayList<String[]>();

    public class myOnItemSelectedListener implements OnItemSelectedListener {
        final int     position;
        myOnItemSelectedListener(int pos)
        {
            position = pos;
        }

        public void onItemSelected(AdapterView<?> parent, View v1, int pos, long row) {
            if (position >=0  &&  position < itemsArray.size())
            {
                itemsArray.set(position, new String[] { Integer.toString(pos), itemsArray.get(position)[1]});
                adapter.notifyDataSetChanged();
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class FTArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
        final Context cntx;

        FTArrayAdapter(Context context, int resource)
        {
            super(context, resource);
            cntx = context;
        }

        @Override
        public int getCount() {
            return itemsArray.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Do not reuse convertView and create new view each time !!!
            // I know it is not so efficient, but I don't know how to prevent
            // spinner's value for different position in list be mixed otherwise.
            // Anyway filters list can't be big, so I hope its OK.
            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View         v = vi.inflate(R.layout.filters_item, null);

            Spinner      methodSpn = (Spinner) v.findViewById(R.id.filters_method);
            ImageButton  rmBtn = (ImageButton) v.findViewById(R.id.filters_delete);
            Button       valBtn = (Button) v.findViewById(R.id.filters_type);
            TextView     condTxt = (TextView)v.findViewById(R.id.filters_condition);

            final String[] item = itemsArray.get(position);
            if (item == null)
                return v;

                // Set  spinner
            Integer spos = 0;
            try {
                spos = Integer.parseInt(item[0]);
            } catch(NumberFormatException e) { }

            ArrayAdapter<CharSequence> sadapter = ArrayAdapter.createFromResource(
                        cntx, R.array.filter_values, android.R.layout.simple_spinner_item);
            sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            methodSpn.setAdapter(sadapter);
            methodSpn.setSelection(spos, false);
            methodSpn.setOnItemSelectedListener(new myOnItemSelectedListener(position));

            // Set remove button
            rmBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        itemsArray.remove(position);
                        adapter.notifyDataSetChanged();
                    }});

            // Set value button
            if (spos == app.FLT_SELECT  ||  spos == app.FLT_NEW  ||  spos == app.FLT_NEW_AND_READING)
            {
                valBtn.setText("");
                valBtn.setEnabled(false);
            }
            else
            {
                valBtn.setText(item[1]);
                valBtn.setEnabled(true);
                valBtn.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
                        builder.setTitle("Filter value:");
                        final EditText input = new EditText(cntx);
                        input.setText(item[1]);
                        builder.setView(input);

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    if (value.equals(""))
                                        Toast.makeText(cntx, "Can't be empty!", Toast.LENGTH_LONG).show();
                                    else
                                    {
                                        itemsArray.set(position, new String[] { item[0], value});
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
                    }});
            }

            // Set condition text
            if (position >= itemsArray.size()-1)
                condTxt.setText("");
            else if (app.filters_and)
                condTxt.setText("AND");
            else
                condTxt.setText("OR");

            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create global storage with values
        app = (ReLaunchApp)getApplicationContext();
        app.setFullScreenIfNecessary(this);
        setContentView(R.layout.filters_view);

        lv = (ListView) findViewById(R.id.filters_lv);
        itemsArray = app.getList("filters");
        adapter = new FTArrayAdapter(this, R.layout.filters_item);
        lv.setAdapter(adapter);

        // OK/Save button
        Button    okBtn = (Button) findViewById(R.id.filters_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    // Save list
                    app.setList("filters", itemsArray);
                    app.writeFile("filters", ReLaunch.FILT_FILE, 0, ":");

                    // Save and/or flag
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("filtersAnd", app.filters_and);
                    editor.commit();

                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

        // Add new button
        Button    addBtn = (Button) findViewById(R.id.filters_new);
        addBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    itemsArray.add(new String[] { "0", ""});
                    adapter.notifyDataSetChanged();
                }
            });

        // AND/OR button
        final Button    andorBtn = (Button) findViewById(R.id.filters_andor);
        if (app.filters_and)
            andorBtn.setText("OR");
        else
            andorBtn.setText("AND");
        andorBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    app.filters_and = ! app.filters_and;
                    if (app.filters_and)
                        andorBtn.setText("OR");
                    else
                        andorBtn.setText("AND");
                    adapter.notifyDataSetChanged();
                }
            });

        // Cancel button
        Button    cancelBtn = (Button) findViewById(R.id.filters_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }});
    }
}
