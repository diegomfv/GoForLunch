package com.example.android.goforlunch.activities.auth;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;

import java.util.List;

/**
 * Created by Diego Fajardo on 20/05/2018.
 */

/**
 * Caches data. Since it is lifecycle aware we avoid
 * lifecycle problems
 * */
public class MainViewModelDELETE extends AndroidViewModel {

    private static final String TAG = MainViewModelDELETE.class.getSimpleName();

    /** We will use this ViewModel to cache the data.
     * This variable has to be "private" and has to have
     * a "public" getter.
     * */
    private LiveData<List<RestaurantEntry>> restaurants;

    public MainViewModelDELETE(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "MainViewModelDELETE: Actively retrieving tasks from the Database");
        restaurants = database.restaurantDao().getAllRestaurants();

    }

    public LiveData<List<RestaurantEntry>> getRestaurants() {
        return restaurants;
    }




}
