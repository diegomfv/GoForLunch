package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.constants.Repo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 24/07/2018.
 */
public class AuthChooseLoginActivity extends AppCompatActivity implements Observer {

    private static final String TAG = AuthChooseLoginActivity.class.getSimpleName();

    //Google Sign In Request Code
    private static int RC_GOOGLE_SIGN_IN = 101;

    //Widgets
    @BindView(R.id.choose_sign_in_password_button_id)
    Button buttonPassword;

    @BindView(R.id.choose_textView_register)
    TextView tvRegister;

    @BindView(R.id.choose_google_sign_in_button)
    SignInButton buttonGoogle;

    @BindView(R.id.choose_facebook_sign_in_button)
    LoginButton buttonFacebook;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    @BindView(R.id.main_layout_id)
    LinearLayout mainContent;

    private Unbinder unbinder;

    private GoogleSignInClient mGoogleSignInClient;

    //For facebook login
    private CallbackManager mCallbackManager;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    //Shared Preferences
    private SharedPreferences sharedPref;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;

    private boolean internetAvailable;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        /* We delete all info from shared preferences
        * */
        sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthChooseLoginActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().apply();

        /* We establish the entry points
        to get the user information
        * */
        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        /* Leave this two lines of code uncommented to do trials without
        * logging in directly
        * */
//        auth.signOut();
//        LoginManager.getInstance().logOut();

        /* We check if the user is logged in in a background thread.
         * */
        if (!UtilsGeneral.hasPermissions(AuthChooseLoginActivity.this, Repo.PERMISSIONS)) {
            UtilsGeneral.getPermissions(AuthChooseLoginActivity.this);
        }

        checkIfUserIsLoggedInInBackgroundThread();


        /* internetAvailable is false till the update() callback changes it
        * */
        internetAvailable = false;

        /////////////////////////////////////////////
        /* We set the content view
        * */
        setContentView(R.layout.activity_auth_choose_login);
        unbinder = ButterKnife.bind(this);

        /* We set the listeners
        * */
        buttonPassword.setOnClickListener(buttonPasswordOnClickListener);

        tvRegister.setOnClickListener(tvRegisterOnClickListener);

        /* Configure Google Sign In
         *  */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonGoogle.setStyle(SignInButton.SIZE_WIDE,SignInButton.COLOR_DARK);
        buttonGoogle.setOnClickListener(buttonGoogleOnClickListener);

        mCallbackManager = CallbackManager.Factory.create();
        buttonFacebook.setReadPermissions("email", "public_profile", "user_friends");
        buttonFacebook.registerCallback(mCallbackManager, facebookCallbackLoginResult);

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

        this.buttonPassword.setOnClickListener(null);
        this.tvRegister.setOnClickListener(null);
        this.buttonGoogle.setOnClickListener(null);

