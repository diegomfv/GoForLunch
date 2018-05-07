package com.example.android.goforlunch.recyclerviewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterCoworkers extends RecyclerView.Adapter<RVAdapterCoworkers.ViewHolder> {

    private static final String TAG = "RVAdapterCoworkers";

    private int mShortAnimationDuration;

    private Context mContext;

    public RVAdapterCoworkers(Context context) {
        this.mContext = context;
        this.mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(
                R.layout.list_item_coworkers,
                parent,
                false);

        RVAdapterCoworkers.ViewHolder viewHolder = new RVAdapterCoworkers.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        Anim.crossFadeShortAnimation(holder.itemView);

    }

    @Override
    public int getItemCount() {
        return 10;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);

        }
    }


    // ------------------------- METHODS -------------------------------

}
