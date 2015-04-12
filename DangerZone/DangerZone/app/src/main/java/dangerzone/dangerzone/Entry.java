package dangerzone.dangerzone;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Patrick on 4/11/2015
 */
public class Entry implements Parcelable {
    public int ccn;
    public Date reportdatetime;
    public String offense;
    public String method;
    public String blocksiteaddress;
    public double blockxcoord;
    public double blockycoord;
    public double latitude;
    public double longitude;

    public Entry() {

    }

    //Parcels
    public static final Parcelable.Creator<Entry> CREATOR
            = new Parcelable.Creator<Entry>() {
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public Entry(Parcel in) {
        ccn = in.readInt();
        reportdatetime = new Date(in.readLong());
        offense = in.readString();
        method = in.readString();
        blocksiteaddress = in.readString();
        blockxcoord = in.readDouble();
        blockycoord = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ccn);
        dest.writeLong(reportdatetime.getTime());
        dest.writeString(offense);
        dest.writeString(method);
        dest.writeString(blocksiteaddress);
        dest.writeDouble(blockxcoord);
        dest.writeDouble(blockycoord);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public int describeContents() {
        return 0;
    }

    //Affine transformation for approximation

    //387000,125000 -> 38.792656145954,-77.1496413967816
    //405000,125000 -> 38.7927379317937,-76.9424455726982
    //387000,146000 -> 38.9818317605639,-77.1500386930623

    //18000,0 -> 8.178583970419595e-5, 0.2071958240833993
    //0,21000 -> 0.1891756146099013, -3.972962806955138e-4

    //1,0 -> 4.54365776134422e-9, 1.151087911574441e-5
    //0,1 -> 9.00836260047149e-6, -1.891887050931018e-8

    public void convertCoordinates() {
        double translatedxcoord = blockxcoord - 387000;
        double translatedycoord = blockycoord - 125000;
        double dlat = 4.54365776134422e-9*translatedxcoord + 9.00836260047149e-6*translatedycoord;
        double dlong = 1.151087911574441e-5*translatedxcoord - 1.891887050931018e-8*translatedycoord;

        latitude = dlat + 38.792656145954;
        longitude = dlong - 77.1496413967816;
    }

    public String toString() {
        return "{" +
                ccn + ", " +
                reportdatetime + ", " +
                offense + ", " +
                method + ", " +
                blocksiteaddress + ", " +
                latitude + ", " +
                longitude + "}";
    }
}
