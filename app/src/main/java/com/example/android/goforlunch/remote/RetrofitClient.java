package com.example.android.goforlunch.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Diego Fajardo on 14/05/2018.
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient (String baseUrl) {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofit;
    }
}
