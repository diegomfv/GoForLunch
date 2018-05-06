package com.example.android.goforlunch.pageFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.pojo.RestaurantObject;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapter;

import java.util.List;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentRestaurantListView extends Fragment {

    private static final String TAG = "PageFragmentRestaurantL";

    //Widgets
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

    //Animation duration
    private int mShortAnimationDuration;

    //List of elements
    private List<RestaurantObject> listOfRestaurantObjects;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentRestaurantListView newInstance() {
        FragmentRestaurantListView fragment = new FragmentRestaurantListView();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        toolbar = (Toolbar) view.findViewById(R.id.list_main_toolbar_id);
        toolbar2 = (RelativeLayout) view.findViewById(R.id.list_toolbar_search_id);

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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setVisibility(View.GONE);

        mAdapter = new RVAdapter(getContext(), listOfRestaurantObjects);
        mRecyclerView.setAdapter(mAdapter);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        crossFade(mRecyclerView);

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                Log.i(TAG, "onOptionsItemSelected: home clicked");
                toolbar.setVisibility(View.GONE);

                // Retrieve and cache the system's default "short" animation time.
                mShortAnimationDuration = getResources().getInteger(
                        android.R.integer.config_shortAnimTime);

                crossFade(toolbar2);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void crossFade(View view) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);


    }



}
