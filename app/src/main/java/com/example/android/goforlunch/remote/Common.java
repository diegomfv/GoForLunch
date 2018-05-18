package com.example.android.goforlunch.remote;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */
public class Common {

    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";

    public static GooglePlaceWebAPIService getGoogleAPIService() {

        return RetrofitClient.getClient(GOOGLE_API_URL).create(GooglePlaceWebAPIService.class);
    }

}
