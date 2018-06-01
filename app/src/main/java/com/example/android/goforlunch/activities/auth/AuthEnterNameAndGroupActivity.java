package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.strings.RepoStrings;
import com.example.android.goforlunch.widgets.TextInputAutoCompleteTextView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterNameAndGroupActivity extends AppCompatActivity{

    private static final String TAG = "AuthEnterNameAndGroupAc";

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputAutoCompleteTextView inputGroup;

    private Button buttonStart;

    private ProgressBar progressBar;

    private List<String> listOfGroups;
    private String[] arrayOfGroups;

    private String email;
    private String password;
    private String targetEmptyUserKey = "";


    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefGroups;
    private DatabaseReference fireDbRefUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name_and_group);

        Intent intent = getIntent();
        if (intent.getStringExtra(RepoStrings.SentIntent.EMAIL) != null
                && intent.getStringExtra(RepoStrings.SentIntent.PASSWORD) != null) {

            email = intent.getStringExtra(RepoStrings.SentIntent.EMAIL);
            password = intent.getStringExtra(RepoStrings.SentIntent.PASSWORD);
        }

        inputFirstName = (TextInputEditText) findViewById(R.id.enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.enter_last_name_id);
        inputEmail = (TextInputEditText) findViewById(R.id.enter_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.enter_password_id);
        inputGroup = (TextInputAutoCompleteTextView) findViewById(R.id.enter_group_id);
        buttonStart = (Button) findViewById(R.id.enter_start_button_id);
        progressBar = (ProgressBar) findViewById(R.id.enter_progressbar);

        /** Instantiation of list
         * */
        listOfGroups = new ArrayList<>();

        /** We fill the email and password fields with the user info
         * */
        inputEmail.setText(email);
        inputPassword.setText(password);

        /** Instantiation of
         *  the FirebaseAuth object
         * */
        auth = FirebaseAuth.getInstance();

        /** We get the list of groups from the database and fill the adapter of the
         * TextInputAutocompleteTextView with it
         * */
        fireDb = FirebaseDatabase.getInstance();
        fireDbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
        fireDbRefGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    listOfGroups.add(Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue()).toString());

                    Log.d(TAG, "onDataChange: listOfGroups.size() = " + listOfGroups.size());

                    if (listOfGroups.size() > 0) {

                        arrayOfGroups = new String[listOfGroups.size()];
                        arrayOfGroups = listOfGroups.toArray(arrayOfGroups);

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                AuthEnterNameAndGroupActivity.this,
                                android.R.layout.simple_list_item_1,
                                arrayOfGroups
                        );

                        Log.d(TAG, "onDataChange: array = " + Arrays.toString(arrayOfGroups));

                        inputGroup.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        /** We get an empty userId if it exists in the list. We will use it with a new user (this way,
         * we will avoid creating new ids when there are some that are not used) */
        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
        fireDbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    if (Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()).equals("")) {
                        Log.d(TAG, "onDataChange: found an empty userId");
                        Log.d(TAG, "onDataChange: " + Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.EMAIL).getValue()));

                        targetEmptyUserKey = item.getKey();
                        break;

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        /** When the user
         * clicks the START button
         * */
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                if (inputFirstName.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Please, enter your First name");

                } else if (inputLastName.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Please, enter your Last name");

                } else if (inputEmail.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Please, enter email");

                } else if (inputPassword.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Please, enter password");

                } else if (inputPassword.getText().toString().length() < 6) {
                    ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Sorry, password is too short");

                } else {

                    //We create the user
                    auth.createUserWithEmailAndPassword(inputEmail.getText().toString().toLowerCase().trim(), inputPassword.getText().toString().trim())
                            .addOnCompleteListener(AuthEnterNameAndGroupActivity.this, new OnCompleteListener<AuthResult>() {
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

                                        ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, e.getMessage());

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

                                                                ToastHelper.toastShort(AuthEnterNameAndGroupActivity.this, "Something went wrong. Please, sign up again");

                                                            } else {
                                                                Log.d(TAG, "onComplete: task was successful");

                                                                if (targetEmptyUserKey.equals("")) {
                                                                    /** If there is no empty key
                                                                     */
                                                                    Log.d(TAG, "onDataChange: no empty keys");

                                                                    DatabaseReference fireDbRefNewUser =
                                                                            fireDb.getReference(RepoStrings.FirebaseReference.USERS);

                                                                    Map<String, Object> map = new HashMap<>();

                                                                    map.put((RepoStrings.FirebaseReference.FIRST_NAME), inputFirstName.getText().toString());
                                                                    map.put((RepoStrings.FirebaseReference.LAST_NAME), inputLastName.getText().toString());
                                                                    map.put((RepoStrings.FirebaseReference.EMAIL), inputEmail.getText().toString().toLowerCase().trim());
                                                                    map.put((RepoStrings.FirebaseReference.GROUP), inputGroup.getText().toString());

                                                                    map.put(RepoStrings.FirebaseReference.PLACE_ID,"");
                                                                    map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME,"");
                                                                    map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE,"");
                                                                    map.put(RepoStrings.FirebaseReference.ADDRESS, "");
                                                                    map.put(RepoStrings.FirebaseReference.RATING,"");
                                                                    map.put(RepoStrings.FirebaseReference.PHONE,"");
                                                                    map.put(RepoStrings.FirebaseReference.IMAGE_URL,"");
                                                                    map.put(RepoStrings.FirebaseReference.WEBSITE_URL, "");

                                                                    fireDbRefNewUser.push().setValue(map);

                                                                    startActivity(new Intent(AuthEnterNameAndGroupActivity.this, MainActivity.class));
                                                                    finish();

                                                                } else {
                                                                    /** If there is an empty key, we use it
                                                                     */
                                                                    Log.d(TAG, "onComplete: empty keys");

                                                                    DatabaseReference fireDbRefSpecificUser =
                                                                            fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + targetEmptyUserKey);

                                                                    Map<String, Object> map = new HashMap<>();

                                                                    map.put((RepoStrings.FirebaseReference.FIRST_NAME), inputFirstName.getText().toString());
                                                                    map.put((RepoStrings.FirebaseReference.LAST_NAME), inputLastName.getText().toString());
                                                                    map.put((RepoStrings.FirebaseReference.EMAIL), inputEmail.getText().toString().toLowerCase().trim());
                                                                    map.put((RepoStrings.FirebaseReference.GROUP), inputGroup.getText().toString());

                                                                    map.put(RepoStrings.FirebaseReference.PLACE_ID,"");
                                                                    map.put(RepoStrings.FirebaseReference.RESTAURANT_NAME,"");
                                                                    map.put(RepoStrings.FirebaseReference.RESTAURANT_TYPE,"");
                                                                    map.put(RepoStrings.FirebaseReference.ADDRESS, "");
                                                                    map.put(RepoStrings.FirebaseReference.RATING,"");
                                                                    map.put(RepoStrings.FirebaseReference.PHONE,"");
                                                                    map.put(RepoStrings.FirebaseReference.IMAGE_URL,"");
                                                                    map.put(RepoStrings.FirebaseReference.WEBSITE_URL, "");

                                                                    fireDbRefSpecificUser.updateChildren(map);

                                                                    startActivity(new Intent(AuthEnterNameAndGroupActivity.this, MainActivity.class));
                                                                    finish();

                                                                }

                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

}
