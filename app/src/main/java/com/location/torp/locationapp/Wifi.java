package com.location.torp.locationapp;

/**
 * Created by JoachimSkov on 09/10/2014.
 */
public class Wifi {

    private String SSID;
    private String BSSID;

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getSSID() {

        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }
}
