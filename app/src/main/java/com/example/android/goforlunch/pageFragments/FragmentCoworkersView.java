package com.example.android.goforlunch.pageFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
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
import android.widget.TextView;

import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.pojo.User;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterCoworkers;
import com.example.android.goforlunch.strings.RepoStrings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentCoworkersView extends Fragment {

    private static final String TAG = "PageFragmentCoworkersVi";

    //Variables to store views related to the articles upload
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private ActionBar actionBar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firabase
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;

    private String usersEmail;
    private String userGroup;

    //List of Coworkers
    private List<User> listOfCoworkers;

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

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_coworkers_view, container, false);

        listOfCoworkers = new ArrayList<>();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.coworkers_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        /** We get the group of the user
         * */
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            usersEmail = auth.getCurrentUser().getEmail();
        } else {
            // TODO: 24/05/2018 Remove this
            usersEmail = RepoStrings.FAKE_USER_EMAIL;
        }

        fireDb = FirebaseDatabase.getInstance();
        dbRef = fireDb.getReference(RepoStrings.FirebaseReference.USERS);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                for (DataSnapshot item:
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: in the foreach loop");

                    if (usersEmail != null) {
                        Log.d(TAG, "onDataChange: users email is not null");

                        /** If the usersEmail of the user in the list is the same as the current user,
                         * then it is the user we are looking for and we save the user's group to
                         * build a list */
                        if (item.child(RepoStrings.FirebaseReference.EMAIL).getValue().equals(usersEmail)) {
                            userGroup = item.child(RepoStrings.FirebaseReference.GROUP).getValue().toString();
                            Log.d(TAG, "onDataChange: userGroup = " + userGroup);
                            break;
                        }
                    }
                }

                if (userGroup != null) {
                    Log.d(TAG, "onDataChange: userGroup is not null");

                    for (DataSnapshot item:
                            dataSnapshot.getChildren()) {

                        /** If the user's group and the coworker's group coincide, we add the
                         * coworker to the list
                         * */
                        if(item.child(RepoStrings.FirebaseReference.GROUP).getValue().equals(userGroup)){

                            if (!item.child(RepoStrings.FirebaseReference.EMAIL).getValue().equals(usersEmail)) {
                                // TODO: 24/05/2018 Check null values
                                listOfCoworkers.add(new User(
                                        item.child(RepoStrings.FirebaseReference.FIRSTNAME).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.LASTNAME).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.EMAIL).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.GROUP).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.PLACE_ID).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.RESTAURANT).getValue().toString(),
                                        item.child(RepoStrings.FirebaseReference.RESTAURANT_TYPE).getValue().toString()
                                        )
                                );
                            }
                        }
                    }

                    Log.d(TAG, "onDataChange: setting the adapter with list.size() = " + listOfCoworkers.size());

                    mAdapter = new RVAdapterCoworkers(getContext(), listOfCoworkers);
                    mRecyclerView.setAdapter(mAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());
            }
        });

        toolbar = (Toolbar) view.findViewById(R.id.map_toolbar_id);
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

        Anim.crossFadeShortAnimation(mRecyclerView);

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




}
