package com.location.torp.locationapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        startBlueTooth();
    }

    protected void onResume() {
        super.onResume();
        startBlueTooth();

    }

    private void startBlueTooth () {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not working", Toast.LENGTH_SHORT).show();
        } else {
            listView = (ListView) findViewById(R.id.listView);
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(arrayAdapter);
        }

        arrayAdapter.clear();
        bluetoothAdapter.startDiscovery();
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Clicked: " + value, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("name", value);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayAdapter.add(device.getName());
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        bluetoothAdapter.cancelDiscovery();
    }
}
