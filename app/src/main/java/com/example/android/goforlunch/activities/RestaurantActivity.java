package com.example.android.goforlunch.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.models.modelnearby.MyPlaces;
import com.example.android.goforlunch.models.modelplacebyid.Photos;
import com.example.android.goforlunch.models.modelplacebyid.PlaceById;
import com.example.android.goforlunch.models.modelplacebyid.Result;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;
import com.example.android.goforlunch.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantActivity";

    //Widgets
    private FloatingActionButton fab;
    private ImageView ivRestPicture;
    private TextView tvRestName;
    private TextView tvRestAddress;
    private RatingBar rbRestRating;

    //Variables
    private boolean fabIsOpen = true;
    private String callToastString = "No phone available";
    private String webUrlToastString = "No web available";
    private String likeToastString = "Liked!";

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        fab = findViewById(R.id.restaurant_fab_id);
        fab.setOnClickListener(mFabListener);

        ivRestPicture = (ImageView) findViewById(R.id.restaurant_image_id);
        tvRestName = (TextView) findViewById(R.id.restaurant_title_id);
        tvRestAddress = (TextView) findViewById(R.id.restaurant_address_id);
        rbRestRating = (RatingBar) findViewById(R.id.restaurant_rating_id);

        mRecyclerView = (RecyclerView) findViewById(R.id.restaurant_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(RestaurantActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RVAdapterRestaurant(RestaurantActivity.this);
        mRecyclerView.setAdapter(mAdapter);


        GooglePlaceWebAPIService client = Common.getGooglePlaceIdApiService();
        Call<PlaceById> call = client.fetchDataPlaceId(getIntent().getExtras().getString("placeId"), "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU");
        call.enqueue(new Callback<PlaceById>() {
            @Override
            public void onResponse(Call<PlaceById> call, Response<PlaceById> response) {

                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                PlaceById placeById = response.body();

                Log.d(TAG, "onResponse: " + placeById.toString());

                Result result = placeById.getResult();

                tvRestName.setText(result.getName());
                tvRestAddress.setText(result.getFormatted_address());

                float rating = Float.parseFloat(result.getRating());
                if (rating > 3) {
                    rating = rating * 3 / 5;
                    Log.d(TAG, "onCreate: " + rating);
                }
                rbRestRating.setRating(rating);

                callToastString = result.getFormatted_phone_number();
                webUrlToastString = result.getUrl();

                com.example.android.goforlunch.models.modelplacebyid.Photos[] photo = result.getPhotos();

                GooglePlaceWebAPIService client = Common.getGooglePlacePhotoApiService();
                final Call<String> call1 = client.fetchDataPhoto("800" , photo[0].getPhoto_reference(), "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU");
                call1.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        Log.d(TAG, "onResponse: correct call");
                        Log.d(TAG, "onResponse: url = " + call1.request().url().toString());

                        Glide.with(RestaurantActivity.this)
                                .load(response)
                                .into(ivRestPicture);

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                        Log.d(TAG, "onFailure: there was an error");
                        Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                        Glide.with(RestaurantActivity.this)
                                .load(call.request().url().toString())
                                .into(ivRestPicture);
                    }
                });


            }

            @Override
            public void onFailure(Call<PlaceById> call, Throwable t) {
                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());
            }
        });

        Anim.crossFadeShortAnimation(mRecyclerView);

    }

    /*****************
     * LISTENERS *****
     * **************/


    View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (fabIsOpen) {
                fabIsOpen = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));
                }
            } else {
                fabIsOpen = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));
                }
            }

            ToastHelper.toastShort(RestaurantActivity.this, "Fab Clicked");



        }
    };





    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
