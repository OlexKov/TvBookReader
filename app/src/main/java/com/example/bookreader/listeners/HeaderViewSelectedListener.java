package com.example.bookreader.listeners;

import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.customclassses.MainCategoryInfo;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.extentions.CustomTitleView;
import com.example.bookreader.extentions.IconHeader;

public class HeaderViewSelectedListener implements HeadersSupportFragment.OnHeaderViewSelectedListener {
    private final BrowseSupportFragment fragment;
    private final BookReaderApp app = BookReaderApp.getInstance();
    public HeaderViewSelectedListener(BrowseSupportFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
        if (row != null && row.getHeaderItem() != null) {
            if(!(row.getHeaderItem() instanceof  IconHeader iconHeader)) return;
            String title = row.getHeaderItem().getName();
            fragment.setTitle(title);
            View titleView  = fragment.getTitleView();
            if(titleView instanceof CustomTitleView customTitleView) {
                customTitleView.setTitleIcon(ContextCompat.getDrawable(titleView.getContext(), iconHeader.iconResId));
            }
            Object adapterObj = fragment.getAdapter();
            if (adapterObj instanceof ArrayObjectAdapter adapter) {
                if(!title.equals(app.getSelectedMainCategoryInfo().getName())){
                    var categoryInfo = new MainCategoryInfo(adapter.indexOf(row),row.getId(),iconHeader.getName(),iconHeader.iconResId);
                    app.setSelectedMainCategoryInfo(categoryInfo);
                    app.getGlobalEventListener().sendEvent(GlobalEventType.CATEGORY_SELECTION_CHANGED,categoryInfo);
                }
                int position = adapter.indexOf(row);
                if (position >= 0) {
                    fragment.setSelectedPosition(position, true);
                }
            }
        }
    }
}
