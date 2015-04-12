package dangerzone.dangerzone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Patrick on 4/11/2015
 */
public class EntryList {
    private List<Entry> entries;
    private int lastCcn;

    private long expiration;

    public EntryList() {
        entries = new ArrayList<>();
        lastCcn = 0;
        expiration = 86400000*2;
    }

    public void addLatest(List<Entry> newEntries) {
        for (Entry e : newEntries) {
            if (e.ccn > lastCcn) {
                lastCcn = e.ccn;
                entries.add(e);
            }
        }
    }

    public void expire() {
        Date date = new Date();
        Date minDate = new Date(date.getTime() - expiration);

        while (entries.get(0).reportdatetime.compareTo(minDate) < 0) {
            entries.remove(0);
        }
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
