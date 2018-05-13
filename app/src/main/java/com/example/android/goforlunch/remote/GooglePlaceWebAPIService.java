package com.example.android.goforlunch.remote;

import com.example.android.goforlunch.model.LatLngForRetrofit;
import com.example.android.goforlunch.model.MyPlaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Diego Fajardo on 11/05/2018.
 */
public interface GooglePlaceWebAPIService {

    @GET("json")
    Call<MyPlaces> fetchData (
            @Query("location") LatLngForRetrofit latLngGoForLunch,
            @Query("rankby") String rankby,
            @Query("type") String type,
            @Query("key") String key
    );
}
