package dangerzone.dangerzone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Comparator;

public class ListActivity extends ActionBarActivity {

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
        if (entries == null) {
            entries = new EntryList();
        }
        if (loc == null) {
            loc = new Location("poi");
        }
        adapter.clear();
        for (Entry e : entries.getValues()) {
            adapter.add(new EntryWrapper(e, loc, !loc.getProvider().equals("poi")));
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
        setContentView(R.layout.activity_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (getIntent() != null) {
            entries = getIntent().getParcelableExtra("entries");
            loc = getIntent().getParcelableExtra("location");
        }
        if (savedInstanceState != null) {
            entries = savedInstanceState.getParcelable("entries");
            loc = savedInstanceState.getParcelable("location");
        }

        adapter = new EntryAdapter(this.getApplicationContext(), new ArrayList<EntryWrapper>());

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, EntryActivity.class);
                EntryWrapper w = (EntryWrapper) (parent.getAdapter().getItem(position));
                intent.putExtra("wrapper", w);
                startActivity(intent);
            }
        });

        updateList();
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
}
