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
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.utils.Anim;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.constants.RepoStrings;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapterList extends RecyclerView.Adapter<RVAdapterList.ViewHolder> {

    private static final String TAG = RVAdapterList.class.getSimpleName();

    private Context mContext;
    private List<RestaurantEntry> listOfRestaurants;
    private List<String> listOfRestaurantsByCoworkers;
    private RequestManager glide;
    private Storage storage;

    private String mainPath;
    private String imageDirPath;
    private Disposable getImageFromInternalStorageDisposable;

    private int mShortAnimationDuration;

    public RVAdapterList(
            Context context,
            List<RestaurantEntry> listOfRestaurants,
            List<String> listOfRestaurantsByCoworkers,
            RequestManager glide) {

        this.mContext = context;
        this.listOfRestaurants = listOfRestaurants;
        this.listOfRestaurantsByCoworkers = listOfRestaurantsByCoworkers;
        this.glide = glide;
        this.storage = new Storage(mContext);
        this.mainPath = storage.getInternalFilesDirectory() + File.separator;
        this.imageDirPath = mainPath + File.separator + RepoStrings.Directories.IMAGE_DIR + File.separator;
        this.mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        Log.d(TAG, "RVAdapterList: listOfRestaurantsByCoworkers.size() = " + listOfRestaurantsByCoworkers.size());
        Log.d(TAG, "RVAdapterList: " + listOfRestaurantsByCoworkers.toString());

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

        Anim.showCrossFadeShortAnimation(holder.itemView);

        holder.updateItem(position);
    }

    @Override
    public int getItemCount() {
        return listOfRestaurants.size();
    }

    /** Method that retrieves a user in FragmentCoworkers when clicked
     * */
    public RestaurantEntry getRestaurant (int position) {
        return this.listOfRestaurants.get(position);
    }


    /* Take care!!!!!
    * If "ViewHolder class is static", you cannot access listOfRestaurants
    * */
    public class ViewHolder extends RecyclerView.ViewHolder {

       @BindView(R.id.list_cardview_id)
       CardView cardView;

       @BindView(R.id.cv_title_id)
       TextView title;

       @BindView(R.id.cv_addressandtype_id)
       TextView address;

       @BindView(R.id.cv_timetable_id)
       TextView openUntil;

       @BindView(R.id.cv_distance_id)
       TextView distance;

       @BindView(R.id.cv_coworkersjoining_id)
       TextView coworkersJoining;

       @BindView(R.id.cv_rating_id)
       RatingBar ratingBar;

       @BindView(R.id.cv_image_restaurant_id)
       ImageView photo;


       public ViewHolder(View itemView) {
           super(itemView);
           ButterKnife.bind(this, itemView);
       }

       /** Method that updates the info displayed in an item of the recyclerView
        * */
       public void updateItem(int position) {
           Log.d(TAG, "updateItem: called!");
           title.setText(getTitle(position));
           address.setText(getTypeAndAdress(position));
           openUntil.setText(listOfRestaurants.get(position).getOpenUntil());
           distance.setText(listOfRestaurants.get(position).getDistance());
           coworkersJoining.setText(getCoworkersJoining(position));
           ratingBar.setRating(getRating(position));
           loadImage(position, photo);

           if (listOfRestaurants.size() - 1 == position) {
               addBelowMarginToLastItem();
           }

       }

       /** Method that gets the title of the Restaurant
        * */
       private String getTitle (int position) {

           StringBuilder displayedName = new StringBuilder();
           String tokens[] = listOfRestaurants.get(position).getName().split(" ");

           for (int i = 0; i < tokens.length; i++) {
               if (displayedName.length() < 27) {

                   /* 1 is the space between words*/
                   if ((displayedName.length() + tokens[i].length()) + 1 < 27) {
                       displayedName.append(" ").append(tokens[i]);

                   } else {
                       break;
                   }
               }
           }

            return displayedName.toString().trim();
        }

        /** Method that gets the type and address of the restaurant
         */
        private String getTypeAndAdress (int position) {

            return UtilsGeneral.transformTypeToString(mContext,listOfRestaurants.get(position).getType())
                    + " - "
                    +  listOfRestaurants.get(position).getAddress().substring(0, listOfRestaurants.get(position).getAddress().indexOf(","));

        }

        /** Method that gets the rating of the restaurant
         * */
        private Float getRating (int position) {

            if (listOfRestaurants.get(position).getRating() != null
                    && !listOfRestaurants.get(position).getRating().equals(RepoStrings.NOT_AVAILABLE_FOR_STRINGS)) {
                return UtilsGeneral.adaptRating(Float.parseFloat(listOfRestaurants.get(position).getRating()));

            } else {
                return 0f;
            }
        }

        /** Method that tries to load an image using the storage.
         * If there is no file, it tries to load
         * the image with the url
         * */
        private void loadImage (int position, ImageView imageView) {
            Log.d(TAG, "loadImage: called!");

            //if file exists in the directory -> load with storage
            if (storage.isFileExist(
                    imageDirPath + listOfRestaurants.get(position).getPlaceId())) {
                Log.d(TAG, "loadImage: file does exist in the directory");
                getAndDisplayImageFromInternalStorage(listOfRestaurants.get(position).getPlaceId(), imageView);

            } else {
                Log.d(TAG, "loadImage: file does not exist in the directory");
                loadImageWithUrl(listOfRestaurants.get(position).getImageUrl(), imageView);


            }
        }

        /** Method that tries to load an image with a url.
         * If it is null or equal to "", it loads
         * an standard picture
         * */
        private void loadImageWithUrl (String imageUrl, ImageView imageView) {
            Log.d(TAG, "loadImageWithUrl: called!");

            if (imageUrl == null || imageUrl.equals("")) {
                glide.load(R.drawable.lunch_image).into(imageView);

            } else {
                Log.d(TAG, "loadImageWithUrl: TRYING TO LOAD FROM URL!");
                glide.load(imageUrl).into(imageView);

            }
        }

        /** Used to read an image from the internal storage and convert it to bitmap so that
         * it the image can be stored in a RestaurantEntry and be displayed later using glide
         * in the recyclerView
         * */
        private Observable<byte[]> getObservableImageFromInternalStorage (String filePath) {
            return Observable.just(storage.readFile(filePath));
        }

        /** Loads an image using glide. The observable emits the image in a background thread
         * and the image is loaded using glide in the main thread
         * */
        private void getAndDisplayImageFromInternalStorage(String placeId, final ImageView imageView) {
            Log.d(TAG, "getAndDisplayImageFromInternalStorage: called!");

            getImageFromInternalStorageDisposable = getObservableImageFromInternalStorage(imageDirPath + placeId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<byte[]>() {
                        @Override
                        public void onNext(byte[] bytes) {
                            Log.d(TAG, "onNext: loading image from storage!");

                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                            glide.load(bm).into(imageView);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + Log.getStackTraceString(e));

                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");

                        }
                    });
        }

        /** Method that retrieves the coworkers joining a restaurant for lunch
         * */
        private String getCoworkersJoining (int position) {
            Log.d(TAG, "getCoworkersJoining: called!");

            if (listOfRestaurantsByCoworkers != null) {
                Log.d(TAG, "getCoworkersJoining: " + String.valueOf(Collections.frequency(listOfRestaurantsByCoworkers, listOfRestaurants.get(position).getName())));

                return String.valueOf(Collections.frequency(listOfRestaurantsByCoworkers, listOfRestaurants.get(position).getName()));
            } else {
                return "?";
            }
        }

        /** Method that adds below margin to the last cardView
         * */
        private void addBelowMarginToLastItem () {
            Log.d(TAG, "addBelowMarginToLastItem: called!");

            CardView.LayoutParams params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    (int) UtilsGeneral.convertDpToPixel(90, mContext));

            int margin10 = (int) UtilsGeneral.convertDpToPixel(10, mContext);

            params.setMargins(margin10, margin10, margin10, margin10);
            cardView.setLayoutParams(params);

        }

    }
}
