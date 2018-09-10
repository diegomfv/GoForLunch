package com.example.android.goforlunch.activities.rest;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.receivers.InternetConnectionReceiver;
import com.example.android.goforlunch.utils.ToastHelper;
import com.example.android.goforlunch.utils.Utils;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.constants.Repo;
import com.example.android.goforlunch.utils.TextInputAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class JoinGroupActivity extends AppCompatActivity implements Observer {

    private static final String TAG = JoinGroupActivity.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Widgets
    @BindView(R.id.join_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.join_button_join_group_id)
    Button buttonJoinGroup;

    @BindView(R.id.join_button_create_group_id)
    Button buttonCreateGroup;

    @BindView(R.id.join_textInputAutocompleteTextView)
    TextInputAutoCompleteTextView textInputAutoCompleteTextView;

    @BindView(R.id.join_main_content)
    LinearLayout mainContent;

    @BindView(R.id.progressBar_content)
    LinearLayout progressBarContent;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Unbinder unbinder;

    private String userKey;
    private String userGroup;

    private List<String> listOfGroups;
    private String[] arrayOfGroups;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;
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

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);
        userKey = sharedPref.getString(Repo.SharedPreferences.USER_ID_KEY, "");
        Log.d(TAG, "onCreate: userKey = " + userKey);

        ////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_join_group);
        unbinder = ButterKnife.bind(this);

        Utils.showMainContent(progressBarContent, mainContent);

        listOfGroups = new ArrayList<>();

        fab.setOnClickListener(fabOnClickListener);
        buttonJoinGroup.setOnClickListener(buttonJoinGroupOnClickListener);
        buttonCreateGroup.setOnClickListener(buttonCreateGroupOnClickListener);

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

        this.fab.setOnClickListener(null);
        this.buttonJoinGroup.setOnClickListener(null);
        this.buttonCreateGroup.setOnClickListener(null);

        this.unbinder.unbind();

    }

    /**
     * Callback: listening to broadcast receiver
     */
    @Override
    public void update(Observable o, Object internetAvailableUpdate) {
        Log.d(TAG, "update: called!");

        if ((int) internetAvailableUpdate == 0) {
            Log.d(TAG, "update: Internet Not Available");

            internetAvailable = false;

            if (snackbar == null) {
                snackbar = Utils.createSnackbar(
                        JoinGroupActivity.this,
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

            /* We get the user's group and
            a list with the rest of groups
             * */
            dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey);
            dbRefUsers.addValueEventListener(valueEventListenerGetUserGroupAndRestOfGroups);

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /***************************
     * LISTENERS ***************
     **************************/

    private View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: fab clicked!");

            startActivity(new Intent(JoinGroupActivity.this, MainActivity.class));
            finish();
        }
    };

    private View.OnClickListener buttonJoinGroupOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: buttonJoinGroup clicked!");

            if (textInputAutoCompleteTextView.getText().toString().equals("")) {
                ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinNotChosenGroup));

            } else if (userGroup.equalsIgnoreCase(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim())) {
                ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinThisCurrentlyYourGroup));

            } else {

                if (!internetAvailable) {
                    ToastHelper.toastNoInternet(JoinGroupActivity.this);

                } else {
                    Log.i(TAG, "onClick: listOfGroups = " + listOfGroups.toString());

                    if (listOfGroups.contains(Utils.capitalize(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim()))) {
                        /* The group exists and we create a dialog to join group
                         * */
                        alertDialogJoinGroup(Utils.capitalize(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim()));

                    } else {
                        /* The group does not exist*/
                        ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinGroupDoesNotExist));

                    }
                }
            }
        }
    };

    private View.OnClickListener buttonCreateGroupOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: buttonCreateGroup clicked!");
            ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.notImplemented));

        }
    };

    /**
     * Value Event Listener: get User Group and All Groups
     */
    private ValueEventListener valueEventListenerGetUserGroupAndRestOfGroups = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            /* We get the user's group and display it
             * */
            userGroup = dataSnapshot.child(Repo.FirebaseReference.USER_GROUP).getValue().toString();
            textInputAutoCompleteTextView.setText(userGroup);

            /* We get a list with all the groups
             * */
            dbRefGroups = fireDb.getReference(Repo.FirebaseReference.GROUPS);
            dbRefGroups.addValueEventListener(valueEventListenerGetAllGroups);

            dbRefUsers.removeEventListener(this);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());
        }

    };

    /**
     * Value Event Listener: get All Groups
     */
    private ValueEventListener valueEventListenerGetAllGroups = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            listOfGroups = UtilsFirebase.fillListWithAllGroups(dataSnapshot);

            /* We create the array that will be used by the autocompleteTextView adapter
             * and pass it as an argument to "configure..."
             * */
            arrayOfGroups = new String[listOfGroups.size()];
            arrayOfGroups = listOfGroups.toArray(arrayOfGroups);

            configureAutocompleteTextView(textInputAutoCompleteTextView, arrayOfGroups);

            dbRefGroups.removeEventListener(this);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getMessage());

        }
    };

    /*******************************
     * CONFIGURATION ***************
     ******************************/

    /**
     * Method that connects a broadcastReceiver to the activity.
     * It allows to notify the user about the internet state
     */
    private void connectBroadcastReceiver() {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Repo.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(JoinGroupActivity.this, receiver, intentFilter, this);

    }

    /**
     * Method that disconnects the broadcastReceiver from the activity.
     */
    private void disconnectBroadcastReceiver() {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    JoinGroupActivity.this,
                    receiver,
                    JoinGroupActivity.this);
        }

        receiver = null;
        intentFilter = null;
        snackbar = null;

    }

    /**
     * Method that configures the autocompleteTextView
     */
    @SuppressLint("CheckResult")
    private void configureAutocompleteTextView(AutoCompleteTextView autoCompleteTextView,
                                               String[] arrayOfGroups) {
        Log.d(TAG, "configureAutocompleteTextView: called!");

        if (autoCompleteTextView != null) {

            ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                    JoinGroupActivity.this,
                    android.R.layout.simple_list_item_1, //This layout has to be a textview
                    arrayOfGroups
            );

            autoCompleteTextView.setAdapter(autocompleteAdapter);
            RxTextView.textChangeEvents(autoCompleteTextView)
                    .skip(2)
                    .debounce(600, TimeUnit.MILLISECONDS)
                    .map(new Function<TextViewTextChangeEvent, String>() {
                        @Override
                        public String apply(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                            return textViewTextChangeEvent.text().toString();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<String>() {
                        @Override
                        public void onNext(String group) {
                            Log.d(TAG, "onNext: group = " + group);

                            /* We only hide the keyboard if the group length (inputted) is long enough
                             * */
                            if (group.length() > 3) {
                                Utils.hideKeyboard(JoinGroupActivity.this);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + Log.getStackTraceString(e));

                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");


                        }
                    });
        }

    }

    /**
     * Method that creates an alert dialog that
     * can be used to delete the Read Articles History
     */
    private void alertDialogJoinGroup(final String group) {
        Log.d(TAG, "alertDialogJoinGroup: called!");

        AlertDialog.Builder builder = new AlertDialog.Builder(JoinGroupActivity.this);
        builder.setMessage(getResources().getString(R.string.joinWouldYouLikeJoin) + " (" + group + ")?")
                .setTitle(getResources().getString(R.string.joinGroupJoinGroup))
                .setPositiveButton(getResources().getString(R.string.joinYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: positive button clicked!");

                        if (!internetAvailable) {
                            ToastHelper.toastNoInternet(JoinGroupActivity.this);

                        } else {

                            Utils.hideMainContent(progressBarContent, mainContent);

                            dbRefGroups = fireDb.getReference(Repo.FirebaseReference.GROUPS);
                            dbRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                    String groupKey = UtilsFirebase.getGroupKeyFromDataSnapshot(dataSnapshot, group);

                                    Map<String, Object> map = new HashMap<>();
                                    map.put(Repo.FirebaseReference.USER_GROUP, group);
                                    map.put(Repo.FirebaseReference.USER_GROUP_KEY, groupKey);

                                    dbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey);
                                    UtilsFirebase.updateInfoWithMapInFirebase(dbRefUsers, map);

                                    ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinNewGroupIs) + " " + group + "!");

                                    /* We take the user to Main Activity
                                     * */
                                    startActivity(new Intent(JoinGroupActivity.this, MainActivity.class));

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled: " + databaseError.getCode());

                                }
                            });
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.joinNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: negative button clicked!");
                        ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.persInfoNotUpdated));
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}