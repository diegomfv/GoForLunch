package com.example.android.goforlunch.constants;

import android.Manifest;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public interface Repo {

    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    int REQUEST_CODE_ALL_PERMISSIONS = 105;

    String NOT_AVAILABLE_FOR_STRINGS = "";

    String CONNECTIVITY_CHANGE_STATUS = "android.net.conn.CONNECTIVITY_CHANGE";

    /** This array stores the different types of restaurants available for filtering
     * */
    String[] RESTAURANT_TYPES = {
            "All", //0
            "American", //1
            "Chinese", //2
            "English", //3
            "French", //4
            "German", //5
            "Indian", //6
            "Italian", //7
            "Japanese",//8
            "Mexican", //9
            "Spanish", //10
            "Thai", //11
            "Vietnamese", //12
            "Other" //13
    };

    String FLAG_SPECIFY_FRAGMENT = "flagToSpecifyFragment";

    /* Used to communicate between the fetching service and map fragment
    * */
    String INTENT_ACTION_UPDATE_MAP = "com.example.android.goforlunch.INTENT_ACTION_UPLOAD";

    interface Keys {

        String KEY_GOOGLE_MAPS_PLATFORM = "AIzaSyDq9-hsHk6dNzZDILqfB9rGuv54xuGX7vM";

        String NEARBY_KEY = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
        String MATRIX_DISTANCE_KEY = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
        String PHOTO_KEY = "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU";
        String PLACEID_KEY = "AIzaSyDuv5PtP5uwugkDW189v9_ycrp8A0nlwkU";
        String TEXTSEARCH_KEY = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";

    }

    interface SharedPreferences {

        String USER_ID_KEY = "user_key";

        String USER_FIRST_NAME = "first_name";
        String USER_LAST_NAME = "last_name";

        String USER_RESTAURANT_NAME = "restaurant_name";
        String USER_GROUP = "group_name";
        String USER_GROUP_KEY = "user_group_key";

        String ADD_RESTAURANT_JOB_KEY = "job_dd_restaurant";
        String NOTIFICATIONS_KEY = "job_notifications";
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

        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String ACCESS_INTERNAL_STORAGE_GRANTED = "access_internal_storage";

    }

    interface FirebaseReference {

        String USERS = "users";
        String USER_FIRST_NAME = "user_first_name";
        String USER_LAST_NAME = "user_last_name";
        String USER_EMAIL = "user_email";
        String USER_GROUP = "user_group";
        String USER_GROUP_KEY = "user_group_key";
        String USER_NOTIFICATIONS = "user_notifications";
        String USER_RESTAURANT_INFO = "user_restaurant_info";

        String RESTAURANT_PLACE_ID = "place_id";
        String RESTAURANT_NAME = "restaurant_name";
        String RESTAURANT_TYPE = "restaurant_type";
        String RESTAURANT_ADDRESS = "address";
        String RESTAURANT_PHONE = "phone";
        String RESTAURANT_RATING = "rating";
        String RESTAURANT_IMAGE_URL = "image_url";
        String RESTAURANT_WEBSITE_URL = "website_url";

        String GROUPS = "groups";
        String GROUP_NAME = "group_name";
        String GROUP_RESTAURANTS_VISITED = "group_restaurants_visited";

    }

    interface Directories {

        String IMAGE_DIR = "imageDir";

    }

    interface RequestsCodes {

        int REQ_CODE_LOCATION_PERMISSION_ = 1234;
        int REQ_CODE_WRITE_EXTERNAL_PERMISSION = 1000;

    }

}

