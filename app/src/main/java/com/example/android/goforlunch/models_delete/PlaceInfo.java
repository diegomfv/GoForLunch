package com.example.android.goforlunch.models_delete;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Diego Fajardo on 10/05/2018.
 */
public class PlaceInfo {

    private String id;
    private String name;
    private String address;
    private String attributions;
    private String phoneNumber;
    private String timetable;
    private Uri websiteUri;
    private LatLng latLng;
    private float rating;

    public PlaceInfo() {
    }

    public PlaceInfo(String id, String name, String address, String attributions, String phoneNumber, String timetable, Uri websiteUri, LatLng latLng, float rating) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.attributions = attributions;
        this.phoneNumber = phoneNumber;
        this.timetable = timetable;
        this.websiteUri = websiteUri;
        this.latLng = latLng;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTimetable() {
        return timetable;
    }

    public void setTimetable(String timetable) {
        this.timetable = timetable;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", attributions='" + attributions + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", timetable='" + timetable + '\'' +
                ", websiteUri=" + websiteUri +
                ", latLng=" + latLng +
                ", rating=" + rating +
                '}';
    }
}
