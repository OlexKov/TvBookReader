package com.example.bookreader.diffcallbacks;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.PageRow;

public class PageRowDiffCallback extends DiffCallback<Object> {
    @Override
    public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
        if(oldItem instanceof PageRow oldRow && newItem instanceof PageRow newRow ){
           return oldRow.getHeaderItem().getId() == newRow.getHeaderItem().getId();
        }
        return oldItem.equals(newItem);
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
        if(oldItem instanceof PageRow oldRow && newItem instanceof PageRow newRow ){
            return oldRow.getHeaderItem().getName().equals(newRow.getHeaderItem().getName());
        }
        return oldItem.equals(newItem);
    }
}
