package com.example.android.goforlunch.activities.rest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class JoinGroupActivity extends AppCompatActivity {

    private static final String TAG = "JoinGroupActivity";

    private TextInputAutoCompleteTextView inputGroup;

    private Button buttonJoinGroup;

    private String userGroup;

    private Set<String> setOfGroups;
    private String[] arrayOfGroups;

    //Firebase Database
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefGroups;
    private DatabaseReference fireDbRefUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        inputGroup = (TextInputAutoCompleteTextView) findViewById(R.id.join_autocomplete_enter_group_id);
        buttonJoinGroup = (Button) findViewById(R.id.join_button_join_group_id);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(JoinGroupActivity.this);

        userGroup = sharedPref.getString(RepoStrings.SharedPreferences.USER_GROUP,"");

        inputGroup.setText(userGroup);

        fireDb = FirebaseDatabase.getInstance();
        fireDbRefGroups = fireDb.getReference(RepoStrings.FirebaseReference.GROUPS);
        fireDbRefGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                setOfGroups = new TreeSet<>();

                for (DataSnapshot item :
                        dataSnapshot.getChildren()) {

                    setOfGroups.add(item.child(RepoStrings.FirebaseReference.GROUP_NAME).getValue().toString());

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

                if (setOfGroups.contains(inputGroup.getText().toString())) {

                    if (!userGroup.equals(inputGroup.getText().toString())) {

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
                        fireDbRefUsers.child(sharedPreferences.getString(RepoStrings.SharedPreferences.USER_ID_KEY, ""))
                                .child(RepoStrings.FirebaseReference.GROUP)
                                .setValue(inputGroup.getText().toString());

                        ToastHelper.toastShort(JoinGroupActivity.this, "Changes saved");

                    }

                } else {

                    ToastHelper.toastShort(JoinGroupActivity.this, "Dialog: would you like to create a new group?");

                }
            }
        });


    }
}
