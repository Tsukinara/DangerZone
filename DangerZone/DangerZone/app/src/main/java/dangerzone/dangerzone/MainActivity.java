package dangerzone.dangerzone;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class MainActivity extends ListActivity {

    private ArrayAdapter<String> adapter;
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
                updateList();
            }
        }
    }

    private void updateList() {
        adapter.clear();
        for (Entry e : entries.getValues()) {
            String str;
            if (loc.getProvider().equals("poi")) {
                str = e.offense;
            } else {
                float[] distance = new float[1];
                Location.distanceBetween(e.latitude, e.longitude, loc.getLatitude(), loc.getLongitude(), distance);
                str = e.offense + "\n" +
                        Math.floor(distance[0]) +
                        "m";
            }
            adapter.add(str);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entries = new EntryList();

        adapter = new ArrayAdapter<>(this, R.layout.list_element);
        adapter.add("Hello\nElevator");
        adapter.add("World");

        setListAdapter(adapter);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);

        Intent intent = new Intent(this, DaengerDaemon.class);
        startService(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("refresh");
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
