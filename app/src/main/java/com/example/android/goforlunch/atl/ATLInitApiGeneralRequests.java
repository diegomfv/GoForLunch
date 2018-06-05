package com.example.android.goforlunch.atl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.remote.requesters.RequesterNearby;

/**
 * Created by Diego Fajardo on 21/05/2018.
 */
public class ATLInitApiGeneralRequests extends AsyncTaskLoader{

    private static final String TAG = "ATLInitApiGeneralReques";

    private AppDatabase mDb;
    private LatLngForRetrofit myPosition;

    public ATLInitApiGeneralRequests(@NonNull Context context, AppDatabase appDatabase, LatLngForRetrofit latLngForRetrofit) {
        super(context);
        this.mDb = appDatabase;
        this.myPosition = latLngForRetrofit;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public Object loadInBackground() {

        Log.d(TAG, "loadInBackground: is called");

        /** We delete all the info on the table
         * */
        mDb.restaurantDao().deleteAllRowsInRestaurantTable();

        /** We start the request that will fill the database
         * */
        RequesterNearby requesterNearby = new RequesterNearby(mDb, myPosition, mDb.restaurantDao().getAllRestaurantsNotLiveData());
        requesterNearby.doApiRequest();

        return null;
    }
}
