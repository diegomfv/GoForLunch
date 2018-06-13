package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego Fajardo on 21/05/2018.
 */

/** Class that allows doing requests to Google Places API. Specifically, this class
 * does Place Photos Requests to get the pictures from the specific place
 * */
public class RequesterPhoto {

    private static final String TAG = "RequesterPhoto";

    private static String photoKey = "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU";
    private String maxWidth = "400";
    private AppDatabase mDb;

    public RequesterPhoto(AppDatabase mDb) {
        this.mDb = mDb;
    }

    public void doApiRequest (final String placeId, String photoReference) {
        Log.d(TAG, "doApiRequest: ");

        GooglePlaceWebAPIService clientPhoto = Common.getGooglePlacePhotoApiService();
        Call<String> callPhoto = clientPhoto.fetchDataPhoto(maxWidth, photoReference, photoKey);
        callPhoto.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                final String photo_url = response.body();

                /** Updating the database
                 * */
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.restaurantDao().updateRestaurantPhoto(placeId, photo_url);
                    }
                });
            }

            @Override
            public void onFailure(final Call<String> call, Throwable t) {

                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.restaurantDao().updateRestaurantPhoto(placeId, call.request().url().toString());
                    }
                });


            }
        });
    }
}
