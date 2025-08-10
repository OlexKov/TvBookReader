package com.example.bookreader.presenters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.R;
import com.example.bookreader.extentions.ArrayBookAdapter;
import com.example.bookreader.extentions.IconHeader;

import java.util.List;

public class RowCategoryItemPresenter extends RowHeaderPresenter {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.row_category_item, null);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, @NonNull Object o, @NonNull List<Object> payloads) {
        if (o instanceof ListRow listRow) {
            if(!payloads.isEmpty()){
                View rootView = viewHolder.view;
                updateCount(listRow,rootView);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder,  Object o) {
        if (o instanceof ListRow listRow) {
            updateData(listRow,viewHolder);
        }
    }

     private void updateData(ListRow row,Presenter.ViewHolder viewHolder){
         HeaderItem headerItem = row.getHeaderItem();
         View rootView = viewHolder.view;
         updateCount(row,rootView);
         ImageView iconView = rootView.findViewById(R.id.header_icon);
         if (headerItem instanceof IconHeader iconHeader) {
             iconView.setImageResource(iconHeader.iconResId);
         }
         TextView label = rootView.findViewById(R.id.header_label);
         label.setText(headerItem.getName());
     }

     private void updateCount(ListRow row,View view){
         if (row.getAdapter() instanceof ArrayBookAdapter adapter) {
             TextView label = view.findViewById(R.id.header_book_count);
             label.setText(adapter.getDbElementsCount() != null && adapter.getDbElementsCount() != 0
                     ? adapter.getDbElementsCount().toString()
                     : "");
         }
     }

}
