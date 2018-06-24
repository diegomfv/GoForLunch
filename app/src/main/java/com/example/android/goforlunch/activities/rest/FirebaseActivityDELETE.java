package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.data.sqlite.AndroidDatabaseManager;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.remote.requesters.RequesterTextSearch;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.example.android.goforlunch.helpermethods.Utils.getStringFromSharedPreferences;
import static com.example.android.goforlunch.helpermethods.Utils.updateSharedPreferences;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

// TODO: 20/05/2018 See UDACITY, Android Development, Android Architecture Components, 23 and forward
// TODO: 20/05/2018 to see how to work with search queries by id
public class FirebaseActivityDELETE extends AppCompatActivity {

    private static final String TAG = "FirebaseActivityDELETE";

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button button10;
    private Button button11;
    private Button button12;

    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    //List of Fake Data
    private List<Name> listOfNames;
    private List<String> listOfEmails;
    private List<String> listOfGroups;
    private List<String> listOfRestaurants;
    private Map<String, Object> mapEmailGroup;

    private Random random;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(FirebaseActivityDELETE.this);
        fireDb = FirebaseDatabase.getInstance();

        random = new Random();

        listOfEmails = new ArrayList<>();
        listOfGroups = new ArrayList<>();
        listOfRestaurants = new ArrayList<>();

        listOfGroups.add("Amazon");
        listOfGroups.add("Google");
        listOfGroups.add("Apple");
        listOfGroups.add("Samsung");

        listOfRestaurants.add("Burger King");
        listOfRestaurants.add("McDonalds");
        listOfRestaurants.add("KFC");
        listOfRestaurants.add("Tony Romas");

        final NameGenerator generator = new NameGenerator();

        listOfNames = generator.generateNames(45);

        for (int i = 0; i < listOfNames.size(); i++) {

            listOfEmails.add(
                    listOfNames.get(i).getFirstName().toLowerCase()
                            + "_"
                            + listOfNames.get(i).getLastName().toLowerCase()
                            + "@gmail.com");

        }

        button1 = findViewById(R.id.firebase_button1_id);
        button2 = findViewById(R.id.firebase_button2_id);
        button3 = findViewById(R.id.firebase_button3_id);
        button4 = findViewById(R.id.firebase_button4_id);
        button5 = findViewById(R.id.firebase_button5_id);
        button6 = findViewById(R.id.firebase_button6_id);
        button7 = findViewById(R.id.firebase_button7_id);
        button8 = findViewById(R.id.firebase_button8_id);
        button9 = findViewById(R.id.firebase_button9_id);
        button10 = findViewById(R.id.firebase_button10_id);
        button11 = findViewById(R.id.firebase_button11_id);
        button12 = findViewById(R.id.firebase_button12_id);

        /**
         * INSERT RANDOM USERS IN DATABASE
         * */
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: CALLED!");

