package com.example.android.goforlunch.recyclerviewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.Anim;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterRestaurant extends RecyclerView.Adapter<RVAdapterRestaurant.ViewHolder> {

    private static final String TAG = "RVAdapterRestaurant";

    private Context mContext;

    private List<String> listOfCoworkers;

    private int mShortAnimationDuration;

    public RVAdapterRestaurant(Context context, List<String> listOfCoworkers) {
        this.mContext = context;
        this.listOfCoworkers = listOfCoworkers;
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

        RVAdapterRestaurant.ViewHolder viewHolder = new RVAdapterRestaurant.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        holder.tv_text.setText(listOfCoworkers.get(position) + " is joining!");

        Anim.crossFadeShortAnimation(holder.itemView);

    }

    @Override
    public int getItemCount() {
        return listOfCoworkers.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_text;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_text = itemView.findViewById(R.id.cv_coworkers_textview_id);

        }
    }

    // ------------------------- METHODS -------------------------------

}
