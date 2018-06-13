package com.example.android.goforlunch.activities.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Diego Fajardo on 13/06/2018.
 */

// TODO: 13/06/2018 Use this activity as the end of login process
public class AuthGettingUserInfo extends AppCompatActivity {

    private static final String TAG = "AuthGettingUserInfo";

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    private String userEmail;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: 13/06/2018 Change this!
        setContentView(R.layout.activity_auth_choose_login);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthGettingUserInfo.this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {

            userEmail = user.getEmail();

            dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
            dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                    for (DataSnapshot item :
                            dataSnapshot.getChildren()) {

                        /** We look for the user in the database.
                         * If we do not find the user, we should create him, her.*/
                        if (item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString().equalsIgnoreCase(userEmail)) {

                            Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_ID_KEY, item.getKey());
                            Utils.updateSharedPreferences(sharedPref, RepoStrings.SharedPreferences.USER_GROUP_KEY , item.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue().toString());

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.getCode());

                }
            });
        }
    }
}
