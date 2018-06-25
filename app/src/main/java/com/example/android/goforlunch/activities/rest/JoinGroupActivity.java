package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.recyclerviewadapter.RVJoinGroup;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class JoinGroupActivity extends AppCompatActivity {

    private static final String TAG = JoinGroupActivity.class.getSimpleName();

    private FloatingActionButton fab;
    private Button buttonJoinGroup;
    private Button buttonCreateGroup;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String userKey;
    private String userGroup;

    private List<String> listOfGroups;
    private Map<String,Object> mapOfKeyGroups;

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        fab = (FloatingActionButton) findViewById(R.id.join_fab_id);

        buttonJoinGroup = (Button) findViewById(R.id.join_button_join_group_id);
        buttonCreateGroup = (Button) findViewById(R.id.join_button_create_group_id);

        mRecyclerView = (RecyclerView) findViewById(R.id.join_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(JoinGroupActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        Log.d(TAG, "onCreate: userKey = " + userKey);

        listOfGroups = new ArrayList<>();
        mapOfKeyGroups = new HashMap<>();



        /** We get the user's group
         * */
        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
        dbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                userGroup = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString();

                /** We get the group's list
                 * */
                dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
                dbRefGroups.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        listOfGroups = new ArrayList<>();

                        for (DataSnapshot item :
                                dataSnapshot.getChildren()) {

                            if (!listOfGroups.contains(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue().toString())) {
                                listOfGroups.add(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue().toString());
                            }
                        }

                        /** We fill the RV with the Restaurants in the database, the userKey and the userGroup
                         * */
                        mAdapter = new RVJoinGroup(JoinGroupActivity.this, listOfGroups, userKey, userGroup);
                        mRecyclerView.setAdapter(mAdapter);

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: fab clicked!");

                startActivity(new Intent(JoinGroupActivity.this, MainActivity.class));
                finish();
            }
        });

        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buttonCreateGroup clicked!");

                ToastHelper.toastShort(JoinGroupActivity.this, "Not implemented!");

            }
        });

    }
}
