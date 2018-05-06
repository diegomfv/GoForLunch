package com.example.android.goforlunch.pageFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.anim.Anim;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentRestaurantMapView extends Fragment {

    private static final String TAG = "PageFragmentRestaurantM";

    //Widgets
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

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

        return view;
    }

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
                break;

            }

            case R.id.map_search_button_id: {
                Log.d(TAG, "onOptionsItemSelected: search button clicked");
                toolbar.setVisibility(View.GONE);
                Anim.crossFadeShortAnimation(toolbar2);
                break;

            }
        }
        return super.onOptionsItemSelected(item);
    }

}
