package com.example.bookreader.extentions;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

public class StableIdArrayObjectAdapter extends ArrayObjectAdapter {
    public StableIdArrayObjectAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
        setHasStableIds(true);
    }
    public StableIdArrayObjectAdapter(Presenter presenter) {
        super(presenter);
        setHasStableIds(true);
    }

    @Override
    public long getId(int position) {
        if (get(position) instanceof ListRow item) {
            return item.getId();
        }
        return super.getId(position);
    }
}
