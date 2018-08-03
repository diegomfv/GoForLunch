package com.example.android.goforlunch.activities.rest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.network.models.pojo.User;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.adapters.RVAdapterRestaurant;
import com.example.android.goforlunch.constants.Repo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RestaurantActivity extends AppCompatActivity implements Observer {

    private static final String TAG = RestaurantActivity.class.getSimpleName();

    private Context context;

    //Widgets
    @BindView(R.id.restaurant_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.restaurant_selector_id)
    BottomNavigationView navigationView;

    @BindView(R.id.restaurant_image_id)
    ImageView ivRestPicture;

    @BindView(R.id.restaurant_title_id)
    TextView tvRestName;

    @BindView(R.id.restaurant_address_id)
    TextView tvRestAddress;

    @BindView(R.id.restaurant_rating_id)
    RatingBar rbRestRating;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    @BindView(R.id.main_layout_id)
    CoordinatorLayout mainContent;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;
    private String userRestaurant;
    private String intentRestaurantName;

    private boolean fabShowsCheck;
    private String phoneString;
    private String webUrlString;
    private String likeString;

    private List<User> listOfCoworkers;

    //RecyclerView
    @BindView(R.id.restaurant_recycler_view_id)
    RecyclerView recyclerView;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase Database
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefUsersGetList;

    private SharedPreferences sharedPref;

    //Glide
    private RequestManager glide;

    //Internal Storage
    private Storage storage;
    private String mainPath;
    private String imageDirPath;
    private boolean accessToInternalStorageGranted = false;

    // Disposable
    private Disposable getImageFromInternalStorageDisposable;

    //Intent
    private Intent intent;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = RestaurantActivity.this;

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        userKey = sharedPref.getString(Repo.SharedPreferences.USER_ID_KEY, "");

        glide = Glide.with(context);

        ////////////////////////////////////////////
        setContentView(R.layout.activity_restaurant);
        ButterKnife.bind(this);

        this.configureRecyclerView();
        this.configureInternalStorage(context);
        this.configureStrings();

        listOfCoworkers = new ArrayList<>();

        /* Listeners
        * */
        fab.setOnClickListener(mFabListener);
        navigationView.setOnNavigationItemSelectedListener(bottomViewListener);

        /* We get the intent to display the information
         * */
        intent = getIntent();
        intentRestaurantName = intent.getStringExtra(Repo.SentIntent.RESTAURANT_NAME);
        fillUIUsingIntent(intent);

        Anim.showCrossFadeShortAnimation(recyclerView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        connectBroadcastReceiver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

        disconnectBroadcastReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        disconnectBroadcastReceiver();

        fab.setOnClickListener(null);
        navigationView.setOnNavigationItemSelectedListener(null);
        dbRefUsersGetList.removeEventListener(valueEventListenerGetListOfCoworkers);

        this.disposeWhenDestroy();
    }

    private void disposeWhenDestroy () {
        UtilsGeneral.dispose(this.getImageFromInternalStorageDisposable);

    }

    /** Callback: listening to broadcast receiver
     * */
    @Override
    public void update(java.util.Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet Not Available");

            internetAvailable = false;

            if (snackbar == null) {
                snackbar = UtilsGeneral.createSnackbar(
                        RestaurantActivity.this,
                        mainContent,
                        getResources().getString(R.string.noInternet));

            } else {
                snackbar.show();
            }

        } else {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            if (snackbar != null) {
                snackbar.dismiss();
            }

            /* We get the user information
             * */
            auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
            Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

            if (currentUser != null) {

                userEmail = currentUser.getEmail();

                if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                    dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS).child(userKey);
                    dbRefUsers.addListenerForSingleValueEvent(valueEventListenerGetUserInfo);
                }

                /* We get the list of coworkers that will go to this Restaurant.
                 * They will be displayed in the recyclerView
                 * */
                dbRefUsersGetList = fireDb.getReference(Repo.FirebaseReference.USERS);
                dbRefUsersGetList.addValueEventListener(valueEventListenerGetListOfCoworkers);
            }
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    /*****************
     * LISTENERS *****
     * **************/

    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!internetAvailable) {
                ToastHelper.toastNoInternet(RestaurantActivity.this);

            } else {

                if (fabShowsCheck) {
                    /* If we click the fab when it shows check it has to display "add".
                     * Moreover, we modify the info in the database
                     * */
                    fabShowsCheck = false;
                    Log.d(TAG, "onClick: fabShowsCheck = " + fabShowsCheck);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));

                        /* We delete the restaurant from the database (user's)
                         **/
                        dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey + "/" + Repo.FirebaseReference.USER_RESTAURANT_INFO);
                        UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(dbRefUsers);

                        ToastHelper.toastShort(context, getResources().getString(R.string.restaurantNotGoing));
                    }

                } else {

                    /* If we click the fab when it shows "add" it has to display "check".
                     * Moreover, we modify the info in the database
                     * */
                    fabShowsCheck = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));

                        /* We add the restaurant to the database (user's)
                         * */
                        dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey + "/" + Repo.FirebaseReference.USER_RESTAURANT_INFO);
                        UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.ADDRESS)),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.IMAGE_URL)),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.PHONE)),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.PLACE_ID)),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.RATING)),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.RESTAURANT_NAME)),
                                getIntent().getIntExtra(Repo.SentIntent.RESTAURANT_TYPE, 0),
                                UtilsGeneral.checkIfIsNull(getIntent().getStringExtra(Repo.SentIntent.WEBSITE_URL))
                        );

                        ToastHelper.toastShort(context, getResources().getString(R.string.restaurantGoing) + " " + intent.getStringExtra(Repo.SentIntent.RESTAURANT_NAME) + "!");
                    }
                }
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener bottomViewListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.restaurant_view_call_id: {
                            Log.d(TAG, "onNavigationItemSelected: callButton CLICKED!");
                            Log.d(TAG, "onNavigationItemSelected: phone = " + phoneString);

                            if (phoneString.equals("")) {
                                ToastHelper.toastShort(context, getResources().getString(R.string.restaurantPhoneNotAvailable));

                            } else {
                                ToastHelper.toastShort(context, getResources().getString(R.string.restaurantCallingTo) + " " + phoneString);
                            }

                        } break;

                        case R.id.restaurant_view_like_id: {
                            Log.d(TAG, "onNavigationItemSelected: likeButton CLICKED!");

                            ToastHelper.toastShort(context, likeString);


                        } break;

                        case R.id.restaurant_view_website_id: {
                            Log.d(TAG, "onNavigationItemSelected: websiteButton CLICKED!");
                            Log.d(TAG, "onNavigationItemSelected: web URL = " + webUrlString);

                            if (webUrlString.equals("")) {
                                ToastHelper.toastShort(context, getResources().getString(R.string.restaurantWebsiteNotAvailable));

                            } else {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrlString));
                                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(browserIntent);

                                } else {
                                    ToastHelper.toastShort(context, getResources().getString(R.string.restaurantWebsiteNotAvailable));
                                }

                            }

                        } break;

                    }

                    return false;
                }
            };

    private ValueEventListener valueEventListenerGetUserInfo = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            userFirstName = dataSnapshot.child(Repo.FirebaseReference.USER_FIRST_NAME).getValue().toString();
            userLastName = dataSnapshot.child(Repo.FirebaseReference.USER_LAST_NAME).getValue().toString();
            userEmail = dataSnapshot.child(Repo.FirebaseReference.USER_EMAIL).getValue().toString();
            userGroup = dataSnapshot.child(Repo.FirebaseReference.USER_GROUP).getValue().toString();
            userGroupKey = dataSnapshot.child(Repo.FirebaseReference.USER_GROUP_KEY).getValue().toString();
            userRestaurant = dataSnapshot.child(Repo.FirebaseReference.USER_RESTAURANT_INFO)
                    .child(Repo.FirebaseReference.RESTAURANT_NAME).getValue().toString();

            setFabButtonState(intentRestaurantName);

            dbRefUsers.removeEventListener(this);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());
        }
    };

    private ValueEventListener valueEventListenerGetListOfCoworkers = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            listOfCoworkers = UtilsFirebase.fillListWithCoworkersOfSameGroupAndSameRestaurantExceptIfItsTheUser(dataSnapshot, userEmail, userGroup, intentRestaurantName);

            /* We use the list in the adapter
             * */
            mAdapter = new RVAdapterRestaurant(context, listOfCoworkers);
            recyclerView.setAdapter(mAdapter);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());

        }
    };

    /*******************************
     * CONFIGURATION ***************
     ******************************/

    /** Method that sets the directory variables and creates the directory that will
     * store images if needed
     * */
    private void configureInternalStorage (Context context) {
        Log.d(TAG, "configureInternalStorage: ");

        //If we don't have storage permissions, we don't continue
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ToastHelper.toastShort(context, getResources().getString(R.string.storageNotGranted));
            return;

        } else {
            Log.d(TAG, "configureInternalStorage: access to Internal storage granted");
            accessToInternalStorageGranted = true;
            storage = new Storage(context);
            mainPath = storage.getInternalFilesDirectory() + File.separator;
            imageDirPath = mainPath + Repo.Directories.IMAGE_DIR + File.separator;

            Log.d(TAG, "configureInternalStorage: mainPath = " + mainPath);
            Log.d(TAG, "configureInternalStorage: imageDirPath = " + imageDirPath);

            //ToastHelper.toastShort(context, getResources().getString(R.string.storageGranted));

        }
    }

    /** Method to configure strings
     * */
    private void configureStrings() {
        Log.d(TAG, "configureStrings: called!");

        /* We set this to avoid null exceptions.
         * The intent will fill this information
         * */
        phoneString = "Not available";
        webUrlString = "Not available";
        likeString = "Liked!";

    }

    /** Method to configure the recycler view
     * */
    private void configureRecyclerView () {

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);

    }

    /** Method that connects a broadcastReceiver to the activity.
     * It allows to notify the user about the internet state
     * */
    private void connectBroadcastReceiver () {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Repo.CONNECTIVITY_CHANGE_STATUS);
        UtilsGeneral.connectReceiver(RestaurantActivity.this, receiver, intentFilter, this);

    }

    /** Method that disconnects the broadcastReceiver from the activity.
     * */
    private void disconnectBroadcastReceiver () {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null) {
            UtilsGeneral.disconnectReceiver(
                    RestaurantActivity.this,
                    receiver,
                    RestaurantActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    /******************************************************
     * RX JAVA
     *****************************************************/

    /** Method used to set the Fab button state
     * */
    private boolean setFabButtonState (String intentRestaurantName) {

        if (userRestaurant.equals(intentRestaurantName)) {
            fabShowsCheck = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));
            }

        } else {
            fabShowsCheck = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));
            }
        }

        return true;

    }

    /** Method used to fill the UI using the intent
     * */
    private boolean fillUIUsingIntent(Intent intent) {
        Log.d(TAG, "fillUIUsingIntent: called!");

        if (intent.getStringExtra(Repo.SentIntent.RESTAURANT_NAME) == null
                || intent.getStringExtra(Repo.SentIntent.RESTAURANT_NAME).equals("")) {

            tvRestName.setText(getResources().getString(R.string.restaurantNameNotAvailable));

        } else {

            StringBuilder displayedName = new StringBuilder();
            String tokens[] = intent.getStringExtra(Repo.SentIntent.RESTAURANT_NAME).split(" ");

            for (int i = 0; i < tokens.length; i++) {
                if (displayedName.length() < 27) {

                    /* 1 is the space between words
                     * */
                    if ((displayedName.length() + tokens[i].length()) + 1 < 27) {
                        displayedName.append(" ").append(tokens[i]);

                    } else {
                        break;
                    }
                }
            }

            String transformedName = displayedName.toString().trim();

            tvRestName.setText(transformedName);
        }

        if (intent.getStringExtra(Repo.SentIntent.ADDRESS) == null
                || intent.getStringExtra(Repo.SentIntent.ADDRESS).equals("")) {

            tvRestAddress.setText(getResources().getString(R.string.restaurantAddressNotAvailable));

        } else {

            tvRestAddress.setText(intent.getStringExtra(Repo.SentIntent.ADDRESS));
        }

        if (intent.getStringExtra(Repo.SentIntent.RATING) == null
                || intent.getStringExtra(Repo.SentIntent.RATING).equals("")) {

            rbRestRating.setRating(0f);

        } else {

            float rating = Float.parseFloat(intent.getStringExtra(Repo.SentIntent.RATING));
            rbRestRating.setRating(rating);
        }

        phoneString = intent.getStringExtra(Repo.SentIntent.PHONE);
        webUrlString = intent.getStringExtra(Repo.SentIntent.WEBSITE_URL);

        /* We adapt the web url to be able to be launched with the intent
        * */
        transformWebUrlString();

        if (accessToInternalStorageGranted) {
            loadImage(intent);

        } else {
            loadImageWithUrl(intent);

        }

        return true;
    }

    /** Adds http:// to the beginning of the web url if it
     * did not start like that
     * */
    private void transformWebUrlString() {
        Log.d(TAG, "transformWebUrlString: called!");

        if (!webUrlString.startsWith("http://")) {
            webUrlString = "http://" + webUrlString;
        }
    }

    /** Method that tries to load an image using the storage.
     * If there is no file, it tries to load
     * the image with the url
     * */
    private void loadImage (Intent intent) {
        Log.d(TAG, "loadImage: called!");

        //if file exists in the directory -> load with storage
        if (storage.isFileExist(
                imageDirPath + intent.getStringExtra(Repo.SentIntent.PLACE_ID))) {
            Log.d(TAG, "loadImage: file does exist in the directory");
            getAndDisplayImageFromInternalStorage(imageDirPath + intent.getStringExtra((Repo.SentIntent.PLACE_ID)));

            showMainContent(progressBarContent, mainContent);

        } else {
            Log.d(TAG, "loadImage: file does not exist in the directory");
            loadImageWithUrl(intent);

        }
    }

    /** Method that tries to load an image with a url.
     * If it is null or equal to "", it loads
     * an standard picture
     * */
    private void loadImageWithUrl (Intent intent) {
        Log.d(TAG, "loadImageWithUrl: called!");

        Log.i(TAG, "loadImageWithUrl: " + intent.getStringExtra(Repo.SentIntent.IMAGE_URL));

        if (intent.getStringExtra(Repo.SentIntent.IMAGE_URL) == null
                || intent.getStringExtra(Repo.SentIntent.IMAGE_URL).equals("")) {
            Log.d(TAG, "loadImageWithUrl: image is null");

            glide.load(R.drawable.lunch_image).into(ivRestPicture);

            showMainContent(progressBarContent, mainContent);

        } else {
            Log.d(TAG, "loadImageWithUrl: image is not null or empty");

            glide.load(intent.getStringExtra(Repo.SentIntent.IMAGE_URL)).into(ivRestPicture);

            showMainContent(progressBarContent, mainContent);

        }
    }

    /** Used to read an image from the internal storage and convert it to bitmap so that
     * it the image can be stored in a RestaurantEntry and be displayed later using glide
     * in the recyclerView
     * */
    private Observable<byte[]> getObservableImageFromInternalStorage (String filePath) {
        return Observable.just(storage.readFile(filePath));
    }

    /** Loads an image using glide. The observable emits the image in a background thread
     * and the image is loaded using glide in the main thread
     * */
    public void getAndDisplayImageFromInternalStorage(String filePath) {
        Log.d(TAG, "loadImageFromInternalStorage: called!");

        getImageFromInternalStorageDisposable = getObservableImageFromInternalStorage(filePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<byte[]>() {
                    @Override
                    public void onNext(byte[] bytes) {
                        Log.d(TAG, "onNext: ");

                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                        glide.load(bm).into(ivRestPicture);

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

    /** Method for showing the main content. We cannot use UtilsGeneral because
     * the mainContent Layout is not a Linear Layout
     * */
    private void showMainContent (LinearLayout progressBarContent, CoordinatorLayout mainContent) {
        Log.d(TAG, "showMainContent: called!");

        progressBarContent.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

}