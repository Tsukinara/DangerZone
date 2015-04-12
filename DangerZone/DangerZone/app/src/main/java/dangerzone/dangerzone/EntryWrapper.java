package dangerzone.dangerzone;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class EntryWrapper implements Parcelable{
    public Entry entry;
    public double dist;
    public boolean valid;
    public EntryWrapper(Entry e, Location l, boolean b){
        super();
        entry = e;
        valid = b;
        dist = valid? entry.distTo(l) : -1.0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
