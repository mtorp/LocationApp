package com.location.torp.locationapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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
import java.net.ProtocolException;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class Bluetooth extends FragmentActivity  {

    private static final String DEBUG_TAG = "HttpBluetooth";
    private String bluetooth_URL = "http://androidapp.torpforsikring.dk/getLike.php";
    private Activity activity;

    public Bluetooth(Activity activity){
        this.activity = activity;
    }


    public void postBluetoothName(String name) {
        new postBluetoothNameTask().execute(name);
    }

    private class postBluetoothNameTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            return uploadBluetoothName(strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    Log.d(DEBUG_TAG, "did i get this far");

                    JSONArray jsonArray = jsonObject.getJSONArray("Locations");
                    JSONObject obj = jsonArray.getJSONObject(0);
                    double latitude = obj.getDouble("latitude");
                    double longitude = obj.getDouble("longitude");
                    String title = obj.getString("name");

                    Log.d(DEBUG_TAG, "name: " + title + " Latitude " + latitude + " longitude " + longitude);

                    returnToMapWithResult(title, latitude, longitude);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(DEBUG_TAG, "jsonobject is null");
            }
        }


    }

    private JSONObject uploadBluetoothName(String name) {
        InputStream is;

        Log.d(DEBUG_TAG, "Posting bluetooth name: " + name);

        try {
            URL url = new URL(bluetooth_URL);
            HttpURLConnection conn = (HttpURLConnection)
            url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nameLike", name));

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

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
            Log.d(DEBUG_TAG, result);
            is.close();

            return new JSONObject(result);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void returnToMapWithResult(String title, double latitude, double longitude) {
        Log.d(DEBUG_TAG, "Returning activity");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("title", title);
        returnIntent.putExtra("latitude", latitude);
        returnIntent.putExtra("longitude", longitude);

        if(activity.getParent() == null) {
            setResult(RESULT_OK, returnIntent);
        } else {
            activity.getParent().setResult(RESULT_OK, returnIntent);
        }

        finish();
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