                for (int i = 0; i < 45; i++) {

                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                    String key = dbRefUsers.push().getKey();

                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + key);
                    UtilsFirebase.updateUserInfoInFirebase(dbRefUsers,
                            listOfNames.get(i).getFirstName(),
                            listOfNames.get(i).getLastName(),
                            listOfEmails.get(i),
                            listOfGroups.get(random.nextInt(4)),
                            "",
                            "",
                            "");

                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + key + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                    UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "");

                }
            }
        });

        /** INSERT GROUPS IN DATABASE
         * */
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button 2 CALLED!");

                dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
                Map<String, Object> map;

                for (int i = 0; i < listOfGroups.size(); i++) {

                    map = new HashMap<>();
                    map.put(RepoStrings.FirebaseReference.GROUP_NAME, listOfGroups.get(i));
                    map.put(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED, "");

                    dbRefGroups.push().setValue(map);
                }
            }
        });

        /** GETTING THE KEY WHEN USER IS PUSHED AND INSERTING INFO!
         * **/

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button 3 CALLED!");

                DatabaseReference dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                String key = dbRefUsers.push().getKey();

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + key);
                UtilsFirebase.updateUserInfoInFirebase(dbRefUsers,
                        "Alfa",
                        "Beta",
                        "",
                        "",
                        "",
                        "",
                        "");

            }
        });

        /** INSERTING NEW RESTAURANTS IN ALL GROUPS (NOT USED)*/

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button4 clicked!");

                final DatabaseReference dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                final DatabaseReference dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);

                final Map<String, Object> map = new HashMap<>();
                dbRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {
                            map.put(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue().toString(), item.getKey());
                        }

                        Log.d(TAG, "onDataChange: map!!!! = " + map.toString());

                        dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                List<String> listOfMapKeys = new ArrayList<>(map.keySet());
                                Log.d(TAG, "onDataChange: listOfMapKeys = " + listOfMapKeys.toString());

                                for (int i = 0; i < listOfMapKeys.size() ; i++) {

                                    for (DataSnapshot item :
                                            dataSnapshot.getChildren()) {

                                        if (item.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString()
                                                .equalsIgnoreCase(listOfMapKeys.get(i))){

                                            Map <String, Object> newRestaurant = new HashMap<>();

                                            if (!item.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                                                newRestaurant.put(item.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString(), true);
                                            }

                                            dbRefGroups.child(map.get(listOfMapKeys.get(i)).toString()).child(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED).setValue(newRestaurant);

                                        }
                                    }
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
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button5 clicked!");

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase mDb = AppDatabase.getInstance(getApplicationContext());
                        long value = mDb.restaurantDao().insertRestaurant(new RestaurantEntry(
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS,
                                RepoStrings.NOT_AVAILABLE_FOR_STRINGS
                        ));

                        Log.d(TAG, "run: VALUE = " + value);
                    }
                });

            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.GROUP_NAME);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                        Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());

                    }
                });



            }
        });

        /** INSERTING RESTAURANT IN GROUPS AND DELETING RESTAURANT FROM USER'S INFO*/

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button7 clicked!");

                final String userKey = "-LEqJnfBmv5WGhGQGoC9";
                final String userGroupKey = "-LEqJw9D1D6YwXUrdJb0";

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        /** Getting user's restaurant info
                         * */
                        Map <String, Object> map = UtilsFirebase.fillMapUsingDataSnapshot(dataSnapshot);

                        /** Inserting a new restaurant in the group
                         * */
                        dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS + "/" + userGroupKey + "/" + RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);
                        UtilsFirebase.insertNewRestaurantInGroupInFirebase(dbRefGroups, map.get(RepoStrings.FirebaseReference.RESTAURANT_NAME).toString());

                        /** Deleting user info from database
                         * */
                        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                        UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(dbRefUsers);
                        ToastHelper.toastShort(FirebaseActivityDELETE.this, "User Restaurant Deleted");


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());

                    }
                });





            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button 8 clicked!");

                String userKey = getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY);
                String userRestaurant = getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_RESTAURANT_NAME);
                String userGroupKey = getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_GROUP_KEY);
                String userGroup = getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_GROUP);

                Log.d(TAG, "onClick: userKey: " + userKey);
                Log.d(TAG, "onClick: userRestaurant: " + userRestaurant);
                Log.d(TAG, "onClick: userGroupKey: " + userGroupKey);
                Log.d(TAG, "onClick: userGroup: " + userGroup);

            }
        });

        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button 9 clicked!");

                final String userGroup = getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_GROUP);
                Log.d(TAG, "onClick: userGroup: " + userGroup);

                dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
                dbRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: groupKey = " + item.getKey());

                            if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue()).equals(userGroup)){
                                updateSharedPreferences(sharedPref ,RepoStrings.SharedPreferences.USER_GROUP_KEY, item.getKey());
                                Log.d(TAG, "onDataChange: groupKeySharedPref " + getStringFromSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_GROUP_KEY));
                            }


                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());

                    }
                });



            }
        });

        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button10");

                startActivity(new Intent(FirebaseActivityDELETE.this, AndroidDatabaseManager.class));
            }
        });


        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button11");
                Log.d(TAG, "onClick: " + sharedPref.getAll().toString());

                AppDatabase appDatabase = AppDatabase.getInstance(FirebaseActivityDELETE.this);
                RestaurantEntry restaurant = appDatabase.restaurantDao().getRestaurantByName("Filini Restaurant");

                Map<String, Object> map = UtilsFirebase.fillMapUsingRestaurantEntry(restaurant);

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + "-LEqJnfBmv5WGhGQGoC9" + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                UtilsFirebase.updateInfoWithMapInFirebase(dbRefUsers, map);

            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: button12 clicked!");

                LatLngForRetrofit myPosition = new LatLngForRetrofit(51.456961, -2.606416);
                AppDatabase mDb = AppDatabase.getInstance(FirebaseActivityDELETE.this);
                mDb.restaurantDao().deleteAllRowsInRestaurantTable();

                startRequestUsingTextSearchService(mDb, myPosition);

            }
        });


    }

    /** Method that starts the request using TextSearch service
     * */
    private void startRequestUsingTextSearchService (AppDatabase mDb, LatLngForRetrofit myPosition) {

        RequesterTextSearch requesterTextSearch = new RequesterTextSearch(mDb, myPosition);
        requesterTextSearch.doApiRequest();

    }



}