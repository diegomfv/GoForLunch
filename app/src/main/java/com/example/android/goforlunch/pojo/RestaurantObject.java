package com.example.android.goforlunch.pojo;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantObject {

    String name;
    String address;
    String timetable;
    String distance;
    String coworkersJoining;
    String rating;
    String image_url;

    public RestaurantObject(String name, String address, String timetable, String distance, String coworkersJoining, String rating, String image_url) {
        this.name = name;
        this.address = address;
        this.timetable = timetable;
        this.distance = distance;
        this.coworkersJoining = coworkersJoining;
        this.rating = rating;
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getTimetable() {
        return timetable;
    }

    public String getDistance() {
        return distance;
    }

    public String getCoworkersJoining() {
        return coworkersJoining;
    }

    public String getRating() {
        return rating;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTimetable(String timetable) {
        this.timetable = timetable;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setCoworkersJoining(String coworkersJoining) {
        this.coworkersJoining = coworkersJoining;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @Override
    public String toString() {
        return "RestaurantObject{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", timetable='" + timetable + '\'' +
                ", distance='" + distance + '\'' +
                ", coworkersJoining='" + coworkersJoining + '\'' +
                ", rating='" + rating + '\'' +
                ", image_url='" + image_url + '\'' +
                '}';
    }


}
