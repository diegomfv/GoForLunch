package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.auth.AuthChooseLoginActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.job.AddRestaurantToGroupDailyJob;
import com.example.android.goforlunch.job.AlertJobCreator;
import com.example.android.goforlunch.job.NotificationDailyJob;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.pageFragments.FragmentCoworkersView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapViewTRIAL2;
import com.example.android.goforlunch.repository.RepoStrings;
import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO: 21/05/2018 Add a flag so when we come back from RestaurantActivity when don't do API Requests again
// TODO: 28/05/2018 Check if distance is too far, delete from the database!

// TODO: 29/05/2018 YET TO DO -------------------------------------------------------
// TODO: 29/05/2018 Check if there is internet connection
// TODO: 29/05/2018 Enable notifications at 4pm
// TODO: 29/05/2018 Enable notifications if restaurant is chosen
// TODO: 29/05/2018 Translations
// TODO: 29/05/2018 Check deprecated problem RVAdapter
// TODO: 29/05/2018 General clean up
// TODO: 12/06/2018 Make NOTIFICATIONS false in SharedPref if the user leaves
// TODO: 13/06/2018 Remember to not allow doing queries in the main thread!

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    //Loaders id
    private static final int ID_LOADER_INIT_GENERAL_API_REQUESTS = 1;

    //Widgets

    @BindView(R.id.main_drawer_layout_id)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.main_nav_view_id)
    NavigationView mNavigationView;

    @BindView(R.id.main_bottom_navigation_id)
    BottomNavigationView bottomNavigationView;

    private ProgressBar progressBar;
    private TextView navUserName;
    private TextView navUserEmail;
    private ImageView navUserProfilePicture;

    private FrameLayout container;

    private int jobIdAddRestaurant;
    private int jobIdNotifications;

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

     //Shared Preferences
    private SharedPreferences sharedPref;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUsers;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //---------------------- CODE FIRST WRITTEN --------------------------//

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        fireDb = FirebaseDatabase.getInstance();

        Log.d(TAG, "onCreate: " + sharedPref.getAll().toString());

        bottomNavigationView.setOnNavigationItemSelectedListener(botNavListener);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);

        View headerView = mNavigationView.getHeaderView(0);
        navUserName = (TextView) headerView.findViewById(R.id.nav_drawer_name_id);
        navUserEmail = (TextView) headerView.findViewById(R.id.nav_drawer_email_id);
        navUserProfilePicture = (ImageView) headerView.findViewById(R.id.nav_drawer_image_id);

        container = (FrameLayout) findViewById(R.id.main_fragment_container_id);
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar_id);

        //showProgressBar(progressBar, container);

        /** We get the user information
         * */
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

        if (currentUser != null) {

            userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {

                            if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                                userFirstName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                                userLastName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                                userKey = item.getKey();
                                userGroup = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue()).toString();
                                userGroupKey = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                                Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY, userKey);

                                updateNavDrawerTextViews();
                                chooseGroupReminder();
                                //checkAddRestaurantDailyJob(sharedPref);

                                /** We show the MAP fragment
                                 * */
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.main_fragment_container_id, FragmentRestaurantMapViewTRIAL2.newInstance())
                                        .commit();

                                /** We specify that that is the fragment we are showing
                                 * */
                                flagToSpecifyCurrentFragment = 1;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());

                    }
                });
            }
        }
    }

    /** We use onResume() to check if the notifications
     * are on or off
     * */
    @Override
    protected void onResume() {
        super.onResume();
        //checkNotifications(sharedPref);
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This method creates a new job to add restaurants to the database if it
     * has not been created yet
     * */
    private void checkAddRestaurantDailyJob (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkAddRestaurantDailyJob: called!");

        if (sharedPreferences.getBoolean(getResources().getString(R.string.key_addRestaurantIsOn), true)){
            Log.d(TAG, "checkAddRestaurantDailyJob: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkAddRestaurantDailyJob: create job!");

            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdAddRestaurant = AddRestaurantToGroupDailyJob.scheduleAddRestaurantToGroupDailyJob();
            ToastHelper.toastShort(MainActivity.this, "AddRestaurantToGroupDailyJob created!");

            /** We change sharedPref in the Database
             * */
            sharedPref.edit().putBoolean(getResources().getString(R.string.key_addRestaurantIsOn), true).apply();
        }
    }

    /**
     * This method creates a new job that will create notifications to tell
     * the user where he/she is going to have lunch
     * **/
    private void checkNotifications (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkNotifications: called!");

        if (sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_notifications), true)){
            Log.d(TAG, "checkNotifications: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkNotifications: cancel job and create alarm");

            /** We cancel the job to avoid creating more than one
             * */
            cancelJob(jobIdNotifications);

            /** We create the alarm for notifications using Evernote Android Job Library
             * */
            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdNotifications = NotificationDailyJob.scheduleNotificationDailyJob();

            ToastHelper.toastShort(MainActivity.this, "An alarm has been set at 1pm!");
        }
    }

    /** Method used to cancel notifications
     * */
    private void cancelJob(int JobId) {
        Log.d(TAG, "cancelJob: called!");
        JobManager.instance().cancel(JobId);
    }

    /** Method used to update the NavDrawer info
     * */
    private boolean updateNavDrawerTextViews() {
        Log.d(TAG, "updateNavDrawerTextViews: called!");

        /**
         * We fill the variables for NavigationDrawer
         * */
        navUserName.setText(userFirstName + " " + userLastName);
        navUserEmail.setText(userEmail);

        if (currentUser.getPhotoUrl() != null) {
            Log.d(TAG, "updateNavDrawerTextViews: " + currentUser.getPhotoUrl());
            Glide.with(navUserProfilePicture)
                    .load(currentUser.getPhotoUrl())
                    .into(navUserProfilePicture);
        }

        return true;
    }

    /** Method used to remind the user to choose a group
     * */
    private boolean chooseGroupReminder() {
        Log.d(TAG, "chooseGroupReminder: called!");

        if (userGroup == null || userGroup.equalsIgnoreCase("")) {
            ToastHelper.toastShort(this, "You haven't chosen a group yet!");
        }
        return true;
    }

    /** Method used in fragments to get the DrawerLayout
     * */
    public DrawerLayout getMDrawerLayout() {
        return mDrawerLayout;
    }

    /** Method used to check if there is internet connection
     * */
    // TODO: 13/06/2018 Modify this!
    private boolean internetConnectionIsOK () {
        Log.d(TAG, "internetConnectionIsOK: called!");

        return true;
    }

    /*****************
     * LISTENERS *****
     * **************/

    private BottomNavigationView.OnNavigationItemSelectedListener botNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Log.d(TAG, "onNavigationItemSelected: called!");

                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {

                        case R.id.nav_view_map_id:

                            if (flagToSpecifyCurrentFragment == 1) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantMapViewTRIAL2.newInstance();
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

                            fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                            fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                    if (internetConnectionIsOK()) {
                                        Log.d(TAG, "onDataChange: Internet is OK");

                                        if (dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                                            ToastHelper.toastShort(MainActivity.this, "You haven not chosen a restaurant yet!");

                                        } else {

                                            Map<String, Object> map = UtilsFirebase.fillMapUsingDataSnapshot(dataSnapshot);
                                            Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                                            startActivity(Utils.fillIntentUsingMapInfo(intent, map));

                                        }

                                    } else {
                                        Log.d(TAG, "onDataChange: There is no internet!");
                                        ToastHelper.toastNoInternet(MainActivity.this);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled: " + databaseError.getCode());

                                }
                            });

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
                            LoginManager.getInstance().logOut();

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



    // --------------------------------- NEW CODE ---------------------------------//

