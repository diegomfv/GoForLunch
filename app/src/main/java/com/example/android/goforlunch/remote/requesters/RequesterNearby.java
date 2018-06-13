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
import com.example.android.goforlunch.repostrings.RepoStrings;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

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
    private LatLngForRetrofit myPosition;

    private static int requestCounter = 0;

    public RequesterNearby(AppDatabase mDb, LatLngForRetrofit myPosition) {
        this.mDb = mDb;
        this.myPosition = myPosition;
    }

    public void getDataAndDoApiRequest () {
        Log.d(TAG, "getDataAndDoApiRequest: ");

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                doApiRequest(mDb.restaurantDao().getAllRestaurantsNotLiveData());

            }
        });

    }

    public void doApiRequest(final List<RestaurantEntry> listOfRestaurantsInDatabase) {
        Log.d(TAG, "doApiRequest: ");

        if (listOfRestaurantsInDatabase.size() > 0) {

            GooglePlaceWebAPIService client = Common.getGoogleNearbyAPIService();
            Call<MyPlaces> callNearby = client.fetchDataNearby(myPosition, "distance", "restaurant", nearbyKey);
            callNearby.enqueue(new Callback<MyPlaces>() {

                @Override
                public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                    Log.d(TAG, "onResponse: correct call");
                    Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                    MyPlaces myPlaces = response.body();

                    Log.d(TAG, "onResponse: " + myPlaces.toString());

                    if (myPlaces.getResults() != null) {

                        String placeId;
                        String name;
                        String type;
                        String address;
                        String openUntil;
                        String distance;
                        String rating;
                        String imageUrl;
                        String phone;
                        String websiteUrl;
                        String lat;
                        String lng;

                        Results[] results = myPlaces.getResults();

                        /** Iterating through the results array
                         * */
                        for (int i = 0; i < results.length; i++) {

                            for (int j = 0; j < listOfRestaurantsInDatabase.size(); j++) {

                                // TODO: 07/06/2018 This is failing. Probably listOfRestaurantsInTheDatabase is 0 or null

                                if (listOfRestaurantsInDatabase.get(j).getPlaceId().equalsIgnoreCase(results[i].getPlace_id())) {
                                    Log.d(TAG, "onResponse: places Id are equal, so the restaurant is already in the database");
                                    //do nothing

                                } else {
                                    Log.d(TAG, "onResponse: the restaurant IS NOT in the database");

                                    if (results[i].getPlace_id() != null) {
                                        placeId = results[i].getPlace_id();
                                    } else {
                                        placeId = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    }

                                    if (results[i].getName() != null) {
                                        name = results[i].getName();
                                    } else {
                                        name = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    }

                                    if (results[i].getRating() != null) {
                                        rating = formatToTwoDecimalsAndGetRatingUnder3(results[i].getRating());
                                    } else {
                                        rating = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    }

                                    if (results[i].getVicinity() != null) {
                                        address = results[i].getVicinity(); // TODO: 06/06/2018 Check if we can format it!
                                    } else {
                                        address = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    }

                                    if (results[i].getGeometry() != null) {

                                        Geometry geometry = results[i].getGeometry();

                                        if (geometry.getLocation() != null) {

                                            Location location = geometry.getLocation();

                                            lat = location.getLat();
                                            lng = location.getLng();

                                        } else {
                                            lat = null;
                                            lng = null;
                                        }
                                    } else {
                                        lat = null;
                                        lng = null;
                                    }

                                    /** We will fill the following info with
                                     * the next requests
                                     * */
                                    //Type info will be "Other" because it will never be filled
                                    //"Other" is the last type of te RestaurantTypes array
                                    type = RepoStrings.RESTAURANT_TYPES[RepoStrings.RESTAURANT_TYPES.length - 1];
                                    openUntil = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    distance = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    imageUrl = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    phone = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                    websiteUrl = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;

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
                                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "run: requesterNearby: inserted an element");

                                            long insertion = mDb.restaurantDao().insertRestaurant(restaurantEntry);
                                            Log.d(TAG, "run: " + insertion);
                                        }
                                    });

                                    RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
                                    requesterPlaceId.doApiRequest(results[i].getPlace_id());

                                }
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

        } else {
            Log.d(TAG, "doApiRequest: try again doApiRequest");

            if (requestCounter != 5) {
                Log.d(TAG, "doApiRequest: requestCounter != 5");

                /** If we have not the data yet, we try again
                 * */
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        doApiRequest(listOfRestaurantsInDatabase);
                    }
                });

                requestCounter++;

            } else {
                Log.d(TAG, "doApiRequest: requestCounter = 5");

                /** If we have not the data yet but we tried it 5 times,
                 * we stop and don't do the requests
                 * */

                requestCounter = 0;

            }
        }
    }

    /** This method returns a rating with max. value = 3 if
     * it was higher than 3. Additionally, it formats the rating
     * so it has only 2 decimals
     * */
    private String formatToTwoDecimalsAndGetRatingUnder3(String rating) {

        float temp_rating = Float.parseFloat(rating);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        if (temp_rating > 3) {
            temp_rating = temp_rating * 3 / 5;
            rating = String.valueOf(temp_rating);
        }

        return df.format(temp_rating);
    }

}