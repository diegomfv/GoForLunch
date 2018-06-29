package com.example.android.goforlunch.job;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class AddRestaurantToGroupDailyJob extends DailyJob {

    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    private String userKey;
    private String userGroupKey;

    private SharedPreferences sharedPref;

    public static final String TAG = "AddRestaurantToGroupDai";

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");
        userGroupKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP_KEY, "");

        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
        dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                // TODO: 13/06/2018 Might not be necessary to check internet connection
                /** Getting user's restaurant info
                 * */
                Map<String, Object> map = UtilsFirebase.fillMapWithRestaurantInfoUsingDataSnapshot(dataSnapshot);

                /** Inserting a new restaurant in the group
                 * */
                dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS + "/" + userGroupKey + "/" + RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);
                UtilsFirebase.insertNewRestaurantInGroupInFirebase(dbRefGroups, map.get(RepoStrings.FirebaseReference.RESTAURANT_NAME).toString());

                /** Deleting user info from database
                 * */
                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(dbRefUsers);
                ToastHelper.toastShort(getContext(), "User Restaurant Deleted");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    public static int scheduleAddRestaurantToGroupDailyJob () {

        return DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(16),
                TimeUnit.HOURS.toMillis(17));

    }

}
