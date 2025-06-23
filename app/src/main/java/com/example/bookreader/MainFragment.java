package com.example.bookreader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DividerRow;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.OnChildSelectedListener;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;

public class MainFragment extends BrowseSupportFragment {


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setTitle("Категорія 1");
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.default_background));

        setupRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
        HeadersSupportFragment supportFragment = getHeadersSupportFragment();
        if(supportFragment != null)
        {
            supportFragment.setOnHeaderViewSelectedListener(new HeadersSupportFragment.OnHeaderViewSelectedListener() {
                @Override
                public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
                    if (row != null && row.getHeaderItem() != null) {
                        String title = row.getHeaderItem().getName();
                        setTitle(title);
                        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getAdapter();
                        int position = adapter.indexOf(row);
                        if (position >= 0) {
                            setSelectedPosition(position, true);
                        }
                    }
                }
            });
        }

    }

    private void setupRows() {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new ListRowPresenter());

        // Використовуємо PageRow замість ListRow
        adapter.add(new PageRow(new HeaderItem(0, "Категорія 1")));
        adapter.add(new PageRow(new HeaderItem(1, "Категорія 2")));
        adapter.add(new PageRow(new HeaderItem(2, "Категорія 3")));

        setAdapter(adapter);
    }
}