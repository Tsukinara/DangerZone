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

    public List<Entry> parseInitial(InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readInitial(parser);
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

    private List<Entry> readInitial(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        List<Entry> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "dcst:ReportedCrimes");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("dcst:ReportedCrime")) {
                entries.add(readDcst(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
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
                output = readContent(parser);
            } else {
                skip(parser);
            }
        }
        return output;
    }

    private Entry readContent(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Entry output = null;

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

    private Entry readDcst(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Entry output = new Entry();

        parser.require(XmlPullParser.START_TAG, ns, "dcst:ReportedCrime");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "dcst:ccn":
                    output.ccn = readCcn(parser); break;
                case "dcst:reportdatetime":
                    output.reportdatetime = readDateTime(parser); break;
                case "dcst:offense":
                    output.offense = readOffense(parser); break;
                case "dcst:method":
                    output.method = readMethod(parser); break;
                case "dcst:blocksiteaddress":
                    output.blocksiteaddress = readSiteAddress(parser); break;
                case "dcst:blockxcoord":
                    output.blockxcoord = readXCoord(parser); break;
                case "dcst:blockycoord":
                    output.blockycoord = readYCoord(parser); break;
                default:
                    skip(parser); break;
            }
        }

        output.convertCoordinates();
        return output;
    }

    private int readCcn(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:ccn");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:ccn");
        return Integer.parseInt(str);
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

    private String readMethod(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dcst:method");
        String str = removeCData(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "dcst:method");
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
