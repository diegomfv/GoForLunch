package com.example.android.goforlunch.activities.rest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.network.service.FetchingService;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.sync.AddRestaurantToGroupDailyJob;
import com.example.android.goforlunch.sync.AlertJobCreator;
import com.example.android.goforlunch.sync.NotificationDailyJob;
import com.example.android.goforlunch.fragments.FragmentCoworkers;
import com.example.android.goforlunch.fragments.FragmentRestaurantListView;
import com.example.android.goforlunch.fragments.FragmentRestaurantMapView;
import com.example.android.goforlunch.network.models.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.constants.Repo;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;

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

    private Unbinder unbinder;

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

    //Local database
    private AppDatabase localDatabase;

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

    private boolean accessInternalStorageGranted = false;
    private boolean mLocationPermissionGranted = false;

    //Internal Storage
    private Storage storage;
    private String mainPath;
    private String imageDirPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        fireDb = FirebaseDatabase.getInstance();

        localDatabase = AppDatabase.getInstance(MainActivity.this);

        this.configureStorage();

        //////////////////////////////////////
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        View headerView = mNavigationView.getHeaderView(0);
        navUserName = (TextView) headerView.findViewById(R.id.nav_drawer_name_id);
        navUserEmail = (TextView) headerView.findViewById(R.id.nav_drawer_email_id);
        navUserProfilePicture = (ImageView) headerView.findViewById(R.id.nav_drawer_image_id);

        bottomNavigationView.setOnNavigationItemSelectedListener(botNavListener);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);
        navUserProfilePicture.setOnClickListener(profilePictureListener);

        if (savedInstanceState != null) {
            flagToSpecifyCurrentFragment = savedInstanceState.getInt(Repo.FLAG_SPECIFY_FRAGMENT, 0);
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

        this.connectBroadcastReceiver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

        this.disconnectBroadcastReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        this.disconnectBroadcastReceiver();

        this.bottomNavigationView.setOnNavigationItemSelectedListener(null);
        this.mNavigationView.setNavigationItemSelectedListener(null);

        this.unbinder.unbind();
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
        outState.putInt(Repo.FLAG_SPECIFY_FRAGMENT, flagToSpecifyCurrentFragment);
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
                snackbar = UtilsGeneral.createSnackbar(
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

                    fireDbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS);
                    fireDbRefUsers.addListenerForSingleValueEvent(valueEventListenerGetUserInfo);
                }
            }

        }
    }

    /** Callback: gets the current position obtained in Map Fragment
     * */
    @Override
    public void onCurrentPositionObtained(LatLngForRetrofit myPosition, boolean locationPermission, boolean storageAccessPermission) {
        Log.d(TAG, "onCurrentPositionObtained: called!");
        this.myPosition = myPosition;
        this.mLocationPermissionGranted = locationPermission;
        this.accessInternalStorageGranted = storageAccessPermission;

        Log.i(TAG, "onCurrentPositionObtained: myPosition = " + myPosition);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called!");
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        switch (requestCode) {

            case Repo.REQUEST_CODE_ALL_PERMISSIONS: {

                if (grantResults.length > 0) {

                    /* We initialize and assign 0 to a counter to see if any permission is not
                    * granted (value = -1). If counter is higher than 0, then not all permissions
                    * are granted and we don't proceed with the fetching process
                    * */
                    int counter = 0;

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == -1) {
                            //-1 means access NOT GRANTED
                            counter++;
                        } else {

                            if (grantResults[0] == 0) {
                                /* This grantResults ([0]) has to do with WRITE_EXTERNAL_STORAGE permission.
                                * == 0 means this permission is granted */
                                UtilsGeneral.createImageDirectory(storage, imageDirPath);
                            }

                        }
                    }

                    if (counter > 0) {
                        ToastHelper.toastNotNecessaryPermissionsAvailable(MainActivity.this);
                    } else {
                        Log.i(TAG, "onRequestPermissionsResult: necessary permissions available");

                        /* We start the request fetching process
                         * */
                        startFetchingProcess();
                    }

                } else {
                    ToastHelper.toastNotNecessaryPermissionsAvailable(MainActivity.this);

                }
                break;
            }
        }
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

    /**************************
     * Rx JAVA ****************
     *************************/

    /** Method that returns all restaurants in database of a specific type
     * */
    private Maybe<List<RestaurantEntry>> getAllRestaurantsInDatabase() {
        Log.d(TAG, "getRestaurantsByType: called!");
        return localDatabase.restaurantDao().getAllRestaurantsRxJava();

    }

    private MaybeObserver<List<RestaurantEntry>> maybeObserverStartFetchProcessIfNecessary() {
        Log.d(TAG, "maybeObserverUpdateUI: called1");

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: called!");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntries) {
                Log.d(TAG, "onSuccess: called!");

                if (restaurantEntries.size() == 0) {
                    Log.i(TAG, "onSuccess: the database is EMPTY ++++++++++++");

                    if (internetAvailable) {

                        Intent intent = new Intent(MainActivity.this, FetchingService.class);
                        intent.putExtra(Repo.SentIntent.LATITUDE, myPosition.getLat());
                        intent.putExtra(Repo.SentIntent.LONGITUDE, myPosition.getLng());
                        intent.putExtra(Repo.SentIntent.ACCESS_INTERNAL_STORAGE_GRANTED, accessInternalStorageGranted);
                        startService(intent);

                    } else {
                        //do nothing, there is no internet
                    }

                } else {
                    Log.i(TAG, "onSuccess: the database is FULL ++++++++++++");
                    //do nothing, the database is already full
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: called!");

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: called!");

            }
        };
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

                if (Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                    /* We get the user information
                    * */
                    userFirstName = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                    userLastName = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                    userKey = item.getKey();
                    userGroup = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_GROUP).getValue()).toString();
                    userGroupKey = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                    UtilsGeneral.updateSharedPreferences(sharedPref, Repo.SharedPreferences.USER_ID_KEY, userKey);

                    /* We update Firebase according to the preference fragment
                     * (remember that the shared pref "notifications"
                     * was updated before thanks to firebase)
                     * */
                    fireDbRefUserNotif = fireDb.getReference(Repo.FirebaseReference.USERS).child(userKey).child(Repo.FirebaseReference.USER_NOTIFICATIONS);
                    fireDbRefUserNotif.setValue(sharedPref.getBoolean(getResources().getString(R.string.pref_key_notifications), false));

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

            if (dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue() != null) {
                if (dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                    /* The user has not chosen a restaurant yet
                     * */
                    ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.navDrawerToastNotRestaurantYet));

                } else {
                    /* The user has a restaurant
                     * */
                    Map<String, Object> map = UtilsFirebase.fillMapWithRestaurantInfoUsingDataSnapshot(dataSnapshot);
                    Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                    startActivity(UtilsGeneral.fillIntentUsingMapInfo(intent, map));
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
                                fireDbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey + "/" + Repo.FirebaseReference.USER_RESTAURANT_INFO);
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

                            return true;
                        }

                        case R.id.nav_start_search: {
                            Log.d(TAG, "onNavigationItemSelected: start search clicked!");

                            if (!UtilsGeneral.hasPermissions(MainActivity.this, Repo.PERMISSIONS)) {
                                UtilsGeneral.getPermissionsInActivity(MainActivity.this);

                            } else {
                                startFetchingProcess();

                            }
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


    /** Listener that prints in the
     * logcat a link to the database
     * */
    /* This listener can be deleted
    * */
    private View.OnClickListener profilePictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: profilePictureListener Clicked");

            DebugDB.getAddressLog();

        }
    };

    /** Method for showing the main content.
     * We cannot use UtilsGeneral because
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

    /*******************************
     * CONFIGURATION ***************
     ******************************/

    /** Method that connects a broadcastReceiver to the activity.
     * It allows to notify the user about the internet state
     * */
    private void connectBroadcastReceiver () {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Repo.CONNECTIVITY_CHANGE_STATUS);
        UtilsGeneral.connectReceiver(MainActivity.this, receiver, intentFilter, this);

    }

    /** Method that disconnects the broadcastReceiver from the activity.
     * */
    private void disconnectBroadcastReceiver () {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null) {
            UtilsGeneral.disconnectReceiver(
                    MainActivity.this,
                    receiver,
                    MainActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    /******************************************************
     * INTERNAL STORAGE
     *****************************************************/

    /** Method that launches a dialog asking for storage permissions if they have not been
     * granted before
     * */
    private void getInternalStorageAccessPermission () {
        Log.d(TAG, "getInternalStorageAccessPermission: called!");

        if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            accessInternalStorageGranted = true;

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Repo.RequestsCodes.REQ_CODE_WRITE_EXTERNAL_PERMISSION);
            }
        }
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
            UtilsGeneral.updateSharedPreferences(sharedPref, getResources().getString(R.string.key_alarmAddRestaurantIsOn), true);

            Log.i(TAG, "checkAddRestaurantsAt4pmDailyJob: ALARM IS ON!");
        }
    }

    /**
     * This method creates a new job that will create notifications to tell
     * the user where he/she is going to have lunch
     * **/
    private void checkNotifications (SharedPreferences sharedPreferences) {
        Log.d(TAG, "checkNotifications: called!");

        /* We have to do the conversion to boolean this time because it comes from
         * the preference fragment
          * */
        boolean notificationsAlarmIsTrue = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_notifications), false);

        Log.i(TAG, "checkNotifications: notificationsAlarmIsTrue = " + notificationsAlarmIsTrue);

            if (notificationsAlarmIsTrue) {
            Log.d(TAG, "checkNotifications: notificationsAlarmIsTrue = true");

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

    /** Method to start fetching process from MainActivity when button is clicked
     * */
    private void startFetchingProcess () {
        Log.i(TAG, "startFetchingProcess: called!");

        if (myPosition != null && myPosition.getLat() != 0.0 && myPosition.getLng() != 0.0) {
            Log.d(TAG, "onNavigationItemSelected: myPosition = " + myPosition.toString());

            ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.mainStartRequestProcess));

            Intent intent = new Intent(MainActivity.this, FetchingService.class);
            intent.putExtra(Repo.SentIntent.LATITUDE, myPosition.getLat());
            intent.putExtra(Repo.SentIntent.LONGITUDE, myPosition.getLng());
            intent.putExtra(Repo.SentIntent.ACCESS_INTERNAL_STORAGE_GRANTED, accessInternalStorageGranted);
            startService(intent);

        } else {
            ToastHelper.toastShort(MainActivity.this, getResources().getString(R.string.mainCurrentPositionNotAvailable));
        }

    }

    /** Method that configures storage to persist images
     * to disk
     * */
    private void configureStorage () {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        storage = new Storage(MainActivity.this);
        mainPath = storage.getInternalFilesDirectory() + File.separator;
        imageDirPath = mainPath + File.separator + Repo.Directories.IMAGE_DIR;

    }


}