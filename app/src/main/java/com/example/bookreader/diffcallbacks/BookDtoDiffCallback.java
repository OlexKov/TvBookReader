package com.example.bookreader.diffcallbacks;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.leanback.widget.DiffCallback;
import com.example.bookreader.data.database.dto.BookDto;

import java.util.Objects;

public class BookDtoDiffCallback extends DiffCallback<Object> {
    @Override
    public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
        if(oldItem instanceof BookDto oldDto && newItem instanceof BookDto newDto){
            return oldDto.id == newDto.id;
        }
        return true;
    }

    @Override
    public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
        if(oldItem instanceof BookDto oldDto && newItem instanceof BookDto newDto) {
            return Objects.equals(oldDto.title, newDto.title)
                    && Objects.equals(oldDto.year, newDto.year)
                    && Objects.equals(oldDto.categoryId, newDto.categoryId)
                    && Objects.equals(oldDto.author, newDto.author)
                    && Objects.equals(oldDto.description, newDto.description)
                    && Objects.equals(oldDto.creationDate, newDto.creationDate)
                    && Objects.equals(oldDto.filePath, newDto.filePath)
                    && Objects.equals(oldDto.previewPath, newDto.previewPath)
                    && (oldDto.isFavorite == newDto.isFavorite)
                    && (oldDto.pageCount == newDto.pageCount);
        }
        return true;
    }
}
