package com.example.android.goforlunch.strings;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public interface StringValues {

    // TODO: 25/05/2018 Delete this
    String FAKE_USER_EMAIL = "Dana_Overcash@gmail.com";

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

    interface SentIntent {

        String EMAIL = "email";
        String PASSWORD = "password";

        String PLACE_ID = "placeId";
        String RESTAURANT_TYPE = "restaurant_type";

    }

    interface FirebaseReference {

        String USERS = "users/";
        String FIRSTNAME = "firstName";
        String LASTNAME = "lastName";
        String EMAIL = "email";
        String GROUP = "group";
        String PLACE_ID = "placeId";
        String RESTAURANT = "restaurant";
        String RESTAURANT_TYPE = "restaurantType";
        String PHONE = "phone";
        String RATING = "rating";
        String IMAGE_URL = "image_url";

        String GROUPS = "groups/";
        String NAME = "name";
        String MEMBERS = "members";

        String USER_ID = "userid";

    }





}

