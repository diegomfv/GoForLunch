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
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.google.android.gms.common.oob.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.w3c.dom.Text;

/**
 * Created by Diego Fajardo on 09/05/2018.
 */
public class AuthEnterName extends AppCompatActivity{

    private static final String TAG = "AuthEnterName";

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;

    private Button buttonStart;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_enter_name);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        inputFirstName = (TextInputEditText) findViewById(R.id.enter_first_name_id);
        inputLastName = (TextInputEditText) findViewById(R.id.enter_last_name_id);

        buttonStart = (Button) findViewById(R.id.enter_start_button_id);

        progressBar = (ProgressBar) findViewById(R.id.enter_progressbar);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (user != null) {

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

                                        //We get the exception and display why it was not succesful
                                        FirebaseAuthException e = (FirebaseAuthException) task.getException();

                                        if (e != null) {
                                            Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                        }

                                        ToastHelper.toastShort(AuthEnterName.this, "Something went wrong. Please, sign up again");

                                    } else {
                                        Log.d(TAG, "onComplete: task was succesful");
                                        startActivity(new Intent(AuthEnterName.this, MainActivity.class));
                                    }
                                }
                            });

                } else {
                    ToastHelper.toastShort(AuthEnterName.this, "An error has occurred. Please, sign up again");
                    startActivity(new Intent(AuthEnterName.this, AuthSignUpActivity.class));
                }

            }
        });

    }
}
