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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.anim.Anim;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterCoworkers;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterList;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentCoworkersView extends Fragment {

    private static final String TAG = "PageFragmentCoworkersVi";

    //Variables to store views related to the articles upload
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.map_toolbar_id);
        if (((AppCompatActivity)getActivity()) != null) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }

        final ActionBar actionBar;
        if (((AppCompatActivity)getActivity()) != null) {
            actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.coworkers_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RVAdapterCoworkers(getContext());
        mRecyclerView.setAdapter(mAdapter);

        Anim.crossFadeShortAnimation(mRecyclerView);

        return view;
    }

}
