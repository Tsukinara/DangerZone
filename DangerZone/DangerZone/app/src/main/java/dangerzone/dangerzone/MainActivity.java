package dangerzone.dangerzone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import java.util.List;

public class MainActivity extends Activity {
    private DataUpdateReceiver dataUpdateReceiver;
    private EntryList entries;
    private Location loc;

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

        Intent intent = new Intent(this, DaengerDaemon.class);
        startService(intent);
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
        Intent intent = new Intent("service_settings");
        intent.putExtra("radius", 500);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
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

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }

    public void onListClicked(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void onAboutClicked(View view) {

    }
}