//    /** Checks if the user has the
//     * correct Google Play Services Version
//     */
//    public boolean isGooglePlayServicesOK() {
//        Log.d(TAG, "isGooglePlayServicesOK: checking google services version");
//
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
//
//        if (available == ConnectionResult.SUCCESS) {
//            //Everything is fine and the user can make map requests
//            Log.d(TAG, "isGooglePlayServicesOK: Google Play Services is working");
//            return true;
//
//        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
//            //There is an error but we can resolve it
//            Log.d(TAG, "isGooglePlayServicesOK: an error occurred but we can fix it");
//            Dialog dialog = GoogleApiAvailability.getInstance()
//                    .getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
//            dialog.show();
//
//        } else {
//            Log.d(TAG, "isGooglePlayServicesOK: an error occurred; you cannot make map requests");
//            ToastHelper.toastLong(MainActivity.this, "You can't make map requests");
//
//        }
//        return false;
//    }
//
//    private void getLocationPermission() {
//        Log.d(TAG, "getLocationPermission: getting location permission");
//
//        /** We can also check first if the Android Version of the device is equal or higher than Marshmallow:
//         *      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { "rest of code" } */
//
//        String[] permissions = {
//                android.Manifest.permission.ACCESS_FINE_LOCATION,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//        };
//
//        if (ContextCompat.checkSelfPermission(
//                MainActivity.this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            if (ContextCompat.checkSelfPermission(
//                   MainActivity.this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                mLocationPermissionGranted = true;
//
//            }
//
//        } else {
//            ActivityCompat.requestPermissions(MainActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
//
//        }
//
//        if (mLocationPermissionGranted) {
//            getDeviceLocation();
//        }
//    }
//
//
//    private void getDeviceLocation() {
//
//        Log.d(TAG, "getDeviceLocation: getting device's location");
//
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
//
//        try {
//
//            Task location = mFusedLocationProviderClient.getLastLocation();
//            location.addOnCompleteListener(new OnCompleteListener() {
//                @Override
//                public void onComplete(@NonNull Task task) {
//
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        //&& task.getResult() != null -- allows you to avoid crash if the app
//                        // did not get the location from the device (= currentLocation = null)
//                        Log.d(TAG, "onComplete: found location!");
//                        Location currentLocation = (Location) task.getResult();
//
//                        Log.d(TAG, "onComplete: current location: getLatitude(), getLongitude() " + (currentLocation.getLatitude()) + ", " + (currentLocation.getLongitude()));
//                        myPosition = new LatLngForRetrofit(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//                        /** If the database is empty, we start the request
//                         * */
//                        if (localDatabaseIsEmpty()
//                                && myPosition != null) {
//                            Log.d(TAG, "onComplete: local database status (empty) = " + localDatabaseIsEmpty());
//                            Log.d(TAG, "onComplete: myPosition = " + myPosition.toString());
//
//                            //showProgressBar(progressBar, container);
//                            initRequestProcess();
//
//                        } else {
//                            Log.d(TAG, "onComplete: local database status (empty) = " + localDatabaseIsEmpty());
//                            //hideProgressBar(progressBar, container);
//
//                        }
////
//                    } else {
//                        Log.d(TAG, "onComplete: current location is null");
//                    }
//
//                }
//            });
//
//        } catch (SecurityException e) {
//            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
//        }
//
//    }
//
//    /** Method that returns true if local database is empty
//     * */
//    public boolean localDatabaseIsEmpty() {
//        Log.d(TAG, "ifLocalDatabaseIsNotEmpty: called!");
//
//        DatabaseHelper dbH = new DatabaseHelper(MainActivity.this);
//        return dbH.isTableEmpty("restaurant");
//
//    }
//
//    /** Method that starts doing the requests to the servers to get the
//     * restaurants. Firstly, it deletes the database
//     * */
//    public void initRequestProcess() {
//        Log.d(TAG, "initRequestProcess: called!");
//
//        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//
//                /** We delete all the info from the database
//                 * */
//                mDb.restaurantDao().deleteAllRowsInRestaurantTable();
//
//            }
//        });
//
//        /** After deleting the database, we start the requests
//         * */
//        startRequests();
//
//    }
//
//    /** Method that starts the requests if the database is empty. If not,
//     * it tries again (only 10 times more; this way we avoid doing requests
//     * continuously forever)
//     * */
//    public void startRequests () {
//        Log.d(TAG, "startRequests: called!");
//        // TODO: 06/06/2018 CHECK THIS!
//
//        if (startRequestsCounter != 10) {
//
//            if (localDatabaseIsEmpty()){
//                callLoaderInitApiGeneralRequests(ID_LOADER_INIT_GENERAL_API_REQUESTS);
//
//            } else {
//                startRequests();
//                startRequestsCounter++;
//            }
//        } else {
//
//            startRequestsCounter = 0;
//        }
//    }





