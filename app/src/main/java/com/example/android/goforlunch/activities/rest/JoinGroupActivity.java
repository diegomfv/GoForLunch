package com.example.android.goforlunch.activities.rest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

    private Button buttonJoinGroup;

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
        buttonJoinGroup = (Button) findViewById(R.id.join_button_join_group_id);

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

        buttonJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buttonJoinGroup called!");
                Log.d(TAG, "onClick: setOfGroups = " + setOfGroups.toString());
                Log.d(TAG, "onClick: inputGroup = " + inputGroup.getText().toString());

                /** If input has no info
                 * */
                if (inputGroup.getText().toString().equalsIgnoreCase("")) {

                    ToastHelper.toastShort(
                            JoinGroupActivity.this,
                            "Please, introduce a group name that already exists or a new one");


                /** If input has info
                 * */
                } else {

                    /** If the group in the input exists in the set, it means it exists in the database.
                     * If not, it doesn't*/
                    if (setOfGroups.contains(inputGroup.getText().toString())) {
                        Log.d(TAG, "onClick: setOfGroups contains group");


                        /** The user chose ANOTHER group THAT ALREADY EXISTS but
                         * is not the same as he/she was in
                         * */
                        if (!userGroup.equals(inputGroup.getText().toString())) {
                            Log.d(TAG, "onClick: current user group is different from the inputted");


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

                            ToastHelper.toastShort(JoinGroupActivity.this, "Changes saved");

                            Log.d(TAG, "onClick: SharedPreferences -> " + sharedPreferences.getAll().toString());

                            /** We get the push key of the new user GROUP and put it in SharedPref
                             * */
                            String userGroupsKey = getKeyFromValueInHashMap(mapOfKeyGroups, userGroup).toString();
                            editor.putString(RepoStrings.SharedPreferences.USER_GROUP_KEY, userGroupsKey);
                            editor.apply();

                            /** We end the activity
                             * */
                            finish();


                            /** The user chose THE SAME GROUP as he/she already was in
                             * */
                        } else {
                            Log.d(TAG, "onClick: user chose same group");
                            ToastHelper.toastShort(JoinGroupActivity.this, "You chose the same group as you are in already");

                        }


                        /** The group is not in the list so we open an alert dialog to check if there is internet
                         * */
                    } else {

                        // TODO: 02/06/2018 Check if there is internet before calling this method
                        alertDialogCreateNewGroup(inputGroup.getText().toString());

                    }
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

                        Map<String, Object> map = new HashMap<>();
                        map.put(RepoStrings.FirebaseReference.GROUP_MEMBERS, "");
                        map.put(RepoStrings.FirebaseReference.GROUP_NAME, groupName);
                        map.put(RepoStrings.FirebaseReference.GROUP_RESTAURANTS_VISITED, "");

                        fireDbRefNewGroup.push().setValue(map);

                        /** We change the user group
                         * */
                        fireDbRefUsers = fireDb.getReference(RepoStrings.FirebaseReference.USERS + "/" + userKey);
                        fireDbRefUsers.child(RepoStrings.FirebaseReference.GROUP).setValue(groupName);

                        ToastHelper.toastShort(JoinGroupActivity.this, groupName + " has been created!");

                        /** We change the user group in SharedPreferences
                         * */
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(RepoStrings.SharedPreferences.USER_GROUP, groupName);
                        editor.apply();

                        /** We get the push key of the new user GROUP and put it in SharedPref
                         * */
                        String userGroupsKey = getKeyFromValueInHashMap(mapOfKeyGroups, userGroup).toString();
                        editor.putString(RepoStrings.SharedPreferences.USER_GROUP_KEY, userGroupsKey);
                        editor.apply();

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
