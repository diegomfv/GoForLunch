package com.example.android.goforlunch.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.data.sqlite.DatabaseHelper;
import com.example.android.goforlunch.data.viewmodel.MainViewModel;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.utils.UtilsConfiguration;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.network.models.distancematrix.DistanceMatrix;
import com.example.android.goforlunch.network.models.placebyid.PlaceById;
import com.example.android.goforlunch.network.models.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.network.models.placebynearby.PlacesByNearby;
import com.example.android.goforlunch.network.models.placetextsearch.PlacesByTextSearch;
import com.example.android.goforlunch.network.models.placetextsearch.Result;
import com.example.android.goforlunch.network.remote.AllGoogleServices;
import com.example.android.goforlunch.network.remote.GoogleService;
import com.example.android.goforlunch.network.remote.GoogleServiceStreams;
import com.example.android.goforlunch.constants.Repo;
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
import butterknife.Unbinder;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.goforlunch.utils.UtilsRemote.checkClosingTime;
import static com.example.android.goforlunch.constants.Repo.Keys.NEARBY_KEY;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the Google Map
 * */
public class FragmentRestaurantMapView extends Fragment {

    /* Interface to communicate with Main Activity
     * */
    public interface OnCurrentPositionObtainedListener {
        void onCurrentPositionObtained (LatLngForRetrofit myPosition);
    }

    //////////////////////////////

    private static final String TAG = FragmentRestaurantMapView.class.getSimpleName();

    //ERROR that we are going to handle if the user doesn't have the correct version of the
    //Google Play Services
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 17f;

    //Map variables
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

    //Butterknife
    private Unbinder unbinder;

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

    @BindView(R.id.map_fragment_parent_relative_layout)
    RelativeLayout mapFragmentRelativeLayout;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarFragmentContent;

    //Disposables
    private Disposable autocompleteTextViewDisposable;
    private Disposable textSearchDisposable;
    private Disposable placeIdDisposable;
    private Disposable distanceMatrixDisposable;
    private Disposable nearbyDisposable;
    private Disposable deleteDisposable;

    //Observers
    private MaybeObserver restaurantsObserver;

    //Internal Storage
    private Storage storage;
    private String mainPath;
    private String imageDirPath;
    private boolean accessInternalStorageGranted;

    //Listener: communicates from Main Activity (for when a search is started)
    OnCurrentPositionObtainedListener mCallback;


    /** ------------------------------------------------ */

    /** Method for instantiating the fragment
     * */
    public static FragmentRestaurantMapView newInstance() {
        Log.d(TAG, "newInstance: called!");
        FragmentRestaurantMapView fragment = new FragmentRestaurantMapView();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);

        /* Butterknife binding
         * */
        unbinder = ButterKnife.bind(this, view);

        /* Storage configuration
         * */
        storage = new Storage(getActivity());
        mainPath = storage.getInternalFilesDirectory() + File.separator;
        imageDirPath = mainPath + File.separator + Repo.Directories.IMAGE_DIR;

        /* Configure databases
         * */
        this.configureDatabases(getActivity());
        Log.d(TAG, "onCreate: " + sharedPref.getAll().toString());

        /* Configure toolbar
         * */
        UtilsConfiguration.configureActionBar(getActivity(), toolbar, actionBar);

//        if (((AppCompatActivity) getActivity()) != null) {
//            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        }
//
//        if (((AppCompatActivity) getActivity()) != null) {
//            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
//        }

        /* We get an array of restaurant types from RESOURCES
         * */
        this.arrayOfTypes = getActivity().getResources().getStringArray(R.array.typesOfRestaurants);

        this.listOfVisitedRestaurantsByTheUsersGroup = new ArrayList<>();
        this.listOfAllRestaurantsInDatabase = new ArrayList<>();
        this.listOfMarkers = new ArrayList<>();

        /* Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        /* We get all the user information
         * */
        this.currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {

                            if (Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                                userFirstName = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                                userLastName = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                                userIdKey = item.getKey();
                                userGroup = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_GROUP).getValue()).toString();
                                userGroupKey = Objects.requireNonNull(item.child(Repo.FirebaseReference.USER_GROUP_KEY).getValue()).toString();