//    /** Method that starts the ATL
//     * and starts the requests' process
//     * */
//    private void callLoaderInitApiGeneralRequests(int id) {
//
//        LoaderManager loaderManager = getSupportLoaderManager();
//        Loader<Void> loader = loaderManager.getLoader(id);
//
//        if (loader == null) {
//            Log.i(TAG, "loadLoaderInitApiGeneralRequests: ");
//            loaderManager.initLoader(id, null, loaderInitApiTextSearchRequests);
//        } else {
//            Log.i(TAG, "loadLoaderInitApiGeneralRequests: ");
//            loaderManager.restartLoader(id, null, loaderInitApiTextSearchRequests);
//        }
//    }
//
//    /**********************/
//    /** LOADER CALLBACKS **/
//    /**********************/
//
//    /** This LoaderCallback
//     * uses ATLInitApi
//     * */
//    private LoaderManager.LoaderCallbacks loaderInitApiTextSearchRequests =
//            new LoaderManager.LoaderCallbacks() {
//
//                @Override
//                public Loader onCreateLoader(int id, Bundle args) {
//                    Log.d(TAG, "onCreateLoader: is called");
//                    return new ATLInitApiTextSearchRequests(MainActivity.this, mDb, myPosition);
//                }
//
//                @Override
//                public void onLoadFinished(Loader loader, Object data) {
//                    Log.d(TAG, "onLoadFinished: called!");
//
//                    if (!localDatabaseIsEmpty()) {
//                        Log.d(TAG, "onLoadFinished: database IS NOT EMPTY anymore");
//                        //hideProgressBar(progressBar, container);
//
//                    } else {
//                        Log.d(TAG, "onLoadFinished: database IS EMPTY");
//
//                        ToastHelper.toastShort(MainActivity.this, "Something went wrong. Database is not filled.");
//                    }
//                }
//
//                @Override
//                public void onLoaderReset(Loader loader) {
//
//                }
//            };
}

