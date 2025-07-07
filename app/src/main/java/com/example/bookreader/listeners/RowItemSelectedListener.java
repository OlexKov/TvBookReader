package com.example.bookreader.listeners;

import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.data.database.dto.BookDto;

public class RowItemSelectedListener implements OnItemViewSelectedListener {
    BookReaderApp app = BookReaderApp.getInstance();
    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(!(row instanceof ListRow listRow) || !(item instanceof BookDto book)) return;
        if(app.getSelectedRow() == null || listRow != app.getSelectedRow()){
            app.setSelectedRow(listRow);
            app.getGlobalEventListener().sendEvent(GlobalEventType.ROW_SELECTED_CHANGE, new RowItemData(listRow,book));
        }

        if(app.getSelectedItem() == null || book.id != app.getSelectedItem().id){
            app.setSelectedItem(book);
            app.getGlobalEventListener().sendEvent(GlobalEventType.ITEM_SELECTED_CHANGE,new RowItemData(listRow,book));
        }
    }
}
