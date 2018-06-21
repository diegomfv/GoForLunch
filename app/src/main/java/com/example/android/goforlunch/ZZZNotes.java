package com.example.android.goforlunch;

/**
 * Created by Diego Fajardo on 01/05/2018.
 */

public class ZZZNotes {

/**
 * FirebaseDatabase mFirebaseDatabase; get reference to whole database
 * DatabaseReference mDatabaseReference; get reference to a specific part
 *
 * mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();;
 * mDatabaseReference =  mFirebaseDatabase.child("messages");
 *
 *
 *
 */
    /**
     *
     * https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=51.459558,-2.599193&radius=10000&type=restaurant&key=AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc
     *
     * 51.456052
     *
     * https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CmRaAAAAExdLn8t_pFqhiZ8IX-zb02rY_Gsnh2A7HRCzH43lQOESJL3-04ffP9jxveMqaStl6OZnfUCeI4Er6vsvuX_xXMdr-9LGQT8tvaDDNxYxcjbXqLB_I2Fn8f5M8M-B4dEwEhAnlcHgFb1sImXp0bPb4huUGhR6aoz0Ojsa1q2N6u32Kih6qiiznw&key=AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU
     *
     * https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJo8eWxtqNcUgRqzOCyl2ECWg&key=AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU
     *
     * https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=place_id:&destinations=&key=AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc
     *
     * */


    /**
     * User visible or foreground updates
     Example: A mapping app that needs frequent, accurate updates with very low latency. All updates happen in the foreground: the user starts an activity, consumes location data, and then stops the activity after a short time.

     Use the setPriority() method with a value of PRIORITY_HIGH_ACCURACY or PRIORITY_BALANCED_POWER_ACCURACY.

     The interval specified in the setInterval() method depends on the use case: for real time scenarios, set the value to few seconds; otherwise, limit to a few minutes (approximately two minutes or greater is recommended to minimize battery usage).
     * */

/**
    // An index to track Ada's memberships
    {
        "users": {
        "alovelace": {
            "name": "Ada Lovelace",
                    // Index Ada's groups in her profile
                    "groups": {
                // the value here doesn't matter, just that the key exists
                "techpioneers": true,
                        "womentechmakers": true
            }
        },
    ...
    },
        "groups": {
        "techpioneers": {
            "name": "Historical Tech Pioneers",
                    "members": {
                "alovelace": true,
                        "ghopper": true,
                        "eclarke": true
            }
        },
    ...
    }
    }

    */
}
