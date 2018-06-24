package com.example.android.goforlunch.pageFragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.data.sqlite.AndroidDatabaseManager;
import com.example.android.goforlunch.data.sqlite.DatabaseHelper;
import com.example.android.goforlunch.data.viewmodel.MainViewModel;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.newfetchingsystem.newmodels.distancematrix.DistanceMatrix;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebyid.PlaceById;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placebynearby.PlacesByNearby;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placetextsearch.PlacesByTextSearch;
import com.example.android.goforlunch.newfetchingsystem.newmodels.placetextsearch.Result;
import com.example.android.goforlunch.newfetchingsystem.newremotes.AllGoogleServices;
import com.example.android.goforlunch.newfetchingsystem.newremotes.GoogleService;
import com.example.android.goforlunch.newfetchingsystem.newremotes.GoogleServiceStreams;
import com.example.android.goforlunch.repository.RepoConstants;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.goforlunch.helpermethods.UtilsRemote.checkClosingTime;
import static com.example.android.goforlunch.repository.RepoStrings.Keys.NEARBY_KEY;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the Google Map
 * */
public class FragmentRestaurantMapViewTRIAL2 extends Fragment {

    /**************************
     * LOG ********************
     * ***********************/

    private static final String TAG = "PageFragmentRestaurantM";

    /**************************
     * VARIABLES **************
     * ***********************/

    //ERROR that we are going to handle if the user doesn't have the correct version of the
    //Google Play Services
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 17f;

    //Loaders id
    private static final int ID_LOADER_INIT_GENERAL_API_REQUESTS = 1;

    //vars
    private boolean mLocationPermissionGranted = false; //used in permissions
    private GoogleMap mMap; //used to create the map
    private FusedLocationProviderClient mFusedLocationProviderClient; //used to get the location of the current user



    //List with all the restaurants in the database
    private List<RestaurantEntry> listOfAllRestaurantsInDatabase;

    //List of Visited Restaurants by Group (same group as the user) We will use this
    //list to compare it to the markers (to dra them differently if the restaurant
    // has already been visited by somebody of the group
    private List<String> listOfVisitedRestaurantsByTheUsersGroup;

    //List used to store the markers of the map. It will be used to check if
    //the map has already markers and, if so, not call a function
    private List<Marker> listOfMarkers;

    //Used for Retrofit
    private LatLngForRetrofit myPosition;

    //ViewModel to get info from the database
    private MainViewModel mapFragmentViewModel;

    //SharedPreferences
    private SharedPreferences sharedPref;

    //Firebase Database
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    //Local Database
    private AppDatabase localDatabase;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userIdKey;
    private String userGroup;
    private String userGroupKey;

    //Array of restaurant types (got from Resources, strings)
    private String[] arrayOfTypes;

    //Widgets
    @BindView(R.id.map_autocomplete_id)
    AutoCompleteTextView autocompleteTextView;

    @BindView(R.id.map_main_toolbar_id)
    Toolbar toolbar;

    @BindView(R.id.map_toolbar_search_id)
    RelativeLayout toolbar2;

    private ActionBar actionBar;

    @BindView(R.id.map_fragment_refresh_button_id)
    ImageButton buttonRefreshMap;

    @BindView(R.id.map_fragment_database_button_id)
    ImageButton buttonDatabase;
    @BindView(R.id.map_fragment_parent_relative_layout)
    RelativeLayout mapFragmentRelativeLayout;

    @BindView(R.id.map_progress_bar)
    ProgressBar progressBar;

    //Disposables
    private Disposable autocompleteTextViewDisposable;
    private Disposable textSearchDisposable;
    private Disposable placeIdDisposable;
    private Disposable distanceMatrixDisposable;
    private Disposable nearbyDisposable;

    //Observers
    private MaybeObserver restaurantsObserver;

    //Internal Storage
    private Storage storage;
    private String mainPath;
    private String imageDirPath;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentRestaurantMapViewTRIAL2 newInstance() {
        FragmentRestaurantMapViewTRIAL2 fragment = new FragmentRestaurantMapViewTRIAL2();
        return fragment;
    }

