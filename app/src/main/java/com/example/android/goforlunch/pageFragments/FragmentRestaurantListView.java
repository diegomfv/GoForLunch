package com.example.android.goforlunch.pageFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.data.viewmodel.MainViewModel;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsConfiguration;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterCoworkers;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterList;
import com.example.android.goforlunch.repository.RepoStrings;
import com.example.android.goforlunch.rvitemclick.ItemClickSupport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

/** Fragment that displays the list of restaurants in a recyclerView
 * */
public class FragmentRestaurantListView extends Fragment {

    private static final String TAG = FragmentRestaurantListView.class.getSimpleName();

    //Array of restaurant types (got from Resources, strings)
    private String[] arrayOfTypes;

    //Widgets
    @BindView(R.id.list_autocomplete_id)
    AutoCompleteTextView autocompleteTextView;

    @BindView(R.id.list_main_toolbar_id)
    Toolbar toolbar;

    @BindView(R.id.list_toolbar_search_id)
    RelativeLayout toolbar2;

    private ActionBar actionBar;

    //List of elements
    private List<RestaurantEntry> listOfRestaurants;
    private List<RestaurantEntry> listOfRestaurantsByType;

    //This list will have as many elements repeated as coworkers going to the restaurant
    private List<String> listOfRestaurantsByCoworker;

    //RecyclerView
    @BindView(R.id.list_recycler_view_id)
    RecyclerView recyclerView;

    private RVAdapterList adapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Database
    private AppDatabase localDatabase;
    private MainViewModel mainViewModel;

    //SharedPreferences
    private SharedPreferences sharedPref;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsersGetUserInfo;
    private DatabaseReference dbRefUsersGetListOfRestaurantsByCoworkers;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    //Observers
    private MaybeObserver restaurantsObserver;

    //Disposables
    private Disposable autocompleteTextViewDisposable;

    //Glide
    private RequestManager glide;


    /** ------------------------------------------------ */

    /** Method for instantiating the fragment
     * */
    public static FragmentRestaurantListView newInstance() {
        FragmentRestaurantListView fragment = new FragmentRestaurantListView();
        return fragment;
    }

    /** onCreate()...
     * */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: Map");

