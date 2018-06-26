package com.example.android.goforlunch.pageFragments;

import android.content.Intent;
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

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.UtilsConfiguration;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.pojo.User;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterCoworkers;
import com.example.android.goforlunch.repository.RepoStrings;
import com.example.android.goforlunch.rvitemclick.ItemClickSupport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

// TODO: 28/05/2018 Remove the user from the list!
public class FragmentCoworkers extends Fragment {

    private static final String TAG = FragmentCoworkers.class.getSimpleName();

    @BindView(R.id.coworkers_toolbar_id)
    Toolbar toolbar;

    private ActionBar actionBar;

    //RecyclerView
    @BindView(R.id.coworkers_recycler_view_id)
    RecyclerView recyclerView;

    private RVAdapterCoworkers adapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    private String userFirstName;
    private String userLastName;
    private String userKey;
    private String userGroupKey;
    private String userEmail;
    private String userGroup;

    private SharedPreferences sharedPref;

    //List of Coworkers
    private List<User> listOfCoworkers;

    /******************************
     * STATIC METHOD FOR **********
     * INSTANTIATING THE FRAGMENT *
     *****************************/

    public static FragmentCoworkers newInstance() {
        FragmentCoworkers fragment = new FragmentCoworkers();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_coworkers_view, container, false);

        ButterKnife.bind(this, view);

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        /** Configure toolbar */
        UtilsConfiguration.configureActionBar(getActivity(), toolbar, actionBar);
//        if (((AppCompatActivity) getActivity()) != null) {
//            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        }
//
//        if (((AppCompatActivity) getActivity()) != null) {
//            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
//        }

        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        listOfCoworkers = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        this.configureOnClickRecyclerView();

        /** We get the user information
         * */
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

        if (currentUser != null) {

            userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        userFirstName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue().toString();
                        userLastName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue().toString();
                        userEmail = dataSnapshot.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString();
                        userGroup = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString();
                        userGroupKey = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue().toString();

                        /** We fill the list of the coworkers using the group of the user
                         *  */
                        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                        dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                listOfCoworkers.clear();
                                listOfCoworkers = UtilsFirebase.fillListWithUsersFromDataSnapshot(dataSnapshot, userEmail, userGroup);

                                // TODO: 16/06/2018 Iterate through the list of coworkers to delete the currentUser

                                if (getActivity() != null) {
                                    adapter = new RVAdapterCoworkers(getActivity(), listOfCoworkers);
                                    recyclerView.setAdapter(adapter);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: " + databaseError.getCode());


                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());
                    }
                });

            }
        }

        /** We set a listener for listening for changes in the database
         * */
        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        dbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                if (userGroup != null || !userGroup.equalsIgnoreCase("")) {

                    listOfCoworkers.clear();
                    listOfCoworkers = UtilsFirebase.fillListWithUsersFromDataSnapshot(dataSnapshot, userEmail, userGroup);

                    if (getActivity() != null) {
                        adapter = new RVAdapterCoworkers(getActivity(), listOfCoworkers);
                        recyclerView.setAdapter(adapter);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        Anim.crossFadeShortAnimation(recyclerView);

        return view;
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

        }

        return super.onOptionsItemSelected(item);
    }

    /** Method that configures onClick for recyclerView items
     * */
    private void configureOnClickRecyclerView () {
        Log.d(TAG, "configureOnClickRecyclerView: called!");

        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Log.d(TAG, "onItemClicked: item(" + position + ") clicked!");

                        if (null == adapter.getUser(position).getRestaurantName()
                                || adapter.getUser(position).getRestaurantName().equalsIgnoreCase(RepoStrings.NOT_AVAILABLE_FOR_STRINGS)) {

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

    /** Method that creates an intent and fills it with all the necessary info to be displayed
     * in Restaurant Activity
     * */
    private Intent createAndFillIntentWithUserInfo(RVAdapterCoworkers adapter, int position) {

        Intent intent = new Intent(getActivity(), RestaurantActivity.class);

        intent.putExtra(RepoStrings.SentIntent.PLACE_ID, adapter.getUser(position).getPlaceId());
        intent.putExtra(RepoStrings.SentIntent.IMAGE_URL, adapter.getUser(position).getImageUrl());
        intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME, adapter.getUser(position).getRestaurantName());
        intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, adapter.getUser(position).getRestaurantType());
        intent.putExtra(RepoStrings.SentIntent.ADDRESS, adapter.getUser(position).getAddress());
        intent.putExtra(RepoStrings.SentIntent.RATING, adapter.getUser(position).getRating());
        intent.putExtra(RepoStrings.SentIntent.PHONE, adapter.getUser(position).getPhone());
        intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL, adapter.getUser(position).getWebsiteUrl());

        return intent;

    }

}
