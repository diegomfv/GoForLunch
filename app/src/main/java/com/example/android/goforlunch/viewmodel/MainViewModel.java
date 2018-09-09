package com.example.android.goforlunch.viewmodel;

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
public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    /** We will use this ViewModel to cache the data.
     * This variable has to be "private" and has to have
     * a "public" getter.
     * */
    private LiveData<List<RestaurantEntry>> restaurants;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "MainViewModel: Actively retrieving tasks from the Database");
        restaurants = database.restaurantDao().getAllRestaurants();

    }

    public LiveData<List<RestaurantEntry>> getRestaurants() {
        return restaurants;
    }

}
