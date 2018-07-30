package com.example.android.goforlunch;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.sync.AlertJobCreator;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: called!");
        JobManager.create(this).addJobCreator(new AlertJobCreator());

        final AppDatabase localDatabase = AppDatabase.getInstance(getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: called!");

                localDatabase.restaurantDao().deleteAllRowsInRestaurantTable();

                RestaurantEntry restaurantEntry = new RestaurantEntry(
                        "1",
                        "Koh Thai",
                        11,
                        "7-9 Triangle S, Bristol BS8 1EY",
                        "10pm",
                        "0.3 m.",
                        "4.7",
                        "https://www.gourmetsociety.co.uk/uploads/images/chains/f706687eb66c5141c869a4e3226fd890-image.png",
                        "+44 0117 922 6699",
                        "koh-thai.co.uk",
                        "51.456147",
                        "-2.607457"
                );

                localDatabase.restaurantDao().insertRestaurant(restaurantEntry);

                restaurantEntry = new RestaurantEntry(
                        "2",
                        "Browns",
                        3,
                        "38 Queens Rd, Bristol BS8 1RE",
                        "11pm",
                        "0.2 m.",
                        "4.5",
                        "https://media-cdn.tripadvisor.com/media/photo-s/05/56/53/30/beef-burger-with-cheddar.jpg",
                        "+44 0117 930 4777",
                        "browns-restaurants.co.uk",
                        "51.456386", "-2.605555"
                );

                localDatabase.restaurantDao().insertRestaurant(restaurantEntry);

                restaurantEntry = new RestaurantEntry(
                        "3",
                        "Wagamama",
                        8,
                        "63 Queens Rd, Bristol BS8 1QL",
                        "11pm",
                        "0.4 m.",
                        "4.4",
                        "https://riversidebedford.co.uk/web/wp-content/uploads/bfi_thumb/wagamama-bedford-riverside1-nl89klwwmuxyn9mqpys55rixmqwerehfb5w2jiglw2.jpg",
                        "+44 0117 922 1188",
                        "wagamama.com",
                        "51.456472",
                        "-2.607201"
                );

                localDatabase.restaurantDao().insertRestaurant(restaurantEntry);

                restaurantEntry = new RestaurantEntry(
                        "4",
                        "Yakinori",
                        8,
                        "78 Park St, Bristol BS1 5LA",
                        "10pm",
                        "0.5 m.",
                        "3.9",
                        "http://www.yakinori.co.uk/wp-content/uploads/2014/10/chillinoodle.jpg",
                        "+44 0117 934 9222",
                        "yakinori.co.uk",
                        "51.455428",
                        "-2.603744"
                );

                localDatabase.restaurantDao().insertRestaurant(restaurantEntry);

                restaurantEntry = new RestaurantEntry(
                        "5",
                        "Wahaca",
                        9,
                        "70-78 Queens Rd, Bristol BS8 1QU",
                        "11pm",
                        "0.2 m.",
                        "4.1",
                        "https://visitbristol.co.uk/imageresizer/?image=%2Fdmsimgs%2F5_1690921495.JPG&action=ProductDetail",
                        "+44 0117 332 4486",
                        "wahaca.co.uk",
                        "51.457295",
                        "-2.608004"
                );

                localDatabase.restaurantDao().insertRestaurant(restaurantEntry);
            }
        });
    }
}