package dangerzone.dangerzone;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Patrick on 4/11/2015
 */
public class EntryList {
    private HashMap<Integer, Entry> entries;

    private long expiration;

    public EntryList() {
        entries = new HashMap<>();
        expiration = 86400000*2;
    }

    public void addLatest(List<Entry> newEntries) {
        for (Entry e : newEntries) {
            if (!entries.containsKey(e.ccn)) {
                entries.put(e.ccn, e);
            }
        }
    }

    public void expire() {
        Date date = new Date();
        Date minDate = new Date(date.getTime() - expiration);

        List<Integer> keysToDelete = new ArrayList<>();

        for (Integer i : entries.keySet()) {
            Entry e = entries.get(i);
            if (e.reportdatetime.compareTo(minDate) < 0) {
                keysToDelete.add(i);
            }
        }

        for (Integer i : keysToDelete) {
            entries.remove(i);
        }
    }

    public List<Entry> getNear(Location location, double threshold) {
        List<Entry> output = new ArrayList<>();

        for (Entry e : entries.values()) {
            float[] distance = new float[1];
            Location.distanceBetween(e.latitude, e.longitude, location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] < threshold) {
                System.out.println(distance[0]);
                output.add(e);
            }
        }

        return output;
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
