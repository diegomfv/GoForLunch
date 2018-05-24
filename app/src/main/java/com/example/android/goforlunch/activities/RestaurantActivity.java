package com.example.android.goforlunch.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;
import com.example.android.goforlunch.remote.requesters.RequesterPlaceId;
import com.example.android.goforlunch.strings.StringValues;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantActivity";

    //Widgets
    private FloatingActionButton fab;
    private BottomNavigationView navigationView;
    private ImageView ivRestPicture;
    private TextView tvRestName;
    private TextView tvRestAddress;
    private RatingBar rbRestRating;

    //Variables
    private String placeId;
    private RestaurantEntry restaurant;
    private boolean fabIsOpen = true;
    private String phoneToastString = "No phone available";
    private String webUrlToastString = "No web available";
    private String likeToastString = "Liked!";

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Database
    private AppDatabase mDb;

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        /** Instance of the local database
         * */
        mDb = AppDatabase.getInstance(RestaurantActivity.this);

        /** Reference to Firebase Database
         * */
        fireDb = FirebaseDatabase.getInstance();
        fireDbRef = fireDb.getReference(StringValues.FirebaseReferences.USERS);

        /** Instantiation of the fab and set onClick listener*/
        fab = findViewById(R.id.restaurant_fab_id);
        fab.setOnClickListener(mFabListener);

        navigationView = (BottomNavigationView) findViewById(R.id.restaurant_selector_id);
        navigationView.setOnNavigationItemSelectedListener(bottomViewListener);

        ivRestPicture = (ImageView) findViewById(R.id.restaurant_image_id);
        tvRestName = (TextView) findViewById(R.id.restaurant_title_id);
        tvRestAddress = (TextView) findViewById(R.id.restaurant_address_id);
        rbRestRating = (RatingBar) findViewById(R.id.restaurant_rating_id);

        mRecyclerView = (RecyclerView) findViewById(R.id.restaurant_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(RestaurantActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // TODO: 24/05/2018 Set the list from Firebase!
        mAdapter = new RVAdapterRestaurant(RestaurantActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        /** We pass the placeId with the intent. Then, there can be 2 scenarios:
         * Scenario 1. The placeId is in the local database (we get the item from there).
         * Scenario 2. The placeId is not in the local database (it comes from a click in coworker's list).
         * In the last case, we have to do an ApiRequest.
         * */
        Intent intent = getIntent();

        placeId = intent.getStringExtra(StringValues.SentIntent.PLACE_ID);

        if (placeId != null) {

            /** We check if the placeId is in the database. If it is, we get it from there,
             * if not, we do an ApiRequest.
             * */

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {

                    restaurant = mDb.restaurantDao().getRestaurantById(placeId);

                    if (restaurant != null){

                        /** The restaurant is in the database, so we get the info and we won't do
                         * the ApiRequest
                         * */

                        if (restaurant.getName() != null) {
                            tvRestName.setText(restaurant.getName());
                        }

                        if (restaurant.getAddress() != null) {
                            tvRestAddress.setText(restaurant.getAddress());
                        }

                        if (restaurant.getRating() != null) {
                            float rating = Float.parseFloat(restaurant.getRating());
                            rbRestRating.setRating(rating);
                        }

                        if (restaurant.getPhone() != null) {
                            phoneToastString = restaurant.getPhone();
                        }

                        if (restaurant.getWebsiteUrl() != null) {
                            webUrlToastString = restaurant.getWebsiteUrl();
                        }

                        if (restaurant.getImageUrl() != null) {
                            Glide.with(RestaurantActivity.this)
                                    .load(restaurant.getImageUrl())
                                    .into(ivRestPicture);
                        }

                    } else {

                        /** The restaurant is NOT in the database, so we get the info doing the ApiRequest
                         * */

                        LatLngForRetrofit latLngForRetrofit = new LatLngForRetrofit(0, 0);

                        /** We pass a latLngForRetrofit with lat and lng equal to 0 to prevent the app
                         *  to do the Distance Api Request (see RequesterPlaceId class). We do this
                         *  because we don't have the restaurant location.
                         * */
                        RequesterPlaceId requester = new RequesterPlaceId(mDb, latLngForRetrofit);
                        requester.doApiRequest(placeId);

                        /** We wait a bit for the request to be done
                         * */
                        // TODO: 24/05/2018 Vary time if needed!
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        restaurant = mDb.restaurantDao().getRestaurantById(placeId);

                        if (restaurant.getName() != null) {
                            tvRestName.setText(restaurant.getName());
                        }

                        if (restaurant.getAddress() != null) {
                            tvRestAddress.setText(restaurant.getAddress());
                        }

                        if (restaurant.getRating() != null) {
                            float rating = Float.parseFloat(restaurant.getRating());
                            rbRestRating.setRating(rating);
                        }

                        if (restaurant.getPhone() != null) {
                            phoneToastString = restaurant.getPhone();
                        }

                        if (restaurant.getWebsiteUrl() != null) {
                            webUrlToastString = restaurant.getWebsiteUrl();
                        }

                        if (restaurant.getImageUrl() != null) {
                            Glide.with(RestaurantActivity.this)
                                    .load(restaurant.getImageUrl())
                                    .into(ivRestPicture);
                        }

                    }
                }
            });

            // TODO: 24/05/2018 Hide the progress bar and Show the screen
            //hideprogressBar();
        }


        Anim.crossFadeShortAnimation(mRecyclerView);

    }

    /*****************
     * LISTENERS *****
     * **************/


    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (fabIsOpen) {
                fabIsOpen = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));

                    // TODO: 24/05/2018 Delete from database

                    for (:
                         ) {
                        
                    }
                    
                    
                    fireDbRef.child(StringValues.User.)

                    ToastHelper.toastShort(RestaurantActivity.this, "Going to the restaurant!");
                }
            } else {
                fabIsOpen = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));

                    // TODO: 24/05/2018 Add to database

                    ToastHelper.toastShort(RestaurantActivity.this, "Not going to the restaurant...");
                }
            }

            ToastHelper.toastShort(RestaurantActivity.this, "Fab Clicked");

        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener bottomViewListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.restaurant_view_call_id: {
                            Log.d(TAG, "onNavigationItemSelected: callButton CLICKED!");
                            Log.d(TAG, "onNavigationItemSelected: phone = " + phoneToastString);
                            if (phoneToastString.equals("")) {
                                ToastHelper.toastShort(RestaurantActivity.this, "Phone is not available");
                            } else {
                                ToastHelper.toastShort(RestaurantActivity.this, "Calling to " + phoneToastString);
                            }

                        } break;

                        case R.id.restaurant_view_like_id: {
                            Log.d(TAG, "onNavigationItemSelected: likeButton CLICKED!");
                            ToastHelper.toastShort(RestaurantActivity.this, likeToastString);

                        } break;

                        case R.id.restaurant_view_website_id: {
                            Log.d(TAG, "onNavigationItemSelected: websiteButton CLICKED!");
                            Log.d(TAG, "onNavigationItemSelected: web URL = " + webUrlToastString);
                            if (webUrlToastString.equals("")) {
                                ToastHelper.toastShort(RestaurantActivity.this, "Website is not available");
                            } else {
                                // TODO: 19/05/2018 Bring the user to the website. Don't open in the app, allow the user to go to the true website
                                ToastHelper.toastShort(RestaurantActivity.this, "Brings the user to -> " + webUrlToastString);
                            }

                        } break;

                    }

                    return false; // TODO: 19/05/2018 Check true or false
                }
            };


    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
