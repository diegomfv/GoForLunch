package com.example.android.goforlunch.repostrings;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public interface RepoStrings {

    // TODO: 25/05/2018 Delete this
    String FAKE_USER_EMAIL = "todd_brown@gmail.com";

    String NOT_AVAILABLE_FOR_STRINGS = "";

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
            "Vietnamese",
            "Other"
    };

    interface SharedPreferences {

        String USER_ID_KEY = "user_key";

        String USER_FIRST_NAME = "first_name";
        String USER_LAST_NAME = "last_name";

        String USER_RESTAURANT_NAME = "restaurant_name";
        String USER_GROUP = "group_name";
        String USER_GROUP_KEY = "user_group_key";
    }

    interface SentIntent {

        String EMAIL = "email";
        String PASSWORD = "password";

        String FLAG = "flag";

        String PLACE_ID = "place_id";
        String RESTAURANT_NAME = "restaurant_name";
        String RESTAURANT_TYPE = "restaurant_type";
        String ADDRESS = "address";
        String RATING = "rating";
        String IMAGE_URL = "image_url";
        String PHONE = "phone";
        String WEBSITE_URL = "website_url";

    }

    interface FirebaseReference {

        String USERS = "users";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String EMAIL = "email";
        String GROUP = "group";
        String PLACE_ID = "place_id";
        String RESTAURANT_NAME = "restaurant_name";
        String RESTAURANT_TYPE = "restaurant_type";
        String ADDRESS = "address";
        String PHONE = "phone";
        String RATING = "rating";
        String IMAGE_URL = "image_url";
        String WEBSITE_URL = "website_url";

        String GROUPS = "groups";
        String GROUP_NAME = "group_name";
        String GROUP_MEMBERS = "group_members";
        String GROUP_RESTAURANTS_VISITED = "group_restaurants_visited";

        String USER_ID = "user_id";

    }





}

