package com.example.android.goforlunch.pageFragments;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.data.viewmodel.MainViewModel;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models_delete.PlaceInfo;
import com.example.android.goforlunch.placeautocompleteadapter.PlaceAutocompleteAdapter;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

import java.io.IOException;
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
public class FragmentRestaurantMapViewTRIAL extends Fragment {

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
    private static final float LATITUDE_BOUND = 0.007f;
    private static final float LONGITUDE_BOUND = 0.015f;
    private static LatLngBounds latLngBounds;

    //vars
    private boolean mLocationPermissionGranted = false; //used in permissions
    private GoogleMap mMap; //used to create the map
    private FusedLocationProviderClient mFusedLocationProviderClient; //used to get the location of the current user

    //Widgets
    private AutoCompleteTextView mSearchText;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

    //List of Visited Restaurants by Group (same group as the user) We will use this
    //list to compare it to the markers (to dra them differently if the restaurant
    // has already been visited by somebody of the group
    private List<String> listOfVisitedRestaurantsByTheUsersGroup;

    //This map will store all the restaurants in the database in lists ordered by type
    private Map<String, List<RestaurantEntry>> mapOfListsOfRestaurantsByType;

    //Markers. Used to store the markers of the map. It will be used to check if
    //the map has already markers and, if so, not call a function
    private List<Marker> listOfMarkers;

    //List of Restaurants and their properties
    private List<RestaurantEntry> listOfRestaurants;

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
    private DatabaseReference fireDbRefToUsersGroupsRestaurantsVisited;

    private String usersEmail;
    private String userGroupKey;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentRestaurantMapViewTRIAL newInstance() {
        FragmentRestaurantMapViewTRIAL fragment = new FragmentRestaurantMapViewTRIAL();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);

        listOfVisitedRestaurantsByTheUsersGroup = new ArrayList<>();
        mapOfListsOfRestaurantsByType = new HashMap<>();
        listOfMarkers = new ArrayList<>();

        toolbar = (Toolbar) view.findViewById(R.id.map_main_toolbar_id);
        toolbar2 = (RelativeLayout) view.findViewById(R.id.map_toolbar_search_id);

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

        userGroupKey= sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP_KEY, "");

        /** We get all the user information
         * */
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            usersEmail = currentUser.getEmail();
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

                            if (j == 0) {
                                temporaryList.add(restaurantEntries.get(j));

                            } else {

                                if (restaurantEntries.get(j).getType().equalsIgnoreCase(RepoStrings.RESTAURANT_TYPES[i])) {
                                    temporaryList.add(restaurantEntries.get(j));

                                }
                            }
                        }

                        mapOfListsOfRestaurantsByType.put(RepoStrings.RESTAURANT_TYPES[i], temporaryList);

                    }

                    Log.d(TAG, "onChanged: mapOfListsOfRestaurantsByType = " + mapOfListsOfRestaurantsByType.toString());


                } else {
                    Log.d(TAG, "onChanged: THERE IS NO DATA IN THE DATABASE!");

                }

            }
        });

        // TODO: 06/06/2018 CHECK IF THIS IS CALLED WHEN WE RETURN TO THE ACTIVITY
        /** We use the user's group to get all restaurants visited by that group and
         * differentiate the pins */
        fireDb = FirebaseDatabase.getInstance();
        fireDbRefToUsersGroupsRestaurantsVisited = fireDb.getReference(
                RepoStrings.FirebaseReference.GROUPS + "/" + userGroupKey + "/" + RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);

        fireDbRefToUsersGroupsRestaurantsVisited.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getChildren());

                if (userGroupKey.equalsIgnoreCase("")) {
                    Log.d(TAG, "onDataChange: THE USER HAS NOT CHOSEN A GROUP YET!");
                    //do nothing because the user has not chosen a group yet

                } else {
                    Log.d(TAG, "onDataChange: THE USER HAS ALREADY A GROUP");

                    /** We add all the restaurants to a list
                     * */
                    for (DataSnapshot item :
                            dataSnapshot.getChildren()) {

                        listOfVisitedRestaurantsByTheUsersGroup.add(Objects.requireNonNull(item.getValue()).toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        /** AutoCompleteTextView code. It allows to modify the pins in the map
         * according to the information we got from the databases
         *
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

        // TODO: 06/06/2018 CHECK IF THERE IS INTERNET
        /** STARTING THE MAP:
         * First, we check that the user has the correct Google Play Services Version.
         * If the user does, we start the map
         * **/
        if (isServicesOK()) {
            getLocationPermission();
        }
        return view;
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

    /**************************
     * METHODS ****************
     * ***********************/

    /**
     * Checks if the user has the correct Google Play Services Version
     */
    public boolean isServicesOK() {
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

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permission");

        /** We can also check first if the Android Version of the device is equal or higher than Marshmallow:
         *      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { "rest of code" } */

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(
                this.getActivity().getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(
                    this.getActivity().getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

                        LatLng northEast = new LatLng(currentLocation.getLatitude() + LATITUDE_BOUND, currentLocation.getLongitude() + LONGITUDE_BOUND);
                        LatLng southWest = new LatLng(currentLocation.getLatitude() - LATITUDE_BOUND, currentLocation.getLongitude() - LONGITUDE_BOUND);

                        Log.d(TAG, "onComplete: currentLocation: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                        Log.d(TAG, "onComplete: northEast: " + (currentLocation.getLatitude() + LATITUDE_BOUND) + ", " + (currentLocation.getLongitude() + LONGITUDE_BOUND));
                        Log.d(TAG, "onComplete: southWest: " + (currentLocation.getLatitude() - LATITUDE_BOUND) + ", " + (currentLocation.getLongitude() - LATITUDE_BOUND));

                        myPosition = new LatLngForRetrofit(currentLocation.getLatitude(), currentLocation.getLongitude());

                        latLngBounds = new LatLngBounds(
                                southWest, northEast);

                        moveCamera(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM);

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

                /** Listener for when clicking the info window in a map
                 * */
                if (mMap != null) {
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());
                            Log.d(TAG, "onInfoWindowClick: " + mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]).size());
                            Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());

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

    /** Method taht checks that the map can be filled with the database info (markers) and
     * immediately calls fillMapWithMarkers() to fill the map
     * */
    private void fillMapWithAllDatabaseRestaurants() {

        if (mapOfListsOfRestaurantsByType != null) {

            if (mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]) != null) {
                Log.d(TAG, "fillMapWithAllDatabaseRestaurants: ALL TYPE OF RESTAURANTS is not null");

                /** RepoStrings.RESTAURANT_TYPES[0] refers to "All" types of restaurants
                 * */
                fillMapWithMarkers(mapOfListsOfRestaurantsByType.get(RepoStrings.RESTAURANT_TYPES[0]));

            }
        }
    }

    /** Method that fills the map with Markers using a list
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
}

//    // TODO: 10/05/2018 Explain better
//    /** stopAutoManage() is used to avoid the app to crash when coming back to the
//     * fragment*/
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//}