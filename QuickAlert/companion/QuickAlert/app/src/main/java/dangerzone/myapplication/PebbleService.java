package dangerzone.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleService extends Service {

    private SharedPreferences sp;
    private BroadcastReceiver pdr;

    public PebbleService() {
        super();
    }

    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("dangerzone", Context.MODE_PRIVATE);
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
                        number = "tel:3012817202";

                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(number));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                    startActivity(intent);
                }
            }
        });
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        if(pdr!=null)
            unregisterReceiver(pdr);
        super.onDestroy();
    }
}
