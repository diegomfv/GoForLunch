package com.example.android.goforlunch.activities.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.MainActivity;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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

import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 31/05/2018.
 */

// TODO: 02/06/2018  --------------------------- VERY IMPORTANT!!!!!! 
// TODO: 02/06/2018 Check that sign in works well when we close the app instead of login off
// TODO: 02/06/2018 Once, it allowed us to start with password account but the current user used gmail account
public class AuthChooseLoginActivity extends AppCompatActivity{

    private static final String TAG = AuthChooseLoginActivity.class.getSimpleName();

    //Google Sign In Request Code
    private static int RC_GOOGLE_SIGN_IN = 101;

    //Widgets
    @BindView(R.id.choose_progressbar_id)
    ProgressBar progressBar;

    @BindView(R.id.choose_sign_in_password_button_id)
    Button buttonPassword;

    @BindView(R.id.choose_textView_register)
    TextView tvRegister;

    @BindView(R.id.choose_google_sign_in_button)
    SignInButton buttonGoogle;

    @BindView(R.id.choose_facebook_sign_in_button)
    LoginButton buttonFacebook;

    private GoogleSignInClient mGoogleSignInClient;

    //For facebook login
    private CallbackManager mCallbackManager;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthChooseLoginActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().apply();

        fireDb = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        // TODO: 29/06/2018 Delete signOut()
        auth.signOut();
        LoginManager.getInstance().logOut();
        user = auth.getCurrentUser();

        Log.d(TAG, "onCreate: SHARED_PREFERENCES = " + sharedPref.getAll().toString());
        Log.d(TAG, "onCreate: user = " + user);

        if (user == null) {
            Log.d(TAG, "onCreate: user is null");
            //We delete Shared preferences info
            Utils.deleteSharedPreferencesInfo(sharedPref);
            // TODO: 13/06/2018 Remove all info in SharedPref


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

        /** We set the content view after checking if the user is logged in. If he/she is, then
         * we start immediately MainActivity without showing this screen
         * */
        setContentView(R.layout.activity_auth_choose_login);
        ButterKnife.bind(this);

        buttonPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Password Button clicked!");

                startActivity(new Intent(AuthChooseLoginActivity.this, AuthSignInEmailPasswordActivity.class));

            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Register textView clicked!");

                Intent intent = new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class);

