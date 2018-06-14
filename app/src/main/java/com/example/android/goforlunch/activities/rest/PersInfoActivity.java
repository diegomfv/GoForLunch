package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.example.android.goforlunch.activities.auth.AuthEnterNameActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.example.android.goforlunch.widgets.TextInputAutoCompleteTextView;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class PersInfoActivity extends AppCompatActivity{

    // TODO: 29/05/2018 Eliminate that the FirstName view is focused from the beginning
    // TODO: 29/05/2018 Eliminate the group option here
    // TODO: 29/05/2018 Check AutocompleteTextView. Things are missing
    // TODO: 06/06/2018 Fill the textInputs with first name and last name
    // TODO: 06/06/2018 Allow to modify the profile picture

    private static final String TAG = "PersInfoActivity";

    private ImageView iv_userImage;
    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputAutoCompleteTextView inputGroup;

    private Button buttonSaveChanges;
    private Button buttonChangePassword;

    private ProgressBar progressBar;

    private List<String> listOfGroups;
    private String[] arrayOfGroups;

    //Variables
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userKey;
    private String userGroup;
    private String userGroupKey;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefGroups;
    private DatabaseReference dbRefUsers;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pers_info);

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(PersInfoActivity.this);

        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        iv_userImage = (ImageView) findViewById(R.id.pers_enter_image_id);
        inputFirstName = (TextInputEditText) findViewById(R.id.pers_enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.pers_enter_last_name_id);
        inputEmail = (TextInputEditText) findViewById(R.id.pers_enter_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.pers_enter_password_id);
        inputGroup = (TextInputAutoCompleteTextView) findViewById(R.id.pers_enter_group_id);
        buttonSaveChanges = (Button) findViewById(R.id.pers_enter_save_changes_button_id);
        buttonChangePassword = (Button) findViewById(R.id.pers_enter_change_password_id);
        progressBar = (ProgressBar) findViewById(R.id.pers_enter_progressbar);

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

                        /** We fill the widgets with the user's info
                         * */
                        inputFirstName.setText(userFirstName);
                        inputLastName.setText(userLastName);
                        inputEmail.setText(userEmail);
                        inputGroup.setText(userGroup);
                        inputPassword.setText("******");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

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

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked! " + view.toString());

                ToastHelper.toastShort(PersInfoActivity.this, "Not implemented yet!");
            }
        });
    }
}
