package dangerzone.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

public class DefineActivity extends ActionBarActivity {

    String selected = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Button new_button = (Button) findViewById(R.id.new_button);
        final Button edit_button = (Button) findViewById(R.id.edit_button);
        final Button delete_button = (Button) findViewById(R.id.delete_button);
        edit_button.setEnabled(false);
        delete_button.setEnabled(false);
        ListView emergency_list = (ListView) findViewById(R.id.emergency_list);
        final SharedPreferences pref = getSharedPreferences("dangerzone", Context.MODE_PRIVATE);
        final ArrayList<String> pnumber_list = new ArrayList<String>();
        if(pref.contains("phonenumbers"))
            pnumber_list.addAll(pref.getStringSet("phonenumbers", null));
        else
            pnumber_list.add("911");
        final ArrayAdapter<String> list_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pnumber_list);
        emergency_list.setAdapter(list_adapter);

        emergency_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = (String) parent.getItemAtPosition(position);
                edit_button.setEnabled(true);
                delete_button.setEnabled(true);
                pnumber_list.indexOf(selected);
            }
        });
        new_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DefineActivity.this);
                builder.setTitle("New Entry");
                final EditText input = new EditText(getApplicationContext());
                input.setGravity(Gravity.CENTER);
                input.setTextColor(Color.parseColor("black"));
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pnumber_list.add(input.getText().toString());
                        list_adapter.notifyDataSetChanged();
                        edit_button.setEnabled(false);
                        delete_button.setEnabled(false);
                        selected = null;
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putStringSet("phonenumbers", new HashSet<String>(pnumber_list));
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
        });
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DefineActivity.this);
                builder.setTitle("Edit Entry");
                final EditText input = new EditText(getApplicationContext());
                input.setGravity(Gravity.CENTER);
                input.setTextColor(Color.parseColor("black"));
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                input.setText(selected);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pnumber_list.set(pnumber_list.indexOf(selected),input.getText().toString());
                        list_adapter.notifyDataSetChanged();
                        edit_button.setEnabled(false);
                        delete_button.setEnabled(false);
                        selected = null;
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putStringSet("phonenumbers", new HashSet<String>(pnumber_list));
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        delete_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pnumber_list.remove(selected);
                list_adapter.notifyDataSetChanged();
                edit_button.setEnabled(false);
                delete_button.setEnabled(false);
                selected = null;
                SharedPreferences.Editor editor = pref.edit();
                editor.putStringSet("phonenumbers", new HashSet<String>(pnumber_list));
                editor.commit();
            }
        });
    }
}
