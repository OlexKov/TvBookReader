package com.example.bookreader.fragments.settings.categorysetting;

import static com.example.bookreader.constants.Constants.ACTION_ID_ICON;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.Nullable;

public class CategoryCreateFragmentActionsStylist extends GuidedActionsStylist {
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
//        else if(action.getId() == ACTION_ID_SAVE || action.getId() == ACTION_ID_CANCEL){
//            var titleView = vh.getTitleView();
//            if(titleView != null){
//                titleView.setAllCaps(true);
//                titleView.setGravity(Gravity.CENTER);
//            }
//        }
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
