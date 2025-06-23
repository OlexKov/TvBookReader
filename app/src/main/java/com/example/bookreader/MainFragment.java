package com.example.bookreader;

import android.os.Bundle;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRowPresenter;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.data.database.repository.CategoryRepository;

public class MainFragment extends BrowseSupportFragment {


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CategoryRepository repo = new CategoryRepository();
        repo.getCategoryByIdAsync(1,category ->setTitle(category.name));
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
        CategoryRepository repo = new CategoryRepository();
        repo.getAllCategoriesAsync(categories -> {
            categories.forEach(category -> {
                adapter.add(new PageRow(new HeaderItem(category.id, category.name)));
            });
        });
        setAdapter(adapter);
    }
}