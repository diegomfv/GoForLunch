package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.Utils;
import com.example.android.goforlunch.constants.Repo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */
public class AuthSignInEmailPasswordActivity extends AppCompatActivity implements Observer {

    private static final String TAG = AuthSignInEmailPasswordActivity.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Firebase
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Shared Preferences
    private SharedPreferences sharedPref;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthSignInEmailPasswordActivity.this);

        /* We establish the entry points
        to get the user information
        * */
        fireDb = FirebaseDatabase.getInstance();

        ////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_auth_signin_email_password);
        ButterKnife.bind(this);

        Utils.showMainContent(progressBarContent, mainContent);

        fab.setOnClickListener(fabOnClickOnClickListener);
        buttonSignIn.setOnClickListener(buttonSignInOnClickListener);
        tvForgotPassword.setOnClickListener(tvForgotPasswordOnClickListener);
        buttonSignUp.setOnClickListener(buttonSignUpOnClickListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called!");
        this.connectBroadcastReceiver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called!");
        this.disconnectBroadcastReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");

        this.disconnectBroadcastReceiver();

        this.buttonSignIn.setOnClickListener(null);
        this.buttonSignUp.setOnClickListener(null);
        this.tvForgotPassword.setOnClickListener(null);
        this.fab.setOnClickListener(null);

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

        } else if ((int) internetAvailableUpdate == 1) {
            Log.d(TAG, "update: Internet available");

            internetAvailable = true;

            if (snackbar != null) {
                snackbar.dismiss();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /*******************************
     * CONFIGURATION ***************
     ******************************/

    /** Method that connects a broadcastReceiver to the activity.
     * It allows to notify the user about the internet state
     * */
    private void connectBroadcastReceiver () {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Repo.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(AuthSignInEmailPasswordActivity.this, receiver, intentFilter, this);

    }

    /** Method that disconnects the broadcastReceiver from the activity.
     * */
    private void disconnectBroadcastReceiver () {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

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

            if (inputEmail.getText().toString().trim().length() == 0
                    || inputPassword.getText().toString().trim().length() == 0) {

                Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, AuthEnterNameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {
                Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, AuthEnterNameActivity.class);
                intent.putExtra(Repo.SentIntent.EMAIL, inputEmail.getText().toString().toLowerCase());
                intent.putExtra(Repo.SentIntent.PASSWORD, inputPassword.getText().toString().toLowerCase());

                //We include a boolean intent extra to notify the next activity we launched the intent from this Activity
                intent.putExtra(Repo.SentIntent.FLAG, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
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
                                    /* Sign in was successful
                                    * */

                                    /* We get the user notifications state in firebase to update Shared Preferences
                                    */
                                    dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS);
                                    dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, "onDataChange: called!");

                                            /* We update shared preferences (notifications) according to the information
                                             * of the user in firebase. This is the only moment in which notifications
                                             * in firebase will affect preferences in the app. From this moment on,
                                             * the variations in the preference fragment will affect firebase (and not
                                             * the other way around).
                                             * */

                                            for (DataSnapshot item :
                                                    dataSnapshot.getChildren()) {

                                                /* We get the user
                                                 * */
                                                if (inputEmail.getText().toString().equalsIgnoreCase(
                                                        item.child(Repo.FirebaseReference.USER_EMAIL).getValue().toString())) {

                                                    if (item.child(Repo.FirebaseReference.USER_NOTIFICATIONS).getValue().toString()
                                                            .equals("")
                                                            || item.child(Repo.FirebaseReference.USER_NOTIFICATIONS).getValue().toString()
                                                            .equals("false")) {

                                                        /* If notifications user information is "" or "false",
                                                        * we set the notifications in shared Preferences as false
                                                        * */
                                                        Utils.updateSharedPreferences(
                                                                sharedPref,
                                                                getResources().getString(R.string.pref_key_notifications),
                                                                false);

                                                    } else {
                                                        /* If notifications user information is not "" or "false" then it's true.
                                                         * We set the notifications in shared Preferences as true
                                                         * */
                                                        Utils.updateSharedPreferences(
                                                                sharedPref,
                                                                getResources().getString(R.string.pref_key_notifications),
                                                                true);

                                                    }
                                                }
                                            }

                                            /* We launch Main Activity
                                             * */
                                            Intent intent = new Intent(AuthSignInEmailPasswordActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: called!");

                                            ToastHelper.toastSomethingWentWrong(AuthSignInEmailPasswordActivity.this);
                                            Utils.showMainContent(progressBarContent, mainContent);
                                        }
                                    });
                                }
                            }
                        });

            } else {
                ToastHelper.toastNoInternet(AuthSignInEmailPasswordActivity.this);

            }
        }
    };

}