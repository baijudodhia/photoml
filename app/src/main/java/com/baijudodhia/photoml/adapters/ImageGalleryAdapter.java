package com.baijudodhia.photoml.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder> {
    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public ImageGalleryAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_imagegallery, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.Adapter_ImageViewItem.getContext())
                .load(mData.get(position))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.Adapter_ImageViewItem);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView Adapter_ImageViewItem;

        ViewHolder(View itemView) {
            super(itemView);
            Adapter_ImageViewItem = itemView.findViewById(R.id.Adapter_ImageViewItem);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}