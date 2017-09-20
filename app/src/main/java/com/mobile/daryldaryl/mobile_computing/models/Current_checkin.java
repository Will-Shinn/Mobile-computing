package com.mobile.daryldaryl.mobile_computing.models;

/**
 * Created by liboa on 5/09/2017.
 */

public class Current_checkin {
    private double lat;
    private double lng;
    private long time;

    public Current_checkin() {
    }

    public Current_checkin(double lat, double lng, long time) {
        this.lat = lat;
        this.lng = lng;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
