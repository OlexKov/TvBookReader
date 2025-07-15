package com.example.bookreader.diffcallbacks;
import androidx.leanback.widget.DiffCallback;
import com.example.bookreader.data.database.dto.BookDto;

public class BookDtoDiffCallback extends DiffCallback<BookDto> {
    @Override
    public boolean areItemsTheSame(BookDto oldItem, BookDto newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(BookDto oldItem, BookDto newItem) {
        return oldItem.name.equals(newItem.name)
               && oldItem.isFavorite == newItem.isFavorite;
    }
}
