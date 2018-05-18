package com.example.android.goforlunch.remote;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */
public class Common {

    private static final String GOOGLE_API_NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";

    public static GooglePlaceWebAPIService getGoogleNearbyAPIService() {

        return RetrofitClient.getClient(GOOGLE_API_NEARBY_SEARCH_URL).create(GooglePlaceWebAPIService.class);
    }

    public static final String GOOGLE_API_PLACE_ID_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/details/";

    public static GooglePlaceWebAPIService getGooglePlaceIdApiService() {

        return RetrofitClient.getClient(GOOGLE_API_PLACE_ID_SEARCH_URL).create(GooglePlaceWebAPIService.class);
    }

}
