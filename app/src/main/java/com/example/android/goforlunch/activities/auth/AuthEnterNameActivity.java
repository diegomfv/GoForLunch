package com.example.android.goforlunch.activities.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterNameActivity extends AppCompatActivity{

    private static final String TAG = AuthEnterNameActivity.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    //Widgets
    @BindView(R.id.enter_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.enter_first_name_id)
    TextInputEditText inputFirstName;

    @BindView(R.id.enter_last_name_id)
    TextInputEditText inputLastName;

    @BindView(R.id.enter_email_id)
    TextInputEditText inputEmail;

    @BindView(R.id.enter_password_id)
    TextInputEditText inputPassword;

    @BindView(R.id.enter_image_id)
    ImageView iv_userImage;

    @BindView(R.id.enter_start_button_id)
    Button buttonStart;

    @BindView(R.id.enter_progressbar_id)
    ProgressBar progressBar;

    private String email;
    private String password;

    private Uri userProfilePictureUri;

    //List of Emails to store all the emails and check if an user already exists in the database
    private List<String> listOfEmails;

    //true, we came from Google or Facebook
    //false, we came from password sign up or sign in
    private boolean flag = false;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefNewUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name);

        ButterKnife.bind(this);

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        /** Configuring textInputEditTexts: hide keyboard
         * */
        Utils.configureTextInputEditTextWithHideKeyboard(AuthEnterNameActivity.this, inputFirstName);
        Utils.configureTextInputEditTextWithHideKeyboard(AuthEnterNameActivity.this, inputLastName);
        Utils.configureTextInputEditTextWithHideKeyboard(AuthEnterNameActivity.this, inputEmail);
        Utils.configureTextInputEditTextWithHideKeyboard(AuthEnterNameActivity.this, inputPassword);

        /** If we have sign in with google or facebook first time, the user needs to fill some
         * information. This code will fill the email and password fields for the user automatically.
         * This fields cannot be modified
         * */
        if (user != null) {

            if (user.getDisplayName() == null) {
                email = user.getEmail();
                password = "*******";
            }
        }

        /** If we come from SignUp Activity (email and password login) then the intent won't be null.
         * Otherwise, it will.
         * We fill the email and password info for the user automatically.
         * Both fields cannot be changed.
         * */
        final Intent intent = getIntent();
        if (intent != null) {

            if (intent.getStringExtra(RepoStrings.SentIntent.EMAIL) != null
                    && intent.getStringExtra(RepoStrings.SentIntent.PASSWORD) != null) {

                email = intent.getStringExtra(RepoStrings.SentIntent.EMAIL);
                password = intent.getStringExtra(RepoStrings.SentIntent.PASSWORD);

            }

            flag = intent.getBooleanExtra(RepoStrings.SentIntent.FLAG, false);
        }

        Log.d(TAG, "onCreate: flag = " + flag);

        inputEmail.setText(email);
        inputPassword.setText(password);

        dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        dbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                listOfEmails = new ArrayList<>();

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    listOfEmails.add(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString().toLowerCase().trim());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: fab clicked!");
                NavUtils.navigateUpFromSameTask(AuthEnterNameActivity.this);
            }
        });

        iv_userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: imageView clicked!");

                if (checkPermissionREAD_EXTERNAL_STORAGE(AuthEnterNameActivity.this)) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,0);

                }
            }
        });

        /** When the user
         * clicks the START button two things can happen.
         * If the user is filling the info using a google or facebook account...  todo
         * If the user is filling the info using an email and password account... todo
         * */
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!flag) {
                    Log.d(TAG, "onClick: flag = false ->" + flag);

                    /** flag is false, therefore we came from Google of Facebook SignIn and
                     * we only have to update the user info
                     * */
                    if (user != null) {
                        Log.d(TAG, "onClick: We came from Google or Facebook login");

                        if (checkMinimumRequisites()) {

                            /* We show the progress bar
                             * and block the interaction with buttons
                             * */
                            Utils.showProgressBarAndDisableUserInteraction(AuthEnterNameActivity.this, progressBar);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(
                                            Utils.capitalize(inputFirstName.getText().toString().trim())
                                            + " "
                                            + Utils.capitalize(inputLastName.getText().toString().trim()))
                                    .setPhotoUri(userProfilePictureUri)
                                    .build();

                            user.updateProfile(profileUpdates)
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

                                                ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.somethingWentWrong));

                                                /* We hide the progress bar
                                                 * and enable buttonStart
                                                 * */
                                                Utils.hideProgressBarAndEnableUserInteraction(AuthEnterNameActivity.this, progressBar);


                                            } else {
                                                Log.d(TAG, "onComplete: task was successful");

                                                dbRefNewUser = fireDb.getReference(RepoStrings.FirebaseReference.USERS);

                                                String userKey = dbRefNewUser.push().getKey();

                                                dbRefNewUser = fireDb.getReference(RepoStrings.FirebaseReference.USERS
                                                        + "/" + userKey);
                                                UtilsFirebase.updateUserInfoInFirebase(dbRefNewUser,
                                                        Utils.capitalize(inputFirstName.getText().toString().trim()),
                                                        Utils.capitalize(inputLastName.getText().toString().trim()),
                                                        inputEmail.getText().toString().toLowerCase().trim(),
                                                        "",
                                                        "",
                                                        "",
                                                        "");

                                                dbRefNewUser = fireDb.getReference(
                                                        RepoStrings.FirebaseReference.USERS
                                                                + "/" + userKey
                                                                + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                                UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "");

                                                startActivity(new Intent(AuthEnterNameActivity.this, MainActivity.class));
                                                finish();

                                            }
                                        }
                                    });
                        }
                    }

                } else {
                    Log.d(TAG, "onClick: flag is true -> " + flag);
                    /** flag is true, therefore we came from email/password login. In this
                     * case we have to check if the user exists.
                     * If it does, we doesn't allow the user to continue.
                     * If it doesn't we have to
                     * create the user and update the info.
                     * */

                    if (userAlreadyExists(listOfEmails, inputEmail.getText().toString().toLowerCase().trim())) {
                        /** A user with this email already exists, so we don't let the user to continue with the process
                         * (we cannot create two users with the same email) */
                        ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.enterNameThisEmailAlreadyExists));

                    } else {
                        /** A user with this email DOES NOT exist, so we can continue with the process
                         * */

                        if (checkMinimumRequisites()) {

                            /* We show the progress bar
                             * and block the interaction with buttons
                             * */
                            Utils.showProgressBarAndDisableUserInteraction(AuthEnterNameActivity.this, progressBar);

                            //We create the user
                            auth.createUserWithEmailAndPassword(inputEmail.getText().toString().toLowerCase().trim(), inputPassword.getText().toString().trim())
                                    .addOnCompleteListener(AuthEnterNameActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);

                                            // If sign in fails, display a message to the user. If sign in succeeds
                                            // the auth state listener will be notified and logic to handle the
                                            // signed in user can be handled in the listener.
                                            if (!task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");

                                                //We get the exception and display why it was not successful
                                                FirebaseAuthException e = (FirebaseAuthException) task.getException();
                                                if (e != null) {
                                                    Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                                }

                                                ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.somethingWentWrong));

                                                 /* We hide the progress bar
                                                 * and enable buttonStart
                                                 * */
                                                Utils.hideProgressBarAndEnableUserInteraction(AuthEnterNameActivity.this, progressBar);

                                            } else {

                                                user = auth.getCurrentUser();

                                                if (user != null) {

                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(
                                                                    Utils.capitalize(inputFirstName.getText().toString().trim())
                                                                    + " "
                                                                    + Utils.capitalize(inputLastName.getText().toString().trim()))
                                                            .setPhotoUri(userProfilePictureUri)
                                                            .build();

                                                    user.updateProfile(profileUpdates)
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

                                                                        ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.somethingWentWrong));

                                                                    } else {
                                                                        Log.d(TAG, "onComplete: task was successful");

                                                                        dbRefNewUser = fireDb.getReference(RepoStrings.FirebaseReference.USERS);

                                                                        String userKey = dbRefNewUser.push().getKey();

                                                                        dbRefNewUser = fireDb.getReference(RepoStrings.FirebaseReference.USERS
                                                                                + "/" + userKey);
                                                                        UtilsFirebase.updateUserInfoInFirebase(dbRefNewUser,
                                                                                Utils.capitalize(inputFirstName.getText().toString().trim()),
                                                                                Utils.capitalize(inputLastName.getText().toString().trim()),
                                                                                inputEmail.getText().toString().toLowerCase().trim(),
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "");

                                                                        dbRefNewUser = fireDb.getReference(
                                                                                RepoStrings.FirebaseReference.USERS
                                                                                        + "/" + userKey
                                                                                        + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                                                        UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefNewUser,
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "",
                                                                                "");

                                                                        startActivity(new Intent(AuthEnterNameActivity.this, MainActivity.class));
                                                                        finish();

                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    });
                        }
                    }

                }
            }
        });

    }

    /** This method is used to check that the minimum requisites for creating an user
     * are fulfilled
     * */
    public boolean checkMinimumRequisites () {

        if (inputFirstName.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonToastEnterFirstName));
            return false;

        } else if (inputLastName.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonToastEnterLastName));
            return false;

        } else if (inputEmail.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonToastEnterEmail));
            return false;

        } else if (inputPassword.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonToastEnterPassword));
            return false;

        } else if (inputPassword.getText().toString().length() < 6) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonToastPasswordTooShort));
            return false;

        } else {
            return true;
        }
    }

    /** Checks if a value is in a list. It is used to
     * check if the user email is already in the database.
     * */
    public boolean userAlreadyExists(List<String> listOfEmails, String inputString) {

        if (listOfEmails.contains(inputString)) {
            return true;

        } else {
            return false;

        }
    }

    /** Method that checks if we have permission to read external storage
     * **/
    public boolean checkPermissionREAD_EXTERNAL_STORAGE (
            final Context context) {
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
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(getResources().getString(R.string.permissionPermissionNecessary));
        alertBuilder.setMessage(msg + getResources().getString(R.string.permissionPermissionIsNecessary));
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

    /******************
    * CALLBACKS *******
     * ***************/

    /** External Storage permission
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.getAccountsDenied));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    /** Getting the image
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                iv_userImage.setImageBitmap(selectedImage);

                /** We store the Uri value. We will use it if the user saves changes
                 * */
                userProfilePictureUri = imageUri;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.somethingWentWrong));
            }

        } else {
            ToastHelper.toastShort(AuthEnterNameActivity.this, getResources().getString(R.string.commonYouNotPickedImage));
        }

    }

}
