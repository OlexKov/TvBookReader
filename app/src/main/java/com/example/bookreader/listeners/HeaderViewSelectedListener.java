package com.example.bookreader.listeners;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.GlobalEventType;

public class HeaderViewSelectedListener implements HeadersSupportFragment.OnHeaderViewSelectedListener {
    private final BrowseSupportFragment fragment;
    private final BookReaderApp app = BookReaderApp.getInstance();
    public HeaderViewSelectedListener(BrowseSupportFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
        if (row != null && row.getHeaderItem() != null) {
            String title = row.getHeaderItem().getName();
            fragment.setTitle(title);
            if(!title.equals(app.getCurrentCategory())){
                app.getGlobalEventListener().sendEvent(GlobalEventType.CATEGORY_CHANGED,title);
                app.setCurrentCategory(title);
            }
            Object adapterObj = fragment.getAdapter();
            if (adapterObj instanceof ArrayObjectAdapter) {
                ArrayObjectAdapter adapter = (ArrayObjectAdapter) adapterObj;
                int position = adapter.indexOf(row);
                if (position >= 0) {
                    fragment.setSelectedPosition(position, true);
                }
            }
        }
    }
}
