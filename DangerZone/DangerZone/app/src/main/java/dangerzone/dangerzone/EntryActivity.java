package dangerzone.dangerzone;

import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class EntryActivity extends ActionBarActivity {
    private EntryWrapper wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        wrapper = getIntent().getParcelableExtra("wrapper");

        TextView textView = (TextView) findViewById(R.id.entryTextView);

        StringBuilder builder = new StringBuilder();
        builder.append("Offense: ").append(wrapper.entry.offense).append("\n");
        if (!wrapper.entry.method.equals("OTHERS"))
            builder.append("Method: ").append(wrapper.entry.method).append("\n");
        builder.append("Reported: ").append(SimpleDateFormat.getDateTimeInstance().format(wrapper.entry.reportdatetime)).append("\n");
        builder.append("Distance: ").append((int)Math.floor(wrapper.dist)).append(" meters\n");
        builder.append("Location: ").append(wrapper.entry.blocksiteaddress).append("\n");
        builder.append("Latitude: ").append(wrapper.entry.latitude).append("°N\n");
        builder.append("Longitude: ").append(-wrapper.entry.longitude).append("°W");

        textView.setText(builder.toString() );
    }
}
