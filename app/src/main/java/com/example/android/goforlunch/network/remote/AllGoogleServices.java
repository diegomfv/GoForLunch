package com.example.android.goforlunch.network.remote;

/**
 * Created by Diego Fajardo on 19/06/2018.
 */
public class AllGoogleServices {

    public static GoogleService getGoogleNearbyService() {

        return RetrofitClient.getNearbyClient().create(GoogleService.class);
    }

    public static GoogleService getGooglePlaceTextSearchService() {

        return RetrofitClient.getPlaceByTextSearch().create(GoogleService.class);

    }

    public static GoogleService getGooglePlaceIdService() {

        return RetrofitClient.getPlaceByIdClient().create(GoogleService.class);
    }

    public static GoogleService getGooglePlacePhotoService() {

        return RetrofitClient.getPhotos().create(GoogleService.class);
    }

    public static GoogleService getGoogleDistanceMatrixService() {

        return RetrofitClient.getDistanceMatrix().create(GoogleService.class);
    }


}
