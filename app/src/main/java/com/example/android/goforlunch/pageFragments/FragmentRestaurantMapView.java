package com.example.android.goforlunch.pageFragments;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.atl.ATLInitApiTextSearchRequests;
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
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.repostrings.RepoStrings;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the Google Map
 * */
public class FragmentRestaurantMapView extends Fragment {

    // TODO: 21/05/2018 Add Maps Button to restart search
    // TODO: 29/05/2018 When coming back, no markers are added

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;

    //Loaders id
    private static final int ID_LOADER_INIT_GENERAL_API_REQUESTS = 1;

    //Counter used to limit the times we try to start requests in case database is empty.
    private int startRequestsCounter = 0;

    //vars
    private boolean mLocationPermissionGranted = false; //used in permissions
    private GoogleMap mMap; //used to create the map
    private FusedLocationProviderClient mFusedLocationProviderClient; //used to get the location of the current user

    //Widgets
    private AutoCompleteTextView mSearchText;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;
    private ImageButton buttonRefreshMap;
    private ImageButton buttonDatabase;

    //List of Visited Restaurants by Group (same group as the user) We will use this
    //list to compare it to the markers (to dra them differently if the restaurant
    // has already been visited by somebody of the group
    private List<String> listOfVisitedRestaurantsByTheUsersGroup;

    //This map will store all the restaurants in the database in lists ordered by type
    private Map<String, List<RestaurantEntry>> mapOfListsOfRestaurantsByType;

    //Markers. Used to store the markers of the map. It will be used to check if
    //the map has already markers and, if so, not call a function
    private List<Marker> listOfMarkers;

    //Retrofit usage
    private LatLngForRetrofit myPosition;

    //Database
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
    private AppDatabase mDb;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userIdKey;
    private String userGroup;
    private String userGroupKey;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentRestaurantMapView newInstance() {
        FragmentRestaurantMapView fragment = new FragmentRestaurantMapView();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDb = AppDatabase.getInstance(getActivity());
        fireDb = FirebaseDatabase.getInstance();

        mapOfListsOfRestaurantsByType = new HashMap<>();
        listOfMarkers = new ArrayList<>();

        toolbar = (Toolbar) view.findViewById(R.id.map_main_toolbar_id);
        toolbar2 = (RelativeLayout) view.findViewById(R.id.map_toolbar_search_id);
        buttonRefreshMap = (ImageButton) view.findViewById(R.id.map_fragment_refresh_button_id);
        buttonDatabase = (ImageButton) view.findViewById(R.id.map_fragment_database_button_id);

        mSearchText = (AutoCompleteTextView) view.findViewById(R.id.map_autocomplete_id);

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
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

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
                                        fillMapWithAllDatabaseRestaurants();

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


        /** We use the mapFragmentViewModel to fill a map with lists of restaurants by type.
         * This way, we will be able to access the information very fast when the user searches
         * for a specific restaurant type using the Search Bar (AutoCompleteTextView)
         * */
        mapFragmentViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mapFragmentViewModel.getRestaurants().observe(this, new Observer<List<RestaurantEntry>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantEntry> restaurantEntries) {
                Log.d(TAG, "onChanged: Retrieving data from LiveData inside ViewModel");

                if (restaurantEntries != null) {
                    Log.d(TAG, "onChanged: restaurantEntries.size() = " + restaurantEntries.size());

                    /** We fill the mapOfRestaurants with the information from the local database
                     * */
                    mapOfListsOfRestaurantsByType = new HashMap<>();
                    List<RestaurantEntry> temporaryList;

                    for (int i = 0; i < RepoStrings.RESTAURANT_TYPES.length; i++) {

                        temporaryList = new ArrayList<>();

                        for (int j = 0; j < restaurantEntries.size(); j++) {

                            if (i == 0) {
                                temporaryList.addAll(restaurantEntries);
                                break;

                            } else {

                                if (restaurantEntries.get(j).getType().equals(RepoStrings.RESTAURANT_TYPES[i])) {
                                    temporaryList.add(restaurantEntries.get(j));

                                }
                            }
                        }

                        mapOfListsOfRestaurantsByType.put(RepoStrings.RESTAURANT_TYPES[i], temporaryList);

                    }

                    Log.d(TAG, "onChanged: mapOfListsOfRestaurantsByType = " + mapOfListsOfRestaurantsByType.toString());
                    Log.d(TAG, "onChanged: mapOfListsOfRestaurantsByType.get(\"All\") = " + mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]));

                    for (int i = 0; i < mapOfListsOfRestaurantsByType.size(); i++) {

                        Log.d(TAG, "onChanged: mapOfListsOfRestaurantsByType = " + mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[i]));

                    }