        View view = inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);

        /**Butterknife binding
         * */
        ButterKnife.bind(this, view);

        /** Activates the toolbar menu for the fragment
         * */
        setHasOptionsMenu(true);

        /** Configure databases*/
        this.configureDatabases(getActivity());
        Log.d(TAG, "onCreate: " + sharedPref.getAll().toString());

        listOfRestaurants = new ArrayList<>();
        listOfRestaurantsByType = new ArrayList<>();
        listOfRestaurantsByCoworker = new ArrayList<>();

        /** Configure toolbar */
        UtilsConfiguration.configureActionBar(getActivity(), toolbar, actionBar);

        /** We get an array of restaurant types from RESOURCES
         * */
        this.arrayOfTypes = getActivity().getResources().getStringArray(R.array.typesOfRestaurants);

        /** Glide configuration*/
        glide = Glide.with(getActivity());

        /** Configure RecyclerView*/
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();

        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getRestaurants().observe(this, new Observer<List<RestaurantEntry>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantEntry> restaurantEntries) {
                Log.d(TAG, "onChanged: Retrieving data from LiveData inside ViewModel");

                if (restaurantEntries != null) {
                    Log.d(TAG, "onChanged: restaurantEntries.size() = " + restaurantEntries.size());

                    /** We fill the list with the Restaurants in the database
                     * */
                    listOfRestaurants = restaurantEntries;

                    /** We update the recyclerView with the new list
                     * */
                    updateRecyclerViewWithNewRestaurantsList(restaurantEntries);

                } else {
                    Log.d(TAG, "onChanged: restaurantEntries is NULL");
                }
            }
        });

        /** Configuration process */
        this.configureAutocompleteTextView(autocompleteTextView, autocompleteTextViewDisposable);

        Anim.crossFadeShortAnimation(recyclerView);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.d(TAG, "onNext: ");

                /** We get the user information
                 * */
                currentUser = auth.getCurrentUser();
                Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

                if (currentUser != null) {

                    userEmail = currentUser.getEmail();

                    if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                        dbRefUsersGetUserInfo = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                        dbRefUsersGetUserInfo.addListenerForSingleValueEvent(valueEventListenerGetUserInfo);
                    }

                }

                dbRefUsersGetListOfRestaurantsByCoworkers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                dbRefUsersGetListOfRestaurantsByCoworkers.addValueEventListener(valueEventListenerGetListOfRestaurantsByCoworkers);

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");
        dbRefUsersGetUserInfo.removeEventListener(valueEventListenerGetUserInfo);
        dbRefUsersGetListOfRestaurantsByCoworkers.removeEventListener(valueEventListenerGetListOfRestaurantsByCoworkers);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: called!");

        if (getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.list_menu, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called!");

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

    /*****************
     * LISTENERS *****
     * **************/

    private ValueEventListener valueEventListenerGetUserInfo = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            for (DataSnapshot item :
                    dataSnapshot.getChildren()) {

                if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString().equalsIgnoreCase(userEmail)) {

                    userFirstName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue()).toString();
                    userLastName = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue()).toString();
                    userKey = item.getKey();
                    userGroup = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue()).toString();
                    userGroupKey = Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue()).toString();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    private ValueEventListener valueEventListenerGetListOfRestaurantsByCoworkers = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            listOfRestaurantsByCoworker.clear();

            for (DataSnapshot item :
                    dataSnapshot.getChildren()) {

                if (item.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                        .child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString() != null
                        && !item.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                        .child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")
                        && !item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString().equalsIgnoreCase(userEmail)) {

                    /** We create a list with all the restaurants that the users are going to.
                     * If several coworkers are going to the same restaurant, it will appear in the UI
                     * */
                    Log.d(TAG, "onDataChange: " + item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString());
                    listOfRestaurantsByCoworker.add(item.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                            .child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString());

                }
            }

            /** We update the recyclerView with the new list
             * */
            updateRecyclerViewWithNewListOfRestaurantsByCoworker(
                    autocompleteTextView.getText().toString().trim());

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /*****************
     * CONFIGURATION *
     * **************/

    /** Method that instantiates databases
     * */
    public void configureDatabases (Context context) {
        Log.d(TAG, "configureDatabases: called!");

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        localDatabase = AppDatabase.getInstance(context);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

    }

    private void configureAutocompleteTextView (AutoCompleteTextView autoCompleteTextView,
                                                Disposable disposable) {
        Log.d(TAG, "configureAutocompleteTextView: called!");

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1, //This layout has to be a textview
                arrayOfTypes
        );

        autoCompleteTextView.setAdapter(autocompleteAdapter);

        disposable = RxTextView.textChangeEvents(autoCompleteTextView)
                .skip(2)
                .debounce(600, TimeUnit.MILLISECONDS)
                .map(new Function<TextViewTextChangeEvent, String>() {
                    @Override
                    public String apply(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                        return textViewTextChangeEvent.text().toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String type) {
                        Log.d(TAG, "onNext: type = " + type);
                        Log.d(TAG, "onNext: typeAsInt = " + Utils.getTypeAsStringAndReturnTypeAsInt(type, arrayOfTypes));

                        if (Arrays.asList(arrayOfTypes).contains(type)
                                && Utils.getTypeAsStringAndReturnTypeAsInt(type, arrayOfTypes) != 0) {
                            Log.d(TAG, "onNext: getting restaurant by type");
                            getRestaurantsByTypeAndDisplayThemInRecyclerView(Utils.getTypeAsStringAndReturnTypeAsInt(type, arrayOfTypes));

                        } else {
                            Log.d(TAG, "onNext: getting all restaurants");
                            getAllRestaurantsAndDisplayThemInRecyclerView();
                        }

                        Utils.hideKeyboard(getActivity());

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e));

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");


                    }
                });

    }

    private void configureRecyclerView () {
        Log.d(TAG, "configureRecyclerView: called!");

        if (getActivity() != null) {

            recyclerView.setHasFixedSize(true);
            this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            this.listOfRestaurants = new ArrayList<>();
            this.adapter = new RVAdapterList(
                    getActivity(),
                    this.listOfRestaurants,
                    this.listOfRestaurantsByCoworker,
                    glide);
            this.recyclerView.setAdapter(this.adapter);

        }
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

                        if (null == adapter.getRestaurant(position).getName()
                                || adapter.getRestaurant(position).getName().equalsIgnoreCase(RepoStrings.NOT_AVAILABLE_FOR_STRINGS)) {

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

    /******************************************************
     * RX JAVA
     *****************************************************/

    //****************************************************
    // FETCH DATA, MODIFY DATA from LOCAL DATABASE
    //****************************************************

    /** Method that returns all restaurants in the database
     * */
    private Maybe<List<RestaurantEntry>> getAllRestaurantsInDatabase () {
        Log.d(TAG, "getAllRestaurantsInDatabase: called!");
        return localDatabase.restaurantDao().getAllRestaurantsRxJava();
    }

    /** Method that returns all restaurants in database of a specific type
     * */
    private Maybe<List<RestaurantEntry>> getRestaurantsByType(int type) {
        Log.d(TAG, "getRestaurantsByType: called!");
        if (type == 0) {
            return localDatabase.restaurantDao().getAllRestaurantsRxJava();
        } else {
            return localDatabase.restaurantDao().getAllRestaurantsByTypeRxJava(String.valueOf(type));
        }
    }


    //********************************
    // INTERACT WITH DATA
    //********************************

    /** Method that fetches all the restaurants from the database
     * and displays them in the recyclerView
     * */
    private void getAllRestaurantsAndDisplayThemInRecyclerView () {
        Log.d(TAG, "getAllRestaurantsAndDisplayThemInRecyclerView: called!");

        restaurantsObserver = getAllRestaurantsInDatabase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getMaybeObserverToUpdateRecyclerView());

    }

    /** Method that fetches the restaurants from the database according to the type inputted
     * and displays them in the recyclerView
     * */
    private void getRestaurantsByTypeAndDisplayThemInRecyclerView(final int type) {
        Log.d(TAG, "getRestaurantsByTypeAndDisplayThemInRecyclerView: called!");

        restaurantsObserver = getRestaurantsByType(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getMaybeObserverToUpdateRecyclerView());

    }

    //********************************
    // OBSERVERS
    //********************************

    /** This observer returns all the restaurants in the database
     * and allows updating the UI with the info.
     * */
    private MaybeObserver<List<RestaurantEntry>> getMaybeObserverToUpdateRecyclerView() {
        Log.d(TAG, "getMaybeObserverToUpdateMap: ");

        return new MaybeObserver<List<RestaurantEntry>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");

            }

            @Override
            public void onSuccess(List<RestaurantEntry> restaurantEntryList) {
                Log.d(TAG, "onSuccess: " + restaurantEntryList.toString());
                updateRecyclerViewWithNewRestaurantsList(restaurantEntryList);

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        };
    }

    /******************************************************
     * UPDATE RECYCLER VIEW
     *****************************************************/

    private void updateRecyclerViewWithNewRestaurantsList(
            List<RestaurantEntry> listOfFetchedRestaurants) {
        Log.d(TAG, "updateRecyclerViewWithNewRestaurantsList: called!");
        Log.d(TAG, "updateRecyclerViewWithNewRestaurantsList: list = " + listOfFetchedRestaurants.toString());

        if (getActivity() != null) {

            listOfRestaurants.clear();
            listOfRestaurants.addAll(listOfFetchedRestaurants);

            adapter = new RVAdapterList(getActivity(), listOfRestaurants, listOfRestaurantsByCoworker, glide);
            recyclerView.setAdapter(adapter);

            adapter.notifyDataSetChanged();

        }
    }

    private void updateRecyclerViewWithNewListOfRestaurantsByCoworker(String type) {
        Log.d(TAG, "updateRecyclerViewWithNewListOfRestaurantsByCoworker: called!");

        if (Arrays.asList(arrayOfTypes).contains(type)
                && Utils.getTypeAsStringAndReturnTypeAsInt(type, arrayOfTypes) != 0) {
            Log.d(TAG, "onNext: getting restaurant by type");
            getRestaurantsByTypeAndDisplayThemInRecyclerView(Utils.getTypeAsStringAndReturnTypeAsInt(type, arrayOfTypes));

        } else {
            Log.d(TAG, "onNext: getting all restaurants");
            getAllRestaurantsAndDisplayThemInRecyclerView();
        }

    }

    /******************************************************
     * OTHER METHODS
     *****************************************************/

    /** Method that creates an intent and fills it with all the necessary info to be displayed
     * in Restaurant Activity
     * */
    private Intent createAndFillIntentWithUserInfo(RVAdapterList adapter, int position) {
        Log.d(TAG, "createAndFillIntentWithUserInfo: called!");

        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getPlaceId());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getImageUrl());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getName());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getType());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getAddress());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getRating());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getPhone());
        Log.d(TAG, "createAndFillIntentWithUserInfo: " + adapter.getRestaurant(position).getWebsiteUrl());

        Intent intent = new Intent(getActivity(), RestaurantActivity.class);

        intent.putExtra(RepoStrings.SentIntent.PLACE_ID, adapter.getRestaurant(position).getPlaceId());
        intent.putExtra(RepoStrings.SentIntent.IMAGE_URL, adapter.getRestaurant(position).getImageUrl());
        intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME, adapter.getRestaurant(position).getName());
        intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, adapter.getRestaurant(position).getType());
        intent.putExtra(RepoStrings.SentIntent.ADDRESS, adapter.getRestaurant(position).getAddress());
        intent.putExtra(RepoStrings.SentIntent.RATING, adapter.getRestaurant(position).getRating());
        intent.putExtra(RepoStrings.SentIntent.PHONE, adapter.getRestaurant(position).getPhone());
        intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL, adapter.getRestaurant(position).getWebsiteUrl());

        return intent;

    }



}
