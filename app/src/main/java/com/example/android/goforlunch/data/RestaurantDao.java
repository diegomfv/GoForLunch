package com.example.android.goforlunch.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */

/** Data Access Object
 * */

/** LiveData runs, by default, outside of the main thread.
 * We use LiveDate to observe changes in the Database; for other
 * operations such as insert, update or delete we do not need
 * to observe changes in the database. For those operations,
 * we will not use LiveData and therefore we will keep using the
 * executors.
 * With LiveDate, we will get notified when there are changes
 * in the database.
 * */
@Dao
public interface RestaurantDao {

    @Query("SELECT * FROM restaurant ORDER BY distance")
    LiveData<List<RestaurantEntry>> getAllRestaurants();

    @Insert
    void insertRestaurant (RestaurantEntry restaurantEntry);

    @Update (onConflict = OnConflictStrategy.REPLACE) //Replaces the element in case of conflict
    void updateRestaurant (RestaurantEntry restaurantEntry);

    @Delete
    void deleteRestaurant (RestaurantEntry restaurantEntry);

    @Query("SELECT * FROM restaurant WHERE placeId = :placeId")
    LiveData<RestaurantEntry> getRestaurantByPlaceId(String placeId);



}
