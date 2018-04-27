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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentCoworkersView extends Fragment {

    private static final String TAG = "PageFragmentCoworkersVi";

    //Variables to store views related to the articles upload
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;

    //Variables that reference the views
    private TextView mContentView;
    private int mShortAnimationDuration;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentCoworkersView newInstance() {
        FragmentCoworkersView fragment = new FragmentCoworkersView();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_coworkers_view, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (((AppCompatActivity)getActivity()) != null) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }

        final ActionBar actionBar;
        if (((AppCompatActivity)getActivity()) != null) {
            actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        mContentView = view.findViewById(R.id.tv_coworker);

        // Initially hide the content view.
        mContentView.setVisibility(View.GONE);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        crossFade();

        return view;
    }


    private void crossFade() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        mContentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);


    }

}
