package com.example.android.goforlunch.remote;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */
public class Common {

    public static GooglePlaceWebAPIService getGoogleNearbyAPIService() {

        return RetrofitClient.getNearbyClient().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGooglePlaceIdApiService() {

        return RetrofitClient.getPlaceByIdClient().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGooglePlacePhotoApiService() {

        return RetrofitClient.getPhotos().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGoogleDistanceMatrixApiService() {

        return RetrofitClient.getDistance().create(GooglePlaceWebAPIService.class);
    }


}
