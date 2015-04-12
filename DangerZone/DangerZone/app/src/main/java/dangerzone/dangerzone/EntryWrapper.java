package dangerzone.dangerzone;

import android.location.Location;

public class EntryWrapper<E>{
    public Entry entry;
    public double dist;
    public boolean valid;
    public EntryWrapper(Entry e, Location l, boolean b){
        super();
        entry = e;
        valid = b;
        dist = valid? entry.distTo(l) : -1.0;
    }
}
