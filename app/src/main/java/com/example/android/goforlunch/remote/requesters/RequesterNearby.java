package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.models.modelnearby.Geometry;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
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
public class RequesterNearby {

    private static final String TAG = "RequesterNearby";

    private AppDatabase mDb;

    public RequesterNearby(AppDatabase mDb) {
        this.mDb = mDb;
    }

    public void doApiRequest (final LatLngForRetrofit myPosition, String key) {

        final GooglePlaceWebAPIService client = Common.getGoogleNearbyAPIService();
        Call<MyPlaces> callNearby = client.fetchDataNearby(myPosition, "distance", "restaurant", key);
        callNearby.enqueue(new Callback<MyPlaces>() {

            @Override
            public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                MyPlaces myPlaces = response.body();

                Log.d(TAG, "onResponse: " + myPlaces.toString());

                Results[] results = myPlaces.getResults();

                for (int i = 0; i < results.length; i++) {

                    /** Creating the object restaurant and storing it in the map
                     * */

                    Geometry geometry = results[i].getGeometry();

                    com.example.android.goforlunch.models.modelnearby.Location location =
                            geometry.getLocation();

                    final String placeId = results[i].getPlace_id();
                    String name = results[i].getName();
                    String type = "NoType";
                    String address = "NoAddress";
                    String openUntil = "NoTime";
                    String distance = "NoDistance";
                    String rating = results[i].getRating();
                    String imageUrl = "NoImageUrl";
                    String phone = "NoPhone";
                    String websiteUrl = "NoWebsiteUrl";
                    String lat = location.getLat();
                    String lng = location.getLng();

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

                    mDb.restaurantDao().insertRestaurant(restaurantEntry);

                    /** We insert the object in the database
                     * */
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Inserting restaurantEntry: " + placeId);

                        }
                    });
                }

                for (int i = 0; i < results.length; i++) {

                    RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
                    requesterPlaceId.doApiRequest(results[i].getPlace_id());

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