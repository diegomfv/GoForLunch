package com.example.android.goforlunch.activities.rest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
// TODO: 29/06/2018 Check storage issue
// TODO: 29/05/2018 Check if there is internet connection
// TODO: 29/05/2018 Enable notifications at 4pm
// TODO: 29/05/2018 Enable notifications if restaurant is chosen
// TODO: 29/05/2018 Translations
// TODO: 29/05/2018 General cleanup
// TODO: 12/06/2018 Make NOTIFICATIONS false in SharedPref if the user leaves
// TODO: 02/07/2018 The request gets several times the same restaurant and insert all them in the database
// TODO: 02/07/2018 User image has to be displayed in coworkers 

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

    //Widgets
    @BindView(R.id.main_drawer_layout_id)
    DrawerLayout mDrawerLayout;

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

        Log.d(TAG, "onCreate: " + sharedPref.getAll().toString());

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

        checkAddRestaurantAt4pmDailyJob(sharedPref);
        checkNotifications(sharedPref);

        hideProgressBar(progressBar,container);

    }

    /** We use onResume() to check if the notifications
     * are on or off
     * */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called!");
        //checkNotifications(sharedPref);
        // TODO: 02/07/2018 !!!!
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            alertDialogLogOut();
        }

    }

    /**
     * This method creates a new job to add restaurants to the database every day at 4pm if it
     * has not been created yet
     * */
    private void checkAddRestaurantAt4pmDailyJob(SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: called!");

        /* The first time this method is called, it will return false because there would not be
        * any info in SharedPreferences. When running the "else" part, it will fill SharedPreferences
        * and from that moment on it will do nothing because the alarm will already be set and this
        * alarm will be "true" in SharedPreferences
        * */
        if (sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_addRestaurantIsOn), false)){
            Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkAddRestaurantAt4pmDailyJob: create job!");

            JobManager.create(MainActivity.this).addJobCreator(new AlertJobCreator());
            jobIdAddRestaurant = AddRestaurantToGroupDailyJob.scheduleAddRestaurantToGroupDailyJob();
            ToastHelper.toastShort(MainActivity.this, "AddRestaurantToGroupDailyJob created!");

            /* We change sharedPref in the Database
             *  */
            sharedPref.edit().putBoolean(getResources().getString(R.string.pref_key_addRestaurantIsOn), true).apply();
        }
    }

    /**
     * This method creates a new job that will create notifications to tell
     * the user where he/she is going to have lunch
     * **/
    private void checkNotifications (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkNotifications: called!");

        if (sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_notifications), false)){
            Log.d(TAG, "checkNotifications: do nothing!");
            //do nothing since alarm is currently running

        } else {
            Log.d(TAG, "checkNotifications: cancel job and create alarm");

            /* We cancel the job to avoid
            creating more than one*/
            cancelJob(jobIdNotifications);

            /* We create the alarm for notifications
            using Evernote Android Job Library */
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

                    updateNavDrawerTextViews();
                    chooseGroupReminder();
                    //checkAddRestaurantAt4pmDailyJob(sharedPref);

                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

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

        progressBar.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);

    }

    /** Method that hides the progress bar
     * */
    public void hideProgressBar (ProgressBar progressBar, FrameLayout frameLayout) {

        progressBar.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

    }

    /** Method that creates an alert dialog that
     * can be used to log out
     * */
    private void alertDialogLogOut () {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getResources().getString(R.string.mainDialogAreYouSureLogOut))
                .setTitle(getResources().getString(R.string.mainDialogLoggingOut))
                .setPositiveButton(getResources().getString(R.string.mainDialogLoggingOutYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: yes button clicked!");

                        /** The user signs out
                         *  and goes to AuthSignIn Activity
                         *  */
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

    // TODO: 07/07/2018 Delete!
    public void getTheInfo () {
        Log.d(TAG, "getTheInfo: called!");

        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS)
                .child(userKey)
                .child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
        fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                if (dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString() != null &&
                        dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS).getValue().toString() != null) {

                    ToastHelper.toastShort(MainActivity.this,
                            dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString() + " / "
                            + dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS).getValue().toString());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });



    }



}

