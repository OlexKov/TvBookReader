package com.example.bookreader.presenters;

import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.Presenter;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bookreader.customclassses.EmptyItem;
import com.example.bookreader.utility.AnimHelper;

import org.jspecify.annotations.NonNull;

public class EmptyItemPresenter extends Presenter {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(new View(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
       if(item instanceof EmptyItem emptyItem){
           View rootView = viewHolder.view;
           rootView.setLayoutParams(new GridLayoutManager.LayoutParams(
                   ViewGroup.LayoutParams.MATCH_PARENT,
                   AnimHelper.convertToPx(rootView.getContext(),emptyItem.getHeight())));

       }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }


}
