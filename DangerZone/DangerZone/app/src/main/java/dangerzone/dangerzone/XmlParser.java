package dangerzone.dangerzone;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Patrick on 4/11/2015
 */
public class XmlParser {
    private static final String ns = null;

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        List<Entry> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    public static class Entry {
        public Content content;

        public String toString() {
            return content.toString();
        }
    }

    public static class Content {
        public Date reportdatetime;
        public String offense;
        public String blocksiteaddress;
        public double blockxcoord;
        public double blockycoord;
        public double latitude;
        public double longitude;

        public Content() {
            blockxcoord = 350000; blockycoord=160000;
            convertCoordinates();
        }

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
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append(reportdatetime + ", ");
            builder.append(offense + ", ");
            builder.append(blocksiteaddress + ", ");
            builder.append(latitude + ", ");
            builder.append(longitude + "}");

            return builder.toString();
        }
    }

    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Entry output = new Entry();

        parser.require(XmlPullParser.START_TAG, ns, "entry");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("content")) {
                output.content = readContent(parser);
            } else {
                skip(parser);
            }
        }
        return output;
    }

    private Content readContent(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Content output = null;

        parser.require(XmlPullParser.START_TAG, ns, "content");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("dcst:ReportedCrime")) {
                output = readDcst(parser);
            } else {
                skip(parser);
            }
        }

        return output;
    }

    private Content readDcst(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Content output = new Content();

        parser.require(XmlPullParser.START_TAG, ns, "dcst:ReportedCrime");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("dcst:reportdatetime")) {
                output.reportdatetime = readDateTime(parser);
            } else if (name.equals("dcst:offense")) {
                output.offense = readOffense(parser);
            } else if (name.equals("dcst:blocksiteaddress")) {
                output.blocksiteaddress = readSiteAddress(parser);
            } else if (name.equals("dcst:blockxcoord")) {
                output.blockxcoord = readXCoord(parser);
            } else if (name.equals("dcst:blockycoord")) {
                output.blockycoord = readYCoord(parser);
            } else {
                skip(parser);
            }
        }

        output.convertCoordinates();
        return output;
    }

    private Date readDateTime(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:reportdatetime");
        String str = removeCData(readText(parser));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US);
        Date date = format.parse(str);
        parser.require(XmlPullParser.END_TAG, ns, "dcst:reportdatetime");
        return date;
    }

    private String readOffense(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:offense");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:offense");
        return str;
    }

    private String readSiteAddress(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:blocksiteaddress");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:blocksiteaddress");
        return str;
    }

    private double readXCoord(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:blockxcoord");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:blockxcoord");
        return Double.parseDouble(str);
    }

    private double readYCoord(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:blockycoord");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:blockycoord");
        return Double.parseDouble(str);
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String removeCData(String str) {
        return str;
    }
}
