package com.location.torp.locationapp;

/**
 * Created by JoachimSkov on 11/10/2014.
 */
public class Notification {
    private String message;
    private String SSID;
    private String BSSID;
    private boolean mobileData;
    private boolean sound;


    public String getMessage() {
        return message;
    }

    public String getSSID() {
        return SSID;
    }

    public boolean isMobileData() {
        return mobileData;
    }

    public boolean isSound() {
        return sound;
    }

    public String getBSSID(){
        return BSSID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setMobileData(boolean mobileData) {
        this.mobileData = mobileData;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

}