    /******************
     * LIFECYCLE
     ********************/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);

        /**Butternife binding
         * */
        ButterKnife.bind(this, view);

        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.localDatabase = AppDatabase.getInstance(getActivity());
        this.fireDb = FirebaseDatabase.getInstance();

        /** We get an array of restaurant types from RESOURCES
         * */
        this.arrayOfTypes = getActivity().getResources().getStringArray(R.array.typesOfRestaurants);

        this.listOfVisitedRestaurantsByTheUsersGroup = new ArrayList<>();
        this.listOfAllRestaurantsInDatabase = new ArrayList<>();
        this.listOfMarkers = new ArrayList<>();

        if (((AppCompatActivity) getActivity()) != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (((AppCompatActivity) getActivity()) != null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        /** We get all the user information
         * */
        this.auth = FirebaseAuth.getInstance();
        this.currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {

                            if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                                userFirstName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                                userLastName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                                userIdKey = item.getKey();
                                userGroup = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue()).toString();
                                userGroupKey = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                                Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY, userIdKey);

                                dbRefGroups = fireDb.getReference(
                                        RepoStrings.FirebaseReference.GROUPS
                                                + "/" + userGroupKey
                                                + "/" + RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);
                                dbRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                        listOfVisitedRestaurantsByTheUsersGroup = UtilsFirebase.fillListWithGroupRestaurantsUsingDataSnapshot(dataSnapshot);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(TAG, "onCancelled: " + databaseError.getCode());

                                    }
                                });
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

        /** Configuration process
         * */
        this.configureAutocompleteTextView(autocompleteTextView, autocompleteTextViewDisposable);
        this.configureInternalStorage(getActivity());

        /**
         * STARTING THE MAP:
         * First, we check that the user has the correct Google Play Services Version.
         * If the user does, we start the map
         * **/
        if (isGooglePlayServicesOK()) {
            getLocationPermission();
        }

        /*************************
         * LISTENERS *************
         * **********************/

        buttonRefreshMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: refresh button clicked!");

                ToastHelper.toastShort(getActivity(), "Refresh Button clicked! Starting requests process");
                startHttpRequestProcess();

            }
        });

        // TODO: 07/06/2018 Delete!
        buttonDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: database button clicked!");

                startActivity(new Intent(getActivity(), AndroidDatabaseManager.class));
            }
        });

        return view;
    }

    /** disposeWhenDestroy() avoids memory leaks
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    private void disposeWhenDestroy () {
        dispose(this.autocompleteTextViewDisposable);
        dispose(this.textSearchDisposable);
        dispose(this.placeIdDisposable);
        dispose(this.distanceMatrixDisposable);
        dispose(this.nearbyDisposable);

    }

    /**************************
     * MAP RELATED METHODS ****
     * ***********************/

    /**
     * Checks if the user has the correct
     * Google Play Services Version
     */
    public boolean isGooglePlayServicesOK() {
        Log.d(TAG, "isGooglePlayServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isGooglePlayServicesOK: Google Play Services is working");
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //There is an error but we can resolve it
            Log.d(TAG, "isGooglePlayServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Log.d(TAG, "isGooglePlayServicesOK: an error occurred; you cannot make map requests");
            ToastHelper.toastLong(getActivity(), "You can't make map requests");

        }
        return false;
    }

    /**
     * Method used to get the necessary permissions to get device location
     * */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permission");

        /**
         * We can also check first if the Android Version of the device is equal or higher than Marshmallow:
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { "rest of code" } */

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(
                this.getActivity().getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(
                    this.getActivity().getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLocationPermission: locationPermission Granted!; initializing map");
                mLocationPermissionGranted = true;
                initMap();
            }

        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, RepoConstants.RequestsCodes.REQ_CODE_LOCATION_PERMISSION_);

        }
    }

    /**
     * Method used to initialise the map
     */
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: map is ready");
                ToastHelper.toastShort(getActivity(), "Map is ready");
                mMap = googleMap;

                if (mLocationPermissionGranted) {
                    getDeviceLocation();

                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    mMap.setMyLocationEnabled(true); //displays the blue marker at your location
                    //mMap.getUiSettings().setMyLocationButtonEnabled(false); this would remove the button that allows you to center your position

                }

                /**
                 * Listener for when clicking the info window in a map
                 * */
                if (mMap != null) {
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());

                            for (int i = 0; i < listOfAllRestaurantsInDatabase.size(); i++) {

                                if (marker.getTitle().equalsIgnoreCase(listOfAllRestaurantsInDatabase.get(i).getName())) {

                                    Intent intent = new Intent(getActivity(), RestaurantActivity.class);

                                    Map <String,Object> map = new HashMap<>();
                                    map.put(RepoStrings.SentIntent.RESTAURANT_NAME, listOfAllRestaurantsInDatabase.get(i).getName());
                                    map.put(RepoStrings.SentIntent.RESTAURANT_TYPE, listOfAllRestaurantsInDatabase.get(i).getType());
                                    map.put(RepoStrings.SentIntent.ADDRESS, listOfAllRestaurantsInDatabase.get(i).getAddress());
                                    map.put(RepoStrings.SentIntent.RATING, listOfAllRestaurantsInDatabase.get(i).getRating());
                                    map.put(RepoStrings.SentIntent.PHONE, listOfAllRestaurantsInDatabase.get(i).getPhone());
                                    map.put(RepoStrings.SentIntent.WEBSITE_URL, listOfAllRestaurantsInDatabase.get(i).getWebsiteUrl());
                                    map.put(RepoStrings.SentIntent.IMAGE_URL, listOfAllRestaurantsInDatabase.get(i).getImageBitmap());

                                    Utils.fillIntentUsingMapInfo(intent, map);

                                    startActivity(intent);
                                    break;

                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Method used to get the user's location
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device's current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

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

                        moveCamera(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM);

                        /** We start the request process if the database is empty
                         * */
                        initRequestProcessIfNecessary();

                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                    }

                }
            });

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }




    /**
     * Method used to move the camera in the map
     */
    private void moveCamera(LatLng latLng, float zoom) {

        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }

    /**************************
     * MENU METHODS ***********
     * ***********************/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                Log.d(TAG, "onOptionsItemSelected: home clicked");
                if (((MainActivity) getActivity()) != null) {
                    ((MainActivity) getActivity()).getMDrawerLayout().openDrawer(GravityCompat.START);
                }
                return true;
            }

            case R.id.map_search_button_id: {
                Log.d(TAG, "onOptionsItemSelected: search button clicked");

                toolbar.setVisibility(View.GONE);
                Anim.crossFadeShortAnimation(toolbar2);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that returns true if local database is empty
     * */
    public boolean localDatabaseIsEmpty() {
        Log.d(TAG, "localDatabaseIsEmpty: called!");

        DatabaseHelper dbH = new DatabaseHelper(getActivity());
        return dbH.isTableEmpty("restaurant");

    }

    /**************************
     * REQUEST PERMISSIONS ****
     * ***********************/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {

            case RepoConstants.RequestsCodes.REQ_CODE_LOCATION_PERMISSION_: {
                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            ToastHelper.toastShort(getActivity(), "Location Permission NOT granted");

                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }

                    }
                    //if everything is ok (all permissions are granted),
                    // we want to initialise the map
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    ToastHelper.toastShort(getActivity(), "Location Permission GRANTED");
                }
            } break;

            case RepoConstants.RequestsCodes.REQ_CODE_WRITE_EXTERNAL_PERMISSION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ToastHelper.toastShort(getActivity(), "Write to Storage Permission NOT granted");

                } else {
                    ToastHelper.toastShort(getActivity(), "Write to Storage Permission granted");
                }

            } break;
        }
    }

