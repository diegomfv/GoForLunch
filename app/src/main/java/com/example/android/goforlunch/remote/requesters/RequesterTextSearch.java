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
import com.example.android.goforlunch.strings.StringValues;

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

// TODO: 23/05/2018 We make the thread sleep because, if not, it does the calls in a random order.
// TODO: 23/05/2018 We may be able to remove it now
public class RequesterTextSearch {

    private static final String TAG = "RequesterTextSearch";

    private static String textSearchKey = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
    private AppDatabase mDb;
    private LatLngForRetrofit myPosition;
    private String restaurantType;
    private static int restaurantCounter = 0;

    public RequesterTextSearch(AppDatabase mDb, LatLngForRetrofit myPosition) {
        this.mDb = mDb;
        this.myPosition = myPosition;
    }

    public void doApiRequest () {

        GooglePlaceWebAPIService client = Common.getGooglePlaceTextSearchApiService();

        for (int j = 1; j < StringValues.RESTAURANT_TYPES.length ; j++) {

            restaurantType = StringValues.RESTAURANT_TYPES[j];

            Call<PlacesByTextSearch> callTextSearch = client.fetchDataTextSearch(
                    restaurantType + StringValues.ADD_RESTAURANT_STRING,
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

                            /** We do not get all the result because there are too many (that is why choose 5)
                             * */
                            // TODO: 24/05/2018 Can be changed to more options

                            /** We limit how many results we use
                             * */
                            int resultsUsed;
                            if (results.length > 5) { resultsUsed = 5; }
                            else { resultsUsed = results.length; }

                            for (int i = 0; i < resultsUsed ; i++) {

                                placeId = StringValues.NOT_AVAILABLE;
                                name = StringValues.NOT_AVAILABLE;
                                type = StringValues.NOT_AVAILABLE;
                                address = StringValues.NOT_AVAILABLE;
                                openUntil = StringValues.NOT_AVAILABLE;
                                distance = StringValues.NOT_AVAILABLE;
                                rating = StringValues.NOT_AVAILABLE;
                                imageUrl = StringValues.NOT_AVAILABLE;
                                phone = StringValues.NOT_AVAILABLE;
                                websiteUrl = StringValues.NOT_AVAILABLE;
                                lat = StringValues.NOT_AVAILABLE;
                                lng = StringValues.NOT_AVAILABLE;

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
                                        mDb.restaurantDao().insertRestaurant(restaurantEntry);
                                    }
                                });

                                if (results[i].getPlace_id() != null) {

                                    RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
                                    requesterPlaceId.doApiRequest(results[i].getPlace_id());

                                }
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

        for (int i = 1; i < StringValues.RESTAURANT_TYPES.length ; i++) {

            if (urlSubstring.equals(StringValues.RESTAURANT_TYPES[i].substring(0,4))){
                return StringValues.RESTAURANT_TYPES[i];
            }
        }
        return StringValues.NOT_AVAILABLE;
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

        return df.format(rating);
    }
}
