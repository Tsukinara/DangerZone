package dangerzone.dangerzone;

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
        wrapper = getIntent().getParcelableExtra("wrapper");

        TextView textView = (TextView) findViewById(R.id.entryTextView);

        StringBuilder builder = new StringBuilder();
        builder.append("Offense: ").append(wrapper.entry.offense).append("\n");
        if (!wrapper.entry.method.equals("OTHERS"))
            builder.append("Method: ").append(wrapper.entry.method).append("\n");
        builder.append("Date: ").append(SimpleDateFormat.getDateInstance().format(wrapper.entry.reportdatetime)).append("\n");
        builder.append("Distance: ").append((int)Math.floor(wrapper.dist)).append(" m\n");

        textView.setText(builder.toString() );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry, menu);
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
