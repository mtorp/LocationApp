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
import java.util.jar.Attributes;

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


    public void startFetchLocationTask(String url) {
        new FetchLocationsTask().execute(url);
    }

    public void startPostLocationTask(String url, Location location) {
        new PostLocationTask().execute(url, location);
    }

    public void startFetchLocationTaskWithParams(String url, List<NameValuePair> params) {
        new FetchLocationTaskWithParams().execute(url, params);
    }


    private class FetchLocationsTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(params[0]);
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

    private class PostLocationTask extends AsyncTask <Object, Void, Void> {

        /**
         *
         * @param params
         * First parameter is the url for which the location needs to be uploaded
         * Second parameter is the location to upload
         * @return
         */
        @Override
        protected Void doInBackground(Object... params) {
            //params, should be location to upload
            uploadLocation((String) params[0], (Location) params[1]);
            return null;
        }
    }

    private class FetchLocationTaskWithParams extends AsyncTask<Object, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object... params) {
            return downloadURlWithParams((String) params[0], (List<NameValuePair>) params[1]);
        }

        protected void onPostExecute(JSONObject jsonObject) {

            //TODO Implement me!


        }
    }


    /**
     *  Code from StackOverflow
     *  Method to upload a provided to location to the database
     */

    private void uploadLocation(String urlParam, Location location) {


        MapsActivity context = (MapsActivity) mapContext;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceID", context.getDeviceID()));
        params.add(new BasicNameValuePair("name", context.getName()));
        params.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
        params.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));

        postToServer(urlParam, params);




    }

    /**
     *
     * @param params Parameters to send to server
     * @return
     */

    private boolean postToServer(String urlParam, List<NameValuePair> params) {
        try {

            URL u = new URL(urlParam);
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




    private JSONObject downloadUrl(String urlParam) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        try {
            URL url = new URL(urlParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
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

    private JSONObject downloadURlWithParams(String urlParam, List<NameValuePair> params) {
        InputStream is = null;
        try {

            URL u = new URL(urlParam);
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


            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            String result = sb.toString();
            result = "{Locations:"+result+"}";
            conn.disconnect();
            return new JSONObject(result);



        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
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

}
