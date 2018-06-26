package com.example.android.goforlunch.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */

/** Data Access Object
 * */

/** LiveData runs, by default, outside of the main thread.
 * We use LiveDate to observe changes in the Database; for other
 * operations such as insert, updateItem or delete we do not need
 * to observe changes in the database. For those operations,
 * we will not use LiveData and therefore we will keep using the
 * executors.
 * With LiveDate, we will get notified when there are changes
 * in the database.
 * */

/** A method, annotated with @Insert can return a long.
 * This is the newly generated ID for the inserted row.
 * A method, annotated with @Update can return an int.
 * This is the number of updated rows.
 *
 * @Update will try to updateItem all your fields using the value of the primary key in a where clause.
 * If your entity is not persisted in the database yet,
 * the updateItem query will not be able to find a row and will not updateItem anything.
 *
 * You can use @Insert(onConflict = OnConflictStrategy.REPLACE). This will try
 * to insert the entity and, if there is an existing row that has the same ID value,
 * it will delete it and replace it with the entity you are trying to insert.
 * Be aware that, if you are using auto generated IDs, this means that the the resulting row will
 * have a different ID than the original that was replaced. If you want to preserve the ID,
 * then you have to come up with a custom way to do it.*/

@Dao
public interface RestaurantDao {

    // -------------------
    // READ
    // -------------------

    @Query("SELECT * FROM restaurant ORDER BY distance")
    LiveData<List<RestaurantEntry>> getAllRestaurants();

    @Query("SELECT * FROM restaurant WHERE type = :type")
    List<RestaurantEntry> getAllRestaurantsByType(String type);

    @Query("SELECT * FROM restaurant WHERE placeId = :placeId")
    RestaurantEntry getRestaurantById(String placeId);

    @Query("SELECT * FROM restaurant WHERE name = :name")
    RestaurantEntry getRestaurantByName(String name);

    @Query("SELECT * FROM restaurant ORDER BY distance")
    List<RestaurantEntry> getAllRestaurantsNotLiveDataOrderDistance();

    @Query("SELECT * FROM restaurant ORDER BY placeId")
    List<RestaurantEntry> getAllRestaurantsNotLiveDataOrderPlaceId();

    @Query("SELECT * FROM restaurant WHERE placeId = :placeId")
    LiveData<RestaurantEntry> getRestaurantByPlaceId (String placeId);

    @Query("SELECT * FROM restaurant ORDER BY distance")
    Maybe<List<RestaurantEntry>> getAllRestaurantsRxJava();

    @Query("SELECT * FROM restaurant WHERE type = :type")
    Maybe<List<RestaurantEntry>> getAllRestaurantsByTypeRxJava(String type);


    // -------------------
    // INSERT
    // -------------------

    @Insert
    long insertRestaurant (RestaurantEntry restaurantEntry);

    // -------------------
    // UPDATE
    // -------------------

    @Update (onConflict = OnConflictStrategy.REPLACE) //Replaces the element in case of conflict
    int updateRestaurantGeneralInfo(RestaurantEntry restaurantEntry);

    @Query("UPDATE restaurant SET type = :type WHERE name = :name")
    int updateRestaurantType (String name, int type);

    @Query("UPDATE restaurant SET phone = :phone, website_url = :websiteUrl, open_until = :openUntil WHERE placeId = :placeId")
    int updateRestaurantGeneralInfo(String placeId, String phone, String websiteUrl, String openUntil);

    @Query("UPDATE restaurant SET image_url = :photoUrl WHERE placeId = :placeId")
    int updateRestaurantPhoto(String placeId, String photoUrl);

    @Query("UPDATE restaurant SET distance = :distanceValue WHERE placeId = :place_id")
    int updateRestaurantDistance(String place_id, String distanceValue);

    @Query("UPDATE restaurant SET image_url = :imageUrl WHERE placeId = :place_id")
    int updateRestaurantImageUrl (String place_id, String imageUrl);

    // -------------------
    // DELETE
    // -------------------

    @Delete
    void deleteRestaurant (RestaurantEntry restaurantEntry);

    @Delete
    int deleteAll (RestaurantEntry[] restaurantEntries);

    @Query("DELETE FROM restaurant")
    int deleteAllRowsInRestaurantTable();











}
