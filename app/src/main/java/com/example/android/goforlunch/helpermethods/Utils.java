package com.example.android.goforlunch.helpermethods;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class Utils {

    private static final String TAG = "Utils";

    public static void menuIconColor(MenuItem menuItem, int color) {
        Drawable drawable = menuItem.getIcon();
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Method that hides the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /** Method that
     * capitalizes a string
     * */
    public static String capitalize (String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /** Method to insert
     * info to Shared Preferences
     * */
    public static boolean updateSharedPreferences(SharedPreferences sharedPref, String key, String value) {
        sharedPref.edit().putString(key,value).apply();
        return true;
    }

    /** Method to get
     * info (a String) from Shared Preferences
     * */
    public static String getStringFromSharedPreferences (SharedPreferences sharedPref, String key) {
        return sharedPref.getString(key, "");
    }

    /**
     * Method that deletes
     * all the restaurant info from a user in Firebase
     * */
    public static boolean deleteRestaurantsUserInfoFromFirebase(DatabaseReference dbRef) {

        Map<String, Object> map = new HashMap<>();
        map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PLACE_ID, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_RATING, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_IMAGE_URL, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PHONE, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_WEBSITE_URL, "");

        dbRef.updateChildren(map);
        return true;
    }

    /** Method that updates
     *  user's info in Firebase
     * */
    public static boolean updateUserInfoInFirebase (DatabaseReference dbRef,
                                                    String firstName,
                                                    String lastName,
                                                    String email,
                                                    String group,
                                                    String groupKey,
                                                    String userRestaurantInfo) {

        Map <String, Object> map = new HashMap<>();
        map.put(RepoStrings.FirebaseReference.USER_FIRST_NAME, firstName);
        map.put(RepoStrings.FirebaseReference.USER_LAST_NAME, lastName);
        map.put(RepoStrings.FirebaseReference.USER_EMAIL, email);
        map.put(RepoStrings.FirebaseReference.USER_GROUP, group);
        map.put(RepoStrings.FirebaseReference.USER_GROUP_KEY, groupKey);
        map.put(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO, userRestaurantInfo);

        dbRef.updateChildren(map);
        return true;
    }

    /** Method that returns
     * all user restaurant info
     * */
    public static Map<String,Object> getUserRestaurantInfoFromDataSnapshot (DataSnapshot dataSnapshot) {

        Map <String, Object> map = new HashMap<>();

        map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_TYPE).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_RATING, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_RATING).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PLACE_ID, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_PLACE_ID).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PHONE, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_PHONE).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_IMAGE_URL, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_IMAGE_URL).getValue().toString());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_WEBSITE_URL, dataSnapshot.child(RepoStrings.FirebaseReference.RESTAURANT_WEBSITE_URL).getValue().toString());

        return map;
    }

    /**
     * Method that inserts
     * Restaurant info into user's info in Firebase
     * */
    public static boolean updateUserRestaurantInfoInFirebase (DatabaseReference dbRef,
                                                              Map <String, Object> map) {

        dbRef.updateChildren(map);
        return true;
    }

    /** Method to fill a map with RestaurantEntry info
     * */
    public static Map<String, Object> fillMapUsingRestaurantEntry (RestaurantEntry restaurant) {

        Map<String, Object> map = new HashMap<>();

        map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, restaurant.getName());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, restaurant.getType());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS, restaurant.getAddress());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_RATING, restaurant.getRating());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PLACE_ID, restaurant.getPlaceId());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_PHONE, restaurant.getPhone());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_IMAGE_URL, restaurant.getImageUrl());
        map.put(RepoStrings.FirebaseReference.RESTAURANT_WEBSITE_URL, restaurant.getWebsiteUrl());

        return map;
    }



    /**
     * Method that inserts
     * a new restaurant visited into the group
     * */
    public static boolean insertNewRestaurantInGroupInFirebase (DatabaseReference dbRef,
                                                               String restaurantName) {

        Map<String, Object> map = new HashMap<>();
        map.put(restaurantName, true);

        dbRef.updateChildren(map);
        return true;
   }



   /** Method to fill an intent with Restaurant Entry info
    * */
   public static Intent fillIntentUsingMapInfo (Intent intent, Map <String, Object> map) {

       if (map != null) {
           intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME, map.get(RepoStrings.FirebaseReference.RESTAURANT_NAME).toString());
           intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, map.get(RepoStrings.FirebaseReference.RESTAURANT_TYPE).toString());
           intent.putExtra(RepoStrings.SentIntent.ADDRESS, map.get(RepoStrings.FirebaseReference.RESTAURANT_ADDRESS).toString());
           intent.putExtra(RepoStrings.SentIntent.RATING, map.get(RepoStrings.FirebaseReference.RESTAURANT_RATING).toString());
           intent.putExtra(RepoStrings.SentIntent.PHONE, map.get(RepoStrings.FirebaseReference.RESTAURANT_PHONE).toString());
           intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL, map.get(RepoStrings.FirebaseReference.RESTAURANT_WEBSITE_URL).toString());
           intent.putExtra(RepoStrings.SentIntent.IMAGE_URL, map.get(RepoStrings.FirebaseReference.RESTAURANT_IMAGE_URL).toString());
           return intent;

       } else {
           Log.d(TAG, "fillIntentUsingMapInfo: map is null");
           return null;
       }

   }



}