package dangerzone.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleService extends Service {

    private SharedPreferences sp;
    private LocationManager locManager;
    private LocationListener locListener;
    private Location loc;
    private BroadcastReceiver pdr;

    public PebbleService() {
        super();
    }

    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("dangerzone", Context.MODE_PRIVATE);
        locManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        pdr = PebbleKit.registerReceivedDataHandler(getApplicationContext(), new PebbleKit.PebbleDataReceiver(Constants.PEBBLE_APP_UUID) {
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
                if(data.contains(0)){
                    PebbleDictionary pd = new PebbleDictionary();
                    pd.addUint8(0,(byte) (sp.getInt("countdown", -1) + 5));
                    pd.addString(1, String.valueOf(sp.getInt("passcode", -1)));
                    PebbleKit.sendDataToPebble(getApplicationContext(),Constants.PEBBLE_APP_UUID,pd);
                }
                else{
                    String number;
                    if (sp.contains("thechosenphone"))
                        number = sp.getString("thechosenphone", "");
                    else
                        number = "3012817202";
                    SmsManager sms = SmsManager.getDefault();
                    Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
                    c.moveToFirst();
                    sms.sendTextMessage("sms:" + number, null,
                            "My name is " + c.getString(c.getColumnIndex("display_name")) + ".\nI am in an emergency situation!\nPlease send help to:\n(" + loc.getLatitude() + "," + loc.getLongitude() + ")", null, null);
                    Intent call_intent = new Intent(Intent.ACTION_CALL);
                    call_intent.setData(Uri.parse("tel:" + number.toString().trim()));
                    call_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    call_intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                    startActivity(call_intent);
                }
            }
        });
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locListener == null) {
            locListener = new LZoneListener();
        }
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LZoneListener implements LocationListener {
        public LZoneListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            loc = new Location(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

    }

    public void onDestroy() {
        if(pdr!=null)
            unregisterReceiver(pdr);
        locManager.removeUpdates(locListener);
        super.onDestroy();
    }
}
