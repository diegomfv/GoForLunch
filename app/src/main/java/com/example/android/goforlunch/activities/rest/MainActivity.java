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
import com.example.android.goforlunch.pageFragments.FragmentCoworkers;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListViewTRIAL;
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

// TODO: 29/05/2018 YET TO DO -------------------------------------------------------
// TODO: 29/05/2018 Check if there is internet connection
// TODO: 29/05/2018 Enable notifications at 4pm
// TODO: 29/05/2018 Enable notifications if restaurant is chosen
// TODO: 29/05/2018 Translations
// TODO: 29/05/2018 General clean up
// TODO: 12/06/2018 Make NOTIFICATIONS false in SharedPref if the user leaves
// TODO: 26/06/2018 Bind views with butterKnife


public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

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

    //Background jobs
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

    /** Method used to updateItem the NavDrawer info
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
                                selectedFragment = FragmentRestaurantListViewTRIAL.newInstance();
                                flagToSpecifyCurrentFragment = 2;

                            }
                            break;
                        case R.id.nav_view_coworkers_id:

                            if (flagToSpecifyCurrentFragment == 3) {
                                return true;

                            } else {
                                selectedFragment = FragmentCoworkers.newInstance();
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

//                                    if (Utils.checkInternetInBackgroundThread();) {
                                        Log.d(TAG, "onDataChange: Internet is OK");

                                        if (dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                                            ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.navDrawerToastNotGroupYet));

                                        } else {

                                            Map<String, Object> map = UtilsFirebase.fillMapWithRestaurantInfoUsingDataSnapshot(dataSnapshot);
                                            Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                                            startActivity(Utils.fillIntentUsingMapInfo(intent, map));

                                        }

//                                    } else {
                                        Log.d(TAG, "onDataChange: There is no internet!");
                                        ToastHelper.toastNoInternet(MainActivity.this);

//                                    }
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
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called!");
        super.onDestroy();
    }
}

