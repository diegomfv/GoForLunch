package com.example.android.goforlunch.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.network.models.pojo.User;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterRestaurant extends RecyclerView.Adapter<RVAdapterRestaurant.ViewHolder> {

    private static final String TAG = "RVAdapterRestaurant";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Context mContext;

    private List<User> listOfCoworkers;

    private int mShortAnimationDuration;

    //Firebase Storage
    private FirebaseStorage fireStorage;
    private StorageReference stRef;
    private StorageReference stRefImages;
    private StorageReference stRefUserImage;

    //Glide
    private RequestManager glide;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public RVAdapterRestaurant(Context context, List<User> listOfCoworkers, RequestManager glide) {
        this.mContext = context;
        this.listOfCoworkers = listOfCoworkers;

        this.fireStorage = FirebaseStorage.getInstance();
        this.stRef = fireStorage.getReference();
        this.stRefImages = stRef.child("imageDir");

        this.glide = glide;

        this.mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

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

        holder.updateItem(position);

        Anim.showCrossFadeShortAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {

        if (listOfCoworkers.size() == 0) {
            return 1;
        } else {
            return listOfCoworkers.size();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.coworkers_cardview_id)
        CardView cardView;

        @BindView(R.id.cv_coworkers_textview_id)
        TextView textView;

        @BindView(R.id.cv_coworkers_image_id)
        ImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        /**
         * Method that updates the info displayed in an item of the recyclerView
         */
        private void updateItem(int position) {
            Log.d(TAG, "updateItem: called!");

            if (listOfCoworkers.size() == 0) {

            } else {

                setInfo(position);
                getUserImage(position);

                if (listOfCoworkers.size() - 1 == position) {
                    addBelowMarginToLastItem();
                }
            }

        }

        /**
         * Method to fill the textView
         */
        private void setInfo(int position) {
            Log.d(TAG, "setText: called!");

            if (listOfCoworkers.size() == 0) {
                textView.setText(mContext.getResources().getString(R.string.restaurantNoCoworkersJoining));

            } else {

                textView.setText(mContext.getResources().getString(
                        R.string.restaurantUserIsJoining,
                        listOfCoworkers.get(position).getFirstName() + " " + listOfCoworkers.get(position).getLastName()));
                textView.setTextColor(mContext.getResources().getColor(android.R.color.black));
            }

        }

        /**
         * Method that loads the user image into the imageView
         */
        private void getUserImage(int position) {
            Log.d(TAG, "getUserImage: called!");

            stRefUserImage = stRefImages.child(listOfCoworkers.get(position).getEmail()).child("image");

            final long ONE_MEGABYTE = 1024 * 1024;
            stRefUserImage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Log.d(TAG, "onSuccess: called!");

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    glide.load(bitmap)
                            .into(userImage);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "onFailure: " + exception);

                    glide.load(mContext.getResources().getDrawable(R.drawable.picture_not_available))
                            .into(userImage);


                }
            });
        }

        /**
         * Method that adds below margin to the last cardView
         */
        private void addBelowMarginToLastItem() {
            Log.d(TAG, "addBelowMarginToLastItem: called!");

            CardView.LayoutParams params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    (int) Utils.convertDpToPixel(90, mContext));

            int margin10 = (int) Utils.convertDpToPixel(10, mContext);

            params.setMargins(margin10, margin10, margin10, margin10);
            cardView.setLayoutParams(params);

        }

    }

}
