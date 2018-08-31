package com.example.android.goforlunch.network.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.constants.Repo;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.network.models.distancematrix.DistanceMatrix;
import com.example.android.goforlunch.network.models.placebyid.PlaceById;
import com.example.android.goforlunch.network.models.placebynearby.LatLngForRetrofit;
import com.example.android.goforlunch.network.models.placebynearby.PlacesByNearby;
import com.example.android.goforlunch.network.models.placebynearby.Result;
import com.example.android.goforlunch.network.models.placetextsearch.PlacesByTextSearch;
import com.example.android.goforlunch.network.remote.AllGoogleServices;
import com.example.android.goforlunch.network.remote.GoogleService;
import com.example.android.goforlunch.network.remote.GoogleServiceStreams;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.Utils;
import com.example.android.goforlunch.utils.UtilsRemote;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.goforlunch.constants.Repo.Keys.NEARBY_KEY;

/**
 * Created by Diego Fajardo on 31/08/2018.
 */
public class FetchingIntentService extends IntentService {

    private static final String TAG = FetchingIntentService.class.getSimpleName();

    private ArrayList<RestaurantEntry> listOfRestaurantsEntries;
    private ArrayList<String> listOfPlacesIdsOfRestaurants;

    private String[] arrayOfTypes;

    private LatLngForRetrofit myPosition;
    private Disposable disposable;

    private Handler mHandler;

    private AppDatabase localDatabase;

    private Storage internalStorage;
    private String mainPath;
    private String imageDirPath;

    private boolean accessInternalStorageGranted;

    //Counter to count how many objects we create and notify the service
    //when the fetching process has finished. This is necessary to avoid
    //the UI to get blocked. The counter will increment
    //when we add an object to the database and will decrement when
    //a thread reaches the end of the service code. When the counter < 20
    //(a number close to 0 to avoid any issues)
    //the broadcast to update the UI will be called.
    private AtomicInteger counter;

