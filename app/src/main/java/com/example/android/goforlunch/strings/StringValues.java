package com.example.android.goforlunch.strings;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public interface StringValues {

    String NOT_AVAILABLE = "NotAvailable";

    /** This string is used in for requests (TextSearch)
     * */
    String ADD_RESTAURANT_STRING = "+Restaurant";

    /** This array stores the different types of restaurants available for filtering
     * */
    String[] RESTAURANT_TYPES = {
            "All",
            "American",
            "Chinese",
            "English",
            "French",
            "German",
            "Indian",
            "Italian",
            "Japanese",
            "Mexican",
            "Spanish",
            "Thai",
            "Vietnamese"
    };

    interface User {

        String FIRSTNAME = "firstname";
        String LASTNAME = "lastname";
        String EMAIL = "email";
        String GROUP = "group";
        String RESTAURANT = "restaurant";
        String RESTAURANT_TYPE = "restaurantType";
    }

    interface SentIntent {

        String PLACE_ID = "placeId";
        String RESTAURANT_TYPE = "restaurant_type";

    }

    interface FirebaseReferences {

        String USERS = "users";

    }





}

