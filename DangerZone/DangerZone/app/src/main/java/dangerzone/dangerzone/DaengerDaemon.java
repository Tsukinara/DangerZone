package dangerzone.dangerzone;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DaengerDaemon extends Service {

    private Thread daengerThread;
    private LocationManager locManager;
    private LocationListener locListener;
    protected Location locCurrent;
    protected final Object monitor = new Object();

    private EntryList entries;

    private final long numSecondsPerUpdate = 5;

    public DaengerDaemon() {
        locCurrent = new Location("poi");
    }

    @Override
    public void onCreate() {
        locManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        entries = new EntryList();
    }

    @Override
    public void onDestroy() {
        daengerThread.interrupt();
        locManager.removeUpdates(locListener);
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

    public void sendDataToMain() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("refresh");

        Bundle bundle = new Bundle();
        bundle.putParcelable("data", entries);

        Location loc;
        synchronized (monitor) {
            loc = new Location(locCurrent);
        }
        bundle.putParcelable("location", loc);

        intent.putExtra("data", bundle);

        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locListener == null) {
            locListener = new LZoneListener();
        }
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
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
        private Location loc;
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                downloadWebpage(getInitURL(), true);
            }

            while (true) {
                try {
                    if (networkInfo != null && networkInfo.isConnected()) {
                        downloadWebpage(getURL(), false);
                    } else {
                        System.out.println("Errlopr");
                    }
                    synchronized (monitor) {
                        loc = new Location(locCurrent);
                    }
                    System.out.println(loc.getLatitude() + ", " + loc.getLongitude() + "\n");
					
                    createNotification();
                    downloadWebpage(getURL(), false);

                    sendDataToMain();
                    System.out.println(entries.getNear(loc, 100000).size());

                    Thread.sleep(numSecondsPerUpdate*1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private String getInitURL() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        Date endDate = new Date();
        Date startDate = new Date(endDate.getTime() - 86400000*3);

        return "http://data.octo.dc.gov/Attachment.aspx?where=Citywide&area=&what=XML&date=reportdatetime&from="
        + dateFormat.format(startDate)
        + "%2012:00:00%20AM&to="
        + dateFormat.format(endDate)
        + "%2011:59:00%20PM&dataset=ASAP&datasetid=3&whereInd=0&areaInd=0&whatInd=1&dateInd=0&whenInd=4&disposition=attachment";
    }

    private String getURL() {
        return "http://data.octo.dc.gov/feeds/crime_incidents/crime_incidents_current.xml";
    }
	
    private class LZoneListener implements LocationListener {
        public LZoneListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            synchronized (monitor) {
                locCurrent = new Location(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

    }

    private void downloadWebpage(String url, boolean init) {
        // params comes from the execute() call: params[0] is the url.
        try {
            downloadUrl(url, init);
        } catch (IOException e) {
            System.out.println("Unable to retrieve web page. URL may be invalid.");
        }
    }

    private void downloadUrl(String myurl, boolean init) throws IOException {
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
            readIt(is, len, init);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public void readIt(InputStream stream, int len, boolean init) throws IOException {
        XmlParser parser = new XmlParser();
        try {
            if (init)
                entries.addLatest(parser.parseInitial(stream));
            else
                entries.addLatest(parser.parse(stream));
            entries.expire();
        } catch (XmlPullParserException | ParseException | NumberFormatException e) {
            System.out.println("Oh no! We are doomed!");
        }
    }
}
