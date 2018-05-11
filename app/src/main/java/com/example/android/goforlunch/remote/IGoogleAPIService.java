package com.example.android.goforlunch.remote;

import com.example.android.goforlunch.model.MyPlaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Diego Fajardo on 11/05/2018.
 */
public interface IGoogleAPIService {

    @GET
    Call<MyPlaces> getNearbyPlaces (@Url String url);


}
