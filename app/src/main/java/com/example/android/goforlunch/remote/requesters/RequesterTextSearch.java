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

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
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

        for (int j = 1; j < StringValues.restaurantTypes.length ; j++) {

            restaurantType = StringValues.restaurantTypes[j];

            Call<PlacesByTextSearch> callTextSearch = client.fetchDataTextSearch(
                    restaurantType + StringValues.addRestaurantString,
                    myPosition,
                    20,
                    textSearchKey);

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callTextSearch.enqueue(new Callback<PlacesByTextSearch>() {

                @Override
                public void onResponse(Call<PlacesByTextSearch> call, Response<PlacesByTextSearch> response) {
                    Log.d(TAG, "onResponse: correct call");
                    Log.d(TAG, "onResponse: url = " + call.request().url().toString());
                    Log.d(TAG, "onResponse: TYPE -> " + call.request().url().toString().substring(65,68));

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

                            for (int i = 0; i < results.length ; i++) {

                                placeId = StringValues.notAvailable;
                                name = StringValues.notAvailable;
                                type = StringValues.notAvailable;
                                address = StringValues.notAvailable;
                                openUntil = StringValues.notAvailable;
                                distance = StringValues.notAvailable;
                                rating = StringValues.notAvailable;
                                imageUrl = StringValues.notAvailable;
                                phone = StringValues.notAvailable;
                                websiteUrl = StringValues.notAvailable;
                                lat = StringValues.notAvailable;
                                lng = StringValues.notAvailable;

                                if (results[i].getPlace_id() != null) {
                                    placeId = results[i].getPlace_id();
                                }

                                if (results[i].getName() != null) {
                                    name = results[i].getName();
                                }

                                Log.d(TAG, "onResponse: TYPE -> " + call.request().url().toString().substring(65,69));

                                type = getType(call.request().url().toString().substring(65,69));

                                if (results[i].getRating() != null) {

                                   rating = getRatingUnder3(results[i].getRating());

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

                                // TODO: 22/05/2018 Disabled to avoid to many requests
//                    for (int i = 0; i < results.length; i++) {
//
//                        if (results[i].getPlace_id() != null) {
//
//                            RequesterPlaceId requesterPlaceId = new RequesterPlaceId(mDb, myPosition);
//                            requesterPlaceId.doApiRequest(results[i].getPlace_id());
//
//                        }
//                    }

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

        for (int i = 1; i < StringValues.restaurantTypes.length ; i++) {

            if (urlSubstring.equals(StringValues.restaurantTypes[i].substring(0,4))){
                return StringValues.restaurantTypes[i];
            }
        }
        return StringValues.notAvailable;
    }

    private String getRatingUnder3 (String rating) {

        float temp_rating = Float.parseFloat(rating);

        if (temp_rating > 3) {
            temp_rating = temp_rating * 3 / 5;
            rating = String.valueOf(temp_rating);
        }
        return rating;
    }
}
