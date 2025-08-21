package com.example.bookreader.fragments.settings.categorysetting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.R;

import org.jspecify.annotations.Nullable;

public class IconActionsStylist extends GuidedActionsStylist {
    private static final int ACTION_ID_ICON = 111111112;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, @NonNull GuidedAction action) {
        super.onBindViewHolder(vh, action);
        if(action.getId() == ACTION_ID_ICON){
            var titleView = vh.getTitleView();
            if(titleView != null){
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f);
                titleView.setPadding(10,0,0,0);
                titleView.setTextColor(Color.GRAY);
            }
        }
    }


    @Override
    public void onUpdateExpandedViewHolder(@Nullable ViewHolder avh) {
        super.onUpdateExpandedViewHolder(avh);
        if (avh == null) return;

        VerticalGridView subGrid = getSubActionsGridView();
        if (subGrid == null) return;

        int position = avh.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        if (position == 1) {
            subGrid.setNumColumns(4);
        } else {
            subGrid.setNumColumns(1);
        }
    }

}
