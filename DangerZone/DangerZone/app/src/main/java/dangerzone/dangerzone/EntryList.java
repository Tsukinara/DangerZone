package dangerzone.dangerzone;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Patrick on 4/11/2015
 */
public class EntryList implements Parcelable{
    private HashMap<Integer, Entry> entries;
    private HashSet<Integer> covered;

    public EntryList() {
        entries = new HashMap<>();
        covered = new HashSet<>();
    }

    //Parcels
    public static final Parcelable.Creator<EntryList> CREATOR
            = new Parcelable.Creator<EntryList>() {
        public EntryList createFromParcel(Parcel in) {
            return new EntryList(in);
        }

        public EntryList[] newArray(int size) {
            return new EntryList[size];
        }
    };

    public EntryList(Parcel in) {
        this();
        int size = in.readInt();
        for (int i=0; i<size; i++) {
            Entry e = in.readParcelable(Entry.class.getClassLoader());
            entries.put(e.ccn, e);
        }
        int size2 = in.readInt();
        for (int i=0; i<size2; i++) {
            int ccn = in.readInt();
            covered.add(ccn);
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(entries.size());
        for (Entry e : entries.values()) {
            dest.writeParcelable(e, flags);
        }
        dest.writeInt(covered.size());
        for (int ccn : covered) {
            dest.writeInt(ccn);
        }
    }

    public int describeContents() {
        return 0;
    }


    public void addLatest(List<Entry> newEntries) {
        for (Entry e : newEntries) {
            if (!entries.containsKey(e.ccn)) {
                entries.put(e.ccn, e);
            }
        }
    }

    public void expire(int days) {
        Date date = new Date();
        Date minDate = new Date(date.getTime() - 86400000*days);

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

    public void cover(List<Entry> toCover) {
        for (Entry e : toCover) {
            covered.add(e.ccn);
        }
    }

    public List<Entry> getNear(Location location, double threshold, boolean reentrant) {
        List<Entry> output = new ArrayList<>();

        for (Entry e : entries.values()) {
            if (!(reentrant && covered.contains(e.ccn)) && e.distTo(location) < threshold) {
                output.add(e);
            }
        }

        return output;
    }

    public Collection<Entry> getValues() {
        return entries.values();
    }

    @Override
    public String toString() {
        return entries.values().toString();
    }
}
