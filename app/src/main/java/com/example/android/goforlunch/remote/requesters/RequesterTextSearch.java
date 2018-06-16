package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelplacesbytextsearch.Geometry;
import com.example.android.goforlunch.models.modelplacesbytextsearch.Location;
import com.example.android.goforlunch.models.modelplacesbytextsearch.PlacesByTextSearch;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;
import com.example.android.goforlunch.repostrings.RepoStrings;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */

/** The Google Places search services share the same usage limits. However, the Text Search service
 * is subject to a 10-times multiplier. That is, each Text Search request that you make will count
 * as 10 requests against your quota. If you've purchased the Google Places API as part of your
 * Google Maps APIs Premium Plan contract, the multiplier may be different. Please refer to the
 * Google Maps APIs Premium Plan documentation for details.*/

/** Class that uses the Text Search service from Google Places API to do Requests.
 * It is used to divide the restaurants by type, what will help us in the future to
 * update the UI according to the type searched by the user
 * */
public class RequesterTextSearch {

    private static final String TAG = "RequesterTextSearch";

    private static String textSearchKey = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
    private AppDatabase mDb;
    private LatLngForRetrofit myPosition;
    private String restaurantType;
    private int MAX_TEXT_SEARCH_RESTAURANTS = 3;

    //This value allows to start NearbySearches when all the textSearch requests
    // have been done. The reason is that we won't insert in the database any
    // restaurant found by nearbySearch if it has already been found by TextSearch.
    private int counterLastInsertion = 0;

    public RequesterTextSearch(AppDatabase mDb, LatLngForRetrofit myPosition) {
        this.mDb = mDb;
        this.myPosition = myPosition;
    }

    public void doApiRequest () {
        Log.d(TAG, "doApiRequest: ");

        GooglePlaceWebAPIService client = Common.getGooglePlaceTextSearchApiService();

        for (int j = 1; j < RepoStrings.RESTAURANT_TYPES.length ; j++) {

            restaurantType = RepoStrings.RESTAURANT_TYPES[j];

            Call<PlacesByTextSearch> callTextSearch = client.fetchDataTextSearch(
                    restaurantType + RepoStrings.ADD_RESTAURANT_STRING,
                    myPosition,
                    20,
                    textSearchKey);

            callTextSearch.enqueue(new Callback<PlacesByTextSearch>() {

                @Override
                public void onResponse(Call<PlacesByTextSearch> call, Response<PlacesByTextSearch> response) {
                    Log.d(TAG, "onResponse: correct call");
                    Log.d(TAG, "onResponse: url = " + call.request().url().toString());
                    Log.d(TAG, "onResponse: TYPE -> " + call.request().url().toString().substring(65,69));

                    if (response.body() != null) {

                        PlacesByTextSearch places = response.body();
                        Log.d(TAG, "onResponse: " + places.toString());

                        if (places.getResults() != null) {

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

                            com.example.android.goforlunch.models.modelplacesbytextsearch.Results[] results = places.getResults();

                            /** We limit how many results we use to avoid being OVER_QUERY_LIMIT
                             * */
                            int resultsUsed;
                            if (results.length > MAX_TEXT_SEARCH_RESTAURANTS) { resultsUsed = MAX_TEXT_SEARCH_RESTAURANTS; }
                            else { resultsUsed = results.length; }

                            for (int i = 0; i < resultsUsed ; i++) {

                                placeId = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                name = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                type = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                address = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                openUntil = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                distance = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                rating = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                imageUrl = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                phone = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                websiteUrl = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                lat = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
                                lng = RepoStrings.NOT_AVAILABLE_FOR_STRINGS;

                                if (results[i].getPlace_id() != null) {
                                    placeId = results[i].getPlace_id();
                                }

                                if (results[i].getName() != null) {
                                    name = results[i].getName();
                                }

                                Log.d(TAG, "onResponse: TYPE -> " + call.request().url().toString().substring(65,69));

                                type = getType(call.request().url().toString().substring(65,69));

                                if (results[i].getFormatted_address() != null) {
                                    address = results[i].getFormatted_address();
                                }

                                if (results[i].getRating() != null) {
                                   rating = formatToTwoDecimalsAndGetRatingUnder3(results[i].getRating());
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
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {

                                        long result = mDb.restaurantDao().insertRestaurant(restaurantEntry);
                                        if (result != 0) {
                                            counterLastInsertion++;
                                        }
                                    }
                                });

                                if (results[i].getPlace_id() != null) {
                                    Log.d(TAG, "onResponse: requester PlaceId is called!");

                                    RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
                                    requesterPlaceId.doApiRequest(results[i].getPlace_id());

                                }
                            }

                            if (counterLastInsertion == MAX_TEXT_SEARCH_RESTAURANTS ){
                                Log.d(TAG, "onResponse: counterLastInsertion is called");

                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run: instantiating Requester Nearby");

                                        RequesterNearby requesterNearby = new RequesterNearby(mDb,myPosition);
                                        requesterNearby.getDataAndDoApiRequest();

                                    }
                                });

                                //Counter is restarted
                                counterLastInsertion = 0;
                                Log.d(TAG, "onResponse: counterLastInsertion restarted!");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<PlacesByTextSearch> call, Throwable t) {
                    Log.d(TAG, "onFailure: there was an error");
                    Log.d(TAG, "onFailure: url = " + call.request().url().toString());

                    Log.d(TAG, "onFailure: TYPE -> " + call.request().url().toString().substring(65,68));
                }
            });
        }
    }

    /** This method allows to set the type of the restaurant.
     * It uses the url used to do the API request to determine the type.
     * */
    private String getType (String urlSubstring) {

        for (int i = 1; i < RepoStrings.RESTAURANT_TYPES.length ; i++) {

            if (urlSubstring.equals(RepoStrings.RESTAURANT_TYPES[i].substring(0,4))){
                return RepoStrings.RESTAURANT_TYPES[i];
            }
        }
        return RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
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
