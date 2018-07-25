package com.example.android.goforlunch.activities.rest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amitshekhar.DebugDB;
import com.bumptech.glide.Glide;
import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.auth.AuthChooseLoginActivity;
import com.example.android.goforlunch.broadcastreceivers.InternetConnectionReceiver;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.job.AddRestaurantToGroupDailyJob;
import com.example.android.goforlunch.job.AlertJobCreator;
import com.example.android.goforlunch.job.NotificationDailyJob;
import com.example.android.goforlunch.pageFragments.FragmentCoworkers;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapView;
import com.example.android.goforlunch.remote.models.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.repository.RepoStrings;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO: 29/05/2018 YET TO DO -------------------------------------------------------
// TODO: 29/05/2018 Translations
// TODO: 12/07/2018 DistanceMatrix in ListView - Take care what it shows when there is no info! Might show -- --
// TODO: 29/05/2018 General cleanup
// TODO: 12/06/2018 Make NOTIFICATIONS false in SharedPref if the user leaves
// TODO: 02/07/2018 User image has to be displayed in coworkers

//1
// TODO: 12/07/2018 Changing Fetching process to Service. Information will be displayed using a ViewModel in the Fragment (pins)

//2
// TODO: 12/07/2018 Problem when leaving the app because there is no internet

//3
// TODO: 12/07/2018 Offline challenge! Connect using broadcast receiver

