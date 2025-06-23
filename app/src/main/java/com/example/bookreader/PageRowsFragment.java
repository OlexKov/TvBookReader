package com.example.bookreader;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;

public class PageRowsFragment extends RowsSupportFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         loadCategoryRows();
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private void loadCategoryRows() {
        String category = getArguments() != null ? getArguments().getString("category") : "";

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        if ("Категорія 1".equals(category)) {
            // Перший рядок
            ArrayObjectAdapter adapter1 = new ArrayObjectAdapter(itemPresenter);
            for (int i = 0; i < 15; i++) {
                adapter1.add("Категорія 1 - Елемент " + i);
            }
            HeaderItem header1 = new HeaderItem(0, "Перший рядок Категорії 1");
            rowsAdapter.add(new ListRow(header1, adapter1));

            // Другий рядок
            ArrayObjectAdapter adapter2 = new ArrayObjectAdapter(itemPresenter);
            for (int i = 0; i < 15; i++) {
                adapter2.add("Категорія 2 - Елемент " + i);
            }
            HeaderItem header2 = new HeaderItem(1, "Другий рядок Категорії 1");
            rowsAdapter.add(new ListRow(header2, adapter2));
        }
        else if ("Категорія 2".equals(category)) {
            // Аналогічно для Категорії 2
            ArrayObjectAdapter adapter1 = new ArrayObjectAdapter(itemPresenter);
            adapter1.add("Категорія 2 - Елемент 1");
            adapter1.add("Категорія 2 - Елемент 2");
            HeaderItem header1 = new HeaderItem(0, "Перший рядок Категорії 2");
            rowsAdapter.add(new ListRow(header1, adapter1));

            ArrayObjectAdapter adapter2 = new ArrayObjectAdapter(itemPresenter);
            adapter2.add("Категорія 2 - Елемент 3");
            adapter2.add("Категорія 2 - Елемент 4");
            HeaderItem header2 = new HeaderItem(1, "Другий рядок Категорії 2");
            rowsAdapter.add(new ListRow(header2, adapter2));
        }else if ("Категорія 3".equals(category)) {
            // Аналогічно для Категорії 3
            ArrayObjectAdapter adapter1 = new ArrayObjectAdapter(itemPresenter);
            adapter1.add("Категорія 3 - Елемент 1");
            adapter1.add("Категорія 3 - Елемент 2");
            HeaderItem header1 = new HeaderItem(0, "Перший рядок Категорії 3");
            rowsAdapter.add(new ListRow(header1, adapter1));

            ArrayObjectAdapter adapter2 = new ArrayObjectAdapter(itemPresenter);
            adapter2.add("Категорія 3 - Елемент 3");
            adapter2.add("Категорія 3 - Елемент 4");
            HeaderItem header2 = new HeaderItem(1, "Другий рядок Категорії 3");
            rowsAdapter.add(new ListRow(header2, adapter2));
        }
        // Можеш додати інші категорії так само...

        setAdapter(rowsAdapter);
    }
}
