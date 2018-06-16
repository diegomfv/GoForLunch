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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.pojo.User;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    public static void loadImageWithGlide (Context context, Object object, ImageView view) {

        Glide.with(context)
                .load(object)
                .into(view);

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


   /***/
    /** Method that deletes all the sharedPreferences info
     * */
    public static boolean deleteSharedPreferencesInfo(SharedPreferences sharedPref) {
        Log.d(TAG, "deleteSharedPreferencesInfo: called!");

        if (sharedPref.getAll().size() > 0) {

            Map<String, ?> map = sharedPref.getAll();

            for (Map.Entry<String, ?> entry :
                    map.entrySet()) {

                updateSharedPreferences(sharedPref, entry.getKey(), "");

            }
        }

        // TODO: 02/06/2018 Delete this!
        Map<String, ?> prefsMap = sharedPref.getAll();
        for (Map.Entry<String, ?> entry: prefsMap.entrySet()) {
            Log.d(TAG, "SharedPreferences: " + entry.getKey() + ":" +
                    entry.getValue().toString());
        }

        return true;
    }

}