                //We include a FLAG intent extra (boolean) to notify the next activity we launched the intent from this Activity
                intent.putExtra(RepoStrings.SentIntent.FLAG, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonGoogle.setStyle(SignInButton.SIZE_WIDE,SignInButton.COLOR_DARK);
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Google Button clicked!");

                Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                   @Override
                   public void onNext(Boolean aBoolean) {
                       Log.d(TAG, "onNext: ");

                       if (aBoolean) {
                           googleSignIn();

                       } else {
                           ToastHelper.toastShort(AuthChooseLoginActivity.this, getResources().getString(R.string.noInternet));
                       }

                   }

                   @Override
                   public void onError(Throwable e) {
                       Log.d(TAG, "onError: ");

                   }

                   @Override
                   public void onComplete() {
                       Log.d(TAG, "onComplete: ");

                   }
                });
            }
        });

        // TODO: 16/06/2018 Read https://stackoverflow.com/questions/31327897/custom-facebook-login-button-android
        // TODO: 16/06/2018 For custom button login
        mCallbackManager = CallbackManager.Factory.create();
        buttonFacebook.setReadPermissions("email", "public_profile", "user_friends");
        buttonFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d(TAG, "onSuccess: " + loginResult);

                Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.d(TAG, "onNext: " + aBoolean);

                        if (aBoolean) {
                            handleFacebookAccessToken(loginResult.getAccessToken());

                        } else {
                            // TODO: 02/07/2018 Put sth!

                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");

                    }
                });
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: " + error);

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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

    /** Method for google sign in
     * */
    public void googleSignIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    /** Method for google sign in authentication
     * */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        Utils.showProgressBarAndDisableUserInteraction(AuthChooseLoginActivity.this, progressBar);

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, updateItem UI with the signed-in user's information
                            Log.d(TAG, "GOOGLE signInWithCredential:success");
                            user = auth.getCurrentUser();

                            if (user != null) {
                                Log.d(TAG, "GOOGLE onComplete: user != null");

                                if (user.getDisplayName() == null && user.getDisplayName().equalsIgnoreCase("")) {
                                    Log.d(TAG, "onComplete: GOOGLE user.getDisplayName() == null");
                                    /** If the user has not chosen yet a first name and last name we
                                     * launch AuthEnterNameActivity
                                     * */
                                    Intent intent = new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class);
                                    startActivity(intent);

                                } else {
                                    Log.d(TAG, "onComplete: GOOGLE user.getDisplayName() = " + user.getDisplayName());
                                     /** Two options. The user already exists
                                     * in Firebase Database or not.
                                     * */
                                    fireDb = FirebaseDatabase.getInstance();
                                    DatabaseReference fireDbUsersRef = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                                    fireDbUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, "GOOGLE onDataChange: " + dataSnapshot.toString());

                                            boolean userExists = false;

                                            for (DataSnapshot item :
                                                    dataSnapshot.getChildren()) {

                                                if (Objects.requireNonNull(user.getEmail()).equalsIgnoreCase(
                                                        Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString())) {
                                                    Log.d(TAG, "GOOGLE onDataChange: user already exists");

                                                    // TODO: 02/06/2018 Check this, might be able to be deleted
                                                    userExists = true;

                                                    Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    break;

                                                } else {

                                                }
                                            }

                                            if (!userExists) {
                                                /** If it is not in the foreach loop, then the user does not exist and we have to create him/her
                                                 * */
                                                Log.d(TAG, "GOOGLE onDataChange: user didn't exist");

                                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                                                String userKey = dbRefUsers.push().getKey();

                                                String [] names = Utils.getFirstNameAndLastName(user.getDisplayName());

                                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                                                UtilsFirebase.updateUserInfoInFirebase(dbRefUsers,
                                                        names[0],
                                                        names[1],
                                                        user.getEmail().toLowerCase(),
                                                        "",
                                                        "",
                                                        "",
                                                        "");

                                                dbRefUsers = fireDb.getReference(
                                                        RepoStrings.FirebaseReference.USERS
                                                                + "/" + userKey
                                                                + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                                UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        0,
                                                        "");

                                                Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG, "GOOGLE onCancelled: " + databaseError.getCode());

                                        }
                                    });
                                }
                            }

                        } else {

                            /* If sign in fails, display a message to the user
                            * hide progress bar and enable onClick on views
                            * */
                            Log.w(TAG, "GOOGLE signInWithCredential:failure", task.getException());

                            ToastHelper.toastShort(AuthChooseLoginActivity.this, getResources().getString(R.string.somethingWentWrong));

                            Utils.hideProgressBarAndEnableUserInteraction(AuthChooseLoginActivity.this, progressBar);

                        }

                        // ...
                    }
                });
    }

    /** Method for signing in with facebook
     * */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        Utils.showProgressBarAndDisableUserInteraction(AuthChooseLoginActivity.this, progressBar);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, updateItem UI with the signed-in user's information
                            Log.d(TAG, "FACEBOOK signInWithCredential:success");
                            user = auth.getCurrentUser();

                            if (user != null) {
                                Log.d(TAG, "onComplete: user != null");

                                if (user.getDisplayName() == null && user.getDisplayName().equalsIgnoreCase("")) {
                                    Log.d(TAG, "FACEBOOK onComplete: user.getDisplayName() == null");
                                    /** If the user has not chosen yet a first name and last name we
                                     * launch AuthEnterNameActivity
                                     * */
                                    Intent intent = new Intent(AuthChooseLoginActivity.this, AuthEnterNameActivity.class);
                                    startActivity(intent);

                                } else {
                                    Log.d(TAG, "FACEBOOK onComplete: user.getDisplayName() = " + user.getDisplayName());
                                    /** Two options. The user already exists
                                     * in Firebase Database or not.
                                     * */
                                    fireDb = FirebaseDatabase.getInstance();
                                    DatabaseReference fireDbUsersRef = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                                    fireDbUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, "FACEBOOK onDataChange: " + dataSnapshot.toString());

                                            boolean userExists = false;

                                            for (DataSnapshot item :
                                                    dataSnapshot.getChildren()) {

                                                if (Objects.requireNonNull(user.getEmail()).equalsIgnoreCase(
                                                        Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.USER_EMAIL).getValue()).toString())) {
                                                    Log.d(TAG, "onDataChange: user already exists");

                                                    // TODO: 02/06/2018 Check this, might be able to be deleted
                                                    userExists = true;

                                                    Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    break;

                                                } else {

                                                }
                                            }

                                            if (!userExists) {
                                                /** If it is not in the foreach loop, then the user does not exist and we have to create him/her
                                                 * */
                                                Log.d(TAG, "FACEBOOK onDataChange: user didn't exist");

                                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                                                String userKey = dbRefUsers.push().getKey();

                                                String [] names = Utils.getFirstNameAndLastName(user.getDisplayName());

                                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                                                UtilsFirebase.updateUserInfoInFirebase(dbRefUsers,
                                                        names[0],
                                                        names[1],
                                                        user.getEmail().toLowerCase(),
                                                        "",
                                                        "",
                                                        "",
                                                        "");

                                                dbRefUsers = fireDb.getReference(
                                                        RepoStrings.FirebaseReference.USERS
                                                                + "/" + userKey
                                                                + "/" + RepoStrings.FirebaseReference.USER_RESTAURANT_INFO);
                                                UtilsFirebase.updateRestaurantsUserInfoInFirebase(dbRefUsers,
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        0,
                                                        "");

                                                Intent intent = new Intent(AuthChooseLoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG, "FACEBOOK onCancelled: " + databaseError.getCode());

                                        }
                                    });
                                }
                            }

                        } else {
                            /* If sign in fails, display a message to the user
                             * hide progress bar and enable onClick on views
                             * */
                            Log.w(TAG, "FACEBOOK signInWithCredential:failure", task.getException());

                            ToastHelper.toastShort(AuthChooseLoginActivity.this, getResources().getString(R.string.somethingWentWrong));

                            Utils.hideProgressBarAndEnableUserInteraction(AuthChooseLoginActivity.this, progressBar);

                        }

                        // ...
                    }
                });
    }

}
