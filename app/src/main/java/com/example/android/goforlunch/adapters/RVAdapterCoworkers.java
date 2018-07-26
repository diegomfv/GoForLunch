package com.example.android.goforlunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.models.pojo.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: position# " + position);
        Log.d(TAG, "onBindViewHolder: " + listOfCoworkers.get(position));

        Log.d(TAG, "onBindViewHolder: " + listOfCoworkers.get(holder.getAdapterPosition()).getPlaceId());
        Log.d(TAG, "onBindViewHolder: " + listOfCoworkers.get(holder.getAdapterPosition()).getRestaurantName());
        Log.d(TAG, "onBindViewHolder: " + listOfCoworkers.get(holder.getAdapterPosition()).getRating());

        holder.updateItem(position);

        Anim.crossFadeShortAnimation(holder.itemView);

    }

    @Override
    public int getItemCount() {
        return listOfCoworkers.size();
    }


    /** Method that retrieves a user in FragmentCoworkers when clicked
     * */
    public User getUser (int position) {
        return this.listOfCoworkers.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_coworkers_textview_id)
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        /** Method that updates the info displayed in an item of the recyclerView
         * */
        public void updateItem(int position) {
            Log.d(TAG, "updateItem: called!");

            textView.setText(setInfo(position));
            textView.setTextColor(setColor(position));

        }

        public String setInfo(int position) {

            if (listOfCoworkers.get(position).getRestaurantName() == null
                    || listOfCoworkers.get(position).getRestaurantName().equals("")) {

                return mContext.getResources()
                        .getString(
                                R.string.avCowHasNotDecided,
                                listOfCoworkers.get(position).getFirstName(),
                                listOfCoworkers.get(position).getLastName());

            } else {

                return mContext.getResources()
                        .getString(R.string.avCowHasDecided,
                                listOfCoworkers.get(position).getFirstName(),
                                listOfCoworkers.get(position).getLastName(),
                                UtilsGeneral.transformTypeToString(mContext, listOfCoworkers.get(position).getRestaurantType()),
                                listOfCoworkers.get(position).getRestaurantName());

            }
        }

        private int setColor (int position) {

            if (listOfCoworkers.get(position).getRestaurantName() == null
                    || listOfCoworkers.get(position).getRestaurantName().equals("")) {
                return mContext.getResources().getColor(android.R.color.darker_gray);

            } else {
                return mContext.getResources().getColor(android.R.color.black);

            }
        }
    }




    // ------------------------- METHODS -------------------------------

}
