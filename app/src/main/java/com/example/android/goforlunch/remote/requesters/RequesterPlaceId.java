package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
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
public class RequesterPlaceId {

    private static final String TAG = "RequesterPlaceId";

    private static String placeIdKey = "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU";
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

                final Result result = placeById.getResult();

                final String address = result.getFormatted_address();
                final String phone = result.getInternational_phone_number();
                final String websiteUrl = result.getWebsite();

                final String openTill;
                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_WEEK);

                Opening_hours opening_hours = result.getOpening_hours();
                Periods[] periods = opening_hours.getPeriods();

                switch (day) {

                    case Calendar.SUNDAY: {

                        Close close = periods[0].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.MONDAY: {

                        Close close = periods[1].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.TUESDAY: {

                        Close close = periods[2].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.WEDNESDAY: {

                        Close close = periods[3].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.THURSDAY: {

                        Close close = periods[4].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.FRIDAY: {

                        Close close = periods[5].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    case Calendar.SATURDAY: {

                        Close close = periods[6].getClose();
                        String time = close.getTime();

                        time = time.substring(0, 2) + "." + time.substring(2, time.length());

                        openTill = "Open until " + time;

                    }
                    break;

                    default: {
                        openTill = "No time available";
                    }
                    break;

                }

                mDb.restaurantDao().updateRestaurant(result.getPlace_id(), address, phone, websiteUrl, openTill);

                if (result.getPhotos() != null) {

                    RequesterPhoto requesterPhoto = new RequesterPhoto(mDb);
                    requesterPhoto.doApiRequest(result.getPlace_id(), result.getPhotos()[0].getPhoto_reference());

                }

                RequesterDistance requesterDistance = new RequesterDistance(mDb, myPosition);
                requesterDistance.doApiRequest(result.getPlace_id());

            }

            @Override
            public void onFailure(Call<PlaceById> call, Throwable t) {

                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

            }
        });
    }
}
