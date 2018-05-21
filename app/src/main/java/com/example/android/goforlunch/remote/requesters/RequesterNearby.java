package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.models.modelnearby.Geometry;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelnearby.Location;
import com.example.android.goforlunch.models.modelnearby.MyPlaces;
import com.example.android.goforlunch.models.modelnearby.Results;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego Fajardo on 21/05/2018.
 */

/** Class that allows doing requests to Google Places API. Specifically, this class
 * does Nearby Search Requests to get the places that are close to the user
 * */
public class RequesterNearby {

    private static final String TAG = "RequesterNearby";

    private static String nearbyKey = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
    private AppDatabase mDb;

    public RequesterNearby(AppDatabase mDb) {
        this.mDb = mDb;
    }

    public void doApiRequest (final LatLngForRetrofit myPosition, String key) {

        GooglePlaceWebAPIService client = Common.getGoogleNearbyAPIService();
        Call<MyPlaces> callNearby = client.fetchDataNearby(myPosition, "distance", "restaurant", key);
        callNearby.enqueue(new Callback<MyPlaces>() {

            @Override
            public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                MyPlaces myPlaces = response.body();

                Log.d(TAG, "onResponse: " + myPlaces.toString());

                if (myPlaces.getResults() != null) {

                    String placeId = "nA";
                    String name = "nA";
                    String type = "nA";
                    String address = "nA";
                    String openUntil = "nA";
                    String distance = "nA";
                    String rating = "nA";
                    String imageUrl = "nA";
                    String phone = "nA";
                    String websiteUrl = "nA";
                    String lat = "nA";
                    String lng = "nA";

                    Results[] results = myPlaces.getResults();

                    /** Iterating through the results array
                     * */
                    for (int i = 0; i < results.length; i++) {

                        if (results[i].getPlace_id() != null) {
                            placeId = results[i].getPlace_id();
                        }

                        if (results[i].getName() != null) {
                            name = results[i].getName();
                        }

                        if (results[i].getRating() != null) {
                            rating = results[i].getRating();
                        }

                        if (results[i].getGeometry() != null) {

                            Geometry geometry = results[i].getGeometry();

                            if (geometry.getLocation() != null) {

                                Location location = geometry.getLocation();

                                lat = location.getLat();
                                lng = location.getLng();

                            }
                        }


                        /** We create an object with all the info
                         * */
                        final RestaurantEntry restaurantEntry = new RestaurantEntry(
                                placeId,
                                name,
                                type,
                                address,
                                openUntil,
                                distance,
                                rating,
                                imageUrl,
                                phone,
                                websiteUrl,
                                lat,
                                lng
                        );

                        /** We insert the object in the database
                         * */
                        mDb.restaurantDao().insertRestaurant(restaurantEntry);

                    }

                    for (int i = 0; i < results.length; i++) {

                        if (results[i].getPlace_id() != null) {

                            RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
                            requesterPlaceId.doApiRequest(results[i].getPlace_id());

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MyPlaces> call, Throwable t) {
                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());
            }
        });
    }
}