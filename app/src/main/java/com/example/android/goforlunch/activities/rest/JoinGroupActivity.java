package com.example.android.goforlunch.activities.rest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repository.RepoStrings;
import com.example.android.goforlunch.widgets.TextInputAutoCompleteTextView;
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
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class JoinGroupActivity extends AppCompatActivity {

    private static final String TAG = JoinGroupActivity.class.getSimpleName();

    @BindView(R.id.join_fab_id)
    FloatingActionButton fab;

    @BindView(R.id.join_button_join_group_id)
    Button buttonJoinGroup;

    @BindView(R.id.join_button_create_group_id)
    Button buttonCreateGroup;

    @BindView(R.id.join_textInputAutocompleteTextView)
    TextInputAutoCompleteTextView textInputAutoCompleteTextView;

    private String userKey;
    private String userGroup;

    private List<String> listOfGroups;
    private String[] arrayOfGroups;

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;
    private SharedPreferences sharedPref;

    //Disposable
    private Disposable disposable;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        ButterKnife.bind(this);

        fireDb = FirebaseDatabase.getInstance();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");

        Log.d(TAG, "onCreate: userKey = " + userKey);

        listOfGroups = new ArrayList<>();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: fab clicked!");

                startActivity(new Intent(JoinGroupActivity.this, MainActivity.class));
                finish();
            }
        });

        buttonJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buttonJoinGroup clicked!");

                if (textInputAutoCompleteTextView.equals("")) {
                    ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinNotChosenGroup));

                } else if (userGroup.equalsIgnoreCase(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim())) {
                    ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinThisCurrentlyYourGroup));

                } else {

                    Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            Log.d(TAG, "onNext: " + aBoolean);

                            if (listOfGroups.contains(Utils.capitalize(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim()))) {

                                if (aBoolean) {
                                    Log.d(TAG, "onNext: " + aBoolean);

                                    /* We create a dialog to join group
                                     * */
                                    alertDialogJoinGroup(textInputAutoCompleteTextView.getText().toString().toLowerCase().trim());

                                } else  {
                                    Log.d(TAG, "onNext: " + aBoolean);

                                }
                            } else {
                                ToastHelper.toastShort(JoinGroupActivity.this, getResources().getString(R.string.joinGroupsDoesNotExist));
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
            }
        });

        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buttonCreateGroup clicked!");

                ToastHelper.toastShort(JoinGroupActivity.this, "Not implemented!");

            }
        });

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called!");
        super.onStart();

        Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.d(TAG, "onNext: " + aBoolean);

                if (aBoolean) {

                    /** We get the user's group and a list with
                     * the rest of groups
                     * */
                    dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                    dbRefUsers.addValueEventListener(valueEventListenerGetUserGroupAndRestOfGroups);

                } else {
                    UtilsFirebase.logOut(JoinGroupActivity.this);

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

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called!");
        super.onStop();
        dbRefUsers.removeEventListener(valueEventListenerGetUserGroupAndRestOfGroups);
        dbRefGroups.removeEventListener(valueEventListenerGetAllGroups);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called!");
        super.onDestroy();
        if (null != disposable) {
            disposable.dispose();
        }
    }

    /***************************
     * VALUE EVENT LISTENERS ***
     **************************/

    /** Value Event Listener: get User Group and All Groups
     * */
    private ValueEventListener valueEventListenerGetUserGroupAndRestOfGroups = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

            /* We get the user's group and display it
             * */
            userGroup = dataSnapshot.child(RepoStrings.FirebaseReference.USER_GROUP).getValue().toString();
            textInputAutoCompleteTextView.setText(userGroup);

            /* We get a list with all the groups
             * */
            dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
            dbRefGroups.addValueEventListener(valueEventListenerGetAllGroups);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getCode());
        }

    };

    /** Value Event Listener: get All Groups
     * */
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

            configureAutocompleteTextView(textInputAutoCompleteTextView, disposable, arrayOfGroups);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError.getMessage());

        }
    };

    /*************************
     * CONFIGURATION *********
     ************************/

    /** Method that configures the autocompleteTextView
     * */
    private void configureAutocompleteTextView (AutoCompleteTextView autoCompleteTextView,
                                                Disposable disposable,
                                                String[] arrayOfGroups) {
        Log.d(TAG, "configureAutocompleteTextView: called!");

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                JoinGroupActivity.this,
                android.R.layout.simple_list_item_1, //This layout has to be a textview
                arrayOfGroups
        );

        autoCompleteTextView.setAdapter(autocompleteAdapter);
        disposable = RxTextView.textChangeEvents(autoCompleteTextView)
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


    /** Method that creates an alert dialog that
     * can be used to delete the Read Articles History
     * */
    private void alertDialogJoinGroup (final String group) {

        AlertDialog.Builder builder = new AlertDialog.Builder(JoinGroupActivity.this);
        builder.setMessage(getResources().getString(R.string.joinWouldYouLikeJoin) + group + ")?")
                .setTitle(getResources().getString(R.string.joinGroupJoinGroup))
                .setPositiveButton(getResources().getString(R.string.joinYes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
                        dbRefGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                String groupKey = UtilsFirebase.getGroupKeyFromDataSnapshot(dataSnapshot, group);

                                Map<String,Object> map = new HashMap<>();
                                map.put(RepoStrings.FirebaseReference.USER_GROUP, group);
                                map.put(RepoStrings.FirebaseReference.USER_GROUP_KEY, groupKey);

                                dbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                                UtilsFirebase.updateInfoWithMapInFirebase(dbRefUsers, map);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: " + databaseError.getCode());

                            }
                        });

                    }
                })
                .setNegativeButton(getResources().getString(R.string.joinNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing happens
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
