package com.example.bookreader.fragments.reader;

import static com.example.bookreader.constants.Constants.READER_PAGE_ASPECT_RATIO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.customclassses.PagePreview;
import com.example.bookreader.utility.AnimHelper;

import java.util.List;

import lombok.Setter;

public class BookPageAdapter extends RecyclerView.Adapter<BookPageAdapter.PageViewHolder> {

    @Setter
    private  List<PagePreview> pages;
    private float currentScale = 1f;
    private float screenWidth ;
    private float screenHeight ;
    private Context context;

    public BookPageAdapter(List<PagePreview> pages,float scale) {
        this.pages = pages;
        currentScale = scale;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return pages.get(position).pageIndex; // або pageIndex
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
        context = parent.getContext();
        ImageView imageView = new ImageView(context);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) parent.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        updateLayoutParams(imageView);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // Додаємо світну рамку
        GradientDrawable glowBorder = new GradientDrawable();
        glowBorder.setShape(GradientDrawable.RECTANGLE);
        glowBorder.setStroke(1, Color.DKGRAY);
        glowBorder.setColor(Color.TRANSPARENT);
        imageView.setForeground(glowBorder);

        return new PageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.imageView.setImageBitmap(pages.get(position).preview);
        updateLayoutParams(holder.imageView);
    }

    public void setScale(float scale){
        currentScale = scale;
        notifyItemRangeChanged(0,pages.size());
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    private void updateLayoutParams(ImageView imageView){
        float baseWidth = screenHeight * READER_PAGE_ASPECT_RATIO;
        int finalWidth = (int)(baseWidth * currentScale);
        int finalHeight = (int)(screenHeight * currentScale);
        int marginLeft = (int)((screenWidth - finalWidth) / 2);
        var params = new RecyclerView.LayoutParams(
                finalWidth,
                finalHeight
        );
        params.setMargins(marginLeft,0,0,AnimHelper.convertToPx(context,5));
        imageView.setLayoutParams(params);
    }
}
