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

    //Parcels
    public static final Parcelable.Creator<EntryWrapper> CREATOR
            = new Parcelable.Creator<EntryWrapper>() {
        public EntryWrapper createFromParcel(Parcel in) {
            return new EntryWrapper(in);
        }

        public EntryWrapper[] newArray(int size) {
            return new EntryWrapper[size];
        }
    };

    public EntryWrapper(Parcel in) {
        entry = in.readParcelable(Entry.class.getClassLoader());
        dist = in.readDouble();
        valid = in.readByte() != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(entry, flags);
        dest.writeDouble(dist);
        dest.writeByte(valid ? (byte)1 : (byte)0);
    }

    public int describeContents() {
        return 0;
    }
}
