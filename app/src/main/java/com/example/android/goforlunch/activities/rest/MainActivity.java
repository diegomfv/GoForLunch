package com.example.android.goforlunch.activities.rest;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.auth.AuthChooseLoginActivity;
import com.example.android.goforlunch.atl.ATLInitApiTextSearchRequests;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.sqlite.DatabaseHelper;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.pageFragments.FragmentCoworkersView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapViewTRIAL;
import com.example.android.goforlunch.pojo.User;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

// TODO: 21/05/2018 Add a flag so when we come back from RestaurantActivity when don't do API Requests again
// TODO: 28/05/2018 Check if distance is too far, delete from the database!

// TODO: 29/05/2018 YET TO DO -------------------------------------------------------
// TODO: 31/05/2018 Update user info in onStart()!
// TODO: 29/05/2018 Check if there is internet connection
// TODO: 29/05/2018 Allow the camera access for profile pictures
// TODO: 29/05/2018 Allow to get a picture from facebook or google
// TODO: 29/05/2018 Add filter in Map
// TODO: 29/05/2018 Change that the RV is not setting the adapter each time the word changes in Search Bar
// TODO: 29/05/2018 Use Storage for user's picture (might use FirebaseAuth instead). Profile picture got from facebook or google
// TODO: 29/05/2018 Enable notifications at 4pm
// TODO: 29/05/2018 Enable notifications if restaurant is chosen
// TODO: 29/05/2018 Modify Requests (Nearby + Distance)
// TODO: 29/05/2018 Translations
// TODO: 29/05/2018 Elective Functionality, add Google and Facebook logins (password is done)
// TODO: 29/05/2018 Check deprecated problem RVAdapter
// TODO: 29/05/2018 General clean up

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    //Loaders id
    private static final int ID_LOADER_INIT_GENERAL_API_REQUESTS = 1;

    //Values to store user's info
    private String userName = "anonymous";
    private String userEmail = "anon@anonymous.com";
    private User user;

    //Widgets
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private BottomNavigationView navigationView;

    private ProgressBar progressBar;
    private TextView navUserName;
    private TextView navUserEmail;

    private FrameLayout container;

    //------------------------------------------------

    //ERROR that we are going to handle if the user doesn't have the correct version of the
    //Google Play Services
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;
    private static final float LATITUDE_BOUND = 0.007f;
    private static final float LONGITUDE_BOUND = 0.015f;

    //Vars
    private boolean mLocationPermissionGranted = false; //used in permissions
    private FusedLocationProviderClient mFusedLocationProviderClient; //used to get the location of the current user

    //Flag to know which fragment we are in avoiding relaunching it
    private int flagToSpecifyCurrentFragment;

    //Counter used to limit the times we try to start requests in case database is empty.
    private int startRequestsCounter = 0;

    //Retrofit usage
    private LatLngForRetrofit myPosition;

    //App Local Database
    private AppDatabase mDb;

    //Shared Preferences
    private SharedPreferences sharedPref;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------- CODE FIRST WRITTEN --------------------------//

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mDb = AppDatabase.getInstance(MainActivity.this);

        Log.d(TAG, "onCreate: " + sharedPref.getAll().toString());

        navigationView = findViewById(R.id.main_bottom_navigation_id);
        navigationView.setOnNavigationItemSelectedListener(botNavListener);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout_id);

        mNavigationView = (NavigationView) findViewById(R.id.main_nav_view_id);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);

        View headerView = mNavigationView.getHeaderView(0);
        navUserName = (TextView) headerView.findViewById(R.id.nav_drawer_name_id);
        navUserEmail = (TextView) headerView.findViewById(R.id.nav_drawer_email_id);

        container = (FrameLayout) findViewById(R.id.main_fragment_container_id);
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar_id);

        //showProgressBar(progressBar, container);

        /** We get the user information
         * */
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

        if (currentUser != null) {
            userName = currentUser.getDisplayName();
            userEmail = currentUser.getEmail();

            String[] nameParts = userName.split(" ");

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(RepoStrings.SharedPreferences.USER_FIRST_NAME, nameParts[0]);
            editor.putString(RepoStrings.SharedPreferences.USER_LAST_NAME, nameParts[1]);
            editor.apply();

        }

        /**
         * We fill the variables for NavigationDrawer
         * */
        navUserName.setText(userName);
        navUserEmail.setText(userEmail);

        /** If the user hasn't chosen a restaurant yet, we remind him/her to do it
         * */
        if (Objects.requireNonNull(sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP, "")).equals("")) {
            ToastHelper.toastShort(this, "You haven't chosen a group yet!");
        }

        /** 1. We store the key of the user in Shared Preferences
         *  2. Once we have the key, we store the Restaurant of the user to use all the info
         *  for an intent to Restaurant Activity
         * */
        mDb = AppDatabase.getInstance(getApplicationContext());
        fireDb = FirebaseDatabase.getInstance();
        fireDbRef = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        fireDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                Log.d(TAG, "onDataChange: userEmail = " + userEmail);

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()).toString().equals(userEmail)){

                        /** We save the user's key in SharedPreferences,
                         * the restaurant and the group
                         * */
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(
                                RepoStrings.SharedPreferences.USER_ID_KEY,
                                item.getKey());
                        editor.putString(
                                RepoStrings.SharedPreferences.USER_RESTAURANT_NAME,
                                Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue()).toString());
                        editor.putString(
                                RepoStrings.SharedPreferences.USER_GROUP,
                                Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP).getValue()).toString());
                        editor.apply();

                        // TODO: 06/06/2018 Only returns group_name, user_key and restaurant_name
                        Log.d(TAG, "onDataChange: SharedPreferences = " + sharedPref.getAll().toString());

                        /** We fill the object with the info we will need to pass in the intent
                         * */
                        User.Builder builder = new User.Builder();
                        builder.setFirstName(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.FIRST_NAME).getValue()).toString());
                        builder.setLastName(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.LAST_NAME).getValue()).toString());
                        builder.setEmail(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()).toString());
                        builder.setGroup(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP).getValue()).toString());
                        builder.setPlaceId(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.PLACE_ID).getValue()).toString());
                        builder.setRestaurantName(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue()).toString());
                        builder.setRestaurantType(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.RESTAURANT_TYPE).getValue()).toString());
                        builder.setImageUrl(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.IMAGE_URL).getValue()).toString());
                        builder.setAddress(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.ADDRESS).getValue()).toString());
                        builder.setRating(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.RATING).getValue()).toString());
                        builder.setPhone(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.PHONE).getValue()).toString());
                        builder.setWebsiteUrl(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.WEBSITE_URL).getValue()).toString());

                        user = builder.create();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());
            }
        });

        /** There are two types of background jobs.
         * 1st. If notifications are enabled, the user has to be told at 1.00pm the restaurant
         * where he/she is going. If no restaurant is chosen, no notification should appear.
         * 2nd. At 4pm, the database has to be filled with the visited restaurants.
         * */
        // TODO: 29/05/2018 Do here Android Job, fill database with visited restaurant





        // TODO: 29/05/2018 Do here Android Job, notification to the user
        /** We check if notifications are enabled. If they are and the user has chosen a restaurant,
         * a notification should be triggered at 1.00pm
         * */
