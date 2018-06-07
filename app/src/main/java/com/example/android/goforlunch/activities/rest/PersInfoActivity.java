package com.example.android.goforlunch.activities.rest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.example.android.goforlunch.widgets.TextInputAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

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

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefGroups;
    private DatabaseReference fireDbRefUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pers_info);

        iv_userImage = (ImageView) findViewById(R.id.pers_enter_image_id);
        inputFirstName = (TextInputEditText) findViewById(R.id.pers_enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.pers_enter_last_name_id);
        inputEmail = (TextInputEditText) findViewById(R.id.pers_enter_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.pers_enter_password_id);
        inputGroup = (TextInputAutoCompleteTextView) findViewById(R.id.pers_enter_group_id);
        buttonSaveChanges = (Button) findViewById(R.id.pers_enter_save_changes_button_id);
        buttonChangePassword = (Button) findViewById(R.id.pers_enter_change_password_id);
        progressBar = (ProgressBar) findViewById(R.id.pers_enter_progressbar);

        /** Instantiation of
         *  FirebaseAuth
         * */
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        /** We fill the widgets with the user's info
         * */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PersInfoActivity.this);
        inputFirstName.setText(sharedPref.getString(RepoStrings.SharedPreferences.USER_FIRST_NAME, ""));
        inputLastName.setText(sharedPref.getString(RepoStrings.SharedPreferences.USER_LAST_NAME, ""));

        if (user != null) {
            inputEmail.setText(user.getEmail());
        }

        inputPassword.setText("********");
        inputGroup.setText(sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP,""));


        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked! " + view.toString());

                ToastHelper.toastShort(PersInfoActivity.this, "Not implemented yet!");
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
