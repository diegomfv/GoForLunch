package com.example.android.goforlunch.strings;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public interface RepoStrings {

    // TODO: 25/05/2018 Delete this
    String FAKE_USER_EMAIL = "sophia_collins@gmail.com";

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

    interface SharedPreferences {

        String USER_ID_KEY = "key";

    }

    interface SentIntent {

        String EMAIL = "email";
        String PASSWORD = "password";

        String PLACE_ID = "place_id";
        String RESTAURANT = "restaurant";
        String RESTAURANT_TYPE = "restaurant_type";
        String ADDRESS = "address";
        String RATING = "rating";
        String IMAGE_URL = "image_url";
        String PHONE = "phone";
        String WEBSITE_URL = "website_url";

    }

    interface FirebaseReference {

        String USERS = "users/";
        String FIRSTNAME = "first_name";
        String LASTNAME = "last_name";
        String EMAIL = "email";
        String GROUP = "group";
        String PLACE_ID = "placeId";
        String RESTAURANT = "restaurant";
        String RESTAURANT_TYPE = "restaurantType";
        String ADDRESS = "address";
        String PHONE = "phone";
        String RATING = "rating";
        String IMAGE_URL = "image_url";
        String WEBSITE_URL = "website_url";

        String GROUPS = "groups";
        String GROUPS_NAME = "group_name";
        String MEMBERS = "members";

        String USER_ID = "user_id";

    }





}

