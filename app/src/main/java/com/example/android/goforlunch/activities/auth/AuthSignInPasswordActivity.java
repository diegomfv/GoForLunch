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
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

// TODO: 01/06/2018 We can create a database to store userNames and passwords
// TODO: 01/06/2018 and display them using TextInputAutocompleteTextView
public class AuthSignInPasswordActivity extends AppCompatActivity {

    private static final String TAG = AuthSignInPasswordActivity.class.getSimpleName();

    private FirebaseAuth auth;

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;

    private Button buttonSignIn;
    private Button buttonReset;
    private Button buttonRegister;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        //We set the contentView after checking if the user is already logged in
        setContentView(R.layout.activity_auth_signin_password);

        inputEmail = (TextInputEditText) findViewById(R.id.signin_textinput_email_id);
        inputPassword = (TextInputEditText) findViewById(R.id.signin_textinput_password_id);

        buttonSignIn = (Button) findViewById(R.id.signin_button_id);
        buttonReset = (Button) findViewById(R.id.signin_reset_button_id);
        buttonRegister = (Button) findViewById(R.id.signin_register_button_id);

        // TODO: 01/06/2018 Delete?
        progressBar = (ProgressBar) findViewById(R.id.signin_progressbar_id);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AuthSignInPasswordActivity.this, AuthEnterNameActivity.class);
                intent.putExtra(RepoStrings.SentIntent.EMAIL, inputEmail.getText().toString().toLowerCase());
                intent.putExtra(RepoStrings.SentIntent.PASSWORD, inputPassword.getText().toString().toLowerCase());

                //We include a FLAG intent extra (boolean) to notify the next activity we launched the intent from this Activity
                intent.putExtra(RepoStrings.SentIntent.FLAG, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastHelper.toastShort(AuthSignInPasswordActivity.this, "Not implemented yet");
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.d(TAG, "onNext: ");

                        if (aBoolean) {
                            Log.d(TAG, "onNext: " + aBoolean);

                            String email = inputEmail.getText().toString();
                            final String password = inputPassword.getText().toString();

                            if (TextUtils.isEmpty(email)) {
                                ToastHelper.toastShort(AuthSignInPasswordActivity.this, "Enter email address");
                                return;

                            } else  if (TextUtils.isEmpty(password)) {
                                ToastHelper.toastShort(AuthSignInPasswordActivity.this, "Enter password");
                                return;

                            } else  if (password.length() < 6) {
                                Log.d(TAG, "onClick: password too short, only " + password.length() + " characters" );
                                ToastHelper.toastShort(AuthSignInPasswordActivity.this, "Password is too short");
                                return;

                            }

                            progressBar.setVisibility(View.VISIBLE);

                            //authenticate user
                            auth.signInWithEmailAndPassword(email,password)
                                    .addOnCompleteListener(AuthSignInPasswordActivity.this, new OnCompleteListener<AuthResult>() {
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
                                                    ToastHelper.toastShort(AuthSignInPasswordActivity.this, e.getMessage());
                                                } else {
                                                    ToastHelper.toastShort(AuthSignInPasswordActivity.this, "Something went wrong");
                                                }

                                            } else {
                                                Intent intent = new Intent(AuthSignInPasswordActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(new Intent(AuthSignInPasswordActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                    });

                        } else {
                            Log.d(TAG, "onNext: internet connectio = " + aBoolean);
                            ToastHelper.toastShort(AuthSignInPasswordActivity.this, getResources().getString(R.string.noInternet));

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
        });
    }
}
