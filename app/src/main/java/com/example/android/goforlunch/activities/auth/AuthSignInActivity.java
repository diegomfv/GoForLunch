package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.strings.StringValues;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

public class AuthSignInActivity extends AppCompatActivity {

    private static final String TAG = "AuthSignInActivity";

    private FirebaseAuth auth;

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;

    private Button buttonSignIn;
    private Button buttonReset;
    private Button buttonSignUp;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

//        if (auth.getCurrentUser() != null) {
//            startActivity(new Intent(AuthSignInActivity.this, MainActivity.class));
//            finish();
//        }

        //We set the contentView AFTER checking if the user is already logged in
        setContentView(R.layout.activity_auth_signin);

        inputEmail = (TextInputEditText) findViewById(R.id.signin_textinput_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.signin_textinput_password_id);

        /** We get the info from the other screen AuthSignInActivity
         * */
        Intent intent = getIntent();
        if (intent.getStringExtra(StringValues.SentIntent.EMAIL) != null
                && intent.getStringExtra(StringValues.SentIntent.PASSWORD) != null) {

            inputEmail.setText(intent.getStringExtra(StringValues.SentIntent.EMAIL));
            inputPassword.setText(intent.getStringExtra(StringValues.SentIntent.PASSWORD));
        }

        buttonSignIn = (Button) findViewById(R.id.signin_button_id);
        buttonReset = (Button) findViewById(R.id.signin_reset_button_id);
        buttonSignUp = (Button) findViewById(R.id.signin_signup_button_id);

        progressBar = (ProgressBar) findViewById(R.id.signin_progressbar_id);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AuthSignInActivity.this, AuthSignUpActivity.class);
                intent.putExtra(StringValues.SentIntent.EMAIL,inputEmail.getText().toString().toLowerCase());
                intent.putExtra(StringValues.SentIntent.PASSWORD,inputPassword.getText().toString().toLowerCase());
                startActivity(intent);
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastHelper.toastShort(AuthSignInActivity.this, "Not implemented yet");
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    ToastHelper.toastShort(AuthSignInActivity.this, "Enter email address");
                    return;
                } else  if (TextUtils.isEmpty(password)) {
                    ToastHelper.toastShort(AuthSignInActivity.this, "Enter password");
                    return;
                } else  if (password.length() < 6) {
                    Log.d(TAG, "onClick: password too short, only " + password.length() + " characters" );
                    ToastHelper.toastShort(AuthSignInActivity.this, "Password is too short");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(AuthSignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);

                                if (!task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");

                                    //We get the exception and display why it was not succesful
                                    FirebaseAuthException e = (FirebaseAuthException )task.getException();

                                    if (e != null) {
                                        Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                        ToastHelper.toastShort(AuthSignInActivity.this, e.getMessage());
                                    } else {
                                        ToastHelper.toastShort(AuthSignInActivity.this, "Something went wrong");
                                    }

                                } else {
                                    startActivity(new Intent(AuthSignInActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }
}
