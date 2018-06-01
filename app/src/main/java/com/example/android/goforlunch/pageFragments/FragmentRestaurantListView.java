package com.example.android.goforlunch.pageFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterList;
import com.example.android.goforlunch.strings.RepoStrings;
import com.example.android.goforlunch.data.viewmodel.MainViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentRestaurantListView extends Fragment {

    // TODO: 28/05/2018 Some names appear over the others
    // TODO: 29/05/2018 Modify the number of users that go to a place!

    private static final String TAG = "PageFragmentRestaurantL";

    //Widgets
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private RelativeLayout toolbar2;
    private ActionBar actionBar;

    //List of elements
    private List<RestaurantEntry> listOfRestaurants;
    private List<RestaurantEntry> listOfRestaurantsByType;

    //This list will have as many elements repeated as coworkers going to the restaurant
    private List<String> listOfRestaurantsByCoworker;

    //Map of Restaurants and number of coworkers
    private Map<String, Integer> mapOfCoworkersPerRestaurant;

    //Widgets
    private AutoCompleteTextView mSearchText;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Database
    private AppDatabase mDb;
    private MainViewModel mainViewModel;

    //SharedPreferences
    private SharedPreferences sharedPref;
    private String userGroup;

    // TODO: 29/05/2018 Use these variables to get the necessary info to display the number of coworkers that are going to a specific place
    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUsers;

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

        /** SharedPreferences
         * */
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userGroup = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP, "");

        listOfRestaurants = new ArrayList<>();
        listOfRestaurantsByType = new ArrayList<>();

        mDb = AppDatabase.getInstance(getActivity());

        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getRestaurants().observe(this, new Observer<List<RestaurantEntry>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantEntry> restaurantEntries) {
                Log.d(TAG, "onChanged: Retrieving data from LiveData inside ViewModel");

                if (restaurantEntries != null) {
                    Log.d(TAG, "onChanged: restaurantEntries.size() = " + restaurantEntries.size());

                    listOfRestaurants = restaurantEntries;

                    /** We fill the list with the Restaurants in the database
                     * */
                    mAdapter = new RVAdapterList(getContext(), listOfRestaurants, listOfRestaurantsByCoworker);
                    mRecyclerView.setAdapter(mAdapter);

                    /** We fill the map with the name of the restaurants
                     * */
                    mapOfCoworkersPerRestaurant = new TreeMap<>();

                    for (int i = 0; i < restaurantEntries.size(); i++) {

                        if (restaurantEntries.get(i).getName() != null
                                && !restaurantEntries.get(i).getName().equals("")) {

                            mapOfCoworkersPerRestaurant.put(restaurantEntries.get(i).getName(), 0);

                        }
                    }

                    Log.d(TAG, "onChanged: map.size() = " + mapOfCoworkersPerRestaurant.size());

                } else {
                    Log.d(TAG, "onChanged: restaurantEntries is NULL");
                }
            }
        });

        toolbar = (Toolbar) view.findViewById(R.id.list_main_toolbar_id);
        toolbar2 = (RelativeLayout) view.findViewById(R.id.list_toolbar_search_id);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_recycler_view_id);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RVAdapterList(getContext(), listOfRestaurants, listOfRestaurantsByCoworker);
        mRecyclerView.setAdapter(mAdapter);

        mSearchText = (AutoCompleteTextView) view.findViewById(R.id.list_autocomplete_id);

        if (getActivity() != null) {
            ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1, //This layout has to be a textview
                    RepoStrings.RESTAURANT_TYPES
            );

            // TODO: 29/05/2018 Hide the keyboard when the item is clicked or enter is pressed
            // TODO: 28/05/2018 Change the position of the displayed text in the Adapter
            mSearchText.setAdapter(autocompleteAdapter);
            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "beforeTextChanged: " + charSequence);

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "onTextChanged: " + charSequence);

                    // TODO: 29/05/2018 Might be a way of doing it more efficiently
                    // TODO: 29/05/2018 Eg, only change after 4 letters
                    // TODO: 29/05/2018 Hide keyboard when magnifying class is clicked

                    String type = charSequence.toString();

                    if (Arrays.asList(RepoStrings.RESTAURANT_TYPES).contains(type)) {

                        listOfRestaurantsByType = new ArrayList<>();

                        for (int j = 0; j < listOfRestaurants.size(); j++) {

                            if (listOfRestaurants.get(j).getType().equals(type)) {
                                listOfRestaurantsByType.add(listOfRestaurants.get(j));
                            }
                        }

                        mAdapter = new RVAdapterList(getContext(), listOfRestaurantsByType, listOfRestaurantsByCoworker);
                        mRecyclerView.setAdapter(mAdapter);

                    } else {

                        mAdapter = new RVAdapterList(getContext(), listOfRestaurants, listOfRestaurantsByCoworker);
                        mRecyclerView.setAdapter(mAdapter);

                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d(TAG, "afterTextChanged: " + editable);

                }
            });

            mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick: ITEM CLICKED");

                    Utils.hideKeyboard(getActivity());

                }
            });
        }

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

        /** We get the Coworkers per Restaurant
         */
        fireDb = FirebaseDatabase.getInstance();
        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        fireDbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                mapOfCoworkersPerRestaurant = new TreeMap<>();

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP).getValue()).equals(userGroup)) {

                        //mapOfCoworkersPerRestaurant.put(item.child(RepoStrings.))

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });











        Anim.crossFadeShortAnimation(mRecyclerView);

        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.list_menu, menu);
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

            case R.id.list_search_button_id: {
                Log.d(TAG, "onOptionsItemSelected: search button clicked");
                toolbar.setVisibility(View.GONE);
                Anim.crossFadeShortAnimation(toolbar2);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
