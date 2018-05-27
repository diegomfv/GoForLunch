package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.strings.StringValues;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterNameAndGroup extends AppCompatActivity{

    private static final String TAG = "AuthEnterNameAndGroup";

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


    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefGroups;

    // TODO: 27/05/2018 Block back button! The user is already created and has to choose a group!

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name_and_group);

        Intent intent = getIntent();
        if (intent.getStringExtra(StringValues.SentIntent.EMAIL) != null
                && intent.getStringExtra(StringValues.SentIntent.PASSWORD) != null) {

            email = intent.getStringExtra(StringValues.SentIntent.EMAIL);
            password = intent.getStringExtra(StringValues.SentIntent.PASSWORD);
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
        fireDbRefGroups = fireDb.getReference(StringValues.FirebaseReference.GROUPS);
        fireDbRefGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                for (DataSnapshot item:
                     dataSnapshot.getChildren()) {

                        listOfGroups.add(Objects.requireNonNull(item.child(StringValues.FirebaseReference.NAME).getValue()).toString());
                }

                Log.d(TAG, "onDataChange: listOfGroups.size() = " + listOfGroups.size());

                if (listOfGroups.size() > 0) {

                    arrayOfGroups = new String[listOfGroups.size()];
                    arrayOfGroups = listOfGroups.toArray(arrayOfGroups);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            AuthEnterNameAndGroup.this,
                            android.R.layout.simple_list_item_1,
                            arrayOfGroups
                    );

                    Log.d(TAG, "onDataChange: array = " + Arrays.toString(arrayOfGroups));

                    inputGroup.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        /** When the user clicks the START button
         * */
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                if (inputFirstName.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Please, enter your First name");

                } else if (inputLastName.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Please, enter your Last name");

                } else if (inputEmail.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Please, enter email");

                } else if (inputPassword.getText().toString().length() == 0) {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Please, enter password");

                } else if (inputPassword.getText().toString().length() < 6) {
                    ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Sorry, password is too short");

                } else {

                    //We create the user
                    auth.createUserWithEmailAndPassword(inputEmail.getText().toString().toLowerCase().trim(), inputPassword.getText().toString().trim())
                            .addOnCompleteListener(AuthEnterNameAndGroup.this, new OnCompleteListener<AuthResult>() {
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

                                        ToastHelper.toastShort(AuthEnterNameAndGroup.this, e.getMessage());

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

                                                                ToastHelper.toastShort(AuthEnterNameAndGroup.this, "Something went wrong. Please, sign up again");

                                                            } else {
                                                                Log.d(TAG, "onComplete: task was succesful");

                                                                // TODO: 27/05/2018 We insert the user in the database in position x


                                                                DatabaseReference fireDbRefUser = fireDb.getReference(StringValues.FirebaseReference.USERS);


                                                                startActivity(new Intent(AuthEnterNameAndGroup.this, MainActivity.class));
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
        });
    }

    @Override
    public void onBackPressed() {
        // TODO: 27/05/2018 Don't allow the user to go back!
        super.onBackPressed();
    }
}
