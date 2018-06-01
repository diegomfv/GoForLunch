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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.repostrings.RepoStrings;

import java.util.Collections;
import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterList extends RecyclerView.Adapter<RVAdapterList.ViewHolder> {

    // TODO: 21/05/2018 Add coworkers joining!
    // TODO: 21/05/2018 Get a different address than "formatted address"

    private static final String TAG = "RVAdapterList";

    private int mShortAnimationDuration;

    private Context mContext;
    private List<RestaurantEntry> listOfRestaurantsByType;
    private List<String> listOfRestaurantsByCoworkers;

    public RVAdapterList(Context context, List<RestaurantEntry> listOfRestaurantsByType, List<String> listOfRestaurantsByCoworkers) {
        this.mContext = context;
        this.listOfRestaurantsByType = listOfRestaurantsByType;
        this.listOfRestaurantsByCoworkers = listOfRestaurantsByCoworkers;
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        Anim.crossFadeShortAnimation(holder.itemView);

        StringBuilder displayedName = new StringBuilder();
        String tokens[] = listOfRestaurantsByType.get(position).getName().split(" ");

        for (int i = 0; i < tokens.length; i++) {
            if (displayedName.length() < 27) {

                /** 1 is the space between words
                 * */
                if ((displayedName.length() + tokens[i].length()) + 1 < 27) {
                    displayedName.append(" ").append(tokens[i]);

                } else {
                    break;
                }
            }
        }

        String transformedName = displayedName.toString().trim();
        holder.title.setText(transformedName);

        holder.address.setText(
                listOfRestaurantsByType.get(position).getType()
                        + " - "
                        +  listOfRestaurantsByType.get(position).getAddress().substring(0, listOfRestaurantsByType.get(position).getAddress().indexOf(",")));

        holder.openUntil.setText(listOfRestaurantsByType.get(position).getOpenUntil());
        holder.distance.setText(listOfRestaurantsByType.get(position).getDistance());
        // TODO: 21/05/2018 Add coworkers joining!

        if (listOfRestaurantsByType.get(position).getRating() != null
            && !listOfRestaurantsByType.get(position).getRating().equals("NotAvailable")) {
            holder.ratingBar.setRating(Float.parseFloat(listOfRestaurantsByType.get(position).getRating()));
        }

        if (listOfRestaurantsByType.get(position).getImageUrl() != null
            && !listOfRestaurantsByType.get(position).getImageUrl().equals("NotAvailable")) {
            Glide.with(mContext)
                    .load(listOfRestaurantsByType.get(position).getImageUrl())
                    .into(holder.photo);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.lunch_image)
                    .into(holder.photo);
        }

        holder.coworkersJoining.setText(String.valueOf(Collections.frequency(listOfRestaurantsByCoworkers, listOfRestaurantsByType.get(position).getName())));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, RestaurantActivity.class);
                intent.putExtra(RepoStrings.SentIntent.PLACE_ID, listOfRestaurantsByType.get(holder.getAdapterPosition()).getPlaceId());
                intent.putExtra(RepoStrings.SentIntent.IMAGE_URL, listOfRestaurantsByType.get(holder.getAdapterPosition()).getImageUrl());
                intent.putExtra(RepoStrings.SentIntent.RESTAURANT_NAME,listOfRestaurantsByType.get(holder.getAdapterPosition()).getName());
                intent.putExtra(RepoStrings.SentIntent.RESTAURANT_TYPE, listOfRestaurantsByType.get(holder.getAdapterPosition()).getType());
                intent.putExtra(RepoStrings.SentIntent.ADDRESS, listOfRestaurantsByType.get(holder.getAdapterPosition()).getAddress());
                intent.putExtra(RepoStrings.SentIntent.RATING, listOfRestaurantsByType.get(holder.getAdapterPosition()).getRating());
                intent.putExtra(RepoStrings.SentIntent.PHONE, listOfRestaurantsByType.get(holder.getAdapterPosition()).getPhone());
                intent.putExtra(RepoStrings.SentIntent.WEBSITE_URL, listOfRestaurantsByType.get(holder.getAdapterPosition()).getWebsiteUrl());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfRestaurantsByType.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView title;
        TextView address;
        TextView openUntil;
        TextView distance;
        TextView coworkersJoining;
        RatingBar ratingBar;
        ImageView photo;


        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.list_cardview_id);
            title = itemView.findViewById(R.id.cv_title_id);
            address = itemView.findViewById(R.id.cv_addressandtype_id);
            openUntil = itemView.findViewById(R.id.cv_timetable_id);
            distance = itemView.findViewById(R.id.cv_distance_id);
            coworkersJoining = itemView.findViewById(R.id.cv_coworkersjoining_id);
            ratingBar = itemView.findViewById(R.id.cv_rating_id);
            photo = itemView.findViewById(R.id.cv_image_restaurant_id);
        }
    }

    // TODO: 21/05/2018 Change this
    // ------------------------- METHODS -------------------------------

}
