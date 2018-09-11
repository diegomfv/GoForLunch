package com.example.android.goforlunch.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */
@Entity(tableName = "restaurant")
public class RestaurantEntry {

    @PrimaryKey(autoGenerate = true) // Annotate the id as PrimaryKey. Set autoGenerate to true.
    private int id;
    private String placeId;
    private String name;
    private int type;
    private String address;
    @ColumnInfo(name = "open_until")
    private String openUntil;
    private String distance;
    private String rating;
    @ColumnInfo(name = "image_url")
    private String imageUrl;
    private String phone;
    @ColumnInfo(name = "website_url")
    private String websiteUrl;
    private String latitude;
    private String longitude;

    /**
     * Used for when inserting info in the table
     */
    @Ignore
    // Use the Ignore annotation so Room knows that it has to use the other constructor instead
    public RestaurantEntry(String placeId, String name, int type, String address, String openUntil, String distance, String rating,
                           String imageUrl, String phone, String websiteUrl, String latitude, String longitude) {
        this.placeId = placeId;
        this.name = name;
        this.type = type;
        this.address = address;
        this.openUntil = openUntil;
        this.distance = distance;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Used for when reading from the table
     */
    public RestaurantEntry(int id, String placeId, String name, int type, String address, String openUntil, String distance, String rating,
                           String imageUrl, String phone, String websiteUrl, String latitude, String longitude) {
        this.id = id;
        this.placeId = placeId;
        this.name = name;
        this.type = type;
        this.address = address;
        this.openUntil = openUntil;
        this.distance = distance;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpenUntil() {
        return openUntil;
    }

    public void setOpenUntil(String openUntil) {
        this.openUntil = openUntil;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "RestaurantEntry{" +
                "id=" + id +
                ", placeId='" + placeId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", openUntil='" + openUntil + '\'' +
                ", distance='" + distance + '\'' +
                ", rating='" + rating + '\'' +
                ", imageUrl=" + imageUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
