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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */

/** This class is responsible of
 * adding a restaurant to the group at 4p and
 * deleting it from the user's
 * */
public class AddRestaurantToGroupDailyJob extends DailyJob {

    public static final String TAG = "AddRestaurantToGroupDai";

    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUsers;
    private DatabaseReference fireDbRefGroups;

    private String userKey;
    private String userGroupKey;

    private SharedPreferences sharedPref;

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        Log.d(TAG, "onRunDailyJob: called!");

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");
        userGroupKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP_KEY, "");

        /* We add the restaurant as a visited restaurant to the database and delete the restaurant info
        * of the user
        * */
        addRestaurantToVisitedRestaurantsInDatabaseAndDeleteUsersRestaurantInfo();

        return DailyJobResult.SUCCESS;
    }

    public static int scheduleAddRestaurantToGroupDailyJob () {
        Log.d(TAG, "scheduleAddRestaurantToGroupDailyJob: called!");

        return DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(16),
                TimeUnit.HOURS.toMillis(17));

    }

    /** Method that adds the user restaurant to the current group of the user
     * and deletes the user's restaurant info from the database
     * */
    public void addRestaurantToVisitedRestaurantsInDatabaseAndDeleteUsersRestaurantInfo() {
        Log.d(TAG, "addRestaurantToVisitedRestaurantsInDatabaseAndDeleteUsersRestaurantInfo: called!");

        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS)
                .child(userKey)
                .child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                .child(RepoStrings.FirebaseReference.RESTAURANT_NAME);
        fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                if (userGroupKey != null) {

                    fireDbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS).child(userGroupKey).child(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);

                    if (dataSnapshot.getValue().toString() != null) {
                        Map<String,Object> map = new HashMap<>();
                        map.put(dataSnapshot.getValue().toString(), true);
                        fireDbRefGroups.updateChildren(map);

                        /* We delete the users restaurant info from the database
                        * */
                        deleteUsersRestaurantFromDatabase();

                    } else {
                        ToastHelper.toastShort(getContext(), "No restaurant in database");

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.toString() );

            }
        });

    }

    public void deleteUsersRestaurantFromDatabase () {
        Log.d(TAG, "deleteUsersRestaurantFromDatabase: called!");

        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS)
                .child(userKey)
                .child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);

        UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(fireDbRefUsers);

    }
}
