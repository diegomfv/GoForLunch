package com.example.android.goforlunch.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

/**
 * Created by Diego Fajardo on 19/05/2018.
 * Room database point of entry
 * version -> should be updated when we updateItem our database
 * exportSchema -> writes the database info to a folder
 */

/** Room database point of entry
 * */

/** version -> should be updated when we updateItem our database
 * exportSchema -> writes the database info to a folder */

/** Room cannot automatically map complex extractors like DATE. In this cases,
 * you'll need a "type converter" (See Android Architecture Components,
 * Android Development Course, UDACITY)
 * */

@Database(entities = {RestaurantEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "goforlunch";
    private static AppDatabase sInstance;

    /**
     * We use the Singleton pattern.
     * Returns the instantiation of the class to
     * only one object.
     */
    public static AppDatabase getInstance(Context context) {

        if (sInstance == null) {

            synchronized (LOCK) {

                Log.d(TAG, "getInstance: Creating new Database Instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        //.allowMainThreadQueries() //allows queries in the main thread, test purposes
                        .build();
            }
        }

        Log.d(TAG, "getInstance: Getting the Database Instance");
        return sInstance;
    }

    /** Abstract method that returns the DAO
     * */
    public abstract RestaurantDao restaurantDao();

}
