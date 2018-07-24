package com.example.android.goforlunch.activities.rest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amitshekhar.DebugDB;
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
import com.example.android.goforlunch.pageFragments.FragmentCoworkers;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapView;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

// TODO: 29/05/2018 YET TO DO -------------------------------------------------------
// TODO: 29/05/2018 Translations
// TODO: 12/07/2018 DistanceMatrix in ListView - Take care what it shows when there is no info! Might show -- --
// TODO: 12/07/2018 Modify UI when using FacebookButton
// TODO: 29/05/2018 General cleanup
// TODO: 12/06/2018 Make NOTIFICATIONS false in SharedPref if the user leaves
// TODO: 02/07/2018 User image has to be displayed in coworkers

//1
// TODO: 12/07/2018 Changing Fetching process to Service. Information will be displayed using a ViewModel in the Fragment (pins)

//2
// TODO: 12/07/2018 Problem when leaving the app because there is no internet

//3
// TODO: 12/07/2018 Offline challenge! Connect using broadcast receiver

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

    //Widgets
    @BindView(R.id.main_drawer_layout_id)
    DrawerLayout mainDrawerLayout;

    @BindView(R.id.main_nav_view_id)
    NavigationView mNavigationView;

    @BindView(R.id.main_bottom_navigation_id)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.main_progress_bar_id)
    ProgressBar progressBar;

    @BindView(R.id.main_fragment_container_id)
    FrameLayout container;

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

        bottomNavigationView.setOnNavigationItemSelectedListener(botNavListener);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);

        View headerView = mNavigationView.getHeaderView(0);
        navUserName = (TextView) headerView.findViewById(R.id.nav_drawer_name_id);
        navUserEmail = (TextView) headerView.findViewById(R.id.nav_drawer_email_id);
        navUserProfilePicture = (ImageView) headerView.findViewById(R.id.nav_drawer_image_id);

        showProgressBar(progressBar, container);

        /* When we start main Activity, we always load map fragment
        * */
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container_id, FragmentRestaurantMapView.newInstance())
                .commit();
        flagToSpecifyCurrentFragment = 1;

        hideProgressBar(progressBar,container);

    }

    /** We use onResume() to check if the notifications
     * are on or off
     * */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called!");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.d(TAG, "onNext: ");

                if (aBoolean) {

                    /** We get the user information
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

                } else {
                    // TODO: 22/07/2018 Delete!
                    UtilsFirebase.logOut(MainActivity.this);

                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");
        fireDbRefUsers.removeEventListener(valueEventListenerGetUserInfo);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called!");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            alertDialogLogOut();
        }

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
            Glide.with(MainActivity.this)
                    .load(currentUser.getPhotoUrl())
                    .into(navUserProfilePicture);
        } else {
            Log.d(TAG, "updateNavDrawerTextViews: currentUserPhoto = null");
            Glide.with(MainActivity.this)
                    .load(getResources().getDrawable(R.drawable.picture_not_available))
                    .into(navUserProfilePicture);
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

                    userFirstName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                    userLastName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                    userKey = item.getKey();
                    userGroup = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue()).toString();
                    userGroupKey = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                    Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY, userKey);

                    /* We check that alarms are running
                    * */
                    checkAddRestaurantAt4pmDailyJob(sharedPref);
                    checkNotifications(sharedPref);

                    updateNavDrawerTextViews();
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

                    showProgressBar(progressBar, container);

                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {

                        case R.id.nav_view_map_id:

                            if (flagToSpecifyCurrentFragment == 1) {
                                return true;

                            } else {
                                selectedFragment = FragmentRestaurantMapView.newInstance();
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
                                selectedFragment = FragmentCoworkers.newInstance();
                                flagToSpecifyCurrentFragment = 3;

                            }
                            break;
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_fragment_container_id, selectedFragment)
                            .commit();

                    hideProgressBar(progressBar, container);

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

                            Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    Log.d(TAG, "onNext: " + aBoolean);

                                    if (aBoolean) {

                                        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                        fireDbRefUsers.addListenerForSingleValueEvent(valueEventListenerNavLunch);

                                    } else {
                                        //Do nothing because there is no internet
                                        ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.noInternet));

                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "onError: " + e.getMessage());

                                }

                                @Override
                                public void onComplete() {
                                    Log.d(TAG, "onComplete: ");

                                }
                            });

                            return true;
                        }

                        case R.id.nav_join_group: {
                            Log.d(TAG, "onNavigationItemSelected: join group pressed");

                            startActivity(new Intent(MainActivity.this, JoinGroupActivity.class));

                            // TODO: 12/07/2018 Modify this!

//                            Intent intent = new Intent(MainActivity.this, FetchingService.class);
//
//                            intent.putExtra("latitude", 51.457202);
//                            intent.putExtra("longitude", -2.606345);
//                            intent.putExtra("accessInternalStorage", true);
//
//                            startService(intent);

                            return true;

                        }

                        case R.id.nav_personal_info: {
                            Log.d(TAG, "onNavigationItemSelected: personal info pressed");

                            startActivity(new Intent(MainActivity.this, PersInfoActivity.class));

                            // TODO: 12/07/2018 Delete if necessary
                            DebugDB.getAddressLog();

                            return true;
                        }

                        case R.id.nav_settings: {
                            Log.d(TAG, "onNavigationItemSelected: settings pressed");

                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                            return true;
                        }

                        case R.id.nav_logout: {
                            Log.d(TAG, "onNavigationItemSelected: log out pressed");

                            /* We display a dialog for loggin out
                            * */
                            alertDialogLogOut();

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
        Log.d(TAG, "showProgressBar: called!");

        progressBar.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);

    }

    /** Method that hides the progress bar
     * */
    public void hideProgressBar (ProgressBar progressBar, FrameLayout frameLayout) {
        Log.d(TAG, "hideProgressBar: called!");

        progressBar.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

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
    private void checkAddRestaurantAt4pmDailyJob(SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: called!");

        boolean addRestaurantAlarmIsTrue = sharedPreferences.getBoolean(getResources().getString(R.string.key_alarmAddRestaurantIsOn), false);

        /* The first time this method is called, it will run the else statement because there would not be
         * any info in SharedPreferences. When running the "else" part, it will fill SharedPreferences
         * and from that moment on it will do nothing because the alarm will already be set and this
         * alarm will be "true" in SharedPreferences
         * */

        if (addRestaurantAlarmIsTrue){
            Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: create job!");

             /* We cancel the job to avoid
            creating more than one (just in case) */
            cancelJob(jobIdAddRestaurant);

            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdAddRestaurant = AddRestaurantToGroupDailyJob.scheduleAddRestaurantToGroupDailyJob();

            /* We change sharedPref in the Database. The alarm is set so, from now on, we won't do anything
             *  */
            Utils.updateSharedPreferences(sharedPref, getResources().getString(R.string.key_alarmAddRestaurantIsOn), true);

            Log.i(TAG, "checkAddRestaurantAt4pmDailyJob: ALARM IS ON!");
        }
    }

    /**
     * This method creates a new job that will create notifications to tell
     * the user where he/she is going to have lunch
     * **/
    private void checkNotifications (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkNotifications: called!");

        boolean notificationsAlarmIsTrue = sharedPreferences.getBoolean(getResources().getString(R.string.key_alarmNotificationsAreOn), false);

        /* The first time this method is called, it will run the else statement because there would not be
         * any info in SharedPreferences. When running the "else" part, it will fill SharedPreferences
         * and from that moment on it will do nothing because the alarm will already be set and this
         * alarm will be "true" in SharedPreferences
         * */

        if (notificationsAlarmIsTrue) {
            Log.d(TAG, "checkNotifications: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkNotifications: cancel job and create alarm");

            /* We cancel the job to avoid
            creating more than one (just in case) */
            cancelJob(jobIdNotifications);

            /* We create the alarm for notifications
            using Evernote Android Job Library */
            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdNotifications = NotificationDailyJob.scheduleNotificationDailyJob();

            /* We change sharedPref in the Database. The alarm is set so, from now on, we won't do anything
             *  */
            Utils.updateSharedPreferences(sharedPref, getResources().getString(R.string.key_alarmNotificationsAreOn), true);

            Log.i(TAG, "checkNotifications: ALARM IS ON!");

        }
    }

    /** Method used to cancel notifications
     * */
    private void cancelJob(int JobId) {
        Log.d(TAG, "cancelJob: called!");
        JobManager.instance().cancel(JobId);
    }

}

