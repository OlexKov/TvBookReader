package com.example.bookreader.fragments.reader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.utility.AnimHelper;

import java.util.List;

import lombok.Setter;

public class BookPageAdapter extends RecyclerView.Adapter<BookPageAdapter.PageViewHolder> {

    @Setter
    private  List<Bitmap> pages;
    private float currentScale = 1f;
    float screenWidth ;
    float screenHeight ;

    public BookPageAdapter(List<Bitmap> pages,float scale) {
        this.pages = pages;
        currentScale = scale;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PageViewHolder(ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) parent.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(0,0,0, AnimHelper.convertToPx(parent.getContext(),5));
        return new PageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.imageView.setImageBitmap(pages.get(position));
        holder.imageView.setLayoutParams(new ViewGroup.LayoutParams(
                (int)(screenWidth ),
                (int)(screenHeight * currentScale)
        ));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setScale(float scale){
        currentScale = scale;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }


}