//    // TODO: 10/05/2018 Explain better
//    /** stopAutoManage() is used to avoid the app to crash when coming back to the
//     * fragment*/
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//}

    // TODO: 21/06/2018
    // TODO: 21/06/2018

    /********************
     * CONFIGURATION
     * *****************/

    /** Method that configures the autocompleteTextView
     * */
    private void configureAutocompleteTextView (AutoCompleteTextView autoCompleteTextView,
                                                Disposable disposable) {

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1, //This layout has to be a textview
                arrayOfTypes
        );

        autoCompleteTextView.setAdapter(autocompleteAdapter);
        disposable = RxTextView.textChangeEvents(autoCompleteTextView)
                .skip(2)
                .debounce(600, TimeUnit.MILLISECONDS)
                .map(new Function<TextViewTextChangeEvent, String>() {
                    @Override
                    public String apply(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                        return textViewTextChangeEvent.text().toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String type) {
                        Log.d(TAG, "onNext: type = " + type);
                        Log.d(TAG, "onNext: int = " + getTypeAsStringAndReturnTypeAsInt(type));

                        if (Arrays.asList(arrayOfTypes).contains(type)) {
                            getRestaurantsByTypeAndDisplayPins(getTypeAsStringAndReturnTypeAsInt(type));

                        } else {
                            updateMapWithPins(
                                    listOfAllRestaurantsInDatabase,
                                    listOfVisitedRestaurantsByTheUsersGroup);
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

    /** Method that sets the directory variables and creates the directory that will
     * store images if needed
     * */
    private void configureInternalStorage (Context context) {
        Log.d(TAG, "configureInternalStorage: ");

        // TODO: 24/06/2018 Do here the request
        getInternalStorageAccessPermission();

        storage = new Storage(context);
        mainPath = storage.getInternalFilesDirectory() + File.separator;
        imageDirPath = mainPath + File.separator + RepoStrings.Directories.IMAGE_DIR;

        if (!storage.isDirectoryExists(imageDirPath)) {
            Log.d(TAG, "configureInternalStorage: imageDir does not exist. Creating directory...");
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: creating directory...");
                    boolean isCreated = storage.createDirectory(imageDirPath);
                    Log.d(TAG, "run: directory created = " + isCreated);
                }
            });

        } else {
            Log.d(TAG, "configureInternalStorage: imageDir already exists!");
            //do nothing

        }
    }




    /******************************************************
     * INTERNAL STORAGE
     *****************************************************/

    private void getInternalStorageAccessPermission () {

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RepoConstants.RequestsCodes.REQ_CODE_WRITE_EXTERNAL_PERMISSION);
            }
        }
    }

    /** Saves an image in the internal storage using a background thread
     * */
    public void saveImageInInternalStorage (final String filePath, final Bitmap bitmap) {
        Log.d(TAG, "saveImageInInternalStorage: called!");

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: Saving file...");
                boolean isFileCreated = storage.createFile(imageDirPath + File.separator + filePath, bitmap);
                Log.d(TAG, "run: file saved = " + isFileCreated);

            }
        });
    }

    /******************************************************
     * RX JAVA
     *****************************************************/

    /** Method used to avoid memory leaks
     * */
    private void dispose (Disposable disposable) {
        if (disposable != null
                && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    //****************************************************
    // INTERNAL STORAGE
    //****************************************************

    /** Used to read an image from the internal storage and convert it to bitmap so that
     * it the image can be stored in a RestaurantEntry and be displayed later using glide
     * in the recyclerView
     * */
    private Observable<byte[]> getObservableImageFromInternalStorage (String filePath) {
        return Observable.just(storage.readFile(filePath));
    }


    //****************************************************
    // FETCH DATA, MODIFY DATA from LOCAL DATABASE
    //****************************************************

    /* OBSERVABLES /

    /** Method that returns all restaurants in the database
     * */
    private Maybe<List<RestaurantEntry>> getAllRestaurantsInDatabase () {
        return localDatabase.restaurantDao().getAllRestaurantsRxJava();
    }

    /** Method that returns all restaurants in database of a specific type
     * */
    private Maybe<List<RestaurantEntry>> getRestaurantsByType(int type) {
        if (type == 0) {
            return localDatabase.restaurantDao().getAllRestaurantsRxJava();
        } else {
            return localDatabase.restaurantDao().getAllRestaurantsByTypeRxJava(String.valueOf(type));
        }
    }

    /* OTHERS /

    /** Method to insert a restaurant in the database
     * */
    private long insertRestaurantEntryInDatabase (RestaurantEntry restaurantEntry) {
        return localDatabase.restaurantDao().insertRestaurant(restaurantEntry);
    }

    /** Update methods
     * */
    private int updateGeneralRestaurantInfoUsingPlaceId(String placeId, String phone, String websiteUrl, String openUntil) {
        return localDatabase.restaurantDao().updateRestaurantGeneralInfo(placeId, phone, websiteUrl, openUntil);
    }

    public int updateDistanceToRestaurantUsingPlaceId (String placeId, String distanceValue) {
        return localDatabase.restaurantDao().updateRestaurantDistance(placeId, distanceValue);
    }

    public int updateRestaurantPhotoUsingPlaceId(String placeId, String photoUrl) {
        return localDatabase.restaurantDao().updateRestaurantPhoto(placeId, photoUrl);
    }

    //********************************
    // INTERACT WITH DATA
    //********************************

    /** Method that fetches all the restaurants from the database
     * and displays the proper pins in the map
     * */
    private void getAllRestaurantsAndDisplayPins () {

        restaurantsObserver = getAllRestaurantsInDatabase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getMaybeObserverToUpdateMap());

    }

    /** Method that fetches the restaurants from the database according to the type inputted
     * and displays the proper pins in the map
     * */
    private void getRestaurantsByTypeAndDisplayPins(final int type) {

        restaurantsObserver = getRestaurantsByType(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getMaybeObserverToUpdateMap());

    }

    /** Method that checks if the database is empty. If so, it starts the
     * request process
     * */
    private void initRequestProcessIfNecessary() {

        restaurantsObserver = getAllRestaurantsInDatabase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getMaybeObserverToStartRequestProcess());
    }

    private void getAllRestaurantsAndFilter () {

        restaurantsObserver = getAllRestaurantsInDatabase()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeWith(getMaybeObserverToFilterNearbyPlaces());

    }

    //********************************
    // OBSERVERS
    //********************************

    /** Observer that helps update the pins in the map
     * */
    private MaybeObserver<List<RestaurantEntry>> getMaybeObserverToUpdateMap() {

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntryList) {
                Log.d(TAG, "onSuccess: " + restaurantEntryList.toString());

                // TODO: 21/06/2018 Update this method!
                updateMapWithPins(restaurantEntryList,
                        listOfVisitedRestaurantsByTheUsersGroup);

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        };
    }

    /** Observer that starts the request process if the database is empty
     * */
    private MaybeObserver<List<RestaurantEntry>> getMaybeObserverToStartRequestProcess() {

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntryList) {
                Log.d(TAG, "onSuccess: " + restaurantEntryList.toString());

                if (restaurantEntryList.size() == 0) {
                    startHttpRequestProcess();
                } else {
                    updateMapWithPins(restaurantEntryList,
                            listOfVisitedRestaurantsByTheUsersGroup);
                }


                // TODO: 21/06/2018 Update this method!
                updateMapWithPins(restaurantEntryList,
                        listOfVisitedRestaurantsByTheUsersGroup);

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        };
    }

    private MaybeObserver<List<RestaurantEntry>> getMaybeObserverToFilterNearbyPlaces () {

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntryList) {
                Log.d(TAG, "onSuccess: " + restaurantEntryList.toString());

                nearbyDisposable =
                        GoogleServiceStreams.streamFetchPlacesNearby(
                                myPosition,
                                "distance",
                                "restaurant",
                                NEARBY_KEY)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribeWith(getPlacesByNearbyAndFilter(
                                        restaurantEntryList));

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        };
    }

    /** Observer called after starting the Http request process.
     * It inserts restaurants by type in the database
     * */
    private DisposableObserver<PlacesByTextSearch> getAllRestaurantsDisposableObserver (final int type) {

        return new DisposableObserver<PlacesByTextSearch>() {
            @Override
            public void onNext(PlacesByTextSearch placesByTextSearch) {
                Log.d(TAG, "onNext: " + placesByTextSearch.toString());

                List<Result> listOfResults = placesByTextSearch.getResults();

                for (int i = 0; i < listOfResults.size(); i++) {

                    insertRestaurantEntryInDatabase(new RestaurantEntry(
                            Utils.checkToAvoidNull(listOfResults.get(i).getPlaceId()),
                            Utils.checkToAvoidNull(listOfResults.get(i).getName()),
                            type,
                            Utils.checkToAvoidNull(listOfResults.get(i).getFormattedAddress()),
                            "",
                            "",
                            Utils.checkToAvoidNull(listOfResults.get(i).getRating()),
                            null,
                            "",
                            "",
                            "",
                            Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLat().toString()),
                            Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLng().toString())));

                    placeIdDisposable =
                            GoogleServiceStreams.streamFetchPlaceById(
                                    listOfResults.get(i).getPlaceId(),
                                    RepoStrings.Keys.PLACEID_KEY)
                                    .subscribeWith(getPlaceByIdDisposableObserver());

                }

                if (type == 12) {
                    // if type is 12, then is type = Vietnamese which is the last one (before Other)
                    // This guarantees all restaurants are already in the database
                    getAllRestaurantsAndFilter();

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
        };
    }

    /** Disposable observer which function is to
     * get a place by Id
     * */
    private DisposableObserver<PlaceById> getPlaceByIdDisposableObserver () {

        return new DisposableObserver<PlaceById>() {
            @Override
            public void onNext(PlaceById placeById) {
                Log.d(TAG, "onNext: " + placeById.toString());

                final com.example.android.goforlunch.newfetchingsystem.newmodels.placebyid.Result result = placeById.getResult();

                String closingTime = checkClosingTime(result);

                updateGeneralRestaurantInfoUsingPlaceId(
                        Utils.checkToAvoidNull(result.getPlaceId()),
                        Utils.checkToAvoidNull(result.getInternationalPhoneNumber()),
                        Utils.checkToAvoidNull(result.getWebsite()),
                        closingTime);

                Log.d(TAG, "onNext: This is reached !!!!!!!!!!!!!!!!!!");

                distanceMatrixDisposable =
                        GoogleServiceStreams.streamFetchDistanceMatrix(
                                "imperial",
                                "place_id:" + result.getPlaceId(),
                                myPosition,
                                RepoStrings.Keys.MATRIX_DISTANCE_KEY)
                                .subscribeWith(getDistanceMatrixDisposableObserver(result.getPlaceId()));

                Log.d(TAG, "onNext: result.getPhotos() = " + result.getPhotos());

                if (null != result.getPhotos()) {
                    Log.d(TAG, "onNext: result.getPhotos != null");

                    // TODO: 24/06/2018 do here


                    GoogleService googleService = AllGoogleServices.getGooglePlacePhotoService();
                    Call<String> callPhoto = googleService.fetchDataPhoto(
                            "400",
                            result.getPhotos().get(0).getPhotoReference(),
                            RepoStrings.Keys.PHOTO_KEY);
                    callPhoto.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, final Response<String> response) {
                            Log.d(TAG, "onResponse: correct call");
                            Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    updateRestaurantPhotoUsingPlaceId(result.getPlaceId(), response.body());
                                }
                            });

                        }

                        @Override
                        public void onFailure(final Call<String> call, Throwable t) {
                            Log.e(TAG, "onFailure: url = " + call.request().url().toString());
                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    updateRestaurantPhotoUsingPlaceId(result.getPlaceId(), call.request().url().toString());
                                }
                            });
                        }
                    });

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
        };
    }

    /** Disposable observer which function is
     * to get the distance from user's position
     * to the restaurant
     * */
    private DisposableObserver<DistanceMatrix> getDistanceMatrixDisposableObserver (final String placeId) {

        return new DisposableObserver<DistanceMatrix>() {
            @Override
            public void onNext(DistanceMatrix distanceMatrix) {
                Log.d(TAG, "onNext: " + distanceMatrix.toString());

                updateDistanceToRestaurantUsingPlaceId(
                        placeId,
                        Utils.checkToAvoidNull(distanceMatrix.getRows().get(0).getElements().get(0).getDistance().getText()));
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: getDistanceMatrixDisposableObserver");
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        };

    }

    /** Disposable observer used to include other restaurants
     * in the database
     * */
    private DisposableObserver<PlacesByNearby> getPlacesByNearbyAndFilter (final List<RestaurantEntry> restaurantEntryList) {

        return new DisposableObserver<PlacesByNearby>() {
            @Override
            public void onNext(PlacesByNearby placesByNearby) {
                Log.d(TAG, "onNext: " + placesByNearby.toString());

                List<String> listOfPlaceIds = new ArrayList<>();

                for (int i = 0; i < restaurantEntryList.size(); i++) {
                    listOfPlaceIds.add(restaurantEntryList.get(i).getPlaceId());
                }

                List<com.example.android.goforlunch.newfetchingsystem.newmodels.placebynearby.Result> listOfResults = placesByNearby.getResults();

                for (int i = 0; i < listOfResults.size(); i++) {

                    if (!listOfPlaceIds.contains(listOfResults.get(i).getPlaceId())) {

                        insertRestaurantEntryInDatabase(new RestaurantEntry(
                                Utils.checkToAvoidNull(listOfResults.get(i).getPlaceId()),
                                Utils.checkToAvoidNull(listOfResults.get(i).getName()),
                                13,
                                Utils.checkToAvoidNull(listOfResults.get(i).getVicinity()),
                                "",
                                "",
                                Utils.checkToAvoidNull(listOfResults.get(i).getRating()),
                                null,
                                "",
                                "",
                                "",
                                Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLat().toString()),
                                Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLng().toString())));

                        placeIdDisposable =
                                GoogleServiceStreams.streamFetchPlaceById(
                                        listOfResults.get(i).getPlaceId(),
                                        RepoStrings.Keys.PLACEID_KEY)
                                        .subscribeWith(getPlaceByIdDisposableObserver());

                    }
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
        };
    }

    //********************************
    // RETROFIT: Http Requests
    //********************************

    private void startHttpRequestProcess() {

        for (int i = 1; i < arrayOfTypes.length - 1; i++) { //-1 because we don't want to fetch "type OTHER" restaurants

            this.textSearchDisposable =
                    GoogleServiceStreams.streamFetchPlacesTextSearch(
                            arrayOfTypes[i] + "+" + "Restaurant",
                            myPosition,
                            20,
                            RepoStrings.Keys.TEXTSEARCH_KEY)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribeWith(getAllRestaurantsDisposableObserver(
                                    getTypeAsStringAndReturnTypeAsInt(arrayOfTypes[i])));

        }


    }

    /********************
     * UPDATE UI
     * *****************/

    /** Method that updates the map with pins
     * */
    public void updateMapWithPins(List<RestaurantEntry> restaurantEntryList,
                                  List<String> listOfVisitedRestaurantsByTheUsersGroup) {

        if (mMap != null) {
            Log.d(TAG, "fillMapWithMarkers: the Map is not null");

            if (restaurantEntryList != null
                    && !restaurantEntryList.isEmpty()) {
                Log.d(TAG, "displayPinsInMap: listOfRestaurants IS NOT NULL and IS NOT EMPTY");

                /** We delete all the elements of the listOfMarkers
                 * */
                listOfMarkers.clear();
                mMap.clear();

                for (int i = 0; i < restaurantEntryList.size(); i++) {

                    MarkerOptions options;

                    LatLng latLng = new LatLng(
                            Double.parseDouble(restaurantEntryList.get(i).getLatitude()),
                            Double.parseDouble(restaurantEntryList.get(i).getLongitude()));

                    if (listOfVisitedRestaurantsByTheUsersGroup.contains(restaurantEntryList.get(i).getName())) {
                        Log.d(TAG, "displayPinsInMap: The place has been visited by somebody before");

                        options = new MarkerOptions()
                                .position(latLng)
                                .title(restaurantEntryList.get(i).getName())
                                .snippet(restaurantEntryList.get(i).getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); //Different colour
                    } else {
                        Log.d(TAG, "displayPinsInMap: The place has not been visited yet");

                        options = new MarkerOptions()
                                .position(latLng)
                                .title(restaurantEntryList.get(i).getName())
                                .snippet(restaurantEntryList.get(i).getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    }

                    /** We fill the listOfMarkers and the map with the markers
                     * */
                    listOfMarkers.add(mMap.addMarker(options));

                }
            }

        } else {
            ToastHelper.toastShort(getActivity(), "The map is not ready...");
        }

    }

    /********************
     * HELPER METHODS
     * *****************/

    /** Method that transforms a restaurant type from String to an int
     * */
    public int getTypeAsStringAndReturnTypeAsInt (String type) {

        if (Arrays.asList(arrayOfTypes).contains(type)) {
            return Arrays.asList(arrayOfTypes).indexOf(type);
        } else return 0;

    }

}
