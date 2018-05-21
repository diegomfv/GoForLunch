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
import com.example.android.goforlunch.activities.RestaurantActivity;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.helpermethods.Anim;
import com.example.android.goforlunch.pojo_delete.RestaurantObject;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterList extends RecyclerView.Adapter<RVAdapterList.ViewHolder> {

    // TODO: 21/05/2018 Add coworkers joining!
    // TODO: 21/05/2018 Get a different address that "formatted address"

    private static final String TAG = "RVAdapterList";

    private int mShortAnimationDuration;

    private Context mContext;
    private List<RestaurantEntry> listOfObjects;

    public RVAdapterList(Context context, List<RestaurantEntry> listOfObjects) {
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);

        Anim.crossFadeShortAnimation(holder.itemView);

        holder.title.setText(listOfObjects.get(position).getName());
        holder.address.setText(listOfObjects.get(position).getAddress());
        holder.openUntil.setText(listOfObjects.get(position).getOpenUntil());
        holder.distance.setText(listOfObjects.get(position).getDistance());
        // TODO: 21/05/2018 Add coworkers joining!

        if (listOfObjects.get(position).getRating() != null
            && !listOfObjects.get(position).getRating().equals("nA")) {
            holder.ratingBar.setRating(Float.parseFloat(listOfObjects.get(position).getRating()));
        }

        if (!listOfObjects.get(position).getImageUrl().equals("nA")) {
            Glide.with(mContext)
                    .load(listOfObjects.get(position).getImageUrl())
                    .into(holder.photo);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.lunch_image)
                    .into(holder.photo);
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, RestaurantActivity.class);

                intent.putExtra(mContext.getResources().getString(R.string.i_image_url),
                        listOfObjects.get(position).getImageUrl());
                intent.putExtra(mContext.getResources().getString(R.string.i_name),
                        listOfObjects.get(position).getName());
                intent.putExtra(mContext.getResources().getString(R.string.i_address),
                        listOfObjects.get(position).getAddress());
                intent.putExtra(mContext.getResources().getString(R.string.i_rating),
                        listOfObjects.get(position).getRating());
                intent.putExtra(mContext.getResources().getString(R.string.i_phone),
                        listOfObjects.get(position).getPhone());
                intent.putExtra(mContext.getResources().getString(R.string.i_website),
                        listOfObjects.get(position).getWebsiteUrl());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfObjects.size();
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
