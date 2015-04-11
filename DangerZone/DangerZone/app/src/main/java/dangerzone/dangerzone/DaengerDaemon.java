package dangerzone.dangerzone;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

public class DaengerDaemon extends Service {

    private Thread daengerThread;

    public DaengerDaemon() {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        daengerThread.interrupt();
    }

    public void createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_ab_share_pack_mtrl_alpha)
                        .setContentTitle("Service Started")
                        .setContentText("Really.")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (daengerThread != null && daengerThread.isAlive()) {
            daengerThread.interrupt();
        }
        daengerThread = new DaengerThread();
        daengerThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class DaengerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        downloadWebpage("http://data.octo.dc.gov/feeds/crime_incidents/crime_incidents_current.xml");
                    } else {
                        System.out.println("Errlopr");
                    }

                    createNotification();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void downloadWebpage(String url) {
        // params comes from the execute() call: params[0] is the url.
        try {
            System.out.println(downloadUrl(url));
        } catch (IOException e) {
            System.out.println("Unable to retrieve web page. URL may be invalid.");
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("HttpExample", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException {
        XmlParser parser = new XmlParser();
        try {
            return parser.parse(stream).toString();
        } catch (XmlPullParserException | ParseException | NumberFormatException e) {
            return "Oh no! We are doomed!";
        }
    }
}
