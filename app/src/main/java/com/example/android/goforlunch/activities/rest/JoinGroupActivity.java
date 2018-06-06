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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.example.android.goforlunch.widgets.TextInputAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class JoinGroupActivity extends AppCompatActivity {

    private static final String TAG = "JoinGroupActivity";

    private TextInputAutoCompleteTextView inputGroup;

    private FloatingActionButton fab;
    private Button buttonJoinGroup;
    private Button buttonCreateGroup;
    private String userGroup;
    private String userKey;

    private Set<String> setOfGroups;
    private Map<String,Object> mapOfKeyGroups;
    private String[] arrayOfGroups;

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefGroups;
    private DatabaseReference fireDbRefUsers;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        inputGroup = (TextInputAutoCompleteTextView) findViewById(R.id.join_autocomplete_enter_group_id);
        fab = (FloatingActionButton) findViewById(R.id.join_fab_id);
        buttonJoinGroup = (Button) findViewById(R.id.join_button_join_group_id);
        buttonCreateGroup = (Button) findViewById(R.id.join_button_create_group_id);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);
        userKey = sharedPref.getString(RepoStrings.SharedPreferences.USER_ID_KEY, "");
        userGroup = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP,"");

        Log.d(TAG, "onCreate: userKey = " + userKey);
        Log.d(TAG, "onCreate: userGroup = " + userGroup);

        inputGroup.setText(userGroup);

        setOfGroups = new TreeSet<>();
        mapOfKeyGroups = new HashMap<>();

        fireDb = FirebaseDatabase.getInstance();
        fireDbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
        fireDbRefGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                setOfGroups = new TreeSet();
                mapOfKeyGroups = new HashMap<>();

                /** We fill the mapOfKeyGroups with all the groups from Firebase Database. This
                 * will be used to get the Key of each group.
                 * Each time a group is added, this map will be updated
                 * */
                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    mapOfKeyGroups.put(item.getKey(), Objects.requireNonNull(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue()).toString());

                }

                /** We fill a set with the group names and fill the array that will
                 * be user in the Adapter
                 * */
                for (Map.Entry<String, Object> entry : mapOfKeyGroups.entrySet())
                {
                    setOfGroups.add(entry.getValue().toString());
                }
                arrayOfGroups = new String[setOfGroups.size()];
                arrayOfGroups = setOfGroups.toArray(arrayOfGroups);
                Log.d(TAG, "onDataChange: " + Arrays.toString(arrayOfGroups));

                ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                        JoinGroupActivity.this,
                        android.R.layout.simple_list_item_1,
                        arrayOfGroups
                );

                inputGroup.setAdapter(autocompleteAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });

        inputGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: ITEM CLICKED: " + view.toString());

                Utils.hideKeyboard(JoinGroupActivity.this);

            }
        });


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
                Log.d(TAG, "onClick: setOfGroups = " + setOfGroups.toString());
                Log.d(TAG, "onClick: inputGroup = " + inputGroup.getText().toString());

                if (inputGroup.getText().toString().equalsIgnoreCase("")) {
                    /** If input has no info
                     * */
                    ToastHelper.toastShort(
                            JoinGroupActivity.this,
                            "Please, introduce a group name that already exists or create a new group");

                } else {
                    /** If input has info
                     * */


                    if (setOfGroups.contains(inputGroup.getText().toString())) {
                        Log.d(TAG, "onClick: setOfGroups contains group");
                        /** If the group written in the input exists in the set, it means it exists in the database.
                         * If not, it doesn't*/

                        if (!userGroup.equals(inputGroup.getText().toString())) {
                            Log.d(TAG, "onClick: current user group is different from the inputted");
                            /** The user chose ANOTHER group THAT ALREADY EXISTS but
                             * is not the same as he/she was in
                             * */

                            /** We modify the user group in SharedPreferences and
                             * in Firebase Database
                             * */
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(
                                    RepoStrings.SharedPreferences.USER_GROUP,
                                    inputGroup.getText().toString());
                            editor.apply();

                            fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS);
                            fireDbRefUsers.child(userKey)
                                    .child(RepoStrings.FirebaseReference.GROUP)
                                    .setValue(inputGroup.getText().toString());

                            Log.d(TAG, "onClick: SharedPreferences -> " + sharedPreferences.getAll().toString());

                            /** We get the push key of the new user GROUP and put it in SharedPref
                             * */
                            String userGroupsKey = getKeyFromValueInHashMap(mapOfKeyGroups, userGroup).toString();
                            editor.putString(RepoStrings.SharedPreferences.USER_GROUP_KEY, userGroupsKey);
                            editor.apply();

                            ToastHelper.toastShort(JoinGroupActivity.this, "Changes saved");

                            /** We end the activity
                             * */
                            finish();


                        } else {
                            Log.d(TAG, "onClick: user chose same group");

                            /** The user chose THE SAME GROUP as he/she already was in
                             * */
                            ToastHelper.toastShort(JoinGroupActivity.this, "You chose the same group as you are in already");

                        }


                    } else {

                        /** The group is not in the list so it does not exist
                         * */
                        ToastHelper.toastShort(JoinGroupActivity.this, "The group doesn't exist. You can create it.");

                    }
                }
            }
        });


        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buttonCreateGroup clicked!");

                /** If the group is in the set, it exists in the database
                 * */
                if (setOfGroups.contains(inputGroup.getText().toString())) {
                    Log.d(TAG, "onClick: setOfGroups contains group");

                    ToastHelper.toastShort(JoinGroupActivity.this, "A group with that name already exists!");

                } else if (inputGroup.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: the user did not input anything");

                    ToastHelper.toastShort(JoinGroupActivity.this, "Please, choose a name for the group");


                } else if (inputGroup.getText().toString().length() < 4) {
                    Log.d(TAG, "onClick: the user input a too short group name");

                    ToastHelper.toastShort(JoinGroupActivity.this, "Sorry, the group name is too short");

                } else {
                    Log.d(TAG, "onClick: the groups doesnt exist in the database");

                    // TODO: 02/06/2018 Check if there is internet before calling this method
                    alertDialogCreateNewGroup(inputGroup.getText().toString());

                }
            }
        });

    }

    /** Method that creates an alert dialog that
     * can be used to create a new group
     * */
    private void alertDialogCreateNewGroup (final String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(JoinGroupActivity.this);
        builder.setMessage("Would you like to create a new group called " + groupName + "?")
                .setTitle("Creating a new group")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        /** We create the group in the firebase database
                         * */
                        DatabaseReference fireDbRefNewGroup = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);

                        String newGroupKey = fireDbRefNewGroup.push().getKey();

                        Map<String, Object> map = new HashMap<>();
                        map.put(RepoStrings.FirebaseReference.GROUP_MEMBERS, "");
                        map.put(RepoStrings.FirebaseReference.GROUP_NAME, groupName);
                        map.put(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED, "");

                        fireDbRefNewGroup.child(newGroupKey).setValue(map);

                        /** We change the user's group and user's group key
                         * */
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(RepoStrings.SharedPreferences.USER_GROUP, groupName);
                        editor.putString(RepoStrings.SharedPreferences.USER_GROUP_KEY, newGroupKey);
                        editor.apply();

                        ToastHelper.toastShort(JoinGroupActivity.this, groupName + " has been created!");

                        Log.d(TAG, "onClick: SharedPref = " + sharedPref.getAll().toString());

                        /** We end the activity
                         * */
                        finish();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing happens
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Object getKeyFromValueInHashMap (Map<String, Object> map, String string) {

        Object key = "";

        for (Object o :
                map.keySet()) {

            if (map.get(o).equals(string)) {
                key = o;
            }
        }

        return key;
    }
}
