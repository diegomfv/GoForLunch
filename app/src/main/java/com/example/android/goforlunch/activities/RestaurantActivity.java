package com.example.android.goforlunch.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantActivity extends AppCompatActivity {

    //Widgets
    private FloatingActionButton fab;

    //Variables
    private boolean fabIsOpen = true;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Anim duration
    private int mShortAnimationDuration;
    private int mMediumAnimationDuration;
    private int mLongAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        fab = findViewById(R.id.restaurant_fab_id);
        fab.setOnClickListener(mFabListener);

        mRecyclerView = (RecyclerView) findViewById(R.id.restaurant_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(RestaurantActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RVAdapterRestaurant(RestaurantActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        // Retrieve and cache the system's default "short, medium and long" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        mLongAnimationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);

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
