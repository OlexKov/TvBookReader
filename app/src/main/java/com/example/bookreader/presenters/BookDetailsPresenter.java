package com.example.bookreader.presenters;

import static android.graphics.text.LineBreakConfig.LINE_BREAK_STYLE_STRICT;
import static android.graphics.text.LineBreakConfig.LINE_BREAK_STYLE_UNSPECIFIED;
import static android.graphics.text.LineBreakConfig.LINE_BREAK_WORD_STYLE_PHRASE;

import android.graphics.text.LineBreakConfig;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

public class BookDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(@NonNull ViewHolder viewHolder, @NonNull Object item) {
        if(!(item instanceof BookDto book)) return;
        View view = viewHolder.view;
        String unknown = view.getContext().getString(R.string.unknown);
        viewHolder.getTitle().setText(book.title);
        StringBuilder sb = new StringBuilder();

        if(!book.year.equals(unknown)){
            sb.append(book.year);
        }
        if(!book.author.equals(unknown)){
            if(sb.length() > 0){
                sb.append(" • ");
            }
            sb.append(book.author);
        }

        if(book.pageCount != 0){
            if(sb.length() > 0){
                sb.append(" • ");
            }
            sb.append(book.pageCount);
            sb.append(" ст.");
        }

        viewHolder.getSubtitle().setText(sb.toString());
        TextView description = viewHolder.getBody();
        if(!book.description.equals(unknown)){
            description.setText(book.description);
        }

        description.post(() -> {
            description.setMaxLines(20);
            description.setLines(20);
        });
    }
}
