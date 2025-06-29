package com.example.bookreader.extentions;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.PresenterSelector;

public class StableIdArrayObjectAdapter extends ArrayObjectAdapter {
    public StableIdArrayObjectAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
        setHasStableIds(true);
    }

    @Override
    public long getId(int position) {
        Object item = get(position);
        if (item instanceof ListRow) {
            return ((ListRow) item).getId();
        }
        return super.getId(position);
    }
}
