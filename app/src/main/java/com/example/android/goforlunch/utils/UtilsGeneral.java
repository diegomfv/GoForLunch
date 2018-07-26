package com.example.android.goforlunch.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.rx.ObservableObject;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.constants.RepoStrings;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class UtilsGeneral {

    private static final String TAG = "UtilsGeneral";

    public static void checkInternetInBackgroundThread (final DisposableObserver disposableObserver) {
        Log.d(TAG, "checkInternetInBackgroundThread: called! ");

        // TODO: 27/06/2018 If I don't use AppExecutors it crashes
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: checking internet connection...");

                Observable.just(UtilsGeneral.isInternetAvailable())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(disposableObserver);
            }

        });

    }

    // Background thread!!
    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    private static boolean isInternetAvailable() {
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

    /** Method used to connect
     * the broadcast receiver with
     * the activity
     * */
    public static void connectReceiver (Context context, BroadcastReceiver receiver, IntentFilter intentFilter, Observer observer){
        Log.d(TAG, "connectReceiver: called!");

        context.registerReceiver(receiver, intentFilter);
        ObservableObject.getInstance().addObserver(observer);

    }

    /** Method used to disconnect
     * the broadcast receiver from the activity
     * */
    public static void disconnectReceiver (Context context, BroadcastReceiver receiver, Observer observer) {
        Log.d(TAG, "disconnectReceiver: called!");

        context.unregisterReceiver(receiver);
        ObservableObject.getInstance().deleteObserver(observer);

    }

    /** Method to create a Snackbar
     * displaying that there is no internet
     * */
    public static Snackbar createSnackbar (Context context, View mainLayout, String message) {

        final Snackbar snackbar = Snackbar.make(
                mainLayout,
                message,
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(
                context.getResources().getString(R.string.snackbarNoInternetButton),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: snackbar clicked!");
                        snackbar.dismiss();
                    }
                });

        View snackbarView = snackbar.getView();
        //snackbarView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(context.getResources().getColor(android.R.color.white));
        Button snackbarButton = (Button) snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        snackbarButton.setTextColor(context.getResources().getColor(android.R.color.white));
        snackbar.show();

        return snackbar;

    }


    // TODO: 22/07/2018 Delete!
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
    // TODO: 22/07/2018 Delete!

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

    public static boolean updateSharedPreferences(SharedPreferences sharedPref, String key, boolean value) {
        sharedPref.edit().putBoolean(key,value).apply();
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

                // TODO: 10/07/2018 Take care!! What if it's boolean!?
                updateSharedPreferences(sharedPref, entry.getKey(), "");

            }
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
                      UtilsGeneral.hideKeyboard(activity);
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

    /** Method that displays the main content
     * and hides de progress bar that occupies
     * all the screen
     * */
    public static void showMainContent (LinearLayout progressBarContent, LinearLayout mainContent) {
        Log.d(TAG, "showMainContent: called!");

        progressBarContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);

    }

    /** Method that hides the main content
     * and displays de progress bar that occupies
     * all the screen
     * */
    public static void hideMainContent (LinearLayout progressBarContent, LinearLayout mainContent) {
        Log.d(TAG, "hideMainContent: called!");

        progressBarContent.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

    }



    /** Method to print sharedPreferences
     * */
    public static void printSharedPreferences (SharedPreferences sharedPreferences) {
        Log.d(TAG, "printSharedPreferences: called!");

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.i(TAG, "map values -> " + entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    /** Method to check if an imageView has a drawable as image.
     * Note that if you set an image via ImageView.setImageBitmap(BITMAP) it internally creates
     * a new BitmapDrawable even if you pass null.
     * In that case the check imageViewOne.getDrawable() == null is false anytime.
     * To get to know if an image is set you can do the following
     * */
    public static boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }

    /** Method that converts dp unit to equivalent pixels,
     * depending on device density. */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

}