    public FetchingIntentService() {
        super("FetchingIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: called!");

        //We send a Broadcast that will update FragmentRestaurantMapView
        //Progress bar will be shown and the ViewModel will be temporarily blocked
        //2 as an extra means "process has started" (see "updateMapFragmentAccordingToProcessState" method)
        hideMap();

        localDatabase = AppDatabase.getInstance(getApplicationContext());

        /* We delete the database (could be done without app executors because we are in a
        * background thread */
        clearDatabase();

        configuringInternalStorage();

        listOfRestaurantsEntries = new ArrayList<>();
        listOfPlacesIdsOfRestaurants = new ArrayList<>();

        /* We pass the NON TRANSLATABLE ARRAY!
         * */
        arrayOfTypes = Repo.RESTAURANT_TYPES;

        /* We will use this to show a Toast in Map Fragment
         * if there is no internet
         * */
        mHandler = new Handler();

        counter = new AtomicInteger(0);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: called!");

        double latitude;
        double longitude;

        if (intent != null) {
            latitude = intent.getDoubleExtra(Repo.SentIntent.LATITUDE, 0.0d);
            longitude = intent.getDoubleExtra(Repo.SentIntent.LONGITUDE, 0.0d);
            accessInternalStorageGranted = intent.getBooleanExtra(Repo.SentIntent.ACCESS_INTERNAL_STORAGE_GRANTED, false);

        } else {
            latitude = 0.0d;
            longitude = 0.0d;

        }

        Log.i(TAG, "onStartCommand: latitude = " + latitude);
        Log.i(TAG, "onStartCommand: longitude = " + longitude);
        Log.d(TAG, "onStartCommand: accessInternalStorageGranted = " + accessInternalStorageGranted);

        if (latitude == 0.0d || longitude == 0.0d) {
            Log.d(TAG, "onStartCommand: latitude or longitude are 0");

            //position is incorrect, do nothing
            ToastHelper.toastShort(getApplicationContext(), getResources().getString(R.string.mainCurrentPositionNotAvailable));
            showMap();

        } else if (!accessInternalStorageGranted){
            Log.d(TAG, "onStartCommand: internalStorageAccess not granted");

            //storage access not granted, do nothing
            ToastHelper.toastShort(getApplicationContext(), getResources().getString(R.string.storageNotGranted));
            showMap();

        } else {

            myPosition = new LatLngForRetrofit(latitude,longitude);

            Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    Log.d(TAG, "onNext: " + aBoolean);

                    if (!aBoolean) {
                        Log.i(TAG, "onNext: no internet");
                        //show "no internet message" and do nothing
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: handler running!");
                                ToastHelper.toastShort(FetchingIntentService.this, getResources().getString(R.string.noInternet));
                                showMap();
                            }
                        });

                    } else {

                        /* We start the fetching process
                         * */
                        startNearbyPlacesProcess();

                    }

                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "onError: " + e.getMessage());
                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete: ");

                }
            });

        }
    }

    /** This method starts the process for fetching restaurants using
     * nearby places service
     * */
    private void startNearbyPlacesProcess () {
        Log.d(TAG, "startNearbyPlacesProcess: called!");

        /*1. We start fetching nearby places. */
        disposable =
                GoogleServiceStreams.streamFetchPlacesNearby(
                        myPosition,
                        "distance",
                        "restaurant",
                        NEARBY_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<PlacesByNearby>() {
                            @Override
                            public void onNext(PlacesByNearby placesByNearby) {
                                Log.d(TAG, "onNext: called!");

                                List<Result> listOfResults = placesByNearby.getResults();

                                if (listOfResults.size() == 0) {
                                    Log.i(TAG, "onNext: listOfResults.size() = " + listOfResults.size());

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "run: handler running!");
                                            ToastHelper.toastShort(FetchingIntentService.this, getResources().getString(R.string.serviceProblemServer));
                                            showMap();
                                        }
                                    });

                                } else {
                                    Log.i(TAG, "onNext: listOfResults.size() = " + listOfResults.size());

                                    for (int i = 0; i < listOfResults.size(); i++) {

                                        listOfPlacesIdsOfRestaurants.add(Utils.checkToAvoidNull(listOfResults.get(i).getPlaceId()));

                                        listOfRestaurantsEntries.add(
                                                new RestaurantEntry(
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getPlaceId()),
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getName()),
                                                        13,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getVicinity()),
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getRating()),
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLat().toString()),
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLng().toString()))
                                        );
                                        counter.getAndIncrement();
                                        Log.i(TAG, "onNext: counter = " + counter.get());

                                    }

                                    /* Fetching nearby places has ended,
                                    we start Text Search Process
                                    */
                                    Log.i(TAG, "onNext: NEARBY PLACES PROCESS ENDED!");
                                    Log.i(TAG, "onNext: NEARBY PLACES PROCESS ENDED! Restaurants.size() = " + listOfRestaurantsEntries.size());

                                    for (int i = 1; i < arrayOfTypes.length - 1; i++) {
                                        //-1 because we don't want to fetch "type OTHER" restaurants
                                        startTextSearchProcess(i);

                                    }
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: called!");

                            }
                        });

    }

    /** This method starts the process for fetching restaurants using
     * text search service
     * */
    private void startTextSearchProcess(final int type) {
        Log.d(TAG, "startTextSearchProcess: called!");

        disposable =
                GoogleServiceStreams.streamFetchPlacesTextSearch(
                        arrayOfTypes[type] + "+" + "Restaurant",
                        myPosition,
                        20,
                        Repo.Keys.TEXTSEARCH_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<PlacesByTextSearch>() {
                            @Override
                            public void onNext(PlacesByTextSearch placesByTextSearch) {
                                Log.d(TAG, "onNext: ");

                                List<com.example.android.goforlunch.network.models.placetextsearch.Result> listOfResults = placesByTextSearch.getResults();

                                for (int i = 0; i < listOfResults.size(); i++) {

                                    /* If the place is already in the lists and the type is equal to 13,
                                    we only update the type */

                                    if (listOfPlacesIdsOfRestaurants.contains(listOfResults.get(i).getPlaceId())) {

                                        for (int j = 0; j < listOfRestaurantsEntries.size(); j++) {

                                            if (listOfRestaurantsEntries.get(j).getPlaceId().equalsIgnoreCase(listOfResults.get(i).getPlaceId())) {
                                                if (listOfRestaurantsEntries.get(j).getType() == 13) {
                                                    listOfRestaurantsEntries.get(j).setType(type);
                                                }
                                            }
                                            break;

                                        }

                                    } else {

                                        /* If the place is not already in the lists,
                                        we add the restaurant to them */

                                        listOfPlacesIdsOfRestaurants.add(listOfResults.get(i).getPlaceId());

                                        listOfRestaurantsEntries.add(
                                                new RestaurantEntry(
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getPlaceId()),
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getName()),
                                                        type,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getFormattedAddress()),
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getRating()),
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Repo.NOT_AVAILABLE_FOR_STRINGS,
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLat().toString()),
                                                        Utils.checkToAvoidNull(listOfResults.get(i).getGeometry().getLocation().getLng().toString()))
                                        );
                                        counter.getAndIncrement();
                                        Log.i(TAG, "onNext: counter = " + counter.get());
                                    }
                                }

                                if (type == 12) {
                                    /* if type is 12, then is type = Vietnamese which is the last one (before Other)
                                    This guarantees all restaurants are already in the map. We can proceed with getting placeId information
                                    to update Distance and Photos */

                                    Log.i(TAG, "onNext: TEXT SEARCH PROCESS ENDED!");

                                    startPlaceIdProcess();
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());

                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: ");

                            }
                        });

    }

    /** This methods starts several loops for doing 3 types of calls:
     * place Id call,
     * distance matrix call,
     * photo call
     * */
    private void startPlaceIdProcess () {
        Log.d(TAG, "startPlaceIdProcess: called!");

        /* We iterate thorough the list to start Place Id requests
         * */
        Log.i(TAG, "startPlaceIdProcess: PLACE ID PROCESS STARTED");

        for (int i = 0; i < listOfRestaurantsEntries.size(); i++) {
            updateMapWithPlaceIdInfo(listOfRestaurantsEntries.get(i));
        }

        Log.i(TAG, "startPlaceIdProcess: PLACE ID PROCESS ENDED");
        Log.i(TAG, "startPlaceIdProcess: DISTANCE MATRIX PROCESS STARTED");

        /* We have reached the end of the loop, so we can start with
         * Distance Matrix requests */

        for (int i = 0; i < listOfRestaurantsEntries.size(); i++) {
            updateMapWithDistanceMatrix(listOfRestaurantsEntries.get(i));
        }

        Log.i(TAG, "startPlaceIdProcess: DISTANCE MATRIX PROCESS ENDED!");
        Log.i(TAG, "startPlaceIdProcess: PHOTO PROCESS STARTED");

        /* We have reached the end of the loop, so we can start with
         * Photo requests */

        for (int i = 0; i < listOfRestaurantsEntries.size(); i++) {

            if (listOfRestaurantsEntries.get(i).getImageUrl() != null) {
                if (!listOfRestaurantsEntries.get(i).getImageUrl().equalsIgnoreCase("")) {

                    /* If we have a valid photo reference, we start fetching photo process and
                     * we will insert the restaurant in the database at the end of this process
                     * */
                    updateMapAndInternalStorageWithPhotos(listOfRestaurantsEntries.get(i));
                }
            } else {

                /* If we do not have a valid photo reference, we insert the restaurant
                 * directly in the database
                 * */
                insertRestaurantEntryInDatabase(listOfRestaurantsEntries.get(i));
            }
        }

        Log.i(TAG, "startPlaceIdProcess: PHOTO PROCESS PROCESS ENDED!");


    }

    /** This methods updates the map of
     * restaurant entries with Place Id information
     * */
    private void updateMapWithPlaceIdInfo(final RestaurantEntry restaurantEntry) {
        Log.d(TAG, "updateMapWithPlaceIdInfo: called!");

        disposable = GoogleServiceStreams.streamFetchPlaceById(
                restaurantEntry.getPlaceId(),
                Repo.Keys.PLACEID_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<PlaceById>() {
                    @Override
                    public void onNext(PlaceById placeById) {
                        Log.d(TAG, "onNext: ");

                        com.example.android.goforlunch.network.models.placebyid.Result result =
                                placeById.getResult();

                        if (result != null) {

                            String closingTime = UtilsRemote.checkClosingTime(result);

                            restaurantEntry.setOpenUntil(closingTime);
                            restaurantEntry.setPhone(Utils.checkToAvoidNull(result.getInternationalPhoneNumber()));
                            restaurantEntry.setWebsiteUrl(Utils.checkToAvoidNull(result.getWebsite()));

                            if (result.getPhotos() != null) {

                                for (int i = 0; i < result.getPhotos().size(); i++) {
                                    if (result.getPhotos().get(i) != null) {
                                        if (result.getPhotos().get(i).getPhotoReference() != null
                                                && !result.getPhotos().get(i).getPhotoReference().equalsIgnoreCase("")) {
                                            restaurantEntry.setImageUrl(result.getPhotos().get(i).getPhotoReference());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");

                    }

                });
    }

    /** This methods updates the map of
     * restaurant entries with DistanceMatrix information
     * */
    private void updateMapWithDistanceMatrix (final RestaurantEntry restaurantEntry) {
        Log.d(TAG, "updateMapWithDistanceMatrix: called!");

        disposable =
                GoogleServiceStreams.streamFetchDistanceMatrix(
                        "imperial",
                        "place_id:" + restaurantEntry.getPlaceId(),
                        myPosition,
                        Repo.Keys.MATRIX_DISTANCE_KEY)
                        .subscribeWith(new DisposableObserver<DistanceMatrix>() {
                            @Override
                            public void onNext(DistanceMatrix distanceMatrix) {
                                Log.d(TAG, "onNext: ");

                                if (distanceMatrix != null) {
                                    if (distanceMatrix.getRows() != null) {
                                        if (distanceMatrix.getRows().get(0) != null) {
                                            if (distanceMatrix.getRows().get(0).getElements() != null) {
                                                if (distanceMatrix.getRows().get(0).getElements().get(0) != null) {
                                                    if (distanceMatrix.getRows().get(0).getElements().get(0).getDistance() != null) {
                                                        if (distanceMatrix.getRows().get(0).getElements().get(0).getDistance().getText() != null) {
                                                            restaurantEntry.setDistance(distanceMatrix.getRows().get(0).getElements().get(0).getDistance().getText());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage() );



                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: ");

                            }
                        });

    }

    private void updateMapAndInternalStorageWithPhotos(final RestaurantEntry restaurantEntry) {
        Log.d(TAG, "updateMapAndInternalStorageWithPhotos: called!");

        GoogleService googleService = AllGoogleServices.getGooglePlacePhotoService();
        Call<ResponseBody> callPhoto = googleService.fetchDataPhoto(
                "400",
                restaurantEntry.getImageUrl(),
                Repo.Keys.PHOTO_KEY);
        callPhoto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(final Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: PHOTO: url = " + call.request().url().toString());
                Log.d(TAG, "onResponse: PHOTO: response = " + response.toString());

                /* We save the image url in the database
                 * */
                Log.d(TAG, "onResponse: PHOTO: saving url in database");
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: saving url...");

                        restaurantEntry.setImageUrl(call.request().url().toString());
                        insertRestaurantEntryInDatabase(restaurantEntry);

                    }
                });

                Log.d(TAG, "onResponse: PHOTO: accessInternalStorage");

                /* We store the image in the internal storage if the access is granted
                 * */
                Log.d(TAG, "onResponse: PHOTO saving image in storage");

                if (response.body() != null) {
                    Log.d(TAG, "onResponse: PHOTO response.body() IS NOT NULL");

                    Bitmap bm = BitmapFactory.decodeStream(response.body().byteStream());

                    if (restaurantEntry.getPlaceId() != null && bm != null) {
                        saveImageInInternalStorage(restaurantEntry.getPlaceId(), bm);
                    }

                } else {
                    Log.d(TAG, "onResponse: response.body() is null");
                }

            }

            @Override
            public void onFailure(final Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: url = " + call.request().url().toString());

                /* We save the image url in the database
                 * */
                Log.d(TAG, "onFailure: PHOTO: saving url in database");
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: saving url...");

                        restaurantEntry.setImageUrl(call.request().url().toString());
                        insertRestaurantEntryInDatabase(restaurantEntry);

                    }
                });

            }
        });

        counter.getAndDecrement();
        Log.i(TAG, "updateMapAndInternalStorageWithPhotos: counter = " + counter.get());
        if (counter.get() < 20) {
            //We send a Broadcast that will update FragmentRestaurantMapView
            //Progress bar will be hidden and the ViewModel will be activated again
            //3 as an extra means "process has finished"
            showMap();
        }
    }

    /** This method saves the fetched image in the internal storage
     * */
    private void saveImageInInternalStorage (String placeId, final Bitmap bitmap) {
        Log.d(TAG, "saveImageInInternalStorage: called!");

        if (accessInternalStorageGranted) {

            if (internalStorage.isDirectoryExists(imageDirPath)) {

                final String filePath = imageDirPath + placeId;

                if (bitmap != null) {

                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Saving file...");
                            boolean isFileCreated = internalStorage.createFile(filePath, bitmap);
                            Log.d(TAG, "run: file saved = " + isFileCreated);

                        }
                    });
                }
            }

        } else {
            Log.i(TAG, "saveImageInInternalStorage: accessInternalStorageGrantes = false");
        }
    }


    /** This method destroys and recreates the internal directory where all the images are stored.
     * The objective of this process is to delete all old images to free memory space
     * */
    private void configuringInternalStorage () {
        Log.d(TAG, "configuringInternalStorage: called!");

        internalStorage = new Storage(getApplicationContext());
        mainPath = internalStorage.getInternalFilesDirectory() + File.separator;
        imageDirPath = mainPath + File.separator + Repo.Directories.IMAGE_DIR + File.separator;

        /* We delete the directory to delete all the information
         * */
        boolean isDeleted = internalStorage.deleteDirectory(imageDirPath);
        Log.i(TAG, "configuringInternalStorage: isDeleted = " + isDeleted);

        /* We create it again
         * */
        String newDirectory = imageDirPath;
        boolean isCreated = internalStorage.createDirectory(newDirectory);
        Log.d(TAG, "onClick: isCreated = " + isCreated);

    }

    /** This method inserts a restaurant in the database
     * */
    private void insertRestaurantEntryInDatabase (RestaurantEntry restaurantEntry) {
        Log.d(TAG, "insertRestaurantEntryInDatabase: called!");

        localDatabase.restaurantDao().insertRestaurant(restaurantEntry);

    }

    private void updateMapFragmentAccordingToProcessState (int value) {
        Log.d(TAG, "updateMapFragmentAccordingToProcessState: called!");

        Intent intent = new Intent();
        intent.setAction(Repo.SentIntent.LOAD_DATA_IN_VIEWMODEL);
        intent.putExtra(Repo.SentIntent.FETCHING_PROCESS_STAGE, value);
        sendBroadcast(intent);
    }

    private void showMap () {
        Log.d(TAG, "showMap: called!");
        updateMapFragmentAccordingToProcessState(3);
    }

    private void hideMap() {
        Log.d(TAG, "hideMap: called!");
        updateMapFragmentAccordingToProcessState(2);
    }


    private void clearDatabase () {
        Log.d(TAG, "clearDatabase: called!");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: called!");
                localDatabase.clearAllTables();
            }
        });
    }


}
