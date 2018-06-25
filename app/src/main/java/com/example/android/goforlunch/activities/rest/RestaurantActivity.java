package com.example.android.goforlunch.activities.rest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.recyclerviewadapter.RVAdapterRestaurant;
import com.example.android.goforlunch.repository.RepoStrings;
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
import java.util.Map;

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

public class RestaurantActivity extends AppCompatActivity {

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
    private String phoneToastString = "No phone available";
    private String webUrlToastString = "No web available";
    private String likeToastString = "Liked!";

    private List<String> listOfCoworkers;

    //RecyclerView
    @BindView(R.id.restaurant_recycler_view_id)
    RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase Database
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        ButterKnife.bind(this);

        context = RestaurantActivity.this;

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        glide = Glide.with(context);

        this.configureInternalStorage(context);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        /** Instantiation of the fab and set onClick listener*/
        fab.setOnClickListener(mFabListener);

        listOfCoworkers = new ArrayList<>();

        navigationView.setOnNavigationItemSelectedListener(bottomViewListener);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        /** We get the intent to display the information
         * */
        Intent intent = getIntent();
        fillUIUsingIntent(intent);
        intentRestaurantName = intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME);

        /** We get the user information
         * */
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

        if (currentUser != null) {

            userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        userFirstName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue().toString();
                        userLastName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue().toString();
                        userEmail = dataSnapshot.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString();
                        userGroup = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString();
                        userGroupKey = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue().toString();
                        userRestaurant = dataSnapshot.child(RepoStrings.FirebaseReference.USER_RESTAURANT_INFO)
                                .child(RepoStrings.FirebaseReference.RESTAURANT_NAME).getValue().toString();

                        setFabButtonState(intentRestaurantName);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getCode());
                    }
                });

            }
        }

        /** Reference to Firebase Database, users.
         * We get the list of coworkers that will go to this Restaurant which
         * will be displayed in the recyclerView
         * */
        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        dbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                listOfCoworkers = UtilsFirebase.fillListWithCoworkersOfSameGroupAndSameRestaurant(dataSnapshot, userGroup, intentRestaurantName);

                /** We use the list in the adapter
                 * */
                mAdapter = new RVAdapterRestaurant(context, listOfCoworkers);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });


        Anim.crossFadeShortAnimation(mRecyclerView);


    }

    /** disposeWhenDestroy() avoids memory leaks
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    private void disposeWhenDestroy () {
        dispose(this.getImageFromInternalStorageDisposable);

    }

    /*****************
     * LISTENERS *****
     * **************/

    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Map<String,Object> map;

            if (fabShowsCheck) {
                /** If we click the fab when it shows check it has to display "add".
                 * Moreover, we modify the info in the database
                 * */
                fabShowsCheck = false;
                Log.d(TAG, "onClick: fabShowsCheck = " + fabShowsCheck);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, getApplicationContext().getTheme()));

                    /** We delete the restaurant from the database (user's)
                     **/
                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                    UtilsFirebase.deleteRestaurantInfoOfUserInFirebase(dbRefUsers);

                    ToastHelper.toastShort(context, "Not Going to the restaurant!");
                }

            } else {

                /** If we click the fab when it shows "add" it has to display "check".
                 * Moreover, we modify the info in the database
                 * */
                fabShowsCheck = true;
                Log.d(TAG, "onClick: fabShowsCheck = " + fabShowsCheck);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getApplicationContext().getTheme()));

                    /** We add the restaurant to the database (user's)
                     * */
                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                    UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.ADDRESS)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.IMAGE_URL)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.PHONE)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.PLACE_ID)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RATING)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.RESTAURANT_TYPE)),
                            checkIfIsNull(getIntent().getStringExtra(RepoStrings.SentIntent.WEBSITE_URL))
                    );
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
                            Log.d(TAG, "onNavigationItemSelected: phone = " + phoneToastString);
                            if (phoneToastString.equals("")) {
                                ToastHelper.toastShort(context, "Phone is not available");
                            } else {
                                ToastHelper.toastShort(context, "Calling to " + phoneToastString);
                            }

                        } break;

                        case R.id.restaurant_view_like_id: {
                            Log.d(TAG, "onNavigationItemSelected: likeButton CLICKED!");
                            ToastHelper.toastShort(context, likeToastString);

                            // TODO: 25/06/2018 Delete!
                            List<File> files = getListFiles(new File(mainPath));
                            Log.d(TAG, "onNavigationItemSelected: " + files.toString());

                        } break;

                        case R.id.restaurant_view_website_id: {
                            Log.d(TAG, "onNavigationItemSelected: websiteButton CLICKED!");
                            Log.d(TAG, "onNavigationItemSelected: web URL = " + webUrlToastString);
                            if (webUrlToastString.equals("")) {
                                ToastHelper.toastShort(context, "Website is not available");
                            } else {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrlToastString));
                                startActivity(browserIntent);

                            }

                        } break;

                    }

                    return false; // TODO: 19/05/2018 Check true or false
                }
            };


    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    /** We use this method to check if the strings that come from the intent are null or not
     * */
    private String checkIfIsNull (String string) {

        if (string == null) {
            return "";
        } else {
            return string;
        }
    }

    /******************************************************
     * RX JAVA
     *****************************************************/

    /** Method used to avoid memory leaks
     * */
    private void dispose (Disposable disposable) {
        if (disposable != null
                && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }


    /** Method used to set the Fab button state
     * */
    private boolean setFabButtonState (String intentRestaurantName) {

        // TODO: 28/05/2018 See another way of doing things for lower versions
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

    /** Method that sets the directory variables and creates the directory that will
     * store images if needed
     * */
    private void configureInternalStorage (Context context) {
        Log.d(TAG, "configureInternalStorage: ");

        //If we don't have storage permissions, we don't continue
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ToastHelper.toastShort(context, "You don't have access to Internal Storage");
            return;

        } else {
            Log.d(TAG, "configureInternalStorage: access to Internal storage granted");
            accessToInternalStorageGranted = true;
            storage = new Storage(context);
            mainPath = storage.getInternalFilesDirectory() + File.separator;
            imageDirPath = mainPath + File.separator + RepoStrings.Directories.IMAGE_DIR;

            ToastHelper.toastShort(context, "Access to internal storage granted");

        }
    }



    /** Method used to fill the UI using the intent
     * */
    private boolean fillUIUsingIntent(Intent intent) {

        if (intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME) == null
                || intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME).equals("")) {

            tvRestName.setText("Restaurant name not available");

        } else {

            StringBuilder displayedName = new StringBuilder();
            String tokens[] = intent.getStringExtra(RepoStrings.SentIntent.RESTAURANT_NAME).split(" ");

            for (int i = 0; i < tokens.length; i++) {
                if (displayedName.length() < 27) {

                    /** 1 is the space between words
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

        if (intent.getStringExtra(RepoStrings.SentIntent.ADDRESS) == null
                || intent.getStringExtra(RepoStrings.SentIntent.ADDRESS).equals("")) {

            tvRestAddress.setText("Address not available");

        } else {

            tvRestAddress.setText(intent.getStringExtra(RepoStrings.SentIntent.ADDRESS));
        }

        if (intent.getStringExtra(RepoStrings.SentIntent.RATING) == null
                || intent.getStringExtra(RepoStrings.SentIntent.RATING).equals("")) {

            rbRestRating.setRating(0f);

        } else {

            float rating = Float.parseFloat(intent.getStringExtra(RepoStrings.SentIntent.RATING));
            rbRestRating.setRating(rating);
        }

        phoneToastString = intent.getStringExtra(RepoStrings.SentIntent.PHONE);
        webUrlToastString = intent.getStringExtra(RepoStrings.SentIntent.WEBSITE_URL);

        if (accessToInternalStorageGranted) {
            loadImage(intent);

        } else {
            loadImageWithUrl(intent);

        }

        return true;
    }

    /** Method that tries to load an image using the storage.
     * If there is no file, it tries to load
     * the image with the url
     * */
    private void loadImage (Intent intent) {
        Log.d(TAG, "loadImage: called!");

        //if file exists in the directory -> load with storage
        if (storage.isFileExist(
                imageDirPath + File.separator + intent.getStringExtra(RepoStrings.SentIntent.PLACE_ID))) {
            Log.d(TAG, "loadImage: file does exist in the directory");
            getAndDisplayImageFromInternalStorage(intent.getStringExtra(RepoStrings.SentIntent.PLACE_ID));

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

        if (intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL) == null
                || intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL).equals("")) {

            glide.load(R.drawable.lunch_image).into(ivRestPicture);

        } else {

            glide.load(intent.getStringExtra(RepoStrings.SentIntent.IMAGE_URL)).into(ivRestPicture);
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

        getImageFromInternalStorageDisposable = getObservableImageFromInternalStorage(imageDirPath + File.separator + filePath)
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

    // TODO: 25/06/2018 Delete!
    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
