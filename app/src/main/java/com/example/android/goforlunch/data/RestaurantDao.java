package com.example.android.goforlunch.data;

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
@Dao
public interface RestaurantDao {

    @Query("SELECT * FROM RestaurantEntry ORDER BY distance")
    List<RestaurantEntry> getAllRestaurants();

    @Insert
    void insertRestaurant (RestaurantEntry restaurantEntry);

    @Update (onConflict = OnConflictStrategy.REPLACE) //Replaces the element in case of conflict
    void updateRestaurant (RestaurantEntry restaurantEntry);

    @Delete
    void deleteRestaurant (RestaurantEntry restaurantEntry);


}
