package com.example.android.goforlunch.activities.rest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class PersInfoActivity extends AppCompatActivity{

    // TODO: 29/05/2018 Eliminate that the FirstName view is focused from the beginning
    // TODO: 29/05/2018 Eliminate the group option here
    // TODO: 29/05/2018 Check AutocompleteTextView. Things are missing
    // TODO: 06/06/2018 Fill the textInputs with first name and last name
    // TODO: 06/06/2018 Allow to modify the profile picture

    private static final String TAG = PersInfoActivity.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @BindView(R.id.pers_enter_image_id)
    ImageView iv_userImage;

    @BindView(R.id.pers_enter_first_name_id)
    TextInputEditText inputFirstName;

    @BindView(R.id.pers_enter_last_name_id)
    TextInputEditText inputLastName;

    @BindView(R.id.pers_enter_email_id)
    TextInputEditText inputEmail;

    @BindView(R.id.pers_enter_password_id)
    TextInputEditText inputPassword;

    @BindView(R.id.pers_enter_group_id)
    TextInputEditText inputGroup;

    @BindView(R.id.pers_enter_save_changes_button_id)
    Button buttonSaveChanges;

    @BindView(R.id.pers_enter_change_password_id)
    Button buttonChangePassword;

    @BindView(R.id.pers_enter_progressbar)
    ProgressBar progressBar;

    private List<String> listOfGroups;
    private String[] arrayOfGroups;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    private Uri userProfilePictureUri;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefGroups;
    private DatabaseReference dbRefUsers;

    private AppDatabase localDatabase;

    private SharedPreferences sharedPref;

    //Glide
    private RequestManager glide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pers_info);

        ButterKnife.bind(this);

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        localDatabase = AppDatabase.getInstance(PersInfoActivity.this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(PersInfoActivity.this);

        glide = Glide.with(PersInfoActivity.this);

        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked! " + view.toString());

                if (inputFirstName.getText().toString().trim().length() == 0) {
                    ToastHelper.toastShort(PersInfoActivity.this, "Please insert your first name");

                } else if (inputLastName.getText().toString().trim().length() == 0) {
                    ToastHelper.toastShort(PersInfoActivity.this, "Please, insert your last name");

                } else {

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(
                                    Utils.capitalize(inputFirstName.getText().toString().trim())
                                            + " "
                                            + Utils.capitalize(inputLastName.getText().toString().trim()))
                            .setPhotoUri(userProfilePictureUri)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");

                                        //We get the exception and display why it was not successful
                                        FirebaseAuthException e = (FirebaseAuthException) task.getException();

                                        if (e != null) {
                                            Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                        }

                                        ToastHelper.toastShort(PersInfoActivity.this, "Something went wrong. Please, try again");

                                    } else {
                                        Log.d(TAG, "onComplete: task was successful");

                                        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);

                                        Map<String, Object> map = new HashMap<>();
                                        map.put(RepoStrings.FirebaseReference.USER_FIRST_NAME, inputFirstName.getText().toString().trim());
                                        map.put(RepoStrings.FirebaseReference.USER_LAST_NAME, inputLastName.getText().toString().trim());
                                        UtilsFirebase.updateInfoWithMapInFirebase(dbRefUsers, map);

                                        ToastHelper.toastShort(PersInfoActivity.this, "Your information has been updated");

                                        startActivity(new Intent(PersInfoActivity.this, MainActivity.class));
                                        finish();

                                    }
                                }
                            });
                }
            }
        });

        iv_userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: iv_userImage clicked!");

                if (checkPermissionREAD_EXTERNAL_STORAGE(PersInfoActivity.this)) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,0);

                }
            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked! " + view.toString());
                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.notImplemented));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.d(TAG, "onNext: " + aBoolean);

                if(aBoolean) {

                    /** We get the user information
                     * */
                    currentUser = auth.getCurrentUser();
                    Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

                    if (currentUser != null) {

                        userEmail = currentUser.getEmail();
                        userProfilePictureUri = currentUser.getPhotoUrl();
                        Log.d(TAG, "onCreate: userProfilePictureUri = " + userProfilePictureUri);

                        if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                            dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                            dbRefUsers.addListenerForSingleValueEvent(valueEventListenerGetInfoAndFillWidgets);
                        }
                    }

                } else {

                    ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.noInternetLoggingOut));

                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: " + Log.getStackTraceString(e));

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");
        dbRefUsers.removeEventListener(valueEventListenerGetInfoAndFillWidgets);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called!");
        super.onDestroy();
    }

    /** Value Event ListenerL gets all user's info from Firebase and fills all the widgets
     *  */
    private ValueEventListener valueEventListenerGetInfoAndFillWidgets = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            userFirstName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_FIRST_NAME).getValue().toString();
            userLastName = dataSnapshot.child(RepoStrings.FirebaseReference.USER_LAST_NAME).getValue().toString();
            userEmail = dataSnapshot.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue().toString();
            userGroup = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString();
            userGroupKey = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP_KEY).getValue().toString();

            /** We fill the widgets with the user's info
             * */
            inputFirstName.setText(userFirstName);
            inputLastName.setText(userLastName);
            inputEmail.setText(userEmail);
            inputGroup.setText(userGroup);
            inputPassword.setText("******");

            glide.load(userProfilePictureUri).into(iv_userImage);

            //iv_userImage.setImageURI(userProfilePictureUri);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: called!");

        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called!");

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                glide.load(selectedImage).into(iv_userImage);

                /** We store the Uri value. We will use it if the user saves changes
                 * */
                userProfilePictureUri = imageUri;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ToastHelper.toastShort(PersInfoActivity.this, "Something went wrong");
            }

        } else {
            ToastHelper.toastShort(PersInfoActivity.this, "You have not picked an image");
        }

    }

    /** Method that checks if we have permission to read external storage
     * **/
    public boolean checkPermissionREAD_EXTERNAL_STORAGE (
            final Context context) {
        Log.d(TAG, "checkPermissionREAD_EXTERNAL_STORAGE: called!");

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    /** Method used to display a dialog
     * */
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        Log.d(TAG, "showDialog: called!");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called!");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    ToastHelper.toastShort(PersInfoActivity.this, "GET_ACCOUNTS Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
