package com.mobile.daryldaryl.mobile_computing.models;


public class Place {

    private String name;
    private double lat;
    private double lng;
    private String vicinity;

    public Place() {

    }

    public Place(String name, double lat, double lng, String vicinity) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.vicinity = vicinity;
    }

    public Place(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
