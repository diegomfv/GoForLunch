package com.example.android.goforlunch.recyclerviewadapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;
import com.example.android.goforlunch.helpermethods.UtilsFirebase;
import com.example.android.goforlunch.repostrings.RepoStrings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Diego Fajardo on 13/06/2018.
 */
public class RVJoinGroup extends RecyclerView.Adapter<RVJoinGroup.ViewHolder> {

    private static final String TAG = "RVJoinGroup";

    private Context mContext;
    private List<String> listOfGroups;
    private String userKey;
    private String userGroup;

    private FirebaseDatabase fireDb;
    private DatabaseReference dbRefUsers;
    private DatabaseReference dbRefGroups;

    private int mShortAnimationDuration;

    public RVJoinGroup(Context context, List<String> listOfGroups, String userKey, String userGroup) {
        this.mContext = context;
        this.listOfGroups = listOfGroups;
        this.userKey = userKey;
        this.userGroup = userGroup;
        fireDb = FirebaseDatabase.getInstance();
        this.mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(
                R.layout.list_item_join_group,
                parent,
                false);

        RVJoinGroup.ViewHolder viewHolder = new RVJoinGroup.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        if (listOfGroups.size() == 0) {
            holder.tv_text.setText("There are no groups at the moment!");

        } else {
            holder.tv_text.setText(listOfGroups.get(position));
            holder.tv_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (holder.tv_text.getText().toString().equalsIgnoreCase(userGroup)) {
                        ToastHelper.toastShort(mContext, "This is already your group");

                    } else {
                        alertDialogJoinGroup(holder.tv_text.getText().toString());

                    }
                }
            });
        }

        Anim.crossFadeShortAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {

        if (listOfGroups.size() == 0){
            return 1;
        } else {
            return listOfGroups.size();
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_text;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_text = itemView.findViewById(R.id.list_join_group_tv_id);

        }
    }

    // ------------------------- METHODS -------------------------------

    /** Method that creates an alert dialog that
     * can be used to delete the Read Articles History
     * */
    private void alertDialogJoinGroup (final String group) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Would you like to join this group (" + group + ")?")
                .setTitle("Join the group")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
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
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing happens
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



}