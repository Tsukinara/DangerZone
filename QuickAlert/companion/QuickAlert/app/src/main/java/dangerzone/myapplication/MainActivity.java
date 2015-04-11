package dangerzone.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.UUID;


public class MainActivity extends Activity {

    private final static UUID PEBBLE_APP_UUID = UUID.fromString("56bdf9e4-1cbd-4840-b615-464bd4a002de");

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
                seek.setMax(10);
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
                arrayAdapter.add("911");
                arrayAdapter.add("420");
                arrayAdapter.add("42");
                arrayAdapter.add("69");
                arrayAdapter.add("1337");
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
                pbw.setDataAndType(
                        Uri.parse("http:....pbw"),
                        "application/pbw");

                startActivity(pbw);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent about_intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(about_intent);
            }
        });

        /*PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                System.out.println("Pebble connected!");
            }

        });
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                System.out.println("Pebble disconnected!");
            }

        });
        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                Log.i(getLocalClassName(), "Received value=" + data.getInteger(0) + " for key: 0");

                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
            }

        });*/
        /*String number = "tel:3012817202";
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        startActivity(callIntent);*/
    }
}
