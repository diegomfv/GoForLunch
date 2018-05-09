package com.example.android.goforlunch.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.pojo.RestaurantObject;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

public class FirebaseActivityDELETE extends AppCompatActivity {

    Button button;

    Map<String,String> userData;

    private FirebaseDatabase database;
    private DatabaseReference myRefToRestaurants;
    private DatabaseReference myRefToIDs;

    private ChildEventListener mChildEventListener;

    private RestaurantObject object;
    private RestaurantObject object2;
    private RestaurantObject object3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        database = FirebaseDatabase.getInstance();
        myRefToRestaurants = database.getReference();

        userData = new HashMap<>();

        userData.put("username","David123");
        userData.put("password","yeeha!");

/*
        object = new RestaurantObject(
                1,
                "LaOrza",
                "Calle",
                "10.00",
                "200m",
                "0",
                "3",
                "www.");

        object2 = new RestaurantObject(
                2,
                "LaOrza",
                "Calle",
                "10.00",
                "200m",
                "0",
                "3",
                "www.");

        object3 = new RestaurantObject(
                3,
                "LaOrza",
                "Calle",
                "10.00",
                "200m",
                "0",
                "3",
                "www.");*/

        button = (Button) findViewById(R.id.firebase_button_id);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myRefToRestaurants.child("Users").child("lord_edeas").setValue(userData);

            }
        });

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /** Gets called whenever a niew message is inserted into the
                 * messages list.
                 * Besides, is also triggered for every child message in the
                 * list when the listener is first attached.
                 * */

                //In this case, the dataSnapshot will always contain the message
                //that has been added

                //This will only work if the dataSnapshot has the same fields
                //as the object

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                /** Gets called when the contents of an existing message
                 * gets changed */



            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /** Will get called when an existing message is deleted
                 * */
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                /** Would get called if one of out messages changed
                 * position in the list
                 * */

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /** Indicates that some sort of error occurred when you
                 * are trying to make changes. Typically, if this is being called
                 * it means that you don't have permission to read the data
                 * */

            }
        };





    }
}
