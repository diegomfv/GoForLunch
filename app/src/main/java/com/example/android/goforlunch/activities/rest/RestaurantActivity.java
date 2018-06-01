package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userRestaurant;

    private boolean fabShowsCheck;
    private String phoneToastString = "No phone available";
    private String webUrlToastString = "No web available";
    private String likeToastString = "Liked!";

    private List<String> listOfCoworkers;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase Database
    private FirebaseAuth auth;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUserWithKey;
    private DatabaseReference fireDbRefUsers;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        /** We get the user email. We will use it for when clicking the fab button
         *  */
        auth = FirebaseAuth.getInstance();

        if (auth != null) {
            userEmail = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        }

        /** Instantiation of the fab and set onClick listener*/
        fab = findViewById(R.id.restaurant_fab_id);
        fab.setOnClickListener(mFabListener);

        listOfCoworkers = new ArrayList<>();

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

        /** We get the intent to display the information
         * */
        final Intent intent = getIntent();

        // TODO: 28/05/2018 We can use here the checkIfIsNull method (for intent strings)

        if (intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME) == null
                || intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME).equals("")) {

            tvRestName.setText("Restaurant name not available");

        } else {

            StringBuilder displayedName = new StringBuilder();
            String tokens[] = intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME).split(" ");

            for (int i = 0; i < tokens.length; i++) {
                if (displayedName.length() < 27) {

                    /** 1 is the space between words
                     * */
                    if ((displayedName.length() + tokens[i].length()) + 1 < 27) {
                        displayedName.append(" ").append(tokens[i]);

                    } else {
                        break;
                    }
                }
            }

            String transformedName = displayedName.toString().trim();

            tvRestName.setText(transformedName);
        }

        if (intent.getStringExtra(RepoStrings.SentIntent.ADDRESS) == null
                || intent.getStringExtra(RepoStrings.SentIntent.ADDRESS).equals("")) {

            tvRestAddress.setText("Address not available");

        } else {

            tvRestAddress.setText(intent.getStringExtra(RepoStrings.SentIntent.ADDRESS));
        }

        if (intent.getStringExtra(RepoStrings.SentIntent.RATING) == null
                || intent.getStringExtra(RepoStrings.SentIntent.RATING).equals("")) {

            rbRestRating.setRating(0f);

        } else {

            float rating = Float.parseFloat(intent.getStringExtra(RepoStrings.SentIntent.RATING));
            rbRestRating.setRating(rating);
        }

        phoneToastString = intent.getStringExtra(RepoStrings.SentIntent.PHONE);
        webUrlToastString = intent.getStringExtra(RepoStrings.SentIntent.WEBSITE_URL);

        if (intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL) == null
                || intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL).equals("")) {

            Glide.with(RestaurantActivity.this)
                    .load(R.drawable.lunch_image)
                    .into(ivRestPicture);

        } else {

            Glide.with(RestaurantActivity.this)
                    .load(intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL))
                    .into(ivRestPicture);
        }

        /** We get the necessary info from Shared Preferences
         * */
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RestaurantActivity.this);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "Not Found");
        userRestaurant = sharedPref.getString(RepoStrings.SharedPreferences.USER_RESTAURANT_NAME, "Not Found");
        userGroup = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP, "Not Found");

        // TODO: 28/05/2018 See another way of doing things for lower versions
        if (userRestaurant.equals(intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME))) {
            fabShowsCheck = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));
            }

        } else {
            fabShowsCheck = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));
            }
        }

        /** We get the Firebase Database
         * */
        fireDb = FirebaseDatabase.getInstance();

        /** Reference to Firebase Database, users.
         * We get the list of coworkers that will go to this Restaurant which
         * will be displayed in the recyclerView
         * */
        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        fireDbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                Log.d(TAG, "onDataChange: userEmail = " + userEmail);

                /** We clear the list to avoid adding same users to it
                 * */
                listOfCoworkers.clear();

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    /** If another user is in the same group of the current user
                     *  */
                    if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP).getValue()).equals(userGroup)) {

                        /** If another user is going to the restaurant we are seeing in the screen
                         * */
                        if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue())
                                .equals(intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME))) {

                            /** If the user we are going to add to the recyclerView is not the current user
                             * */
                            if (!Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()).equals(userEmail)) {

                                /** We add the other user to the list, and this list will be used in the recyclerview
                                 * */
                                listOfCoworkers.add(item.child(RepoStrings.FirebaseReference.FIRST_NAME).getValue().toString());
                            }
                        }
                    }
                }

                Log.d(TAG, "onDataChange: listOfCoworkers.size() = " + listOfCoworkers.size());

                /** We used the list in the adapter
                 * */
                mAdapter = new RVAdapterRestaurant(RestaurantActivity.this, listOfCoworkers);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        Anim.crossFadeShortAnimation(mRecyclerView);

        // TODO: 28/05/2018 Delete when app is finished
        Map<String, ?> prefsMap = sharedPref.getAll();
        for (Map.Entry<String, ?> entry: prefsMap.entrySet()) {
            Log.d("SharedPreferences: ", entry.getKey() + ":" +
                    entry.getValue().toString());
        }

    }

    /*****************
     * LISTENERS *****
     * **************/

    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Map<String,Object> map;

            if (fabShowsCheck) {
                /** If we click the fab when it shows check it has to display "add".
                 * Moreover, we modify the info in the database
                 * */
                fabShowsCheck = false;
                Log.d(TAG, "onClick: fabShowsCheck = " + fabShowsCheck);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));

                    /** We delete the restaurant from the database (user's)
                     **/
                    fireDbRefUserWithKey = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);

                    map = new HashMap<>();

                    // TODO: 28/05/2018 Take care, if sth is null it will be deleted from the database because it won't be added to the map
                    map.put(RepoStrings.FirebaseReference.PLACE_ID, "");
                    map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, "");
                    map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, "");
                    map.put(RepoStrings.FirebaseReference.ADDRESS, "");
                    map.put(RepoStrings.FirebaseReference.RATING, "");
                    map.put(RepoStrings.FirebaseReference.PHONE, "");
                    map.put(RepoStrings.FirebaseReference.IMAGE_URL, "");
                    map.put(RepoStrings.FirebaseReference.WEBSITE_URL, "");

                    fireDbRefUserWithKey.updateChildren(map);

                    ToastHelper.toastShort(RestaurantActivity.this, "Not Going to the restaurant!");
                }

            } else {

                /** If we click the fab when it shows "add" it has to display "check".
                 * Moreover, we modify the info in the database
                 * */
                fabShowsCheck = true;
                Log.d(TAG, "onClick: fabShowsCheck = " + fabShowsCheck);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));

                    /** We add the restaurant to the database (user's)
                     * */
                    fireDbRefUserWithKey = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);

                    map = new HashMap<>();

                    // TODO: 28/05/2018 Take care, if sth is null it will be deleted from the database because it won't be added to the map
                    map.put(RepoStrings.FirebaseReference.PLACE_ID,  checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.PLACE_ID)));
                    map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME)));
                    map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RESTAURANT_TYPE)));
                    map.put(RepoStrings.FirebaseReference.ADDRESS, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.ADDRESS)));
                    map.put(RepoStrings.FirebaseReference.RATING, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RATING)));
                    map.put(RepoStrings.FirebaseReference.PHONE, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.PHONE)));
                    map.put(RepoStrings.FirebaseReference.IMAGE_URL,  checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.IMAGE_URL)));
                    map.put(RepoStrings.FirebaseReference.WEBSITE_URL, checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.WEBSITE_URL)));

                    Log.d(TAG, "onClick: " + map.get(RepoStrings.FirebaseReference.IMAGE_URL));

                    fireDbRefUserWithKey.updateChildren(map);

                    ToastHelper.toastShort(RestaurantActivity.this, "Going to "
                            + getIntent().getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME));
                }
            }
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

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrlToastString));
                                startActivity(browserIntent);

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

    /** We use this method to check if the strings that come from the intent are null or not
     * */
    private String checkIfIsNull (String string) {

        if (string == null) {
            return "";
        } else {
            return string;
        }
    }
}
