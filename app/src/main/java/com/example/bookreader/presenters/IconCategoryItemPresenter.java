package com.example.bookreader.presenters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.R;
import com.example.bookreader.extentions.IconHeader;

public class IconCategoryItemPresenter extends RowHeaderPresenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.category_menu_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object o) {
            if(o instanceof PageRow){
                HeaderItem headerItem = ((PageRow) o).getHeaderItem();
                View rootView = viewHolder.view;
                rootView.setFocusable(View.FOCUSABLE);
                ImageView iconView = rootView.findViewById(R.id.header_icon);
                if (headerItem instanceof IconHeader) {
                    int iconResId = ((IconHeader) headerItem).iconResId;
                    iconView.setImageResource(iconResId);
                }
                TextView label = rootView.findViewById(R.id.header_label);
                label.setText(headerItem.getName());
            }
     }
}
