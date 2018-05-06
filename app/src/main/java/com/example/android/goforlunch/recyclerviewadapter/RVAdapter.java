package com.example.android.goforlunch.recyclerviewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.pojo.RestaurantObject;

import java.util.List;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private Context mContext;
    private List<RestaurantObject> listOfObjects;

    public RVAdapter(Context context, List<RestaurantObject> listOfObjects) {
        this.mContext = context;
        this.listOfObjects = listOfObjects;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(
                R.layout.list_item,
                parent,
                false);

        RVAdapter.ViewHolder viewHolder = new RVAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }

}