public class MainActivity extends AppCompatActivity implements Observer, FragmentRestaurantMapView.OnCurrentPositionObtainedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //Widgets
    @BindView(R.id.main_drawer_layout_id)
    DrawerLayout mainDrawerLayout;

    @BindView(R.id.main_nav_view_id)
    NavigationView mNavigationView;

    @BindView(R.id.main_bottom_navigation_id)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.main_fragment_container_id)
    FrameLayout container;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    private TextView navUserName;
    private TextView navUserEmail;
    private ImageView navUserProfilePicture;

    //Background jobs
    private int jobIdAddRestaurant;
    private int jobIdNotifications;

    //------------------------------------------------

    //Flag to know which fragment we are in avoiding relaunching it
    private int flagToSpecifyCurrentFragment;

    //Shared Preferences
    private SharedPreferences sharedPref;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUsers;
    private DatabaseReference fireDbRefGroups;
    private DatabaseReference fireDbRefUserNotif;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    //Current Position
    private LatLngForRetrofit myPosition;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        fireDb = FirebaseDatabase.getInstance();

        /* We update users notifications information in Firebase according to the preference fragment
        * */

        Utils.printSharedPreferences(sharedPref);

        //////////////////////////////////////
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        View headerView = mNavigationView.getHeaderView(0);
        navUserName = (TextView) headerView.findViewById(R.id.nav_drawer_name_id);
        navUserEmail = (TextView) headerView.findViewById(R.id.nav_drawer_email_id);
        navUserProfilePicture = (ImageView) headerView.findViewById(R.id.nav_drawer_image_id);

        bottomNavigationView.setOnNavigationItemSelectedListener(botNavListener);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);

        if (savedInstanceState != null) {
            flagToSpecifyCurrentFragment = savedInstanceState.getInt(RepoStrings.FLAG_SPECIFY_FRAGMENT, 0);
        }

        /* When we start main Activity, we always load map fragment
        * */
        Log.i(TAG, "onCreate: flagToSpecifyCurrentFragment = " + flagToSpecifyCurrentFragment);
        loadFragment();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(RepoStrings.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(MainActivity.this, receiver, intentFilter, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    MainActivity.this,
                    receiver,
                    MainActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    MainActivity.this,
                    receiver,
                    MainActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

        bottomNavigationView.setOnNavigationItemSelectedListener(null);
        mNavigationView.setNavigationItemSelectedListener(null);

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called!");
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            alertDialogLogOut();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called!");
        outState.putInt(RepoStrings.FLAG_SPECIFY_FRAGMENT, flagToSpecifyCurrentFragment);
        super.onSaveInstanceState(outState);

    }

    /** Callback: listening to broadcast receiver
     * */
    @Override
    public void update(Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet Not Available");

            internetAvailable = false;

            if (snackbar == null) {
                snackbar = Utils.createSnackbar(
                        MainActivity.this,
                        mainDrawerLayout,
                        getResources().getString(R.string.noInternet));

            } else {
                snackbar.show();
            }

        } else {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            if (snackbar != null) {
                snackbar.dismiss();
            }

            /* We get the user information
             * */
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
            Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

            if (currentUser != null) {

                userEmail = currentUser.getEmail();

                if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                    fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                    fireDbRefUsers.addListenerForSingleValueEvent(valueEventListenerGetUserInfo);
                }
            }

        }
    }

    /** Callback: gets the current position obtained in Map Fragment
     * */
    @Override
    public void onCurrentPositionObtained(LatLngForRetrofit myPosition) {
        Log.d(TAG, "onCurrentPositionObtained: called!");
        this.myPosition = myPosition;

    }

    /** Method used to update the NavDrawer info
     * */
    private boolean updateNavDrawerViews() {
        Log.d(TAG, "updateNavDrawerViews: called!");

        /* We fill the variables for NavigationDrawer
         * */
        StringBuilder userName = new StringBuilder(userFirstName);
        userName.append(" ").append(userLastName);

        navUserName.setText(userName.toString());
        navUserEmail.setText(userEmail);

        if (!internetAvailable) {

            Glide.with(MainActivity.this)
                    .load(getResources().getDrawable(R.drawable.picture_not_available))
                    .into(navUserProfilePicture);

        } else {

            if (currentUser.getPhotoUrl() != null) {
                Log.d(TAG, "updateNavDrawerViews: " + currentUser.getPhotoUrl());
                Glide.with(MainActivity.this)
                        .load(currentUser.getPhotoUrl())
                        .into(navUserProfilePicture);
            } else {
                Log.d(TAG, "updateNavDrawerViews: currentUserPhoto = null");
                Glide.with(MainActivity.this)
                        .load(getResources().getDrawable(R.drawable.picture_not_available))
                        .into(navUserProfilePicture);
            }
        }

        return true;
    }

    /** Method used to remind the user to choose a group
     * */
    private boolean chooseGroupReminder() {
        Log.d(TAG, "chooseGroupReminder: called!");

        if (userGroup == null || userGroup.equalsIgnoreCase("")) {
            ToastHelper.toastShort(this, getResources().getString(R.string.joinNotChosenGroup));
        }
        return true;
    }

    /** Method used in fragments to get the DrawerLayout
     * */
    public DrawerLayout getMDrawerLayout() {
        return mainDrawerLayout;
    }

    /*****************
     * LISTENERS *****
     * **************/

    /** Listener to get all the user's information from the database
     * */
    private ValueEventListener valueEventListenerGetUserInfo = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            for (DataSnapshot item :
                    dataSnapshot.getChildren()) {

                if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                    /* We update firebase database using notifications information
                    * */



                    userFirstName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                    userLastName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                    userKey = item.getKey();
                    userGroup = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue()).toString();
                    userGroupKey = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                    Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY, userKey);

                    /* We update SharedPreferences (notifications) according to the user's information in firebase
                    * */
                    Utils.updateSharedPreferences(
                            sharedPref,
                            getResources().getString(R.string.key_alarmNotificationsAreOn),
                            Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_NOTIFICATIONS).getValue()).toString());

                    /* We check that alarms are running
                    * */
                    checkAddRestaurantsAt4pmDailyJob(sharedPref);
                    checkNotifications(sharedPref);

                    updateNavDrawerViews();
                    chooseGroupReminder();

                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /** Listener for when the user clicks
     * the Lunch button in nav drawer
     * */
    private ValueEventListener valueEventListenerNavLunch = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
            Log.d(TAG, "onDataChange: Internet is OK");

            if (dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue() != null) {
                if (dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                    /* The user has not chosen a restaurant yet
                     * */
                    ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.navDrawerToastNotRestaurantYet));

                } else {
                    /* The user has a restaurant
                     * */
                    Map<String, Object> map = UtilsFirebase.fillMapWithRestaurantInfoUsingDataSnapshot(dataSnapshot);
                    Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                    startActivity(Utils.fillIntentUsingMapInfo(intent, map));
                    fireDbRefUsers.removeEventListener(this);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /** Listener for bottom navigation view
     * */
    private BottomNavigationView.OnNavigationItemSelectedListener botNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Log.d(TAG, "onNavigationItemSelected: called!");

                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {

                        case R.id.nav_view_map_id:

                            if (flagToSpecifyCurrentFragment == 0) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantMapView.newInstance();
                                flagToSpecifyCurrentFragment = 0;

                            }
                            break;

                        case R.id.nav_view_list_id:

                            if (flagToSpecifyCurrentFragment == 1) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantListView.newInstance();
                                flagToSpecifyCurrentFragment = 1;

                            }
                            break;
                        case R.id.nav_view_coworkers_id:

                            if (flagToSpecifyCurrentFragment == 2) {
                                return true;

                            } else {
                                selectedFragment = FragmentCoworkers.newInstance();
                                flagToSpecifyCurrentFragment = 2;

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

    /** Listener for the navigation drawer
     * */
    private NavigationView.OnNavigationItemSelectedListener navViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){

                        case R.id.nav_lunch: {
                            Log.d(TAG, "onNavigationItemSelected: lunch pressed");

                            if (!internetAvailable) {
                                ToastHelper.toastNoInternet(MainActivity.this);

                            } else {
                                fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                fireDbRefUsers.addListenerForSingleValueEvent(valueEventListenerNavLunch);

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

                            // TODO: 12/07/2018 Delete if necessary
                            DebugDB.getAddressLog();

                            return true;
                        }

                        case R.id.nav_start_search: {

                            if (myPosition != null) {
                                Log.i(TAG, "onNavigationItemSelected: my Position = " + myPosition.toString());

                            }

//                            if (myPosition != null) {
//                                Intent intent = new Intent(MainActivity.this, FetchingService.class);
//                                intent.putExtra("latitude", myPosition.getLat());
//                                intent.putExtra("longitude", myPosition.getLng());
//                                intent.putExtra("accessInternalStorage", true);
//                                startService(intent);
//
//                            } else {
//                                ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.mainCurrentPositionNotAvailable));
//
//                            }

                            return true;
                        }

                        case R.id.nav_settings: {
                            Log.d(TAG, "onNavigationItemSelected: settings pressed");

                            /* Navigating to Settings
                            * */
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                            return true;
                        }

                        case R.id.nav_logout: {
                            Log.d(TAG, "onNavigationItemSelected: log out pressed");

                            /* We display a dialog for logging out
                            * */
                            alertDialogLogOut();

                            return true;
                        }

                    }

                    item.setChecked(true);

                    return true;
                }
            };

    /** Method for showing the main content.
     * We cannot use Utils because
     * the mainContent Layout is not a Linear Layout
     * */
    private void showMainContent (LinearLayout progressBarContent, DrawerLayout mainContent) {
        Log.d(TAG, "showMainContent: called!");

        progressBarContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }


    /** Method that loads the fragment that is selected (the first time,
     * the fragment will be the map fragment).
     * This method is necessary
     * for configuration changes.
     * */
    private void loadFragment () {
        Log.i(TAG, "loadFragment: called!");

        switch (flagToSpecifyCurrentFragment) {

            case 0: {

                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container_id, FragmentRestaurantMapView.newInstance())
                        .commit();
                flagToSpecifyCurrentFragment = 0;

                showMainContent(progressBarContent, mainDrawerLayout);

            }
            break;

            case 1: {

                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container_id, FragmentRestaurantListView.newInstance())
                        .commit();
                flagToSpecifyCurrentFragment = 1;

                showMainContent(progressBarContent, mainDrawerLayout);

            }
            break;

            case 2: {

                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container_id, FragmentCoworkers.newInstance())
                        .commit();
                flagToSpecifyCurrentFragment = 2;

                showMainContent(progressBarContent, mainDrawerLayout);

            }
            break;

            default: {

                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container_id, FragmentRestaurantMapView.newInstance())
                        .commit();
                flagToSpecifyCurrentFragment = 0;

                showMainContent(progressBarContent, mainDrawerLayout);

            }
        }
    }

    /** Method that creates an alert dialog that
     * can be used to log out
     * */
    private void alertDialogLogOut () {
        Log.d(TAG, "alertDialogLogOut: called!");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getResources().getString(R.string.mainDialogAreYouSureLogOut))
                .setTitle(getResources().getString(R.string.mainDialogLoggingOut))
                .setPositiveButton(getResources().getString(R.string.mainDialogLoggingOutYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: yes button clicked!");

                        if (!internetAvailable) {
                            ToastHelper.toastNoInternet(MainActivity.this);

                        } else {

                            /* The user signs out
                             * and goes to AuthSignIn Activity
                             */
                            auth.signOut();
                            LoginManager.getInstance().logOut();

                            Intent intent = new Intent(MainActivity.this, AuthChooseLoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.mainDialogLoggingOutNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: no button clicked!");
                        //Nothing happens
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*********************
     * NOTIFICATIONS *****
     * ******************/















    /**
     * This method creates a new job to add restaurants to the database every day at 4pm if it
     * has not been created yet
     * */
    private void checkAddRestaurantsAt4pmDailyJob(SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkAddRestaurantsAt4pmDailyJob: called!");

        boolean addRestaurantAlarmIsTrue = sharedPreferences.getBoolean(getResources().getString(R.string.key_alarmAddRestaurantIsOn), false);

        /* The first time this method is called, it will always be false.
         * When running the "else" part, it will fill SharedPreferences
         * and from that moment on it will do nothing because the alarm will already be set and this
         * alarm will be "true" in SharedPreferences
         * */

        if (addRestaurantAlarmIsTrue){
            Log.d(TAG, "checkAddRestaurantsAt4pmDailyJob: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkAddRestaurantsAt4pmDailyJob: create job!");

             /* We cancel the job to avoid
            creating more than one (just in case) */
            cancelJob(jobIdAddRestaurant);

            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdAddRestaurant = AddRestaurantToGroupDailyJob.scheduleAddRestaurantToGroupDailyJob();

            /* We change sharedPref in the Database. The alarm is set so, from now on, we won't do anything
             *  */
            Utils.updateSharedPreferences(sharedPref, getResources().getString(R.string.key_alarmAddRestaurantIsOn), true);

            Log.i(TAG, "checkAddRestaurantsAt4pmDailyJob: ALARM IS ON!");
        }
    }

    /**
     * This method creates a new job that will create notifications to tell
     * the user where he/she is going to have lunch
     * **/
    private void checkNotifications (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkNotifications: called!");

        boolean notificationsAlarmIsTrue = sharedPreferences.getBoolean(getResources().getString(R.string.key_alarmNotificationsAreOn), false);

        Log.i(TAG, "checkNotifications: notificationsAlarmIsTrue = " + notificationsAlarmIsTrue);

        /* The first time this method is called, it will run the else statement because there would not be
         * any info in SharedPreferences. When running the "else" part, it will fill SharedPreferences
         * and from that moment on it will do nothing because the alarm will already be set and this
         * alarm will be "true" in SharedPreferences
         * */

        if (notificationsAlarmIsTrue) {
            Log.d(TAG, "checkNotifications: notificationsAlarmIsTrue = true");

//            /* We change the notifications information in firebase about the user. This way,
//            we can keep track of all the users that are using the device
//             */
//            fireDbRefUserNotif = fireDb.getReference(RepoStrings.FirebaseReference.USERS).child(userKey);
//            fireDbRefUserNotif.setValue(RepoStrings.FirebaseReference.USER_NOTIFICATIONS, notificationsAlarmIsTrue);
//
//            // TODO: 25/07/2018 Change this!!!!!

            fireDbRefUserNotif = fireDb.getReference(RepoStrings.FirebaseReference.USERS).child(userKey).child(RepoStrings.FirebaseReference.USER_NOTIFICATIONS);
            fireDbRefUserNotif.setValue(true);







            /* We change sharedPref in the Database and and the user.
            Now, when the alarm is triggered, the system will search for the current user's email
            and the same email in shared preferences. If they match, the notification
            will be shown (see alarm).
            */
            Utils.updateSharedPreferences(sharedPref, userEmail, true);

            /* The notifications are on.
            We cancel the job to avoid
            creating more than one (just in case)
            and set the alarm
            */
            cancelJob(jobIdNotifications);

            Log.i(TAG, "checkNotifications: ALARM CANCELLED");

            /* We create the alarm for notifications
            using Evernote Android Job Library
            */
            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdNotifications = NotificationDailyJob.scheduleNotificationDailyJob();

            Log.i(TAG, "checkNotifications: ALARM IS ON!");

        } else {
            Log.d(TAG, "checkNotifications: notificationsAlarmIsTrue = false");

            /* The notifications are off.
            Then, we cancel the job
            * */
            cancelJob(jobIdNotifications);

            Log.i(TAG, "checkNotifications: ALARM CANCELLED AND NOT SCHEDULED");


        }
    }

    /** Method used to cancel notifications
     * */
    private void cancelJob(int JobId) {
        Log.d(TAG, "cancelJob: called!");
        JobManager.instance().cancel(JobId);
    }

}

