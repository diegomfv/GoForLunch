package com.example.android.goforlunch.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
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
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;

import java.util.Objects;

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

        tvRestName.setText(Objects.requireNonNull(getIntent().getExtras()).getString("name"));
        tvRestAddress.setText(Objects.requireNonNull(getIntent().getExtras()).getString("address"));






        float rating = Float.parseFloat(getIntent().getExtras().getString("rating"));
        if (rating > 3) {

            rating = rating * 3 / 5;
            Log.d(TAG, "onCreate: " + rating);

        }
        rbRestRating.setRating(rating);

        if (getIntent().getExtras().getString("image_url") == null ||
                getIntent().getExtras().getString("image_url").equals("")) {

        } else {
            Glide.with(RestaurantActivity.this)
                    .load(getIntent().getExtras().getString("image_url"))
                    .into(ivRestPicture);
        }

        Log.d(TAG, "onCreate: " + getIntent().getExtras().getString("id"));

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
