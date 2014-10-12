package com.location.torp.locationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class NotificationActivity extends Activity {

    public final static String NOTIFICATION_UP = "http://androidapp.torpforsikring.dk/insertNotification.php";
    public final static String NOTIFICATION_DELETE = "http://androidapp.torpforsikring.dk/deleteNotification.php";

    private NotificationList notifications;

    private String deviceID;

    private ListView listView;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Bundle extras = getIntent().getExtras();
        deviceID = extras.getString("deviceID");
        Log.d("note", deviceID);

        notifications = new NotificationList(deviceID);

        listView = (ListView) findViewById(R.id.notificationListView);

        adapter = new NotificationAdapter(getApplicationContext(), notifications.getNotifications());

        listView.setAdapter(adapter);

        registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification, menu);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Edit notification");
        menu.add(0, v.getId(), 0, "Delete notification");

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        String title = item.getTitle().toString();

        if (title.equals("Edit notification")) {

            editNotification(notifications.getNotifications().get(index));

        } else if(title.equals("Delete notification")) {


            deleteNotification(notifications.getNotifications().get(index));

            Toast.makeText(getApplicationContext(), "Notifications deleted!", Toast.LENGTH_SHORT).show();

        }


        return false;
    }

    public void editNotification(final Notification notification){



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

                uploadEditNotification(inputMessage.getText().toString(), notification.getBSSID(), sound.isChecked(), mobileData.isChecked());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void uploadEditNotification(String message, String BSSID, Boolean sound, Boolean mobileData) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("message", message));
        params.add(new BasicNameValuePair("BSSID", BSSID));
        params.add(new BasicNameValuePair("sound", sound.toString()));
        params.add(new BasicNameValuePair("mobileData", mobileData.toString()));
        params.add(new BasicNameValuePair("deviceID", deviceID));

        WebUtils.startPostParamsTask(NOTIFICATION_UP, params);
        notifications.downloadNotifications(deviceID);

        finish();
        startActivity(getIntent());
    }



    public void deleteNotification(Notification notification) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("BSSID", notification.getBSSID()));
        params.add(new BasicNameValuePair("deviceID", deviceID));

        WebUtils.startPostParamsTask(NOTIFICATION_DELETE, params);

        notifications.downloadNotifications(deviceID);
        finish();
        startActivity(getIntent());
    }
}