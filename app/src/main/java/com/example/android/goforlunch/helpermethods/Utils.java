package com.example.android.goforlunch.helpermethods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.repository.RepoStrings;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class Utils {

    private static final String TAG = "Utils";

    public static void checkInternetInBackgroundThread (final DisposableObserver disposableObserver) {
        Log.d(TAG, "checkInternetInBackgroundThread: called! ");

        // TODO: 27/06/2018 If I don't use AppExecutors it crashes
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: checking internet connection...");

                Observable.just(Utils.isInternetAvailable())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(disposableObserver);
            }

        });

    }

    // Background thread!!
    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    public static boolean isInternetAvailable() {
        Log.d(TAG, "isInternetAvailable: called!");
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            Log.d(TAG, "isInternetAvailable: true");
            return true;
        } catch (IOException e) {
            Log.d(TAG, "isInternetAvailable: false");
            return false; }
    }

    /** Method that shows progress bar and disables user interaction
     * */
    public static void showProgressBarAndDisableUserInteraction (AppCompatActivity appCompatActivity, ProgressBar progressBar) {

        progressBar.setVisibility(View.VISIBLE);

        appCompatActivity
                .getWindow()
                .setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


    }

    /**  Method that hides progress bar and enables user interaction
     * */
    public static void hideProgressBarAndEnableUserInteraction (AppCompatActivity appCompatActivity, ProgressBar progressBar) {

        progressBar.setVisibility(View.INVISIBLE);

        appCompatActivity
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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

   /** Method to fill an intent with Restaurant Entry info
    * */
   public static Intent fillIntentUsingMapInfo (Intent intent, Map <String, Object> map) {

       if (map != null) {
           intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME, map.get(RepoStrings.FirebaseReference.RESTAURANT_NAME).toString());
           intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, map.get(RepoStrings.FirebaseReference.RESTAURANT_TYPE).toString());
           intent.putExtra(RepoStrings.SentIntent.PLACE_ID, map.get(RepoStrings.FirebaseReference.RESTAURANT_PLACE_ID).toString());
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

    public static String checkToAvoidNull (String string) {
        if (null != string) {
            return string;
        } else {
            return RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
        }
    }

    /** Method that formats the date that we get from the request to insert it in
     * the database with the new format (the one that will be displayed)
     * */
    public static String formatTime (String time) {

        time = time.substring(0, 2) + "." + time.substring(2, time.length());
        return "Open until " + time;

    }

    /** Method that transforms a restaurant type from String type to int type
     * */
    public static int getTypeAsStringAndReturnTypeAsInt (String type, String[] arrayOfTypes) {
        Log.d(TAG, "getTypeAsStringAndReturnTypeAsInt: called!");

        if (Arrays.asList(arrayOfTypes).contains(type)) {
            return Arrays.asList(arrayOfTypes).indexOf(type);
        } else return 0;

    }

    /** This method transforms the type of the restaurant from an int to a String
     * */
    public static String transformTypeToString (Context context, int type) {

        String[] arrayOfTypes = context.getResources().getStringArray(R.array.typesOfRestaurants);

        switch (type) {

            case 0: return arrayOfTypes[0];
            case 1: return arrayOfTypes[1];
            case 2: return arrayOfTypes[2];
            case 3: return arrayOfTypes[3];
            case 4: return arrayOfTypes[4];
            case 5: return arrayOfTypes[5];
            case 6: return arrayOfTypes[6];
            case 7: return arrayOfTypes[7];
            case 8: return arrayOfTypes[8];
            case 9: return arrayOfTypes[9];
            case 10: return arrayOfTypes[10];
            case 11: return arrayOfTypes[11];
            case 12: return arrayOfTypes[12];
            case 13: return arrayOfTypes[13];
            default: return arrayOfTypes[13];

        }
    }

    /** Method that transforms the rating to adapt it to 3 stars
     *  */
    public static float adaptRating (float rating ) {
        return rating * 3 / 5;
    }

    /** Method that prints internal storage files (used for debug)
     * */
    public static void printFiles (String dirPath) {
        Log.d(TAG, "printFiles: called!");

        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files.length != 0) {
            for (File aFile : files) {
                Log.d(TAG, "getFiles: " + aFile.getName() + ", " + aFile.length());
            }
        } else {
            Log.d(TAG, "printFiles: no files found!");
        }
    }

    /** Checks if a string can be transformed to an Integer
     * */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /** Method used to get
     * the first name and last name of the user
     * */
    public static String[] getFirstNameAndLastName (String names) {

        if (names != null) {
            return names.split(" ");

        } else {
            return null;
        }
    }

    /** We use this method to check if the strings that come from the intent are null or not
     * */
    public static String checkIfIsNull (String string) {

        if (string == null) {
            return "";
        } else {
            return string;
        }
    }
    /** Method used to avoid memory leaks
     * */
    public static void dispose (Disposable disposable) {
        if (disposable != null
                && !disposable.isDisposed()) {
            disposable.dispose();
        }

    }

    @SuppressLint("CheckResult")
    public static void configureTextInputEditTextWithHideKeyboard (final AppCompatActivity activity, TextInputEditText textInputEditText) {

        RxTextView.textChangeEvents(textInputEditText)
            .skip(2)
            .debounce(1000,TimeUnit.MILLISECONDS)
            .map(new Function<TextViewTextChangeEvent, String>() {
                @Override
                public String apply(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                    return textViewTextChangeEvent.text().toString();
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableObserver<String>() {
                @Override
                public void onNext(String text) {
                    Log.d(TAG, "onNext: text = " + text);

                  if (text.length() > 3) {
                      Utils.hideKeyboard(activity);
                  }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "onError: " + Log.getStackTraceString(e));

                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete: ");


                }
            });



    }



}