        this.unbinder.unbind();

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
                snackbar = UtilsGeneral.createSnackbar(
                        AuthChooseLoginActivity.this,
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

            /* We get the user info. If the user is already registered and we have his/her name
            and last name,
            we launch MainActivity
            * */
            getUserAndLaunchSpecificActivity();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called!");


        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Log.d(TAG, "onActivityResult: google process..!");
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, updateItem UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }

        } else {
            // Result returned from launching the Intent from Facebook Login
            Log.d(TAG, "onActivityResult: facebook process...!");
            mCallbackManager.onActivityResult(requestCode,resultCode,data);

        }
    }

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
        UtilsGeneral.connectReceiver(AuthChooseLoginActivity.this, receiver, intentFilter, this);

    }

    /** Method that disconnects the broadcastReceiver from the activity.
     * */
    private void disconnectBroadcastReceiver () {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null) {
            UtilsGeneral.disconnectReceiver(
                    AuthChooseLoginActivity.this,
                    receiver,
                    AuthChooseLoginActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    /*******************************
     * LISTENERS *******************
     ******************************/

    private View.OnClickListener buttonPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Password Button clicked!");

            startActivity(new Intent(AuthChooseLoginActivity.this, AuthSignInEmailPasswordActivity.class));

        }
    };

    private View.OnClickListener tvRegisterOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Register textView clicked!");

            Intent intent = new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class);

            //We include a FLAG intent extra (boolean) to notify the next activity we launched the intent from this Activity
            intent.putExtra(Repo.SentIntent.FLAG, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    };


    private View.OnClickListener buttonGoogleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Google Button clicked!");

            if (internetAvailable) {
                googleSignIn();

            } else {
                ToastHelper.toastNoInternet(AuthChooseLoginActivity.this);

            }

        }
    };

    private FacebookCallback<LoginResult> facebookCallbackLoginResult = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "onSuccess: called!");

            if (internetAvailable) {
                handleFacebookAccessToken(loginResult.getAccessToken());

            } else {
                ToastHelper.toastNoInternet(AuthChooseLoginActivity.this);
            }

        }

        @Override
        public void onCancel() {
            Log.d(TAG, "onCancel: called!");

        }

        @Override
        public void onError(FacebookException error) {
            Log.e(TAG, "onError: " + error.toString() );

        }
    };

    /*******************************
     * METHODS *********************
     ******************************/

    /** Method that checks if the user is currently
     * logged in in a background thread
     * */
    private void checkIfUserIsLoggedInInBackgroundThread() {
        Log.d(TAG, "checkIfUserIsLoggedInInBackgroundThread: called!");

        /* We use this method instead of internetAvailable because we are still
        * in onCreate() and internetAvailable would be false in both cases (with
        * and without internet available)
        * */
        UtilsGeneral.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean internetAvailableBackgroundThread) {
                Log.d(TAG, "onNext: called!");

                if (!internetAvailableBackgroundThread) {
                    ToastHelper.toastNoInternetFeaturesNotWorking(AuthChooseLoginActivity.this);
                    UtilsGeneral.showMainContent(progressBarContent, mainContent);

                } else {
                    /* Internet is available
                    * */
                    getUserAndLaunchSpecificActivity();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: called!");

            }
        });
    }

    /** Method that gets the user and launches an specific activity if needed
     * */
    private void getUserAndLaunchSpecificActivity () {
        Log.d(TAG, "getUserAndLaunchSpecificActivity: called!");

        user = auth.getCurrentUser();

        if (user == null) {
            Log.d(TAG, "onCreate: user is null");
            //We delete Shared preferences info
            UtilsGeneral.deleteSharedPreferencesInfo(sharedPref);

            /* If the user is null, we won't launch a new activity,
             so we show the main layout
            * */
            UtilsGeneral.showMainContent(progressBarContent, mainContent);


        } else {

            if (user.getDisplayName() != null){
                Log.d(TAG, "onCreate: user is not null");

                //go directly to MainActivity
                startActivity(new Intent(AuthChooseLoginActivity.this, MainActivity.class));
                finish();


            } else {
                //go to AuthEnterNameActivity
                startActivity(new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class));
                finish();

            }
        }
    }

    /** Method for google sign in
     * */
    public void googleSignIn() {
        Log.d(TAG, "googleSignIn: called!");

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    /********************
     * SIGN IN **********
     * with google ******
     * and facebook *****
     * *****************/

    /** Method that handles google sign in
     * */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: called!");
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        if (internetAvailable) {
            /* We hide the main screen while the process runs
             * */
            UtilsGeneral.hideMainContent(progressBarContent, mainContent);

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.d(TAG, "GOOGLE signInWithCredential: success");
                                user = auth.getCurrentUser();

                                if (user != null) {
                                    checkIfUserExistsInDatabase(user);

                                }

                            } else {
                                /* Something went wrong during sign in */
                                Log.d(TAG, "GOOGLE signInWithCredential: failure");
                                ToastHelper.toastShort(AuthChooseLoginActivity.this, getResources().getString(R.string.somethingWentWrong));
                                UtilsGeneral.showMainContent(progressBarContent, mainContent);

                            }
                        }
                    });

        } else {
            /* There is no internet
            * */
            ToastHelper.toastNoInternet(AuthChooseLoginActivity.this);
        }
    }

    /** Method that handles facebook sign in
     * */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken: called!");

        if (internetAvailable) {

            UtilsGeneral.hideMainContent(progressBarContent, mainContent);

            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, updateItem UI with the signed-in user's information
                                Log.d(TAG, "FACEBOOK signInWithCredential: success");
                                user = auth.getCurrentUser();

                                if (user != null) {
                                    checkIfUserExistsInDatabase(user);

                                }

                            } else {
                                /* If sign in fails, display a message to the user
                                 * hide progress bar and enable onClick on views
                                 * */
                                Log.w(TAG, "FACEBOOK signInWithCredential: failure", task.getException());
                                ToastHelper.toastShort(AuthChooseLoginActivity.this, getResources().getString(R.string.somethingWentWrong));
                                UtilsGeneral.showMainContent(progressBarContent, mainContent);

                            }

                            // ...
                        }
                    });

        } else {
            ToastHelper.toastNoInternet(AuthChooseLoginActivity.this);
        }

    }

    /** Method that checks if a user exists in the database.
     * If the user does, MainActivity is launched. If the user doesn't,
     * the user is created in the database and afterwards MainActivity is launched
     * */
    private void checkIfUserExistsInDatabase (final FirebaseUser user) {
        Log.d(TAG, "checkIfUserExistsInDatabase: called!");

        if (!internetAvailable) {
            ToastHelper.toastSomethingWentWrong(AuthChooseLoginActivity.this);

        } else {

            /* We check if the user exists in the database.
             * If the user does, we launch Main Activity directly.
             * If the user does not, we create the user and launch Main Activity
             * */
            dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS);
            dbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                    /* We use two variables to store if the user exists and, if the user does,
                     * we store the user notification info in Firebase
                     * */
                    boolean userExists = false;
                    boolean userNotifInfo = false;

                    for (DataSnapshot item :
                            dataSnapshot.getChildren()) {

                        /* If the email of the user exists in the database, it's the user
                         * */
                        if (user.getEmail().equalsIgnoreCase(
                                item.child(Repo.FirebaseReference.USER_EMAIL).getValue().toString())) {

                            userExists = true;

                            /* We get the notifications information and will use it to update SharedPreferences.
                             */

                            if (item.child(Repo.FirebaseReference.USER_NOTIFICATIONS).getValue().toString()
                                    .equals("")
                                    || item.child(Repo.FirebaseReference.USER_NOTIFICATIONS).getValue().toString()
                                    .equals("false")) {

                                /* If notifications user information is "" or "false",
                                 * we leave userNotifInfo as false.
                                 * */

                            } else {
                                /* If notifications user information is not "" or "false" then it's true.
                                 * We set userNotifInfo as true
                                 * */
                                userNotifInfo = true;

                            }
                        }
                    }

                    if (!userExists) {

                        /* User does not exist. We create the user in firebase and launch MainActivity
                         * */
                        dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS);
                        String userKey = dbRefUsers.push().getKey();

                        String[] names = UtilsGeneral.getFirstNameAndLastName(user.getDisplayName());

                        dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey);
                        UtilsFirebase.updateUserInfoInFirebase(dbRefUsers,
                                names[0],
                                names[1],
                                user.getEmail().toLowerCase(),
                                "",
                                "",
                                "false",
                                "");

                        dbRefUsers = fireDb.getReference(
                                Repo.FirebaseReference.USERS
                                        + "/" + userKey
                                        + "/" + Repo.FirebaseReference.USER_RESTAURANT_INFO);
                        UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                0,
                                "");

                        /* We update shared preferences (notifications) according to the information
                         * of the user in firebase. This is the only moment in which notifications
                         * in firebase will affect preferences in the app. From this moment on,
                         * the variations in the preference fragment will affect firebase (and not
                         * the other way around).
                         * */
                        UtilsGeneral.updateSharedPreferences(sharedPref,
                                getResources().getString(R.string.pref_key_notifications),
                                userNotifInfo);

                        /* We remove the listener
                         * */
                        dbRefUsers.removeEventListener(this);

                        /* We launch Main Activity
                         * */
                        Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    } else {

                        /* User exists. We launch Main Activity
                         * */

                        /* We update shared preferences (notifications) according to the information
                         * of the user in firebase. This is the only moment in which notifications
                         * in firebase will affect preferences in the app. From this moment on,
                         * the variations in the preference fragment will affect firebase (and not
                         * the other way around).
                         * */
                        UtilsGeneral.updateSharedPreferences(sharedPref,
                                getResources().getString(R.string.pref_key_notifications),
                                userNotifInfo);

                        /* We remove the listener
                         * */
                        dbRefUsers.removeEventListener(this); /* We remove the listener

                        /* We launch Main Activity
                         * */
                        Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: " + databaseError.getMessage());

                    ToastHelper.toastSomethingWentWrong(AuthChooseLoginActivity.this);

                    UtilsGeneral.showMainContent(progressBarContent, mainContent);

                    /* We remove the listener
                     * */
                    dbRefUsers.removeEventListener(this);

                }
            });

        }
    }

}