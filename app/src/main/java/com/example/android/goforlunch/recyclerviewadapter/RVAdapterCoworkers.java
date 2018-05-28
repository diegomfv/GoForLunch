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
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.pojo.User;
import com.example.android.goforlunch.strings.RepoStrings;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterCoworkers extends RecyclerView.Adapter<RVAdapterCoworkers.ViewHolder> {

    private static final String TAG = "RVAdapterCoworkers";

    private int mShortAnimationDuration;

    private Context mContext;

    private List<User> listOfCoworkers;

    public RVAdapterCoworkers(Context context, List<User> listOfCoworkers) {
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

        RVAdapterCoworkers.ViewHolder viewHolder = new RVAdapterCoworkers.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);
        Log.d(TAG, "onBindViewHolder: " + listOfCoworkers.get(position));

        holder.textview.setText(listOfCoworkers.get(position).getFirstName()
                + " "
                + listOfCoworkers.get(position).getLastName()
                + " is eating "
                + listOfCoworkers.get(position).getRestaurantType()
                + " ("
                + listOfCoworkers.get(position).getRestaurant()
                + ")"
        );

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: 24/05/2018 Brings the user to the restaurant page

                Intent intent = new Intent(mContext, RestaurantActivity.class);
                intent.putExtra(RepoStrings.SentIntent.PLACE_ID, listOfCoworkers.get(position).getPlaceId());
                intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, listOfCoworkers.get(position).getRestaurantType());

                mContext.startActivity(intent);

            }
        });

        Anim.crossFadeShortAnimation(holder.itemView);

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView textview;

        public ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.coworkers_cardview_id);
            textview = itemView.findViewById(R.id.cv_coworkers_textview_id);

        }
    }


    // ------------------------- METHODS -------------------------------

}