                                UtilsGeneral.updateSharedPreferences(sharedPref, Repo.SharedPreferences.USER_ID_KEY, userIdKey);

                                /* STARTING THE MAP:
                                 * First, we check that the user has the correct Google Play Services Version.
                                 * If the user does, we start the map*/
                                if (isGooglePlayServicesOK()) {
                                    getLocationPermission();
                                }

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

        /* Configuration process */
        this.configureAutocompleteTextView(autocompleteTextView, autocompleteTextViewDisposable);
        this.getPermissionsProcess();

//        /* STARTING THE MAP:
//         * First, we check that the user has the correct Google Play Services Version.
//         * If the user does, we start the map*/
//        if (isGooglePlayServicesOK()) {
//            getLocationPermission();
//        }

        /*************************
         * LISTENERS *************
         * **********************/

        buttonRefreshMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: refresh button clicked!");
                ToastHelper.toastShort(getActivity(), "Refresh Button clicked! Nothing happens...");
                //deleteAllFilesInStorage();
                //deleteAllRestaurantsAndStartRequestProcess();

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: called!");
        unbinder.unbind();
    }

    /** We prepare the callback for listening to changes in Position
     * */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called!");

        try {
            mCallback = (OnCurrentPositionObtainedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener\")");
        }
    }

    /** When the fragment is detached from the activity, we nullify the callback
     * */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    /** disposeWhenDestroy() avoids memory leaks
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
        this.disposeWhenDestroy();

    }

    private void disposeWhenDestroy () {
        Log.d(TAG, "disposeWhenDestroy: called!");
        UtilsGeneral.dispose(this.autocompleteTextViewDisposable);
        UtilsGeneral.dispose(this.textSearchDisposable);
        UtilsGeneral.dispose(this.placeIdDisposable);
        UtilsGeneral.dispose(this.distanceMatrixDisposable);
        UtilsGeneral.dispose(this.nearbyDisposable);

    }

    /**************************
     * REQUEST PERMISSIONS ****
     * ***********************/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {

            /* Location Permission request code */
            case Repo.RequestsCodes.REQ_CODE_LOCATION_PERMISSION_: {
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

            /* Write to storage request code */
            case Repo.RequestsCodes.REQ_CODE_WRITE_EXTERNAL_PERMISSION: {
                accessInternalStorageGranted = false;

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ToastHelper.toastShort(getActivity(), "Write to Storage Permission GRANTED");
                    accessInternalStorageGranted = true;

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

                    /* After storage permission process, we ask for device location permissions
                     * */
                    if (isGooglePlayServicesOK()) {
                        getLocationPermission();
                    }


                } else {
                    ToastHelper.toastShort(getActivity(), getActivity().getResources().getString(R.string.storageGranted));
                    accessInternalStorageGranted = false;

                    /* After storage permission process, we ask for device location permissions
                     * */
                    if (isGooglePlayServicesOK()) {
                        getLocationPermission();
                    }
                }

            } break;
        }
    }

    /**************************
     * MAP RELATED METHODS ****
     * ***********************/

    /** This method starts both, internal storage permission process and
     * device location permission process
     * */
    public void getPermissionsProcess () {
        Log.d(TAG, "getPermissionsProcess: called!");
        this.getInternalStorageAccessPermission();
    }

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
            ActivityCompat.requestPermissions(getActivity(), permissions, Repo.RequestsCodes.REQ_CODE_LOCATION_PERMISSION_);

        }
    }

    /**
     * Method used to initialise the map
     */
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        progressBarFragmentContent.setVisibility(View.GONE);
        mapFragmentRelativeLayout.setVisibility(View.VISIBLE);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: map is ready");
                ToastHelper.toastShort(getActivity(), "Map is ready");
                mMap = googleMap;

                if (mLocationPermissionGranted) {

                    /* We share myPosition with MainActivity.
                     * */
                    // TODO: 30/07/2018 Check this
                    mCallback.onCurrentPositionObtained(myPosition);

                    getDeviceLocation();

                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    /* Updating the map
                    * */
                    updateMapWithPins();

                    mMap.setMyLocationEnabled(true); //displays the blue marker at your location
                    //mMap.getUiSettings().setMyLocationButtonEnabled(false); this would remove the button that allows you to center your position

                }

                /*Listener for when clicking the info window in a map
                 * */
                if (mMap != null) {
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());

                            if (listOfAllRestaurantsInDatabase.size() > 0) {

                                for (int i = 0; i < listOfAllRestaurantsInDatabase.size(); i++) {

                                    if (marker.getTitle().equalsIgnoreCase(listOfAllRestaurantsInDatabase.get(i).getName())) {

                                        Intent intent = new Intent(getActivity(), RestaurantActivity.class);

                                        Map <String,Object> map = new HashMap<>();
                                        map.put(Repo.SentIntent.RESTAURANT_NAME, listOfAllRestaurantsInDatabase.get(i).getName());
                                        map.put(Repo.SentIntent.RESTAURANT_TYPE, listOfAllRestaurantsInDatabase.get(i).getType());
                                        map.put(Repo.SentIntent.PLACE_ID, listOfAllRestaurantsInDatabase.get(i).getPlaceId());
                                        map.put(Repo.SentIntent.ADDRESS, listOfAllRestaurantsInDatabase.get(i).getAddress());
                                        map.put(Repo.SentIntent.RATING, listOfAllRestaurantsInDatabase.get(i).getRating());
                                        map.put(Repo.SentIntent.PHONE, listOfAllRestaurantsInDatabase.get(i).getPhone());
                                        map.put(Repo.SentIntent.WEBSITE_URL, listOfAllRestaurantsInDatabase.get(i).getWebsiteUrl());
                                        map.put(Repo.SentIntent.IMAGE_URL, listOfAllRestaurantsInDatabase.get(i).getImageUrl());

                                        UtilsGeneral.fillIntentUsingMapInfo(intent, map);

                                        startActivity(intent);
                                        break;

                                    }
                                }
                            } else {
                                Log.d(TAG, "onInfoWindowClick: listOfAllRestaurantsInDatabase is EMPTY");
                                ToastHelper.toastShort(getActivity(), "Please, wait till the system updates...");

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

                        /* We share myPosition with MainActivity
                        * */
                        Log.d(TAG, "onComplete: Sending position to MainActivity");
                        mCallback.onCurrentPositionObtained(myPosition);

                        moveCamera(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM);

                        /** We start the request process if the database is empty
                         * */
                        //initRequestProcessIfNecessary();

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
        Log.d(TAG, "onCreateOptionsMenu: called!");

        getActivity().getMenuInflater().inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called!");

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
                Anim.showCrossFadeShortAnimation(toolbar2);
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

    /********************
     * CONFIGURATION
     * *****************/

    /** Method that instantiates databases
     * */
    public void configureDatabases (Context context) {

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        localDatabase = AppDatabase.getInstance(context);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

    }

    /** Method that configures the autocompleteTextView
     * */
    private void configureAutocompleteTextView (AutoCompleteTextView autoCompleteTextView,
                                                Disposable disposable) {
        Log.d(TAG, "configureAutocompleteTextView: called!");

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

                        /* We update the map
                        * */
                        updateMapWithPins();

                        UtilsGeneral.hideKeyboard(getActivity());

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


    /******************************************************
     * INTERNAL STORAGE
     *****************************************************/

    /** Method that launches a dialog asking for storage permissions if they have not been
     * granted before
     * */
    private void getInternalStorageAccessPermission () {
        Log.d(TAG, "getInternalStorageAccessPermission: called!");

        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            accessInternalStorageGranted = true;

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Repo.RequestsCodes.REQ_CODE_WRITE_EXTERNAL_PERMISSION);
            }
        }
    }

    /** Saves an image in the
     * internal storage using a background thread
     * */
    public void saveImageInInternalStorage (final String filePath, final Bitmap bm) {
        Log.d(TAG, "saveImageInInternalStorage: called!");

           if (filePath != null && bm != null) {

               AppExecutors.getInstance().diskIO().execute(new Runnable() {
                   @Override
                   public void run() {
                       Log.d(TAG, "run: Saving file...");
                       boolean isFileCreated = storage.createFile(imageDirPath + File.separator + filePath, bm);
                       Log.d(TAG, "run: file saved = " + isFileCreated);

                   }
               });

           }
    }

    /** Method that deletes all files in the internal storage: "imageDir" path */
    public void deleteAllFilesInStorage () {
        Log.d(TAG, "deleteAllFilesInStorage: called!");

        if (accessInternalStorageGranted) {
            Log.d(TAG, "deleteAllFilesInStorage: access to storage GRANTED!");

            if (storage.isDirectoryExists(imageDirPath)) {
                UtilsGeneral.printFiles(imageDirPath);

                /* We delete the directory (which will delete all the files in it)
                */
                storage.deleteDirectory(imageDirPath);
                UtilsGeneral.printFiles(imageDirPath);

                /* We create it again
                * */
                storage.createDirectory(imageDirPath);
                UtilsGeneral.printFiles(imageDirPath);

            } else {

                /* Since the directory does not exist yet, we create it
                * */
                storage.createDirectory(imageDirPath);
                UtilsGeneral.printFiles(imageDirPath);

            }

        } else {
            Log.d(TAG, "deleteAllFilesInStorage: access to internal storage NOT GRANTED!");

        }
    }

    /******************************************************
     * RX JAVA
     *****************************************************/

    /** Method that returns all restaurants in database of a specific type
     * */
    private Maybe<List<RestaurantEntry>> getRestaurantsByType(int type) {
        Log.d(TAG, "getRestaurantsByType: called!");
        if (type == 0) {
            return localDatabase.restaurantDao().getAllRestaurantsRxJava();
        } else {
            return localDatabase.restaurantDao().getAllRestaurantsByTypeRxJava(String.valueOf(type));
        }
    }

    /********************
     * UPDATE UI
     * *****************/

    /** Method that updates the map with pins.
     * It firstly fills gets the restaurants visited by the group and
     * afterwards all the restaurants in the database
     * */
    public void updateMapWithPins() {
        Log.d(TAG, "updateMapWithPins: called!");

        if (userGroupKey != null) {
            Log.i(TAG, "updateMapWithPins: userGroupKey is not null");

            dbRefGroups = fireDb.getReference(Repo.FirebaseReference.GROUPS)
                    .child(userGroupKey)
                    .child(Repo.FirebaseReference.GROUP_RESTAURANTS_VISITED);

            dbRefGroups.addListenerForSingleValueEvent(singleValueEventListenerGetRestaurantsVisited);

        } else {
            Log.i(TAG, "updateMapWithPins: userGroupKey is null");

        }
    }

    /** Listener for getting the restaurants visited by a group,
     * get all the restaurants in the database and start
     * filling the map
     * */
    private ValueEventListener singleValueEventListenerGetRestaurantsVisited = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            listOfVisitedRestaurantsByTheUsersGroup = UtilsFirebase.fillListWithGroupRestaurantsUsingDataSnapshot(dataSnapshot);

            /* We updateItem the map's pins
             * */
            if (mMap != null) {
                Log.d(TAG, "updateMapWithPins: the map is not null");

                String typeInEnglish = UtilsGeneral.getTypeInSpecificLanguage(getActivity(), autocompleteTextView.getText().toString().trim());
                //Returns "" if there is no type or type is null

                int typeAsInt = UtilsGeneral.getTypeAsStringAndReturnTypeAsInt(typeInEnglish);

                restaurantsObserver = getRestaurantsByType(typeAsInt)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(maybeObserverUpdateUI());

            } else {
                Log.d(TAG, "updateMapWithPins: the map IS NULL!");
                ToastHelper.toastShort(getActivity(), "The map is not ready...");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /** This Observer is used
     * to get all the restaurants in the database
     * */
    private MaybeObserver<List<RestaurantEntry>> maybeObserverUpdateUI() {
        Log.d(TAG, "getMaybeObserverThatStartsRequestProcessIfNecessary: called!");

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntryList) {
                Log.d(TAG, "onSuccess: " + restaurantEntryList.toString());

                if (!restaurantEntryList.isEmpty()) {
                    Log.i(TAG, "displayPinsInMap: listOfRestaurants IS NOT EMPTY");

                    /* We delete all the elements of the listOfMarkers and clear the map
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

                        /* We fill the listOfMarkers and the map with the markers
                         * */
                        listOfMarkers.add(mMap.addMarker(options));

                    }

                } else {
                    Log.d(TAG, "displayPinsInMap: listOfRestaurants IS EMPTY");
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
}
