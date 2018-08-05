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
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.Utils;
import com.example.android.goforlunch.network.models.pojo.User;
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

public class RVAdapterCoworkers extends RecyclerView.Adapter<RVAdapterCoworkers.ViewHolder> {

    private static final String TAG = RVAdapterCoworkers.class.getSimpleName();

    private Context mContext;

    private List<User> listOfCoworkers;

    //Firebase Storage
    private FirebaseStorage fireStorage;
    private StorageReference stRef;
    private StorageReference stRefImages;
    private StorageReference stRefUserImage;

    private int mShortAnimationDuration;

    //Glide
    private RequestManager glide;

    public RVAdapterCoworkers(Context context, List<User> listOfCoworkers) {
        this.mContext = context;
        this.listOfCoworkers = listOfCoworkers;

        this.fireStorage = FirebaseStorage.getInstance();
        this.stRef = fireStorage.getReference();
        this.stRefImages = stRef.child("imageDir");

        this.glide = Glide.with(context);

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

        Anim.showCrossFadeShortAnimation(holder.itemView);

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

        /** Method that updates the info displayed in an item of the recyclerView
         * */
        private void updateItem(int position) {
            Log.d(TAG, "updateItem: called!");

            textView.setText(setInfo(position));
            textView.setTextColor(setColor(position));
            getUserImage(position);

            if (listOfCoworkers.size() - 1 == position) {
                addBelowMarginToLastItem();
            }

        }

        //////////////////////

        /** Method to fill the textView
         * */
        private String setInfo(int position) {
            Log.d(TAG, "setInfo: called!");

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
                                listOfCoworkers.get(position).getLastName() + '\n',
                                Utils.transformTypeAsIntToString(listOfCoworkers.get(position).getRestaurantType()),
                                listOfCoworkers.get(position).getRestaurantName());

            }
        }

        /** Method that changes the color of the textView depending on
         * if the user has chosen a restaurant or not
         * */
        private int setColor (int position) {
            Log.d(TAG, "setColor: called!");

            if (listOfCoworkers.get(position).getRestaurantName() == null
                    || listOfCoworkers.get(position).getRestaurantName().equals("")) {
                return mContext.getResources().getColor(android.R.color.darker_gray);

            } else {
                return mContext.getResources().getColor(android.R.color.black);

            }
        }

        /** Method that loads the user image into the imageView
         * */
        private void getUserImage (int position) {
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

        /** Method that adds below margin to the last cardView
         * */
        private void addBelowMarginToLastItem () {
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
