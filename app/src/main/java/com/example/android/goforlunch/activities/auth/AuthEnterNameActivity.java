package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.repostrings.RepoStrings;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterNameActivity extends AppCompatActivity{

    private static final String TAG = "AuthEnterNameAndGroupAc";

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;

    private ImageView imageviewProfilePicture;

    private Button buttonStart;

    private ProgressBar progressBar;

    private String email;
    private String password;

    //List of Emails to store all the emails and check if an user already exists in the database
    private List<String> listOfEmails;

    private boolean flag = false;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name);

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        inputFirstName = (TextInputEditText) findViewById(R.id.enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.enter_last_name_id);
        inputEmail = (TextInputEditText) findViewById(R.id.enter_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.enter_password_id);
        imageviewProfilePicture = (ImageView) findViewById(R.id.enter_image_id);
        buttonStart = (Button) findViewById(R.id.enter_start_button_id);
        progressBar = (ProgressBar) findViewById(R.id.enter_progressbar);

        /** If we have sign in with google or facebook first time, the user needs to fill some
         * information. This code will fill the email and password fields for the user automatically.
         * This fields cannot be modified*/
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

                    listOfEmails.add(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()).toString().toLowerCase().trim());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());
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

                            progressBar.setVisibility(View.VISIBLE);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(inputFirstName.getText().toString() + " " + inputLastName.getText().toString())
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

                                                ToastHelper.toastShort(AuthEnterNameActivity.this, "Something went wrong. Please, sign up again");

                                            } else {
                                                Log.d(TAG, "onComplete: task was successful");

                                                DatabaseReference fireDbRefNewUser =
                                                        fireDb.getReference(RepoStrings.FirebaseReference.USERS);

                                                fireDbRefNewUser.push().setValue(createMapWithUserInfo());

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
                        ToastHelper.toastShort(AuthEnterNameActivity.this, "A user with this email already exists.");

                    } else {
                        /** A user with this email DOES NOT exist, so we can continue with the process
                         * */

                        if (checkMinimumRequisites()) {

                            progressBar.setVisibility(View.VISIBLE);

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

                                                //We get the exception and display why it was not succesful
                                                FirebaseAuthException e = (FirebaseAuthException) task.getException();
                                                if (e != null) {
                                                    Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                                }

                                                // TODO: 01/06/2018 Need to delete this
                                                ToastHelper.toastShort(AuthEnterNameActivity.this, e.getMessage());

                                            } else {

                                                user = auth.getCurrentUser();

                                                if (user != null) {

                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(inputFirstName.getText().toString() + " " + inputLastName.getText().toString())
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

                                                                        ToastHelper.toastShort(AuthEnterNameActivity.this, "Something went wrong. Please, sign up again");

                                                                    } else {
                                                                        Log.d(TAG, "onComplete: task was successful");

                                                                        DatabaseReference fireDbRefNewUser =
                                                                                fireDb.getReference(RepoStrings.FirebaseReference.USERS);

                                                                        fireDbRefNewUser.push().setValue(createMapWithUserInfo());

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

    /** This method is used to check that the minimun requisites for creating an user
     * are fulfilled
     * */
    public boolean checkMinimumRequisites () {

        if (inputFirstName.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, "Please, enter your First name");
            return false;

        } else if (inputLastName.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, "Please, enter your Last name");
            return false;

        } else if (inputEmail.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, "Please, enter email");
            return false;

        } else if (inputPassword.getText().toString().length() == 0) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, "Please, enter password");
            return false;

        } else if (inputPassword.getText().toString().length() < 6) {
            ToastHelper.toastShort(AuthEnterNameActivity.this, "Sorry, password is too short");
            return false;

        } else {
            return true;
        }
    }

    // TODO: 02/06/2018 Could be probably done with a Static method
    /** Method that create a map with certain info
     * */
    public Map<String,Object> createMapWithUserInfo () {

        Map<String, Object> map = new HashMap<>();

        map.put((RepoStrings.FirebaseReference.FIRST_NAME), inputFirstName.getText().toString());
        map.put((RepoStrings.FirebaseReference.LAST_NAME), inputLastName.getText().toString());
        map.put((RepoStrings.FirebaseReference.EMAIL), inputEmail.getText().toString().toLowerCase().trim());
        map.put((RepoStrings.FirebaseReference.GROUP), "");

        map.put(RepoStrings.FirebaseReference.PLACE_ID, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME, "");
        map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE, "");
        map.put(RepoStrings.FirebaseReference.ADDRESS, "");
        map.put(RepoStrings.FirebaseReference.RATING, "");
        map.put(RepoStrings.FirebaseReference.PHONE, "");
        map.put(RepoStrings.FirebaseReference.IMAGE_URL, "");
        map.put(RepoStrings.FirebaseReference.WEBSITE_URL, "");

        return map;
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

}