                        /** We fill the Map with pins from all the restaurants
                         * */
                        fillMapWithAllDatabaseRestaurants();


                } else {
                    Log.d(TAG, "onChanged: THERE IS NO DATA IN THE DATABASE!");

                }
            }
        });

        /** AutoCompleteTextView code. It allows to modify the pins in the map
         * according to the information we got from the databases
         * */
        mSearchText = (AutoCompleteTextView) view.findViewById(R.id.map_autocomplete_id);

        if (getActivity() != null) {
            ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1, //This layout has to be a textview
                    RepoStrings.RESTAURANT_TYPES
            );

            mSearchText.setAdapter(autocompleteAdapter);
            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "beforeTextChanged: " + charSequence);

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "onTextChanged: " + charSequence);

                    String type = Utils.capitalize(charSequence.toString());
                    List<RestaurantEntry> listOfRestaurantsByTypeInputted;

                    if (mMap != null) {

                        // TODO: 06/06/2018 Might fail because of the lower case
                        if (Arrays.asList(RepoStrings.RESTAURANT_TYPES).contains(type)
                                && mapOfListsOfRestaurantsByType.get(type) != null) {
                            Log.d(TAG, "onTextChanged: THE TYPE INPUTTED IS IN THE MAP");

                            /** We first clear the google map and the list of markers
                             * */
                            mMap.clear();
                            listOfMarkers.clear();

                            /** We get the list from the mapOfListsOfRestaurantsByType
                             * which coincides with the type inputted */
                            listOfRestaurantsByTypeInputted = mapOfListsOfRestaurantsByType.get(type);

                            /** We check if the restaurants has been visited in the user's group.
                             * If so, we modify the pin colour (done in the method)
                             * */
                            fillMapWithMarkers(listOfRestaurantsByTypeInputted);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d(TAG, "afterTextChanged: " + editable);

                }
            });

            mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick: ITEM CLICKED");

                    Utils.hideKeyboard(getActivity());

                }
            });
        }

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
                initRequestProcess();

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

        // TODO: 06/06/2018 CHECK IF THERE IS INTERNET

        return view;
    }



    /**************************
     * METHODS ****************
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
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);

        }
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

                        /** If the database is empty, we start the request
                         * */
                        if (localDatabaseIsEmpty()
                                && myPosition != null) {
                            Log.d(TAG, "onComplete: local database status (empty) = " + localDatabaseIsEmpty());
                            Log.d(TAG, "onComplete: myPosition = " + myPosition.toString());

                            //showProgressBar(progressBar, container);
                            initRequestProcess();

                        } else {
                            Log.d(TAG, "onComplete: local database status (empty) = " + localDatabaseIsEmpty());
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

    /**
     * Method used to initialise the map
     */
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
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

                    if (listOfMarkers.isEmpty()) {
                        Log.d(TAG, "onMapReady: there are no markers in the map");

                        /** We call this just in case
                         * */
                        mMap.clear();

                        /** If listOfMarkers is empty, there are not markers in the map, so we
                         * fill it with new ones
                         * */
                        fillMapWithAllDatabaseRestaurants();

                    } else {
                        Log.d(TAG, "onMapReady: map has already markers");
                    }


                }

                /**
                 * Listener for when clicking the info window in a map
                 * */
                if (mMap != null) {
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());
                            Log.d(TAG, "onInfoWindowClick: " + mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).size());
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());

                            /** REMEMBER mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]) is a list!
                             *  */

                            for (int i = 0; i < mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).size(); i++) {

                                if (mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getName().equals(marker.getTitle())) {

                                    Intent intent = new Intent(getActivity(), RestaurantActivity.class);

                                    intent.putExtra(RepoStrings.SentIntent.IMAGE_URL,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getImageUrl());
                                    intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getName());
                                    intent.putExtra(RepoStrings.SentIntent.ADDRESS,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getAddress());
                                    intent.putExtra(RepoStrings.SentIntent.RATING,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getRating());
                                    intent.putExtra(RepoStrings.SentIntent.PHONE,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getPhone());
                                    intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getWebsiteUrl());
                                    intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE,
                                            mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).get(i).getType());

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
     * Method that starts doing the requests to the servers to get the
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

    /**
     * Method that starts the requests if the database is empty. If not,
     * it tries again (only 10 times more; this way we avoid doing requests
     * continuously forever)
     * */
    public void startRequests () {
        Log.d(TAG, "startRequests: called!");
        // TODO: 06/06/2018 CHECK THIS!

        if (startRequestsCounter != 10) {

            if (localDatabaseIsEmpty()){
                //callLoaderInitApiGeneralRequests(ID_LOADER_INIT_GENERAL_API_REQUESTS);

            } else {
                startRequests();
                startRequestsCounter++;
            }
        } else {

            startRequestsCounter = 0;
        }
    }

    /**
     * Method that checks that the map can be filled with the database info (markers) and
     * immediately calls fillMapWithMarkers() to fill the map
     * */
    private void fillMapWithAllDatabaseRestaurants() {
        Log.d(TAG, "fillMapWithAllDatabaseRestaurants: called!");

        if (mapOfListsOfRestaurantsByType != null) {
            Log.d(TAG, "fillMapWithAllDatabaseRestaurants: mapOfListsOfRestaurantsByType IS NOT NULL");

            if (mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]) != null) {
                Log.d(TAG, "fillMapWithAllDatabaseRestaurants: ALL type of restaurants IS NOT NULL");

                if (listOfVisitedRestaurantsByTheUsersGroup != null) {

                    /** RepoStrings.RESTAURANT_TYPES[0] refers to "All" types of restaurants
                     * */
                    fillMapWithMarkers(mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]));

                } else {
                    Log.d(TAG, "fillMapWithAllDatabaseRestaurants: listOfVisitedRestaurantsByTheUsersGroup = null");

                }

            } else {
                Log.d(TAG, "fillMapWithAllDatabaseRestaurants: ALL type of restaurants IS NULL");

            }

        } else {
            Log.d(TAG, "fillMapWithAllDatabaseRestaurants: mapOfListsOfRestaurantsByType IS NULL");

        }
    }

    /**
     * Method that fills the map with Markers using a list
     * */
    private void fillMapWithMarkers (List<RestaurantEntry> listOfRestaurantsByType){

        if (mMap != null) {
            Log.d(TAG, "fillMapWithMarkers: the Map is not null");

            if (listOfRestaurantsByType != null
                    && !listOfRestaurantsByType.isEmpty()) {
                Log.d(TAG, "fillMapWithMarkers: listOfRestaurants IS NOT NULL and IS NOT EMPTY");

                /** We delete all the elements of the listOfMarkers
                 * */
                listOfMarkers.clear();
                mMap.clear();

                for (int i = 0; i < listOfRestaurantsByType.size(); i++) {

                    MarkerOptions options;

                    LatLng latLng = new LatLng(
                            Double.parseDouble(listOfRestaurantsByType.get(i).getLatitude()),
                            Double.parseDouble(listOfRestaurantsByType.get(i).getLongitude()));

                    if (listOfVisitedRestaurantsByTheUsersGroup.contains(listOfRestaurantsByType.get(i).getName())) {
                        Log.d(TAG, "onTextChanged: The place has been visited by somebody before");

                        options = new MarkerOptions()
                                .position(latLng)
                                .title(listOfRestaurantsByType.get(i).getName())
                                .snippet(listOfRestaurantsByType.get(i).getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); //Different colour
                    } else {
                        Log.d(TAG, "onTextChanged: The place has not been visited yet");

                        options = new MarkerOptions()
                                .position(latLng)
                                .title(listOfRestaurantsByType.get(i).getName())
                                .snippet(listOfRestaurantsByType.get(i).getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    }

                    /** We fill the listOfMarkers and the map with the markers
                     * */
                    listOfMarkers.add(mMap.addMarker(options));

                }
            }

        } else {
            Log.d(TAG, "fillMapWithMarkers: the Map is not null");

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



    /**
     * This method allows us to get a request permission result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        mLocationPermissionGranted = false;

        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            //Might be that can be removed initMap();

                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }

                    }
                    //if everything is ok (all permissions are granted),
                    // we want to initialise the map
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /*************/
    /** LOADER  **/
    /*************/

    /** Method that starts the ATL
     * and starts the requests' process
     * */
    private void callLoaderInitApiGeneralRequests(int id) {

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
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
                    return new ATLInitApiTextSearchRequests(getActivity(), mDb, myPosition);
                }

                @Override
                public void onLoadFinished(Loader loader, Object data) {
                    Log.d(TAG, "onLoadFinished: called!");

                    if (!localDatabaseIsEmpty()) {
                        Log.d(TAG, "onLoadFinished: database IS NOT EMPTY anymore");
                        //hideProgressBar(progressBar, container);

                    } else {
                        Log.d(TAG, "onLoadFinished: database IS EMPTY");

                        ToastHelper.toastShort(getActivity(), "Something went wrong. Database is not filled.");
                    }
                }

                @Override
                public void onLoaderReset(Loader loader) {

                }
            };
}

//    // TODO: 10/05/2018 Explain better
//    /** stopAutoManage() is used to avoid the app to crash when coming back to the
//     * fragment*/
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//}