package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.broadcastreceivers.InternetConnectionReceiver;
import com.example.android.goforlunch.broadcastreceivers.ObservableObject;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.repository.RepoStrings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */
public class AuthSignInEmailPasswordActivity extends AppCompatActivity implements Observer {

    private static final String TAG = AuthSignInEmailPasswordActivity.class.getSimpleName();

    //Widgets
    @BindView(R.id.signin_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.signin_textinput_email_id)
    TextInputEditText inputEmail;

    @BindView(R.id.signin_textinput_password_id)
    TextInputEditText inputPassword;

    @BindView(R.id.signin_signIn_button_id)
    Button buttonSignIn;

    @BindView(R.id.signin_textView_forgot_password)
    TextView tvForgotPassword;

    @BindView(R.id.signin_signUp_button_id)
    Button buttonSignUp;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    @BindView(R.id.signin_main_content)
    LinearLayout mainContent;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_signin_email_password);

        ButterKnife.bind(this);

        Utils.showMainContent(progressBarContent, mainContent);

        // TODO: 29/06/2018 Delete this! Do it with Shared Preferences
        inputEmail.setText("diego.fajardo@hotmail.com");
        inputPassword.setText("123456");

        /** Configuring textInputEditTexts: hide keyboard
         * */
        Utils.configureTextInputEditTextWithHideKeyboard(AuthSignInEmailPasswordActivity.this, inputEmail);
        Utils.configureTextInputEditTextWithHideKeyboard(AuthSignInEmailPasswordActivity.this, inputPassword);

        fab.setOnClickListener(fabOnClickOnClickListener);
        buttonSignIn.setOnClickListener(buttonSignInOnClickListener);
        tvForgotPassword.setOnClickListener(tvForgotPasswordOnClickListener);
        buttonSignUp.setOnClickListener(buttonSignUpOnClickListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(RepoStrings.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(AuthSignInEmailPasswordActivity.this, receiver, intentFilter, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    AuthSignInEmailPasswordActivity.this,
                    receiver,
                    AuthSignInEmailPasswordActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    AuthSignInEmailPasswordActivity.this,
                    receiver,
                    AuthSignInEmailPasswordActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

        buttonSignIn.setOnClickListener(null);
        buttonSignUp.setOnClickListener(null);
        tvForgotPassword.setOnClickListener(null);
        fab.setOnClickListener(null);

    }

    /** Callback: listening to broadcast receiver
     * */
    @Override
    public void update(Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet Not Available");

            internetAvailable = false;

            if (snackbar == null) {
                snackbar = Utils.createSnackbar(
                        AuthSignInEmailPasswordActivity.this,
                        mainContent,
                        getResources().getString(R.string.noInternet));

            } else {
                snackbar.show();
            }

        } else {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            if (snackbar != null) {
                snackbar.dismiss();
            }
        }
    }

    /*******************************
     * LISTENERS *******************
     ******************************/

    private View.OnClickListener fabOnClickOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: fab clicked!");
            NavUtils.navigateUpFromSameTask(AuthSignInEmailPasswordActivity.this);
        }
    };

    private View.OnClickListener buttonSignUpOnClickListener = new View.OnClickListener() {
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
    };


    private View.OnClickListener tvForgotPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: tvForgotPassword clicked!");
            ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.notImplemented));
        }
    };

    private View.OnClickListener buttonSignInOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (internetAvailable) {
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

                Utils.hideMainContent(progressBarContent, mainContent);

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
                                    FirebaseAuthException e = (FirebaseAuthException) task.getException();

                                    if (e != null) {
                                        Log.e(TAG, "onComplete: task NOT SUCCESSFUL: " + e.getMessage());
                                        ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, e.getMessage());
                                    } else {
                                        ToastHelper.toastShort(AuthSignInEmailPasswordActivity.this, getResources().getString(R.string.somethingWentWrong));
                                    }

                                    Utils.showMainContent(progressBarContent, mainContent);

                                } else {
                                    Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(new Intent(AuthSignInEmailPasswordActivity.this, MainActivity.class));
                                    finish();

                                }
                            }
                        });

            } else {
                ToastHelper.toastNoInternet(AuthSignInEmailPasswordActivity.this);

            }
        }
    };

}