package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelplacebyid.Close;
import com.example.android.goforlunch.models.modelplacebyid.Opening_hours;
import com.example.android.goforlunch.models.modelplacebyid.Periods;
import com.example.android.goforlunch.models.modelplacebyid.PlaceById;
import com.example.android.goforlunch.models.modelplacebyid.Result;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego Fajardo on 21/05/2018.
 */

/** Class that allows doing requests to Google Places API. Specifically, this class
 * does Place Details Requests to get detailed information about places
 * */
public class RequesterPlaceId {

    private static final String TAG = "RequesterPlaceId";

    private static String placeIdKey = "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU";
    private static int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    private LatLngForRetrofit myPosition;
    private AppDatabase mDb;

    public RequesterPlaceId(AppDatabase mDb, LatLngForRetrofit myPosition) {
        this.mDb = mDb;
        this.myPosition = myPosition;
    }

    public void doApiRequest(final String placeId) {

        GooglePlaceWebAPIService clientPlaceId = Common.getGooglePlaceIdApiService();
        Call<PlaceById> callPlaceId = clientPlaceId.fetchDataPlaceId(placeId, placeIdKey);
        callPlaceId.enqueue(new Callback<PlaceById>() {
            @Override
            public void onResponse(Call<PlaceById> call, Response<PlaceById> response) {

                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                PlaceById placeById = response.body();

                Log.d(TAG, "onResponse: " + placeById.toString());

                String phone = "nA";
                String websiteUrl = "nA";
                String openTill = "nA";

                if (placeById.getResult() != null) {

                    final Result result = placeById.getResult();

                    if (result.getFormatted_phone_number() != null) {
                        phone = result.getInternational_phone_number();
                    }

                    if (result.getWebsite() != null) {
                        websiteUrl = result.getWebsite();
                    }

                    if (result.getOpening_hours() != null) {
                        Opening_hours opening_hours = result.getOpening_hours();
                        if (opening_hours.getPeriods() != null) {
                            Periods[] periods = opening_hours.getPeriods();
                            openTill = formatTime(periods,day);
                        }
                    }

                    final String finalPhone = phone;
                    final String finalWebsiteUrl = websiteUrl;
                    final String finalOpenTill = openTill;
                    /** Updating the database
                     * */
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.restaurantDao().updateRestaurant(result.getPlace_id(), finalPhone, finalWebsiteUrl, finalOpenTill);
                        }
                    });


                    /** Request to get a photo of the place
                     * */
                    if (result.getPhotos() != null) {
                        RequesterPhoto requesterPhoto = new RequesterPhoto(mDb);
                        requesterPhoto.doApiRequest(result.getPlace_id(), result.getPhotos()[0].getPhoto_reference());
                    }

                    if (myPosition.getLat() != 0) {

                        /** Request to get the distance to the place
                         * */
                        RequesterDistance requesterDistance = new RequesterDistance(mDb, myPosition);
                        requesterDistance.doApiRequest(result.getPlace_id());

                    }
                }
            }

            @Override
            public void onFailure(Call<PlaceById> call, Throwable t) {

                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

            }
        });
    }

    /** Method that formats the date that we get from the request to insert it in
     * the database with the new format (the one that will be displayed)
     * */
    private String formatTime (Periods[] periods, int day) {

        Close close = periods[day].getClose();
        String time = close.getTime();
        time = time.substring(0, 2) + "." + time.substring(2, time.length());
        return "Open until " + time;

    }
}
