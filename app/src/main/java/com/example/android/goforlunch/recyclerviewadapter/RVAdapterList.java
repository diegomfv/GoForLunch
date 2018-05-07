package com.example.android.goforlunch.recyclerviewadapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.RestaurantActivity;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.pojo.RestaurantObject;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterList extends RecyclerView.Adapter<RVAdapterList.ViewHolder> {

    private static final String TAG = "RVAdapterList";

    private int mShortAnimationDuration;

    private Context mContext;
    private List<RestaurantObject> listOfObjects;

    public RVAdapterList(Context context, List<RestaurantObject> listOfObjects) {
        this.mContext = context;
        this.listOfObjects = listOfObjects;
        mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(
                R.layout.list_item_list,
                parent,
                false);

        RVAdapterList.ViewHolder viewHolder = new RVAdapterList.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        Anim.crossFadeShortAnimation(holder.itemView);

        holder.cardView.setOnClickListener(mListener);

    }

    @Override
    public int getItemCount() {
        return 10;
    }





    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.list_cardview_id);

        }

    }




    // ------------------------- METHODS -------------------------------


    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(mContext, RestaurantActivity.class);
            mContext.startActivity(intent);

        }
    };

}
