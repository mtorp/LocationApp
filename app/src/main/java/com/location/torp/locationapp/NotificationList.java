package com.location.torp.locationapp;

import android.app.Application;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JoachimSkov on 11/10/2014.
 */
public class NotificationList {

    public final static String NOTIFICATIONS_DOWN = "http://androidapp.torpforsikring.dk/getNotifications.php";

    private static ArrayList<Notification> notifications = new ArrayList<Notification>();

    public NotificationList(String deviceID) {
        notifications = downloadNotifications(deviceID);
    }

    public static ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public static void setNotifications(ArrayList<Notification> note) {
        notifications = note;
    }

    public ArrayList<Notification> downloadNotifications(String deviceID){

        ArrayList<Notification> result = new ArrayList<Notification>();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceID", deviceID));

        JSONObject jsonObject = WebUtils.startFetchParamsTask(NOTIFICATIONS_DOWN, params);

        Log.i("note", jsonObject.toString());

        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("Locations");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Notification notification = new Notification();

                notification.setSSID(obj.getString("SSID"));
                notification.setBSSID(obj.getString("BSSID"));
                notification.setMobileData(obj.getBoolean("mobileData"));
                notification.setSound(obj.getBoolean("sound"));

                notification.setMessage(obj.getString("message"));

                result.add(notification);

                Log.i("note", "Notification.sound is " + notification.isSound() + "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }
}




