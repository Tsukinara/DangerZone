package dangerzone.dangerzone;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bowenzhi on 4/12/2015
 */
public class EntryAdapter extends ArrayAdapter {

    // Vars
    private LayoutInflater mInflater;

    public EntryAdapter(Context context, ArrayList<EntryWrapper> objects) {
        super(context, 0, objects);
        init(context);
    }

    private void init(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_element, parent, false);
            vh = new ViewHolder(convertView);
            vh.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        drawText(vh.textView, (EntryWrapper)(getItem(position)));

        return convertView;
    }

    public void drawText(TextView textView, EntryWrapper entry){
        String text = entry.entry.offense+"\n";
        if (entry.valid){
            text += (int) Math.floor(entry.dist) + " meters";
        }
        textView.setText(text);
    }

    static class ViewHolder {

        TextView textView;

        private ViewHolder(View rootView) {
            textView = (TextView) rootView.findViewById(android.R.id.text1);
        }
    }
}