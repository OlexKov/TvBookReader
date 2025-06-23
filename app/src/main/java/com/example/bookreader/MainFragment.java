package com.example.bookreader;

import android.os.Bundle;
import android.util.Log;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DividerRow;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

public class MainFragment extends BrowseSupportFragment {


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setTitle("Категорії");
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.default_background));

        setupRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {

                if (item instanceof PageRow) {
                    String title = ((PageRow) item).getHeaderItem().getName();
                    setTitle(title); // ← має оновити заголовок
                    Log.d("TAG", "Встановлюємо заголовок: " + title);
                }
            }
        });
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