package com.example.android.goforlunch.pojo_delete;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantObject {

    String placeId;
    String name;
    String address;
    String timetable;
    String distance;
    String coworkersJoining;
    String rating;
    String image_url;
    String phone;
    String website;

    public RestaurantObject() {

    }

    public RestaurantObject(String placeId, String name, String address, String timetable, String distance, String coworkersJoining, String rating, String image_url, String phone, String website) {
        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.timetable = timetable;
        this.distance = distance;
        this.coworkersJoining = coworkersJoining;
        this.rating = rating;
        this.image_url = image_url;
        this.phone = phone;
        this.website = website;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
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

    public String getTimetable() {
        return timetable;
    }

    public void setTimetable(String timetable) {
        this.timetable = timetable;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCoworkersJoining() {
        return coworkersJoining;
    }

    public void setCoworkersJoining(String coworkersJoining) {
        this.coworkersJoining = coworkersJoining;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "RestaurantObject{" +
                "placeId=" + placeId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", timetable='" + timetable + '\'' +
                ", distance='" + distance + '\'' +
                ", coworkersJoining='" + coworkersJoining + '\'' +
                ", rating='" + rating + '\'' +
                ", image_url='" + image_url + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
