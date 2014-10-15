package com.appglue.services;

import android.os.Bundle;

import com.appglue.ComposableService;
import com.appglue.R;
import com.appglue.description.datatypes.IOType;
import com.appglue.library.Network;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.appglue.Constants.NAME;
import static com.appglue.Constants.VALUE;

public class TubeService extends ComposableService {
    public static final IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
    public static final IOType urlType = IOType.Factory.getType(IOType.Factory.URL);
    public static final IOType imageDrawable = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);

    public static final String TAG_LINES = "lines";
    public static final String TAG_UPDATE = "update";
    public static final String TAG_NAME = "name";
    public static final String TAG_STATUS = "status";
    public static final String TAG_MESSAGES = "messages";

    public static final String GOOD_SERVICE = "good service";
    public static final String MINOR_DELAYS = "minor delays";
    public static final String SEVERE_DELAYS = "severe delays";
    public static final String PART_CLOSURE = "part closure";

    public static final String LINE_NAME = "line_name";
    public static final String LINE_STATUS = "line_status";
    public static final String LINE_MESSAGE = "line_message";
    public static final String LINE_URL = "line_url";

    public static final String LINE_ICON = "line_icon";

    public static final String BAKERLOO = "Bakerloo";
    public static final String CENTRAL = "Central";
    public static final String CIRCLE = "Circle";
    public static final String DISTRICT = "District";
    public static final String DLR = "DLR";
    public static final String HAMMERSMITH_CITY = "H'smith & City";
    public static final String JUBILEE = "Jubilee";
    public static final String METROPOLITAN = "Metropolitan";
    public static final String NORTHERN = "Northern";
    public static final String OVERGROUND = "Overground";
    public static final String PICCADILLY = "Piccadilly";
    public static final String VICTORIA = "Victoria";
    public static final String WATERLOO_CITY = "Waterloo & City";

    private String getFromURL(String url, ArrayList<Bundle> parameters) throws IOException {
        if (parameters == null) {
            return Network.httpGet(url);
        } else {
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

            for (Bundle b : parameters) {
                String name = b.getString(NAME);
                String[] values = b.getStringArray(VALUE);
                String value = "";
                for (String value1 : values) {
                    value += value1;
                }

                postData.add(new BasicNameValuePair(name, value));
            }

            return Network.httpPost(url, postData);
        }
    }

    public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) {
        String output;

        try {
            String url = "http://people.bath.ac.uk/ar289/services/tube/tube_status.php";
            output = getFromURL(url, parameters);
        } catch (ClientProtocolException e) {
            fail("Network fail: " + e.getMessage());
            return null;
        } catch (IOException e) {
            fail("Network fail: " + e.getMessage());
            return null;
        }

        return processOutput(output);
    }

    public ArrayList<Bundle> processOutput(String s) {
        try {
            JSONObject json = new JSONObject(s);
            JSONArray lines = json.getJSONArray(TAG_LINES);
            ArrayList<Bundle> deadLines = new ArrayList<Bundle>();

            for (int i = 0; i < lines.length(); i++) {
                JSONObject jsonLine = lines.getJSONObject(i);

                String lineName = jsonLine.getString(TAG_NAME);
                String status = jsonLine.getString(TAG_STATUS);

                JSONArray jsonMessages = jsonLine.getJSONArray(TAG_MESSAGES);

                String[] messages = new String[jsonMessages.length()];
                if(jsonMessages.length() == 0) {
                    messages = new String[1];
                    messages[0] = "";
                }

                for (int j = 0; j < jsonMessages.length(); j++) {
                    messages[j] = jsonMessages.getString(j);
                }

                Bundle lineBundle = new Bundle();
                text.addToBundle(lineBundle, lineName, LINE_NAME);
                text.addToBundle(lineBundle, status, LINE_STATUS);
                text.addToBundle(lineBundle, messages[0], LINE_MESSAGE);
                urlType.addToBundle(lineBundle, "http://www.google.co.uk", LINE_URL);
                imageDrawable.addToBundle(lineBundle, R.drawable.tube_basic, LINE_ICON);

                if (!status.equals(GOOD_SERVICE))
                    deadLines.add(lineBundle);
            }

            if (deadLines.size() == 0) {
                Bundle lineBundle = new Bundle();
                text.addToBundle(lineBundle, "Bakerloo", LINE_NAME);
                text.addToBundle(lineBundle, MINOR_DELAYS, LINE_STATUS);
                text.addToBundle(lineBundle, "", LINE_MESSAGE);
                urlType.addToBundle(lineBundle, "http://www.google.co.uk", LINE_URL);
                imageDrawable.addToBundle(lineBundle, R.drawable.tube_basic, LINE_ICON);
                deadLines.add(lineBundle);
            }

            isList = true;

            return deadLines;
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Parsing error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) {
        if (os.size() > 0)
            return performService(os.get(0), parameters);
        else
            return null;
    }

}
