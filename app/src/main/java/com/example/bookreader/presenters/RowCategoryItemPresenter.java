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

import com.example.bookreader.R;
import com.example.bookreader.extentions.ArrayBookAdapter;
import com.example.bookreader.extentions.IconHeader;

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
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object o) {
         if (o instanceof ListRow listRow) {
            HeaderItem headerItem = listRow.getHeaderItem();
            View rootView = viewHolder.view;
            ImageView iconView = rootView.findViewById(R.id.header_icon);
            if (headerItem instanceof IconHeader iconHeader) {
                iconView.setImageResource(iconHeader.iconResId);
            }
            TextView label = rootView.findViewById(R.id.header_label);
            label.setText(headerItem.getName());

            if(listRow.getAdapter() instanceof ArrayBookAdapter adapter){
                label = rootView.findViewById(R.id.header_book_count);
                label.setText(adapter.getDbElementsCount() != null
                        ? adapter.getDbElementsCount().toString()
                        : "");
            }

        }
    }
}
