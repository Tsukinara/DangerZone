package dangerzone.dangerzone;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends ListActivity {

    private EntryAdapter adapter;
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
            adapter.add(new EntryWrapper(e, loc, loc.getProvider().equals("poi")));
        }
        adapter.sort(new Comparator<EntryWrapper>(){
            @Override
            public int compare(EntryWrapper lhs, EntryWrapper rhs) {
                int out = 0;
                if (lhs.valid && rhs.valid){
                    out = (new Double(lhs.dist)).compareTo(new Double(rhs.dist));
                }
                if (out == 0){
                    out = -lhs.entry.reportdatetime.compareTo(rhs.entry.reportdatetime);
                }
                return out;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entries = new EntryList();

        adapter = new EntryAdapter(this.getApplicationContext(), new ArrayList<EntryWrapper>());

        ListView view = (ListView) findViewById(R.id.list);
        view.setAdapter(adapter);

        Intent intent = new Intent(this, DaengerDaemon.class);
        startService(intent);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, EntryActivity.class);
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
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
