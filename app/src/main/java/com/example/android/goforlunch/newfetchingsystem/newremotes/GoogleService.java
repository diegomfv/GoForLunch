package com.example.android.goforlunch.newfetchingsystem.newremotes;

import com.example.android.goforlunch.newfetchingsystem.newmodels.distancematrix.DistanceMatrix;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebyid.PlaceById;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebynearby.PlacesByNearby;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placetextsearch.PlacesByTextSearch;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Diego Fajardo on 19/06/2018.
 */
public interface GoogleService {

    @GET("json")
    Observable<PlacesByNearby> fetchDataNearby(
            @Query("location") LatLngForRetrofit latLngForRetrofit,
            @Query("rankby") String rankby,
            @Query("type") String type,
            @Query("key") String key
    );

    @GET("json")
    Observable<PlacesByTextSearch> fetchDataTextSearch(
            @Query(value = "query", encoded = true) String query,
            @Query("location") LatLngForRetrofit latLngForRetrofit,
            @Query("radius") int radius,
            @Query("key") String key
    );

    @GET("json")
    Observable<PlaceById> fetchDataPlaceId(
            @Query("placeid") String placeId,
            @Query("key") String key
    );

    @GET("json")
    Observable<DistanceMatrix> fetchDistanceMatrix(
            @Query("units") String units,
            @Query("origins") String placeId,
            @Query("destinations") LatLngForRetrofit latLngForRetrofit,
            @Query("key") String key
    );

    @GET("photo")
    Call<String> fetchDataPhoto(
            @Query("maxwidth") String maxWidth,
            @Query("photoreference") String photoReference,
            @Query("key") String key
    );

}
