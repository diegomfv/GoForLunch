package com.example.android.goforlunch.atl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.remote.requesters.RequesterNearby;
import com.example.android.goforlunch.remote.requesters.RequesterTextSearch;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public class ATLInitApiTextSearchRequests extends AsyncTaskLoader {

    private static final String TAG = "ATLInitApiTextSearchReq";

    private AppDatabase mDb;
    private LatLngForRetrofit myPosition;

    public ATLInitApiTextSearchRequests(@NonNull Context context, AppDatabase appDatabase, LatLngForRetrofit latLngForRetrofit) {
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
         * and start the request that will fill the database
         * */
        if (mDb.restaurantDao().deleteAllRowsInRestaurantTable() > 0) {
            startRequestUsingTextSearchService();
        }

        return null;
    }

     /** Method that starts the requests
     * */
    private void startRequestUsingTextSearchService () {

        RequesterTextSearch requesterTextSearch = new RequesterTextSearch(mDb, myPosition);
        requesterTextSearch.doApiRequest();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RequesterNearby requesterNearby = new RequesterNearby(mDb,myPosition, mDb.restaurantDao().getAllRestaurantsNotLiveData());
        requesterNearby.doApiRequest();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
