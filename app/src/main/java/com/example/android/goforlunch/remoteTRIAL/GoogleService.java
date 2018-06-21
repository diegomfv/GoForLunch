package com.example.android.goforlunch.remoteTRIAL;

import com.example.android.goforlunch.models.modeldistance.MatrixDistance;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelnearby.PlaceByNearby;
import com.example.android.goforlunch.models.modelplacebyid.PlaceById;
import com.example.android.goforlunch.models.modelplacesbytextsearch.PlaceByTextSearch;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Diego Fajardo on 18/06/2018.
 */
public interface GoogleService {

    @GET("json")
    Observable<PlaceByNearby> fetchDataNearby(
            @Query("location") LatLngForRetrofit latLngForRetrofit,
            @Query("rankby") String rankby,
            @Query("type") String type,
            @Query("key") String key
    );

    //encoded true avoids Retrofit to change "+" for 2%B in the url
    @GET("json")
    Observable<PlaceByTextSearch> fetchDataTextSearch(
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

    @GET("photo")
    Call<String> fetchDataPhoto(
            @Query("maxwidth") String maxWidth,
            @Query("photoreference") String photoReference,
            @Query("key") String key
    );

    @GET("json")
    Observable<MatrixDistance> fetchDistance(
            @Query("units") String units,
            @Query("origins") String placeId,
            @Query("destinations") LatLngForRetrofit latLngForRetrofit,
            @Query("key") String key
    );


//    @GET("json")
//    Call<PlaceByNearby> fetchDataNearby(
//            @Query("location") LatLngForRetrofit latLngForRetrofit,
//            @Query("rankby") String rankby,
//            @Query("type") String type,
//            @Query("key") String key
//    );

    //encoded true avoids Retrofit to change "+" for 2%B in the url
//    @GET("json")
//    Call<PlaceByTextSearch> fetchDataTextSearch(
//            @Query(value = "query", encoded = true) String query,
//            @Query("location") LatLngForRetrofit latLngForRetrofit,
//            @Query("radius") int radius,
//            @Query("key") String key
//    );
//
//
//    @GET("json")
//    Call<PlaceById> fetchDataPlaceId(
//            @Query("placeid") String placeId,
//            @Query("key") String key
//    );
//
//    @GET("photo")
//    Call<String> fetchDataPhoto(
//            @Query("maxwidth") String maxWidth,
//            @Query("photoreference") String photoReference,
//            @Query("key") String key
//    );
//
//    @GET("json")
//    Call<MatrixDistance> fetchDistance(
//            @Query("units") String units,
//            @Query("origins") String placeId,
//            @Query("destinations") LatLngForRetrofit latLngForRetrofit,
//            @Query("key") String key
//    );

}
