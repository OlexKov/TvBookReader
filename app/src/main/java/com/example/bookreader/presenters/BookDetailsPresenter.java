package com.example.bookreader.presenters;

import static android.graphics.text.LineBreakConfig.LINE_BREAK_STYLE_STRICT;
import static android.graphics.text.LineBreakConfig.LINE_BREAK_STYLE_UNSPECIFIED;
import static android.graphics.text.LineBreakConfig.LINE_BREAK_WORD_STYLE_PHRASE;

import android.graphics.text.LineBreakConfig;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

public class BookDetailsPresenter extends AbstractDetailsDescriptionPresenter {


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onBindDescription(@NonNull ViewHolder viewHolder, @NonNull Object item) {
        if(!(item instanceof BookDto book)) return;
        viewHolder.getTitle().setText(book.title);
        viewHolder.getSubtitle().setText( book.author);
        TextView description = viewHolder.getBody();
        description.setMaxLines(10);
        description.setLines(10);
        description.setEllipsize(null);
        description.setText(book.description);
    }
}
