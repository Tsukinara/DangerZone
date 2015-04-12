package dangerzone.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button passcode = (Button) findViewById(R.id.passcode);
        Button countdown = (Button) findViewById(R.id.countdown);
        Button emergency = (Button) findViewById(R.id.emergency);
        Button define = (Button) findViewById(R.id.define);
        Button install = (Button) findViewById(R.id.install);
        Button about = (Button) findViewById(R.id.about);

        passcode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent passcode_intent = new Intent(getApplicationContext(),PasscodeActivity.class);
                startActivity(passcode_intent);
            }
        });
        countdown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Set Countdown Time");
                LinearLayout linear=new LinearLayout(getApplicationContext());
                linear.setOrientation(LinearLayout.VERTICAL);
                final TextView seekvalue = new TextView(getApplicationContext());
                seekvalue.setGravity(Gravity.CENTER);
                final SeekBar seek=new SeekBar(getApplicationContext());
                final SharedPreferences pref = getSharedPreferences("dangerzone", Context.MODE_PRIVATE);
                seek.setMax(10);
                if(pref.contains("countdown"))
                    seek.setProgress(pref.getInt("countdown", -1));
                else
                    seek.setProgress(5);
                seekvalue.setText(String.valueOf(seek.getProgress() + 5) + " seconds");
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekvalue.setText(String.valueOf(progress + 5) + " seconds");
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                linear.addView(seekvalue);
                linear.addView(seek);
                alert.setView(linear);
                alert.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int id){
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("countdown", seek.getProgress());
                        editor.commit();
                        PebbleDictionary pd = new PebbleDictionary();
                        pd.addUint8(0,(byte) (seek.getProgress() + 5));
                        PebbleKit.sendDataToPebble(getApplicationContext(), Constants.PEBBLE_APP_UUID, pd);
                        Toast.makeText(getApplicationContext(), "Time Set: " + seekvalue.getText(), Toast.LENGTH_LONG).show();
                    }
                });
                alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int id){
                        Toast.makeText(getApplicationContext(), "Canceled",Toast.LENGTH_LONG).show();
                    }
                });
                alert.show();
            }
        });
        emergency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Select Emergency Number");
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.select_dialog_singlechoice);
                final SharedPreferences pref = getSharedPreferences("dangerzone", Context.MODE_PRIVATE);
                if(pref.contains("phonenumbers"))
                    arrayAdapter.addAll(pref.getStringSet("phonenumbers", null));
                else
                    arrayAdapter.add("911");
                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alert.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                        MainActivity.this);
                                builderInner.setMessage(strName);
                                builderInner.setTitle("Your Selected Item is");
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("thechosenphone", "tel:" + strName.trim());
                                editor.commit();
                                builderInner.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builderInner.show();
                            }
                        });
                alert.show();
            }
        });
        define.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent define_intent = new Intent(getApplicationContext(), DefineActivity.class);
                startActivity(define_intent);
            }
        });
        install.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pbw = new Intent(Intent.ACTION_VIEW);
                pbw.setDataAndType(Uri.parse("http://mattdu.net/files/qa.pbw"), "application/pbw");
                startActivity(pbw);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PebbleKit.startAppOnPebble(getApplicationContext(), UUID.fromString("2edff9df-4d60-47db-9962-437d40c5f1e2"));
                Intent about_intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(about_intent);
            }
        });
        Intent intent = new Intent(this, PebbleService.class);
        startService(intent);
    }
}
