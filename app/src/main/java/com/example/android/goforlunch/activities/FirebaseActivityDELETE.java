package com.example.android.goforlunch.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.auth.RVAdapterRestaurantDELETE;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.pojo.User;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Random;

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

    private FirebaseDatabase fDb;
    private DatabaseReference dbRef;

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

    private static int counter = 0;

    private List<User> listOfUsers;

    //List of Fake Data
    List<Name> listOfNames;
    List<String> listOfEmails;
    List<String> listOfGroups;
    List<String> listOfRestaurants;

    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        random = new Random();

        listOfUsers = new ArrayList<>();
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
        listOfRestaurants.add("Tony Roma's");

        NameGenerator generator = new NameGenerator();

        listOfNames = generator.generateNames(45);

        for (int i = 0; i < listOfNames.size() ; i++) {

            listOfEmails.add(
                    listOfNames.get(i).getFirstName()
                            + "_"
                            + listOfNames.get(i).getLastName()
                            + "@gmail.com");

        }

        fDb = FirebaseDatabase.getInstance();
        dbRef = fDb.getReference("users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildren() != null) {

                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                    Log.d(TAG, "onDataChange: KEY = " + dataSnapshot.getKey());

                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: DATASNAPSHOT = " + item.toString());

//                        User user = new User(
//                        item.child("firstname").getValue().toString(),
//                        item.child("lastname").getValue().toString(),
//                        item.child("group").getValue().toString(),
//                        item.child("restaurant").getValue().toString()
//                        );

                        //listOfUsers.add(user);
                    }

                    Log.d(TAG, "onDataChange: " + listOfUsers.toString());

//                    while (dataSnapshot.getChildren().iterator().next() != null) {
//                    counter++;
//                    Log.d(TAG, "onDataChange: " + dataSnapshot.getChildren().iterator().next());
//                    Log.d(TAG, "onDataChange: COUNTER = " + counter);
//
//                }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        button = findViewById(R.id.firebase_button_id);
        button2 = findViewById(R.id.firebase_button_retrieve_item_id);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: CALLED!");

                User user;

                for (int i = 0; i < listOfNames.size(); i++) {

                    user = new User(
                            listOfNames.get(i).getFirstName(),
                            listOfNames.get(i).getLastName(),
                            listOfEmails.get(i),
                            listOfGroups.get(random.nextInt(4)),
                            listOfRestaurants.get(random.nextInt(4)),
                            "American"
                    );

                    dbRef.push().setValue(user);
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: CALLED!");

            }
        });
    }
}