package com.example.android.goforlunch.activities.auth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.helpermethods.Anim;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterRestaurantDELETE extends RecyclerView.Adapter<RVAdapterRestaurantDELETE.ViewHolder> {

    private static final String TAG = "RVAdapterRestaurant";

    private int mShortAnimationDuration;

    private Context mContext;

    private List<RestaurantEntry> list;

    public RVAdapterRestaurantDELETE(Context context, List<RestaurantEntry> list) {
        this.mContext = context;
        this.list = list;
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

        RVAdapterRestaurantDELETE.ViewHolder viewHolder = new RVAdapterRestaurantDELETE.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        holder.tv_trial.setText(list.get(position).getName());

        Anim.crossFadeShortAnimation(holder.itemView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_trial;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_trial = itemView.findViewById(R.id.cv_coworkers_textview_id);

        }
    }




    // ------------------------- METHODS -------------------------------

}
