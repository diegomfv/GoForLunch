package com.example.android.goforlunch.activities.rest;

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
import com.example.android.goforlunch.remote.requesters.RequesterPlaceId;
import com.example.android.goforlunch.strings.StringValues;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private String restaurantType;
    private RestaurantEntry restaurant;
    private String userEmail;
    private String userKey;

    private boolean fabIsOpen;
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
    private FirebaseAuth auth;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        /** We get the user email. We will use it for when clicking the fab button
         *  */
        auth = FirebaseAuth.getInstance();

        // TODO: 25/05/2018 Modify this!
//        if (auth != null) {
//            userEmail = auth.getCurrentUser().getEmail();
//        }
            userEmail = StringValues.FAKE_USER_EMAIL;
            userKey = "-LDJQcELSJlLyD5LD9PW";

        /** Instantiation of the fab and set onClick listener*/
        fab = findViewById(R.id.restaurant_fab_id);
        fab.setOnClickListener(mFabListener);

        /** Instance of the local database
         * */
        mDb = AppDatabase.getInstance(RestaurantActivity.this);

        /** Reference to Firebase Database
         * */
        fireDb = FirebaseDatabase.getInstance();
        fireDbRef = fireDb.getReference(StringValues.FirebaseReference.USERS);

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
        restaurantType = intent.getStringExtra(StringValues.SentIntent.RESTAURANT_TYPE);

        // TODO: 25/05/2018 delete this if statement and start with the "if else" statement
        if (placeId.equals("0")
                || placeId.equals("1")
                || placeId.equals("2")
                || placeId.equals("3")) {
            Log.d(TAG, "onCreate: placeId = " + placeId);

            String name;

            if (placeId.equals("0")) {
                name = "Burger King";
            } else if (placeId.equals("1")) {
                name = "McDonalds";
            }else if (placeId.equals("2")) {
                name = "KFC";
            } else {
                name = "Tony Romas";
            }

            restaurant = new RestaurantEntry(
                    placeId,
                    name,
                    restaurantType,
                    "Elmdale Road, 9",
                    "21.00h.",
                    "0.1m",
                    "2.75",
                    "image",
                    "+34 65482",
                    "website_url",
                    "0",
                    "0"
            );

            tvRestName.setText(restaurant.getName());
            tvRestAddress.setText(restaurant.getAddress());
            float rating = Float.parseFloat(restaurant.getRating());
            rbRestRating.setRating(rating);
            phoneToastString = restaurant.getPhone();
            webUrlToastString = restaurant.getWebsiteUrl();
            Glide.with(RestaurantActivity.this)
                    .load(restaurant.getImageUrl())
                    .into(ivRestPicture);


            // TODO: 25/05/2018 Make else if an "if statement" that starts everything
        } else if (placeId != null) {
            Log.d(TAG, "onCreate: checking if restaurant is in the database");

            /** We check if the placeId is in the database. If it is, we get it from there,
             * if not, we do an ApiRequest.
             * */

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {

                    restaurant = mDb.restaurantDao().getRestaurantById(placeId);

                    if (restaurant != null){
                        Log.d(TAG, "run: restaurant is in the database");

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
                        Log.d(TAG, "run: restaurant is not in the database. Proceeding with Api Request");

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
            //hideProgressBar();
        }

        /** We get the id of the user. We need it to setValues() when the user clicks
         * the fab button. Additionally, we set the value of "fabIsOpen"
         * */
        fireDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                for (DataSnapshot item:
                        dataSnapshot.getChildren()) {

                    Log.d(TAG, "onDataChange: item.child()... " + item.child(StringValues.FirebaseReference.EMAIL).getValue().toString());
                    Log.d(TAG, "onDataChange: userMail = " + userEmail);

                    if (item.child(StringValues.FirebaseReference.EMAIL).getValue().toString().equals(userEmail)) {

                        /** We set the value of "fabIsOpen" according to the information found in the database
                         * */
                        fabIsOpen = item.child(StringValues.FirebaseReference.RESTAURANT).getValue().toString().equals(restaurant.getName());
                        Log.d(TAG, "onDataChange: item.child()... = " + item.child(StringValues.FirebaseReference.RESTAURANT).getValue().toString());
                        Log.d(TAG, "onDataChange: restaurant.getName() = " + restaurant.getName());
                        Log.d(TAG, "onDataChange: fabIsOpen = " + fabIsOpen);

                        if (fabIsOpen) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());
            }
        });

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

                    /** We delete the restaurant from the database (user's)
                     **/
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.PLACE_ID).setValue("None");
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.RESTAURANT).setValue("None");
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.RESTAURANT_TYPE).setValue("None");

                    ToastHelper.toastShort(RestaurantActivity.this, "Not Going to the restaurant!");

                    Log.d(TAG, "onClick: fabIsOpen = " + fabIsOpen);
                }
            } else {
                fabIsOpen = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));

                    /** We add the restaurant to the database (user's)
                     * */
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.PLACE_ID).setValue(placeId);
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.RESTAURANT).setValue(restaurant.getName());
                    fireDbRef.child(userKey).child(StringValues.FirebaseReference.RESTAURANT_TYPE).setValue(restaurantType);

                    ToastHelper.toastShort(RestaurantActivity.this, "Going to the restaurant...");

                    Log.d(TAG, "onClick: fabIsOpen = " + fabIsOpen);
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
