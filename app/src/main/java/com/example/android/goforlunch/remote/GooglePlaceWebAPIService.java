package com.example.android.goforlunch.remote;

import com.example.android.goforlunch.models.modeldistance.MatrixDistance;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelnearby.MyPlaces;
import com.example.android.goforlunch.models.modelplacebyid.PlaceById;
import com.example.android.goforlunch.models.modelplacesbytextsearch.PlacesByTextSearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Diego Fajardo on 11/05/2018.
 */
public interface GooglePlaceWebAPIService {

    @GET("json")
    Call<MyPlaces> fetchDataNearby(
            @Query("location") LatLngForRetrofit latLngForRetrofit,
            @Query("rankby") String rankby,
            @Query("type") String type,
            @Query("key") String key
    );

    @GET("json")
    Call<PlacesByTextSearch> fetchDataTextSearch(
            @Query("query") String query,
            @Query("location") LatLngForRetrofit latLngForRetrofit,
            @Query("radius") int radius,
            @Query("key") String key
    );


    @GET("json")
    Call<PlaceById> fetchDataPlaceId(
            @Query("placeid") String placeId,
            @Query("key") String key
    );

    @GET("photo")
    Call<String> fetchDataPhoto(
            @Query("maxwidth") String maxWidth,
            @Query("photoreference") String photoReference,
            @Query("key") String key
    );

    @GET("json")
    Call<MatrixDistance> fetchDistance(
            @Query("units") String units,
            @Query("origins") String placeId,
            @Query("destinations") LatLngForRetrofit latLngForRetrofit,
            @Query("key") String key
    );
}


