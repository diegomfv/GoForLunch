package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

public class AuthSignInEmailPasswordActivity extends AppCompatActivity {

    private static final String TAG = AuthSignInEmailPasswordActivity.class.getSimpleName();

    //Widgets
    @BindView(R.id.signin_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.signin_textinput_email_id)
    TextInputEditText inputEmail;

    @BindView(R.id.signin_textinput_password_id)
    TextInputEditText inputPassword;

    @BindView(R.id.signin_button_id)
    Button buttonSignIn;

    @BindView(R.id.signin_register_button_id)
    Button buttonRegister;

    // TODO: 01/06/2018 Delete?
    @BindView(R.id.signin_progressbar_id)
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signin_email_password);

        ButterKnife.bind(this);

        // TODO: 29/06/2018 Delete this!
        inputEmail.setText("diego.fajardo@hotmail.com");
        inputPassword.setText("123456");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: fab clicked!");
                NavUtils.navigateUpFromSameTask(AuthSignInEmailPasswordActivity.this);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, AuthEnterNameActivity.class);
                intent.putExtra(RepoStrings.SentIntent.EMAIL, inputEmail.getText().toString().toLowerCase());
                intent.putExtra(RepoStrings.SentIntent.PASSWORD, inputPassword.getText().toString().toLowerCase());

                //We include a FLAG intent extra (boolean) to notify the next activity we launched the intent from this Activity
                intent.putExtra(RepoStrings.SentIntent.FLAG, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.d(TAG, "onNext: ");

                        /* If there is internet connection
                        * */
                        if (aBoolean) {
                            Log.d(TAG, "onNext: " + aBoolean);

                            String email = inputEmail.getText().toString();
                            final String password = inputPassword.getText().toString();

                            if (TextUtils.isEmpty(email)) {
                                ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.commonToastEnterEmail));
                                return;

                            } else  if (TextUtils.isEmpty(password)) {
                                ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this,  getResources().getString(R.string.commonToastEnterPassword));
                                return;

                            } else  if (password.length() < 6) {
                                Log.d(TAG, "onClick: password too short, only " + password.length() + " characters" );
                                ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.commonToastPasswordTooShort));
                                return;

                            }

                            /* We show the progress bar
                            * */
                            progressBar.setVisibility(View.VISIBLE);

                            //authenticate user
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            auth.signInWithEmailAndPassword(email,password)
                                    .addOnCompleteListener(AuthSignInEmailPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            // If sign in fails, display a message to the user. If sign in succeeds
                                            // the auth state listener will be notified and logic to handle the
                                            // signed in user can be handled in the listener.
                                            //progressBar.setVisibility(View.GONE);

                                            if (!task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: task was NOT SUCCESSFUL");

                                                //We get the exception and display why it was not succesful
                                                FirebaseAuthException e = (FirebaseAuthException )task.getException();

                                                if (e != null) {
                                                    Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                                    ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, e.getMessage());
                                                } else {
                                                    ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.somethingWentWrong));
                                                }

                                            } else {
                                                Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(new Intent(AuthSignInEmailPasswordActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                    });

                        } else {
                            Log.d(TAG, "onNext: internet connection = " + aBoolean);
                            ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.noInternet));

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
