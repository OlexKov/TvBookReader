package com.example.bookreader.diffcallbacks;
import android.annotation.SuppressLint;

import androidx.leanback.widget.DiffCallback;
import com.example.bookreader.data.database.dto.BookDto;

public class BookDtoDiffCallback extends DiffCallback<BookDto> {
    @Override
    public boolean areItemsTheSame(BookDto oldItem, BookDto newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(BookDto oldItem, BookDto newItem) {
        return oldItem.title.equals(newItem.title)
                && (oldItem.isFavorite == newItem.isFavorite)
                && (oldItem.year == newItem.year)
                && (oldItem.pageCount == newItem.pageCount)
                && (oldItem.categoryId == newItem.categoryId)
                && oldItem.author.equals(newItem.author)
                && oldItem.description.equals(newItem.description)
                && oldItem.creationDate.equals(newItem.creationDate)
                && oldItem.filePath.equals(newItem.filePath)
                && oldItem.previewPath.equals(newItem.previewPath);
    }
}
