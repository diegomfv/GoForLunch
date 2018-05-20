package com.example.android.goforlunch.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.auth.MainViewModelDELETE;
import com.example.android.goforlunch.activities.auth.RVAdapterRestaurantDELETE;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

// TODO: 20/05/2018 See UDACITY, Android Development, Android Architecture Components, 23 and forward
// TODO: 20/05/2018 to see how to work with search queries by id
public class FirebaseActivityDELETE extends AppCompatActivity {

    private static final String TAG = "FirebaseActivityDELETE";

    private Button button;
    private Button button2;

    private HashMap<String,String> userData;

    private FirebaseDatabase database;
    private DatabaseReference myRefToRestaurants;
    private DatabaseReference myRefToIDs;

    private ChildEventListener mChildEventListener;


    //Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RVAdapterRestaurantDELETE mAdapter;

    //Database
    private AppDatabase mDb;

    //Variables for objects
    private String placeId;
    private String name;
    private String type;
    private String address;
    private String openUntil;
    private String distance;
    private String rating;
    private String imageUrl;

    private static int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        counter = 0;

        mRecyclerView = (RecyclerView) findViewById(R.id.firebase_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(FirebaseActivityDELETE.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDb = AppDatabase.getInstance(getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.clearAllTables();
            }
        });

        button = findViewById(R.id.firebase_button_id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                counter++;

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {

                        placeId = "1";
                        name = "Koh Thai - " + counter;
                        type = "Thai";
                        address = "Elmdale Road 119";
                        openUntil = "20.00pm";
                        distance = "120m";
                        rating = "4.5";
                        imageUrl = "https://imager.url";

                        RestaurantEntry restaurantEntry = new RestaurantEntry(
                                placeId,
                                name,
                                type,
                                address,
                                openUntil,
                                distance,
                                rating,
                                imageUrl
                        );

                        mDb.restaurantDao().insertRestaurant(restaurantEntry);

                    }
                });

            }
        });

        button2 = findViewById(R.id.firebase_button_retrieve_item_id);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LiveData<RestaurantEntry> restaurant = mDb.restaurantDao().getRestaurantByPlaceId("1");
                restaurant.observe(FirebaseActivityDELETE.this, new Observer<RestaurantEntry>() {
                    @Override
                    public void onChanged(@Nullable RestaurantEntry restaurantEntry) {

                        //Not clear what this does (.removeObserver). See UDACITY, Android Dev,
                        // Lesson 8. Android Architecture Components, 20
                        restaurant.removeObserver(this);
                        Log.d(TAG, "onChanged: Receiving database update from LiveData");
                        Log.d(TAG, "onChanged: restaurant --> " + restaurantEntry.toString());
                    }
                });
            }
        });

        /** LiveData runs by default outside of the main thread. We can avoid using
         * the executor with it (with the other operations (insert, update and delete)
         * we still need to use the executors.
         * - LifeCycleOwner: objects that have a lifecycle, like Activities and Fragments.
         * - LifeCycleObservers: Observe lifeCycleOwners and get notified on
         * lifecycle changes.
         * LiveData is lifecycle aware because it's a lifecycle observer. When we call observe() method
         * and we pass the activity, we tell it which lifeCycleOwnser it should observe (in this case,
         * the activity and therefore its lifecycle).
         */
//        LiveData<List<RestaurantEntry>> restaurants = mDb.restaurantDao().getAllRestaurants();
//        restaurants.observe(FirebaseActivityDELETE.this, new Observer<List<RestaurantEntry>>() {
//            @Override
//            public void onChanged(@Nullable List<RestaurantEntry> restaurantEntries) {
//                Log.d(TAG, "onChanged: Receiving database update from LiveData");
//
//                for (int i = 0; i < restaurantEntries.size() ; i++) {
//                    Log.d(TAG, "onChanged: item#" + i + ": " + restaurantEntries.get(i).toString());
//                }
//                mAdapter = new RVAdapterRestaurantDELETE(FirebaseActivityDELETE.this, restaurantEntries);
//                mRecyclerView.setAdapter(mAdapter);
//            }
//        });


        /** This substitutes the code immediately above. It makes that we receive the updates
         * from the LiveDate IN the ViewModel
         * */
        MainViewModelDELETE mainViewModelDELETE = ViewModelProviders.of(this).get(MainViewModelDELETE.class);
        mainViewModelDELETE.getRestaurants().observe(this, new Observer<List<RestaurantEntry>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantEntry> restaurantEntries) {
                Log.d(TAG, "onChanged: Updating list of tasks from LiveData in ViewModel");
                mAdapter = new RVAdapterRestaurantDELETE(FirebaseActivityDELETE.this, restaurantEntries);
                mRecyclerView.setAdapter(mAdapter);
            }
        });





////
////        database = FirebaseDatabase.getInstance();
////        myRefToRestaurants = database.getReference();
////
////        userData = new HashMap<>();
////
////        userData.put("username","David123");
////        userData.put("password","yeeha!");
//
//
//
//        button = (Button) findViewById(R.id.firebase_button_id);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                myRefToRestaurants.child("Users").child("lord_edeas").setValue(userData);
//
//            }
//        });
//
//        mChildEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                /** Gets called whenever a niew message is inserted into the
//                 * messages list.
//                 * Besides, is also triggered for every child message in the
//                 * list when the listener is first attached.
//                 * */
//
//                //In this case, the dataSnapshot will always contain the message
//                //that has been added
//
//                //This will only work if the dataSnapshot has the same fields
//                //as the object
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                /** Gets called when the contents of an existing message
//                 * gets changed */
//
//
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                /** Will get called when an existing message is deleted
//                 * */
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                /** Would get called if one of out messages changed
//                 * position in the list
//                 * */
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                /** Indicates that some sort of error occurred when you
//                 * are trying to make changes. Typically, if this is being called
//                 * it means that you don't have permission to read the data
//                 * */
//
//            }
//        };





    }




}
