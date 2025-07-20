package com.example.bookreader.diffcallbacks;
import android.annotation.SuppressLint;

import androidx.leanback.widget.DiffCallback;
import com.example.bookreader.data.database.dto.BookDto;

import java.util.Objects;

public class BookDtoDiffCallback extends DiffCallback<BookDto> {
    @Override
    public boolean areItemsTheSame(BookDto oldItem, BookDto newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(BookDto oldItem, BookDto newItem) {
        return  Objects.equals(oldItem.title,newItem.title)
                && Objects.equals(oldItem.year,newItem.year)
                && Objects.equals(oldItem.categoryId,newItem.categoryId)
                && Objects.equals(oldItem.author,newItem.author)
                && Objects.equals(oldItem.description,newItem.description)
                && Objects.equals(oldItem.creationDate,newItem.creationDate)
                && Objects.equals(oldItem.filePath,newItem.filePath)
                && Objects.equals(oldItem.previewPath,newItem.previewPath)
                && (oldItem.isFavorite == newItem.isFavorite)
                && (oldItem.pageCount == newItem.pageCount);
    }
}
