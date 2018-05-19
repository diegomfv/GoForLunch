package com.example.android.goforlunch.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */

@Entity (tableName = "restaurant")
public class RestaurantEntry {

    @PrimaryKey (autoGenerate = true) // Annotate the id as PrimaryKey. Set autoGenerate to true.
    private String id;
    private String placeId;
    private String type;
    private String address;
    @ColumnInfo(name = "open_until")
    private String openUntil;
    private String distance;
    private String rating;
    @ColumnInfo(name = "image_url")
    private String imageUrl;

    /** Used for when inserting info in the table
     * */
    @Ignore  // Use the Ignore annotation so Room knows that it has to use the other constructor instead
    public RestaurantEntry(String placeId, String type, String address, String openUntil, String distance, String rating, String imageUrl) {
        this.placeId = placeId;
        this.type = type;
        this.address = address;
        this.openUntil = openUntil;
        this.distance = distance;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    /** Used for when reading from the table
     * */
    public RestaurantEntry(String id, String placeId, String type, String address, String openUntil, String distance, String rating, String imageUrl) {
        this.id = id;
        this.placeId = placeId;
        this.type = type;
        this.address = address;
        this.openUntil = openUntil;
        this.distance = distance;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }




}
