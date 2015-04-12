package dangerzone.dangerzone;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DaengerDaemon extends Service {

    private Thread daengerThread;
    private LocationManager locManager;
    private LocationListener locListener;
    protected Location locCurrent;
    protected final Object monitor = new Object();
    protected final Object entriesMonitor = new Object();

    private EntryList entries;

    private DataUpdateReceiver dataUpdateReceiver;
    private boolean done;

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("service_settings")) {
                if (intent.hasExtra("update_time")) {
                    numSecondsPerUpdate = intent.getIntExtra("update_time", numSecondsPerUpdate);
                    forceUpdate();
                }
                if (intent.hasExtra("refresh")) {
                    forceUpdate();
                }
                radius = intent.getDoubleExtra("radius", radius);
                if (intent.hasExtra("days")) {
                    days = intent.getIntExtra("days", days);
                    hasMostData = false;
                    forceUpdate();
                }
                if (intent.hasExtra("stop")) {
                    stopSelf();
                }
            }
        }
    }

    private boolean hasMostData = false;

    private int numSecondsPerUpdate = 5;
    private double radius = 100000;
    private int days = 3;

    public DaengerDaemon() {
        locCurrent = new Location("poi");
    }

    @Override
    public void onCreate() {
        locManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        entries = new EntryList();

        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("service_settings");
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        daengerThread.interrupt();
        locManager.removeUpdates(locListener);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        if (dataUpdateReceiver != null) bm.unregisterReceiver(dataUpdateReceiver);

        done = true;
        daengerThread.interrupt();
    }

    public void createNotification(List<Entry> data) {
        double minDistance=radius, maxDistance=0;

        Location loc;
        synchronized (monitor) {
            loc = new Location(locCurrent);
        }

        for (Entry e : data) {
            float[] result = new float[1];
            Location.distanceBetween(e.latitude, e.longitude, loc.getLatitude(), loc.getLongitude(), result);
            if (result[0] < minDistance)
                minDistance = result[0];
            if (result[0] > maxDistance)
                maxDistance = result[0];
        }
        minDistance = Math.floor(minDistance);
        maxDistance = Math.floor(maxDistance);

        String distanceStr;
        if (minDistance == maxDistance)
            distanceStr = (int)minDistance + " meters away";
        else
            distanceStr = (int)minDistance + " to " + (int)maxDistance + " meters away";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notif_icon_hq)
                        .setContentTitle("Warning")
                        .setContentText("Potentially dangerous area ahead. Please consider activating QuickAlert.")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent resultIntent = new Intent(this, ListActivity.class);

        resultIntent.putExtra("entries", entries);
        resultIntent.putExtra("location", loc);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(ListActivity.class);

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

    public void sendDataToMain() {
        synchronized (entriesMonitor) {
            Intent intent = new Intent("refresh");

            Bundle bundle = new Bundle();
            bundle.putParcelable("data", entries);

            Location loc;
            synchronized (monitor) {
                loc = new Location(locCurrent);
            }
            bundle.putParcelable("location", loc);

            intent.putExtra("data", bundle);

            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locListener == null) {
            locListener = new LZoneListener();
        }
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        if (daengerThread == null || !daengerThread.isAlive()) {
            daengerThread = new DaengerThread();
            daengerThread.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void queryWebpage() {
        synchronized (entriesMonitor) {
            if (hasMostData) {
                downloadWebpage(getURL(), false);
            } else {
                downloadWebpage(getInitURL(), true);
            }
            hasMostData = true;
        }
    }

    private synchronized void forceUpdate() {
        if (daengerThread != null && daengerThread.isAlive()) {
            daengerThread.interrupt();
        }
    }

    private class DaengerThread extends Thread {
        private Location loc;
        @Override
        public void run() {
            while (!done) {
                try {
                    queryWebpage();
                    synchronized (monitor) {
                        loc = new Location(locCurrent);
                    }
                    System.out.println(loc.getLatitude() + ", " + loc.getLongitude() + "\n");

                    List<Entry> local = entries.getNear(loc, radius);
                    if (local.size() > 0) {
                        createNotification(local);
                        entries.cover(local);
                    }

                    sendDataToMain();
                    synchronized (entriesMonitor) {
                        entries.getNear(loc, radius);
                    }

                    Thread.sleep(numSecondsPerUpdate*1000);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    private String getInitURL() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        Date endDate = new Date();
        Date startDate = new Date(endDate.getTime() - 86400000*days);

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
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                downloadUrl(url, init);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
        }
    }

    private void downloadUrl(String myurl, boolean init) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            readIt(is, init);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public void readIt(InputStream stream, boolean init) throws IOException {
        XmlParser parser = new XmlParser();
        try {
            if (init)
                entries.addLatest(parser.parseInitial(stream));
            else
                entries.addLatest(parser.parse(stream));
            entries.expire(days);
        } catch (XmlPullParserException | ParseException | NumberFormatException e) {
            System.out.println("Oh no! We are doomed!");
        }
    }
}
