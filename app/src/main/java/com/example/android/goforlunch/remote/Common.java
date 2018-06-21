package com.example.android.goforlunch.remote;

import retrofit2.Retrofit;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */
public class Common {

    public static GooglePlaceWebAPIService getGoogleNearbyService() {

        return RetrofitClient.getNearbyClient().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGooglePlaceTextSearchService() {

        return RetrofitClient.getPlaceByTextSearch().create(GooglePlaceWebAPIService.class);

    }

    public static GooglePlaceWebAPIService getGooglePlaceIdService() {

        return RetrofitClient.getPlaceByIdClient().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGooglePlacePhotoService() {

        return RetrofitClient.getPhotos().create(GooglePlaceWebAPIService.class);
    }

    public static GooglePlaceWebAPIService getGoogleDistanceMatrixService() {

        return RetrofitClient.getDistanceMatrix().create(GooglePlaceWebAPIService.class);
    }


}
