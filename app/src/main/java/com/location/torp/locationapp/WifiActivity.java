package com.location.torp.locationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class WifiActivity extends Activity {
    public final static String WIFI_DOWN = "http://androidapp.torpforsikring.dk/getWifi.php";
    public final static String NOTIFICATION_UP = "http://androidapp.torpforsikring.dk/insertNotification.php";

    private ListView listView;
    private WifiAdapter arrayAdapter;

    public static Wifi wifi;

    private ArrayList<Wifi> wifis;

    private String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        Bundle extras = getIntent().getExtras();
        deviceID = extras.getString("deviceID");
        Log.d("wifi", deviceID);

        listView = (ListView) findViewById(R.id.wifiListView);

        getActionBar().setTitle("Wifi networks");

        wifis = new ArrayList<Wifi>();
        arrayAdapter = new WifiAdapter(this, wifis);
        listView.setAdapter(arrayAdapter);

        getWifi();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                Wifi wifi = (Wifi) adapter.getItemAtPosition(position);
                makeNewNotification(wifi);
            }
        });
    }

    private void makeNewNotification(Wifi w) {
        final Wifi wifi = w;

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Make new notification");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputMessage = new EditText(this);
        inputMessage.setHint("Insert message here");
        layout.addView(inputMessage);

        final Switch sound = new Switch(this);
        sound.setText("Sound");
        layout.addView(sound);

        final Switch mobileData = new Switch(this);
        mobileData.setText("Mobile data");
        layout.addView(mobileData);

        alert.setView(layout);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("wifi", "did i get this far");

                inputMessage.getText().toString();
                wifi.getBSSID();

                uploadNewNotification(inputMessage.getText().toString(), wifi.getBSSID(), sound.isChecked(), mobileData.isChecked());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void getWifi() {
        Log.d("wifi", "Downloading wifi from server");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceID", deviceID));

        JSONObject jsonObject = WebUtils.startFetchParamsTask(WIFI_DOWN, params);


        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("Locations");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Wifi wifi = new Wifi();
                wifi.setSSID(obj.getString("SSID"));
                wifi.setBSSID(obj.getString("BSSID"));

                wifis.add(wifi);

                arrayAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void uploadNewNotification(String message, String BSSID, Boolean sound, Boolean mobileData) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("message", message));
        params.add(new BasicNameValuePair("BSSID", BSSID));
        params.add(new BasicNameValuePair("sound", sound.toString()));
        params.add(new BasicNameValuePair("mobileData", mobileData.toString()));
        params.add(new BasicNameValuePair("deviceID", deviceID));

        WebUtils.startPostParamsTask(NOTIFICATION_UP, params);
    }

    protected void onResume() {


        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
