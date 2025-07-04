package com.example.bookreader.customclassses;

import androidx.leanback.widget.ListRow;

import com.example.bookreader.data.database.dto.BookDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RowItemData {
    private ListRow row;
    private BookDto book;
}
