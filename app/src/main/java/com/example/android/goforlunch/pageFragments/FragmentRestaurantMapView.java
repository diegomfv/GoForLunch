package com.example.android.goforlunch.pageFragments;

import android.app.Dialog;
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
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.MainActivity;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.models_delete.PlaceInfo;
import com.example.android.goforlunch.placeautocompleteadapter.PlaceAutocompleteAdapter;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the Google Map
 * **/
public class FragmentRestaurantMapView extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener {

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
    private static final float DEFAULT_ZOOM = 15f;
    private static final float LATITUDE_BOUND = 0.007f;
    private static final float LONGITUDE_BOUND = 0.015f;
    private static LatLngBounds latLngBounds;

    //vars
    private boolean mLocationPermissionGranted = false; //used in permissions
    private GoogleMap mMap; //used to create the map
    private FusedLocationProviderClient mFusedLocationProviderClient; //used to get the location of the current user
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;

    //Widgets
    private AutoCompleteTextView mSearchText;
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

    //Markers. Used to store the markers and remove them each time we do a new search
    private List<Marker> listOfMarkers;

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

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

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

        //List to store the markers
        listOfMarkers = new ArrayList<>();

        /** First, we check that the user has the correct Google Play Services Version. If the user
         * does, we start the map
         * **/
        if (isServicesOK()) {

            getLocationPermission();
            init();

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

    /** Checks if the user has the correct Google Play Services Version
     */
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //There is an error but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Log.d(TAG, "isServicesOK: an error occurred; you cannot make map requests");
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

    /** Method used to get the user's location
     * */
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

                        latLngBounds = new LatLngBounds(
                                southWest, northEast);

                        moveCamera(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM);

                        Log.d(TAG, "onComplete: current location: getLatitude(), getLongitude() " + (currentLocation.getLatitude()) + ", " + (currentLocation.getLongitude()));

                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                    }

                }
            });

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }

    }

    /** Method used to initialise the map
     * */
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

                }
            }
        });
    }

    /** Method used to enable search in the search bar
     * */
    private void init () {
        Log.d(TAG, "init: initialising");

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(
                getActivity(),
                mGoogleApiClient,
                latLngBounds,
                null);

        mSearchText.setOnItemClickListener(mAutocompleteclickListener);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(searchListener);

        Utils.hideKeyboard(getActivity());
    }

    /** Method used to move the camera in the map
     * */
    private void moveCamera (LatLng latLng, float zoom) {

        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }

    /** Method used to geolocate places
     * */
    private void geolocate () {
        Log.d(TAG, "geolocate: geolocating");

        //We clean the map of markers (if there are any)
        if (listOfMarkers.size() > 0) {

            for (int i = 0; i < listOfMarkers.size(); i++) {
                listOfMarkers.get(i).remove();
            }
            listOfMarkers.clear();
        }

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());

        /** Address object stores the info
         * got from the geocoder when using get... methods
         * */
        List<Address> list = new ArrayList<>();

        try {
            /** Max results gives you the number of results*/
            list = geocoder.getFromLocationName(searchString, 15);

        } catch (IOException e) {
            Log.e(TAG, "geolocate: IOException " + e.getMessage());
        }

        if (list.size() > 0) {
            Log.d(TAG, "geolocate: list size = " + list.size());

            /** Gives you a lot of information
             * */
            for (int i = 0; i < list.size() ; i++) {
                Address address = list.get(i);
                Log.d(TAG, "geolocate: found a location: " + address.toString());

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(address.getFeatureName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_lunch)); // would put this icon as market

                listOfMarkers.add(mMap.addMarker(options));

                //We move the camera to the first result of the list
                if (i == 0) {
                    moveCamera(
                            new LatLng(address.getLatitude(), address.getLongitude()),
                            DEFAULT_ZOOM);
                }

                Utils.hideKeyboard(getActivity());
            }


            //ToastHelper.toastShort(getActivity(), address.toString());
        } else {
            Log.d(TAG, "geolocate: nothing found");
        }


    }

    /** This method allows us to get a request permission result
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        mLocationPermissionGranted = false;

        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE: {

                if(grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length ; i++) {

                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**************************
     * LISTENERS **************
     * ***********************/

    TextView.OnEditorActionListener searchListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            //This ensures that when you press the key on the keyboard the action
            //is executed eg. clicking enter will make the user to enter a next line and
            //what we want is to start the search (See also the layout, editText)
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                //execute our method for searching
                geolocate();
            }
            return false;
        }
    };

    /**********************************
     * GOOGLE PLACES API **************
     * *******************************/

    private AdapterView.OnItemClickListener mAutocompleteclickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId;
            if (item != null) {
                Log.d(TAG, "onItemClick: item is not null");
                placeId = item.getPlaceId();

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId); //We can submit a list instead of only one placeId

                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }

            Utils.hideKeyboard(getActivity());
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {

            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete successfully " + places.getStatus().toString());
                places.release(); //this is necessary to prevent memory leaks
                return;
            }
            
            final Place place = places.get(0);

            //We cannot create a global Place object list to store all the info because we have to call release() at
            //the end of this method to void memory leaks and that will cause an error. That is why we have to create
            //a PlaceInfo Object to store all the information (also, we could create a list

            try {

                mPlace = new PlaceInfo();
                mPlace.setId(place.getId());
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: " + mPlace.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: " + mPlace.getAddress());
                //mPlace.setAttributions(place.getAttributions().toString());
                //Log.d(TAG, "onResult: " + mPlace.getAttributions());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: " + mPlace.getPhoneNumber());
                mPlace.setTimetable("12pm");
                mPlace.setWebsiteUri(place.getWebsiteUri());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setRating(place.getRating());

                Log.d(TAG, "onResult: " + mPlace.toString());

            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException " + e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM);

            places.release();
        }
    };

    // TODO: 10/05/2018 Explain better
    /** stopAutoManage() is used to avoid the app to crash when coming back to the
     * fragment*/
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
    }



}

/**
 * 1. MANIFEST STUFF
 * 2. CHECKING
 * Check the user has the correct Google Play Services Version. If he does, we run the map.
 * 3.
 * */