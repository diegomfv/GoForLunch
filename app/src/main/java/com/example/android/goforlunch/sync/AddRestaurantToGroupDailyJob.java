package com.example.android.goforlunch.sync;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.constants.RepoStrings;
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
    private DatabaseReference fireDbRefUser;
    private DatabaseReference fireDbRefUserRestInfo;
    private DatabaseReference fireDbRefGroup;

    private String userKey;
    private String userGroupKey;
    private String userRestaurant;

    private SharedPreferences sharedPref;

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        Log.d(TAG, "onRunDailyJob: called!");

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        /* We get the user key
        * */
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

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

        /* We will only add to the database the current's user restaurant!
        * If there are more users in the same device, their restaurants
        * won't be added
        * */

        if (userKey.equalsIgnoreCase("")) {
            //do nothing

        } else {

            fireDbRefUser = fireDb.getReference(RepoStrings.FirebaseReference.USERS)
                    .child(userKey);

            fireDbRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                    userGroupKey = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue().toString();
                    userRestaurant = dataSnapshot.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                            .child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString();

                    if (userGroupKey == null) {
                        //do nothing because the user has no group

                    } else {
                        /* The user has a group
                         * */

                        /* We update the restaurants visited of the group in the database
                         * */
                        fireDbRefGroup = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS)
                                .child(userGroupKey)
                                .child(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED);

                        Map <String, Object> map = new HashMap<>();
                        map.put(userRestaurant, true);

                        fireDbRefGroup.updateChildren(map);

                        /* We delete the restaurant info of the user in firebase
                         * */
                        fireDbRefUserRestInfo = fireDbRefUser.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                        UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(fireDbRefUserRestInfo);

                        fireDbRefUser.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: " + databaseError.toString() );

                }
            });
        }
    }
}
