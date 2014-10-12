package com.location.torp.locationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;

public class WifiAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<Wifi> wifis;

    public WifiAdapter(Context context, ArrayList<Wifi> wifis) {
        this.context = context;
        this.wifis = wifis;
    }

    @Override
    public int getCount() {
        return wifis.size();
    }

    @Override
    public Object getItem(int i) {
        return wifis.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TwoLineListItem twoLineListItem;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) view;
        }

        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();

        text1.setText(wifis.get(i).getSSID());
        text2.setText("" + wifis.get(i).getBSSID());

        return twoLineListItem;
    }
}
