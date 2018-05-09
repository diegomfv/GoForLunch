package com.example.android.goforlunch.pageFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.goforlunch.activities.MainActivity;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the Google Map
 * **/
public class FragmentRestaurantMapView extends Fragment implements OnMapReadyCallback {

    /**************************
     * MAP CREATION CALLBACK **
     * ***********************/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

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

    //Widgets
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

    //Map
    private GoogleMap mMap;

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

        if (((AppCompatActivity)getActivity()) != null) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }

        if (((AppCompatActivity)getActivity()) != null) {
            actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        /** First, we check that the user has the correct Google Play Services Version. If the user
         * does, we start the map
         * **/
        if (isServicesOK()) {
            init();
        }




        //SupportMapFragment mapFragment = (SupportMapFragment) ((AppCompatActivity)getActivity()).getSupportFragmentManager()



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
                if (((MainActivity)getActivity()) != null) {
                    ((MainActivity)getActivity()).getMDrawerLayout().openDrawer(GravityCompat.START);
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

    /** Initialises the Google Map
     * */
    private void init () {

    }

    /** Checks if the user has the correct Google Play Services Version
     */
    public boolean isServicesOK () {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
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


}

/**
 * FIRST
 * Check the user has the correct Google Play Services Version. If he does, we run the map
 * */