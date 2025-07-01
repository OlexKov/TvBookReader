package com.example.bookreader.listeners;

import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.data.database.dto.BookDto;

public class RowItemSelectedListener implements OnItemViewSelectedListener {
    BookReaderApp app = BookReaderApp.getInstance();
    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (row instanceof ListRow) {
            ListRow rowList = (ListRow)row;
            if(app.getSelectedRow() != null && rowList != app.getSelectedRow()){
                app.setSelectedRow(rowList);
                app.getGlobalEventListener().sendEvent(GlobalEventType.ROW_SELECTED_CHANGE,row);
            }
        }
        if (item instanceof BookDto) {
            BookDto book = (BookDto)item;
            if(app.getSelectedItem() != null && book != app.getSelectedItem()){
                app.setSelectedItem(book);
                app.getGlobalEventListener().sendEvent(GlobalEventType.ITEM_SELECTED_CHANGE,book);
            }
        }
    }
}
