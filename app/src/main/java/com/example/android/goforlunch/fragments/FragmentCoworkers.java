package com.example.android.goforlunch.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.adapters.RVAdapterCoworkers;
import com.example.android.goforlunch.constants.Repo;
import com.example.android.goforlunch.network.models.pojo.User;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.ItemClickSupport;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.Utils;
import com.example.android.goforlunch.utils.UtilsConfiguration;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentCoworkers extends Fragment implements Observer {

    private static final String TAG = FragmentCoworkers.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Unbinder unbinder;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @BindView(R.id.coworkers_toolbar_id)
    Toolbar toolbar;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarFragmentContent;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //RecyclerView
    @BindView(R.id.coworkers_recycler_view_id)
    RecyclerView recyclerView;

    private RVAdapterCoworkers adapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private FirebaseStorage fireStorage;

    private DatabaseReference dbRefUsersGetUserInfo;
    private DatabaseReference dbRefUsersGetCoworkers;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String userFirstName;
    private String userLastName;
    private String userKey;
    private String userGroupKey;
    private String userEmail;
    private String userGroup;

    //List of Coworkers
    private List<User> listOfCoworkers;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private SharedPreferences sharedPref;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;

    private boolean internetAvailable;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private RequestManager glide;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** Method for instantiating the fragment
     * */
    public static FragmentCoworkers newInstance() {
        FragmentCoworkers fragment = new FragmentCoworkers();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_coworkers_view, container, false);
        unbinder = ButterKnife.bind(this, view);

        /* Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        if (getActivity() != null) {
            glide = Glide.with(getActivity().getApplicationContext());
        }

        fireDb = FirebaseDatabase.getInstance();
        fireStorage = FirebaseStorage.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        /* Configure toolbar */
        UtilsConfiguration.configureActionBar(getActivity(), toolbar);

        userKey = sharedPref.getString(Repo.SharedPreferences.USER_ID_KEY, "");

        listOfCoworkers = new ArrayList<>();

        this.configureRecyclerView();
        this.configureOnClickRecyclerView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");
        connectBroadcastReceiverFragment();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");
        disconnectBroadcastReceiverFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: called!");
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
        disconnectBroadcastReceiverFragment();
    }

    @Override
    public void update(Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet Not Available");

            internetAvailable = false;


        } else if ((int) internetAvailableUpdate == 1) {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            /* We get the user information
             * */
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
            Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

            if (currentUser != null) {

                userEmail = currentUser.getEmail();

                if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                    dbRefUsersGetUserInfo = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey);
                    dbRefUsersGetUserInfo.addListenerForSingleValueEvent(valueEventListenerGetUserInfo);
                }
            }

        }
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
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /*********************
     * LISTENERS *********
     * ******************/

    /**
     * Listener to get user's info
     */
    private ValueEventListener valueEventListenerGetUserInfo = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            userFirstName = dataSnapshot.child(Repo.FirebaseReference.USER_FIRST_NAME).getValue().toString();
            userLastName = dataSnapshot.child(Repo.FirebaseReference.USER_LAST_NAME).getValue().toString();
            userEmail = dataSnapshot.child(Repo.FirebaseReference.USER_EMAIL).getValue().toString();
            userGroup = dataSnapshot.child(Repo.FirebaseReference.USER_GROUP).getValue().toString();
            userGroupKey = dataSnapshot.child(Repo.FirebaseReference.USER_GROUP_KEY).getValue().toString();

            /* We fill the list of the coworkers using the group of the user
             *  */
            dbRefUsersGetUserInfo = fireDb.getReference(Repo.FirebaseReference.USERS);
            dbRefUsersGetUserInfo.addValueEventListener(valueEventListenerGetCoworkers);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());
        }
    };

    /**
     * Listener to get a list of coworkers
     */
    private ValueEventListener valueEventListenerGetCoworkers = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            if (userGroup != null || !userGroup.equalsIgnoreCase("")) {

                listOfCoworkers.clear();
                listOfCoworkers = UtilsFirebase.fillListWithUsersOfSameGroupFromDataSnapshot(dataSnapshot, userEmail, userGroup);

                if (getActivity() != null) {
                    adapter = new RVAdapterCoworkers(getActivity(), listOfCoworkers, glide);
                    recyclerView.setAdapter(adapter);

                    Anim.hideCrossFadeShortAnimation(progressBarFragmentContent);
                    Anim.showCrossFadeShortAnimation(recyclerView);

                }

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /******************************************************
     * CONFIGURATION
     *****************************************************/

    private void configureRecyclerView() {
        Log.d(TAG, "configureRecyclerView: called!");

        if (getActivity() != null) {

            recyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);

        }
    }

    /**
     * Method that configures onClick for recyclerView items
     */
    private void configureOnClickRecyclerView() {
        Log.d(TAG, "configureOnClickRecyclerView: called!");

        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Log.d(TAG, "onItemClicked: item(" + position + ") clicked!");

                        if (null == adapter.getUser(position).getRestaurantName()
                                || adapter.getUser(position).getRestaurantName().equalsIgnoreCase(Repo.NOT_AVAILABLE_FOR_STRINGS)) {

                            if (null != getActivity()) {
                                ToastHelper.toastShort(getActivity(), getActivity().getResources().getString(R.string.noInfoAvailable));
                            }

                        } else {

                            /* We launch Restaurant Activity */
                            startActivity(createAndFillIntentWithUserInfo(adapter, position));
                        }

                    }
                });

    }

    /**
     * Method that connects a broadcastReceiver to the fragment.
     * It allows to notify the user about the internet state
     */
    private void connectBroadcastReceiverFragment() {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Repo.CONNECTIVITY_CHANGE_STATUS);

        if (getActivity() != null) {
            Utils.connectReceiver(getActivity(), receiver, intentFilter, this);
        }

    }

    /**
     * Method that disconnects the broadcastReceiver from the fragment.
     */
    private void disconnectBroadcastReceiverFragment() {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null && getActivity() != null) {
            Utils.disconnectReceiver(
                    getActivity(),
                    receiver,
                    this);
        }

        receiver = null;
        intentFilter = null;

    }

    /**
     * Method that creates an intent and fills it with all the necessary info to be displayed
     * in Restaurant Activity
     */
    // TODO: 09/09/2018 Could be done as a Utils method
    private Intent createAndFillIntentWithUserInfo(RVAdapterCoworkers adapter, int position) {

        Intent intent = new Intent(getActivity(), RestaurantActivity.class);

        intent.putExtra(Repo.SentIntent.PLACE_ID, adapter.getUser(position).getPlaceId());
        intent.putExtra(Repo.SentIntent.IMAGE_URL, adapter.getUser(position).getImageUrl());
        intent.putExtra(Repo.SentIntent.RESTAURANT_NAME, adapter.getUser(position).getRestaurantName());
        intent.putExtra(Repo.SentIntent.RESTAURANT_TYPE, adapter.getUser(position).getRestaurantType());
        intent.putExtra(Repo.SentIntent.ADDRESS, adapter.getUser(position).getAddress());
        intent.putExtra(Repo.SentIntent.RATING, adapter.getUser(position).getRating());
        intent.putExtra(Repo.SentIntent.PHONE, adapter.getUser(position).getPhone());
        intent.putExtra(Repo.SentIntent.WEBSITE_URL, adapter.getUser(position).getWebsiteUrl());

        return intent;

    }
}