//        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sharedPref.getBoolean(getString(R.string.pref_key_notifications), false)) {
//            /** If they are enabled, we check if a job is running
//             * */
//
//
//            if(job is already running) {
//                //do nothing
//            } else {
//                //prepare the alarm
//            }
//        }

        /** We show the MAP fragment
         * */
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container_id, FragmentRestaurantMapViewTRIAL.newInstance())
                .commit();

        /** We specify that that is the fragment we are showing
         * */
        flagToSpecifyCurrentFragment = 1;


        //---------------------- GET CURRENT LOCATION --------------------------//

        if (isGooglePlayServicesOK()) {

            //getLocationPermission() calls getDeviceLocation(). We can then store the Device Location
            //in the database and use it in FragmentRestaurantMapView to display the current location
            getLocationPermission();

        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*****************
     * LISTENERS *****
     * **************/


    private BottomNavigationView.OnNavigationItemSelectedListener botNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {

                        case R.id.nav_view_map_id:

                            if (flagToSpecifyCurrentFragment == 1) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantMapViewTRIAL.newInstance();
                                flagToSpecifyCurrentFragment = 1;

                            }
                            break;

                        case R.id.nav_view_list_id:

                            if (flagToSpecifyCurrentFragment == 2) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantListView.newInstance();
                                flagToSpecifyCurrentFragment = 2;

                            }
                            break;
                        case R.id.nav_view_coworkers_id:

                            if (flagToSpecifyCurrentFragment == 3) {
                                return true;

                            } else {
                                selectedFragment = FragmentCoworkersView.newInstance();
                                flagToSpecifyCurrentFragment = 3;

                            }
                            break;
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_fragment_container_id, selectedFragment)
                            .commit();

                    //true means that we want to select the clicked item
                    //if we choose false, the fragment will be shown but the item
                    //won't be selected
                    return true;
                }
            };


    private NavigationView.OnNavigationItemSelectedListener navViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){

                        case R.id.nav_lunch: {
                            Log.d(TAG, "onNavigationItemSelected: lunch pressed");

                            if (user == null) {
                                ToastHelper.toastShort(MainActivity.this, "An error occurred. Restaurant Entry is null");

                            } else if (user.getRestaurantName().equals("")) {
                                ToastHelper.toastShort(MainActivity.this, "You have not chosen a restaurant yet!");

                            } else {

                                Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                                intent.putExtra(RepoStrings.SentIntent.IMAGE_URL, user.getImageUrl());
                                intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME, user.getRestaurantName());
                                intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, user.getRestaurantType());
                                intent.putExtra(RepoStrings.SentIntent.ADDRESS, user.getAddress());
                                intent.putExtra(RepoStrings.SentIntent.RATING, user.getRating());
                                intent.putExtra(RepoStrings.SentIntent.PHONE, user.getPhone());
                                intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL, user.getWebsiteUrl());

                                startActivity(intent);

                            }

                            return true;
                        }

                        case R.id.nav_join_group: {
                            Log.d(TAG, "onNavigationItemSelected: join group pressed");

                            startActivity(new Intent(MainActivity.this, JoinGroupActivity.class));

                            return true;

                        }

                        case R.id.nav_personal_info: {
                            Log.d(TAG, "onNavigationItemSelected: personal info pressed");

                            startActivity(new Intent(MainActivity.this, PersInfoActivity.class));

                            return true;
                        }

                        case R.id.nav_settings: {
                            Log.d(TAG, "onNavigationItemSelected: settings pressed");

                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                            return true;
                        }

                        case R.id.nav_logout: {
                            Log.d(TAG, "onNavigationItemSelected: log out pressed");

                            /** The user signs out
                             *  and goes to AuthSignIn Activity
                             *  */
                            auth.signOut();

                            Intent intent = new Intent(MainActivity.this, AuthChooseLoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                            return true;
                        }

                    }

                    item.setChecked(true);

                    return true;
                }
            };


    // --------------------------------- NEW CODE ---------------------------------//

    /** Checks if the user has the
     * correct Google Play Services Version
     */
    public boolean isGooglePlayServicesOK() {
        Log.d(TAG, "isGooglePlayServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isGooglePlayServicesOK: Google Play Services is working");
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //There is an error but we can resolve it
            Log.d(TAG, "isGooglePlayServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Log.d(TAG, "isGooglePlayServicesOK: an error occurred; you cannot make map requests");
            ToastHelper.toastLong(MainActivity.this, "You can't make map requests");

        }
        return false;
    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permission");

        /** We can also check first if the Android Version of the device is equal or higher than Marshmallow:
         *      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { "rest of code" } */

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(
                MainActivity.this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(
                   MainActivity.this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;

            }

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);

        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
        }
    }


    private void getDeviceLocation() {

        Log.d(TAG, "getDeviceLocation: getting device's location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        try {

            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful() && task.getResult() != null) {
                        //&& task.getResult() != null -- allows you to avoid crash if the app
                        // did not get the location from the device (= currentLocation = null)
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();

                        Log.d(TAG, "onComplete: current location: getLatitude(), getLongitude() " + (currentLocation.getLatitude()) + ", " + (currentLocation.getLongitude()));

                        myPosition = new LatLngForRetrofit(currentLocation.getLatitude(), currentLocation.getLongitude());


                        /** If the database is empty, we start the request
                         * */
                        if (checkIfLocalDatabaseIsEmpty()
                                && myPosition != null) {
                            Log.d(TAG, "onComplete: local database status (empty) = " + checkIfLocalDatabaseIsEmpty());
                            Log.d(TAG, "onComplete: myPosition = " + myPosition.toString());

                            //showProgressBar(progressBar, container);
                            initRequestProcess();

                        } else {
                            Log.d(TAG, "onComplete: local database status (empty) = " + checkIfLocalDatabaseIsEmpty());
                            //hideProgressBar(progressBar, container);

                        }
//
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                    }

                }
            });

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }

    }

    /** Method that returns true if local database is empty
     * */
    public boolean checkIfLocalDatabaseIsEmpty() {
        Log.d(TAG, "ifLocalDatabaseIsNotEmpty: called!");

        DatabaseHelper dbH = new DatabaseHelper(MainActivity.this);
        return dbH.isTableEmpty("restaurant");

    }

    /** Method that starts doing the requests to the servers to get the
     * restaurants. Firstly, it deletes the database
     * */
    public void initRequestProcess() {
        Log.d(TAG, "initRequestProcess: called!");

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                /** We delete all the info from the database
                 * */
                mDb.restaurantDao().deleteAllRowsInRestaurantTable();

            }
        });

        /** After deleting the database, we start the requests
         * */
        startRequests();

    }

    /** Method that starts the requests if the database is empty. If not,
     * it tries again (only 10 times more; this way we avoid doing requests
     * continuously forever)
     * */
    public void startRequests () {
        Log.d(TAG, "startRequests: called!");
        // TODO: 06/06/2018 CHECK THIS!

        if (startRequestsCounter != 10) {

            if (checkIfLocalDatabaseIsEmpty()){
                callLoaderInitApiGeneralRequests(ID_LOADER_INIT_GENERAL_API_REQUESTS);

            } else {
                startRequests();
                startRequestsCounter++;
            }
        } else {

            startRequestsCounter = 0;
        }
    }

    /** Method that allows to show the progress bar
     * */
    public void showProgressBar (ProgressBar progressBar, FrameLayout frameLayout) {

        progressBar.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);

    }

    /** Method that hides the progress bar
     * */
    public void hideProgressBar (ProgressBar progressBar, FrameLayout frameLayout) {

        progressBar.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

    }

    /** Method used in fragments to get the DraweLayout
     * */
    public DrawerLayout getMDrawerLayout() {
        return mDrawerLayout;
    }

    /** Method that starts the ATL
     * and starts the requests' process
     * */
    private void callLoaderInitApiGeneralRequests(int id) {

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Void> loader = loaderManager.getLoader(id);

        if (loader == null) {
            Log.i(TAG, "loadLoaderInitApiGeneralRequests: ");
            loaderManager.initLoader(id, null, loaderInitApiTextSearchRequests);
        } else {
            Log.i(TAG, "loadLoaderInitApiGeneralRequests: ");
            loaderManager.restartLoader(id, null, loaderInitApiTextSearchRequests);
        }
    }

    /**********************/
    /** LOADER CALLBACKS **/
    /**********************/

    /** This LoaderCallback
     * uses ATLInitApi
     * */
    private LoaderManager.LoaderCallbacks loaderInitApiTextSearchRequests =
            new LoaderManager.LoaderCallbacks() {

                @Override
                public Loader onCreateLoader(int id, Bundle args) {
                    Log.d(TAG, "onCreateLoader: is called");
                    return new ATLInitApiTextSearchRequests(MainActivity.this, mDb, myPosition);
                }

                @Override
                public void onLoadFinished(Loader loader, Object data) {
                    Log.d(TAG, "onLoadFinished: called!");

                    if (!checkIfLocalDatabaseIsEmpty()) {
                        Log.d(TAG, "onLoadFinished: database IS NOT EMPTY anymore");
                        //hideProgressBar(progressBar, container);

                    } else {
                        Log.d(TAG, "onLoadFinished: database IS EMPTY");

                        ToastHelper.toastShort(MainActivity.this, "Something went wrong. Database is not filled.");
                    }
                }

                @Override
                public void onLoaderReset(Loader loader) {

                }
            };
}

