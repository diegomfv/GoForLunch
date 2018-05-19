package com.example.android.goforlunch.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */

/** https://stackoverflow.com/questions/36628399/should-i-use-retrofit-with-a-singleton?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
 * Q: If it is a singleton, can it handle two or more api requests in parallel?
 * A: Yes, it can handle many parallel requests - I'm not sure what the limit is,
 * but when you exceed that, it will queue the request (assuming that you are using
 * it asynchronously, which you should). I've successfully thrown a dozen or more async
 * requests at it in quick succession, without worrying about how many outstanding requests
 * there are. */

/** Singletons of Retrofit. Keeping them as singleton will increase performance,
 * because you will not create each time costful objects like Gson, RestAdapter and ApiService.
 * */
public class RetrofitClient {

    private static final String GOOGLE_API_NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";
    private static final String GOOGLE_API_PLACE_ID_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/details/";
    private static final String GOOGLE_API_PLACE_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/";

    private static Retrofit retrofitNearby = null;
    private static Retrofit retrofitPlaceById = null;
    private static Retrofit retrofitPhotos = null;

    public static Retrofit getNearbyClient () {

        if (retrofitNearby == null) {

            retrofitNearby = new Retrofit.Builder()
                    .baseUrl(GOOGLE_API_NEARBY_SEARCH_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofitNearby;
    }

    public static Retrofit getPlaceByIdClient () {

        if (retrofitPlaceById == null) {

            retrofitPlaceById = new Retrofit.Builder()
                    .baseUrl(GOOGLE_API_PLACE_ID_SEARCH_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofitPlaceById;
    }

    public static Retrofit getPhotos () {

        if (retrofitPhotos == null) {

            retrofitPhotos = new Retrofit.Builder()
                    .baseUrl(GOOGLE_API_PLACE_PHOTO_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofitPhotos;
    }





}
