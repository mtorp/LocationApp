package com.location.torp.locationapp;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by torp on 20/09/14.
 */
public class WebUtils extends FragmentActivity {
    private static final String DEBUG_TAG = "HttpExample";
    private Context mapContext;
    private GoogleMap mMap;

    public WebUtils(Context context, GoogleMap map) {
        mapContext = context;
        mMap = map;
    }


    public void startFetchLocationTask() {
        new FetchLocationsTask().execute();
    }

    public void startPostLocationTask(Location location) {
        new PostLocationTask().execute(location);
    }


    private class FetchLocationsTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (mMap != null && jsonObject != null) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("Locations");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        double latitude = obj.getDouble("latitude");
                        double longitude = obj.getDouble("longitude");
                        String title = obj.getString("name");
                        float color =  (new BigInteger(obj.getString("deviceID"), 16).floatValue()%360);
                        MarkerOptions m = new MarkerOptions();
                        m.title(title);
                        m.icon(BitmapDescriptorFactory.defaultMarker(color));
                        m.position(new LatLng(latitude, longitude));
                        mMap.addMarker(m);

                    }
                    Toast.makeText(mapContext, "Locations added", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }


    }

    private class PostLocationTask extends AsyncTask <Location, Void, Void> {


        @Override
        protected Void doInBackground(Location... locations) {
            //params, should be location to upload
            uploadLocation(locations[0]);
            return null;
        }
    }


    /**
     *  Code from StackOverflow
     *  Method to upload a provided to location to the database
     */

    private void uploadLocation(Location location) {


        MapsActivity context = (MapsActivity) mapContext;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceID", context.getDeviceID()));
        params.add(new BasicNameValuePair("name", context.getName()));
        params.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
        params.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));

        postToServer(params);




    }

    /**
     *
     * @param params Parameters to send to server
     * @return
     */

    private boolean postToServer(List<NameValuePair> params) {
        try {

            URL u = new URL(MapsActivity.UP_URL);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.connect();


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();


            int response = conn.getResponseCode();
            conn.disconnect();
            return true;


        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    //Code from StackOverflow
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private JSONObject downloadUrl() throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        try {
            URL url = new URL(MapsActivity.DOWN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            String result = sb.toString();
            result = "{Locations:"+result+"}";
            return new JSONObject(result);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }


}
