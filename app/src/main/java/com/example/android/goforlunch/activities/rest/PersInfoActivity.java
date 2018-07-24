package com.example.android.goforlunch.activities.rest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.broadcastreceivers.InternetConnectionReceiver;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class PersInfoActivity extends AppCompatActivity implements Observer {

    private static final String TAG = PersInfoActivity.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    //Widgets
    @BindView(R.id.pers_enter_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.pers_enter_image_id)
    ImageView ivUserImage;

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

    @BindView(R.id.pers_enter_tv_change_password_id)
    TextView tvChangePassword;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    @BindView(R.id.pers_main_content)
    LinearLayout mainContent;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    private Uri userProfilePictureUri;
    private InputStream inputStreamSelectedImage;

    //Firebase Database
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    //Firebase Storage
    private FirebaseStorage fireStorage;
    private StorageReference stRefMain;
    private StorageReference stRefImageDir;
    private StorageReference stRefUser;

    //Shared Preferences
    private SharedPreferences sharedPref;

    //Glide
    private RequestManager glide;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        fireStorage = FirebaseStorage.getInstance();
        stRefMain = fireStorage.getReference();
        stRefImageDir = stRefMain.child(RepoStrings.Directories.IMAGE_DIR);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(PersInfoActivity.this);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        glide = Glide.with(PersInfoActivity.this);

        /* We set the content view
        * */
        setContentView(R.layout.activity_pers_info);
        ButterKnife.bind(this);

        /* We set the listeners
        * */
        fab.setOnClickListener(fabOnClickListener);
        buttonSaveChanges.setOnClickListener(buttonSaveChangesOnClickListener);
        ivUserImage.setOnClickListener(ivOnClickListener);
        tvChangePassword.setOnClickListener(tvChangePasswordOnClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(RepoStrings.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(PersInfoActivity.this, receiver, intentFilter, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    PersInfoActivity.this,
                    receiver,
                    PersInfoActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    PersInfoActivity.this,
                    receiver,
                    PersInfoActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

        fab.setOnClickListener(null);
        buttonSaveChanges.setOnClickListener(null);
        tvChangePassword.setOnClickListener(null);
        ivUserImage.setOnClickListener(null);

    }

    @Override
    public void update(Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet not Available");

            internetAvailable = false;

            if (snackbar == null) {
                snackbar = Utils.createSnackbar(
                        PersInfoActivity.this,
                        mainContent,
                        "Internet not available");

                ToastHelper.toastNoInternetFeaturesNotWorking(PersInfoActivity.this);
                Utils.showMainContent(progressBarContent, mainContent);

            } else {
                snackbar.show();
                ToastHelper.toastNoInternetFeaturesNotWorking(PersInfoActivity.this);
                Utils.showMainContent(progressBarContent, mainContent);
            }

        } else {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            if (snackbar != null) {
                snackbar.dismiss();
            }

            /* We get the user information
            when internet comes back
            */
            Utils.hideMainContent(progressBarContent, mainContent);

            currentUser = auth.getCurrentUser();
            Log.d(TAG, "onDataChange... auth.getCurrentUser() = " + (auth.getCurrentUser() != null));

            if (currentUser != null) {

                userEmail = currentUser.getEmail();

                /* We get a reference
                to user's firebase storage
                 * */
                if (userEmail != null) {
                    stRefUser = stRefImageDir.child(userEmail);
                } else {
                    stRefUser = null;
                }

                Log.d(TAG, "onNext: userProfilePictureUri = " + userProfilePictureUri);
                if (userProfilePictureUri == null) {
                    userProfilePictureUri = currentUser.getPhotoUrl();
                    Log.d(TAG, "onCreate: userProfilePictureUri = " + userProfilePictureUri);
                }

                if (userEmail != null && !userEmail.equalsIgnoreCase("")) {

                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                    dbRefUsers.addListenerForSingleValueEvent(valueEventListenerGetInfoAndFillWidgets);
                }
            }
        }

    }

    /***************
     * LISTENERS ***
     **************/

    private View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: fab clicked!");

            NavUtils.navigateUpFromSameTask(PersInfoActivity.this);
        }
    };

    private View.OnClickListener buttonSaveChangesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Clicked! " + view.toString());

            if (inputFirstName.getText().toString().trim().length() == 0) {
                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.commonToastEnterFirstName));

            } else if (inputLastName.getText().toString().trim().length() == 0) {
                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.commonToastEnterLastName));

            } else {
                alertDialogChangeData();
            }
        }
    };


    private View.OnClickListener tvChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Clicked! " + view.toString());
            ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.notImplemented));
        }
    };


    private View.OnClickListener ivOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: ivUserImage clicked!");

            if (checkPermissionREAD_EXTERNAL_STORAGE(PersInfoActivity.this)) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,0);

            }
        }
    };

    /** Value Event Listener: gets all user's info from Firebase and fills all the widgets
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

            /* We fill the widgets with the user's info
             * */
            inputFirstName.setText(userFirstName);
            inputLastName.setText(userLastName);
            inputEmail.setText(userEmail);
            inputGroup.setText(userGroup);
            inputPassword.setText("******");

            if (userProfilePictureUri == null) {
                glide.load(getResources().getDrawable(R.drawable.picture_not_available)).into(ivUserImage);
            } else {
                glide.load(userProfilePictureUri).into(ivUserImage);
            }

            Utils.showMainContent (progressBarContent, mainContent);

            /* We remove the listener (probably not needed because it's a SingleValueEvent Listener)
            * */
            dbRefUsers.removeEventListener(this);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: called!");

        }
    };

    /******************
     * METHODS *******
     * ***************/

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
                    showRequestPermissionsDialog("External storage", context,
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

    /******************
     * CALLBACKS *******
     * ***************/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called!");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.getAccountsDenied));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called!");

        if (resultCode == RESULT_OK) {
            try {
                Log.d(TAG, "onActivityResult: data.getData() = " + data.getData());

                /* We get the image data and update inputStreamSelectedImage variable which will be
                * used later if the user decides to save this image in his/her profile
                * */
                final Uri imageUri = data.getData();
                inputStreamSelectedImage = getContentResolver().openInputStream(imageUri);

                final Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStreamSelectedImage);

                glide.load(selectedBitmap).into(ivUserImage);
                Log.d(TAG, "onActivityResult: image loaded!");

                /** We store the Uri value. We will use it if the user saves changes
                 * */
                userProfilePictureUri = imageUri;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.somethingWentWrong));
            }

        } else {
            ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.commonYouNotPickedImage));
        }

    }

    /******************
     * DIALOGS *******
     * ***************/

    /** Method that creates an alert dialog that
     * can be used save the personal info changes in the data
     * */
    private void alertDialogChangeData() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(PersInfoActivity.this);
        builder.setMessage(getResources().getString(R.string.persInfoDialogAreYouChangeData))
                .setTitle(getResources().getString(R.string.persInfoDialogChangingData))
                .setPositiveButton(getResources().getString(R.string.persInfoDialogChangeDataYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: yes button clicked!");

                        /* If the snackbar is not visible, it means internet is available
                        * */
                        if (internetAvailable) {

                            Utils.hideMainContent(progressBarContent, mainContent);

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

                                                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.somethingWentWrong));

                                                Utils.showMainContent(progressBarContent, mainContent);


                                            } else {
                                                Log.d(TAG, "onComplete: task was successful");

                                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);

                                                /* Updating the database
                                                 * */
                                                Map<String, Object> map = new HashMap<>();
                                                map.put(RepoStrings.FirebaseReference.USER_FIRST_NAME, inputFirstName.getText().toString().trim());
                                                map.put(RepoStrings.FirebaseReference.USER_LAST_NAME, inputLastName.getText().toString().trim());
                                                UtilsFirebase.updateInfoWithMapInFirebase(dbRefUsers, map);

                                                /* We save the image in the image view in the storage
                                                 * */
                                                startStorageProcessWithByteArray(ivUserImage);

                                            }
                                        }
                                    });
                        } else {
                            /* Snackbar is shown, therefore
                            there is no internet connection*/

                            ToastHelper.toastNoInternet(PersInfoActivity.this);

                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.persInfoDialogChangeDataNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: no button clicked!");

                        ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.persInfoNotUpdated));
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Method used to display
     * a dialog for permissions request
     * */
    public void showRequestPermissionsDialog(final String msg, final Context context,
                                             final String permission) {
        Log.d(TAG, "showRequestPermissionsDialog: called!");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(getResources().getString(R.string.permissionPermissionNecessary));
        alertBuilder.setMessage(msg + " " + getResources().getString(R.string.permissionPermissionIsNecessary));
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

    /** Method for saving images
     * in Firebase Storage
     * */
    private void startStorageProcessWithByteArray (ImageView imageView) {
        Log.d(TAG, "startStorageProcessWithByteArray: called!");

        imageView.setDrawingCacheEnabled(true);
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
        imageView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = stRefUser.child("image").putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: something went wrong!");

                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.persInfoSomethingWrongImage));

                startActivity(new Intent(PersInfoActivity.this, MainActivity.class));
                finish();


            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: file uploaded!");

                /* This toast does not appear in "AuthEnterNameActivity"!
                * */
                ToastHelper.toastShort(PersInfoActivity.this, getResources().getString(R.string.persInfoToastYourInfoUpdated));

                startActivity(new Intent(PersInfoActivity.this, MainActivity.class));
                finish();

            }
        });
    }
}
