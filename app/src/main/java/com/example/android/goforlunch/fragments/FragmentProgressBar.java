package com.example.android.goforlunch.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.goforlunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Diego Fajardo (https://github.com/diegomfv) on 09/09/2018.
 */
public class FragmentProgressBar extends Fragment {

    private static final String TAG = FragmentProgressBar.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    private Unbinder unbinder;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Method for instantiating the fragment
     */
    public static FragmentProgressBar newInstance() {
        Log.d(TAG, "newInstance: called!");
        FragmentProgressBar fragment = new FragmentProgressBar();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called!");

        View view = inflater.inflate(R.layout.fragment_progress_bar, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
        unbinder.unbind();
    }
}
