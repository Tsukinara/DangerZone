package dangerzone.dangerzone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    private DataUpdateReceiver dataUpdateReceiver;
    private EntryList entries;
    private Location loc;
    private int radius = 100000, recency = 3, rate = 5;

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("refresh")) {
                Bundle data = intent.getBundleExtra("data");
                entries = data.getParcelable("data");
                loc = data.getParcelable("location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entries = new EntryList();
        loc = new Location("poi");

        if (savedInstanceState != null) {
            entries = savedInstanceState.getParcelable("entries");
            loc = savedInstanceState.getParcelable("location");
        }


        Intent intent = new Intent(this, DaengerDaemon.class);
        startService(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("entries", entries);
        outState.putParcelable("location", loc);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        if (dataUpdateReceiver != null) bm.unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("refresh");
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(dataUpdateReceiver, intentFilter);
    }

    public void onRadiusClicked(View view) {
        final int tmp_rady[] = {radius};
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.radius_settings);
        TextView text = new TextView(this);
        text.setText("How cautious are you feeling?");
        LinearLayout linlay = new LinearLayout(this);
        linlay.setOrientation(LinearLayout.VERTICAL);
        SeekBar radseek = new SeekBar(this);
        radseek.setMax(500000);
        radseek.setProgress(tmp_rady[0]);
        radseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tmp_rady[0] = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        linlay.addView(text);
        linlay.addView(radseek);
        builder.setView(linlay);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                radius = tmp_rady[0];
                Intent intent = new Intent("service_settings");
                intent.putExtra("radius", radius);
                LocalBroadcastManager bm = LocalBroadcastManager.getInstance(context);
                bm.sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "Radius Set", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    public void onRecencyClicked(View view) {
        Intent intent = new Intent("service_settings");
        intent.putExtra("days", 5);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void onRateClicked(View view) {
        Intent intent = new Intent("service_settings");
        intent.putExtra("update_time", 1);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void onForceClicked(View view) {
        Intent intent = new Intent("service_settings");
        intent.putExtra("refresh", 1);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void onStopClicked(View view) {
        Intent intent = new Intent("service_settings");
        intent.putExtra("stop", 1);

        ImageButton button = (ImageButton) findViewById(R.id.stop);
        button.setImageResource(R.drawable.service_start_button);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void onListClicked(View view) {
        Intent intent = new Intent(this, ListActivity.class);

        intent.putExtra("entries", entries);
        intent.putExtra("location", loc);

        startActivity(intent);
    }

    public void onAboutClicked(View view) {

    }
}
