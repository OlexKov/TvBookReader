package com.example.bookreader.presenters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.FileHelper;

public class BookDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(@NonNull ViewHolder viewHolder, @NonNull Object item) {
        if(!(item instanceof BookDto book)) return;
        View view = viewHolder.view;
        String unknown = view.getContext().getString(R.string.unknown);
        viewHolder.getTitle().setText(book.title);
        StringBuilder sb = new StringBuilder();

        if(book.year!= null && !book.year.equals(unknown)){
            sb.append(book.year);
        }
        if(book.author!= null &&!book.author.equals(unknown)){
            addSeparator(sb);
            sb.append(book.author);
        }

        if(book.pageCount != 0){
            addSeparator(sb);
            sb.append(book.pageCount);
            sb.append(" ст.");
        }

        if(book.fileSize != 0){
            addSeparator(sb);
            sb.append(FileHelper.formatSize(book.fileSize));
        }

        if(book.filePath != null && !book.filePath.isEmpty()){
            addSeparator(sb);
            sb.append(FileHelper.getPathFileExtension( book.filePath.toUpperCase()));
        }

        viewHolder.getSubtitle().setText(sb.toString());
        TextView description = viewHolder.getBody();
        if(book.description!= null && !book.description.equals(unknown)){
            description.setText(book.description);
        }

        description.post(() -> {
            description.setMaxLines(20);
            description.setLines(20);
        });
    }

    private void addSeparator(StringBuilder sb){
        if(sb.length() > 0){
            sb.append(" ● ");
        }
    }
}
