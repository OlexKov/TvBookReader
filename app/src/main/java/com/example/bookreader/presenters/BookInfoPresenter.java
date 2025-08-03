package com.example.bookreader.presenters;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.Presenter;
import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.fragments.settings.booksettings.EditBookFragment;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class BookInfoPresenter extends Presenter {

    private final Consumer<BookDto> bookClickHandler;

    public BookInfoPresenter(Consumer<BookDto> bookClickHandler){
        this.bookClickHandler = bookClickHandler;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.book_info, parent, false);
        return new Presenter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        View root = viewHolder.view;
        Context context = root.getContext();
        if (item instanceof BookDto info){
            setInfoData(root,context,info);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookClickHandler.accept(info);
                }
            });
        }



        root.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int color =  hasFocus ? Color.parseColor("#BB494949") : ContextCompat.getColor(root.getContext(),android.R.color.transparent);
                v.setBackgroundColor(color);
                AnimHelper.scale(v,1.05f,hasFocus,150);
            }
        });
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }

    @SuppressLint("SetTextI18n")
    private void setInfoData(View root, Context context, BookDto info){
        ImageView preview = root.findViewById(R.id.preview_image);
        TextView title = root.findViewById(R.id.book_preview_title);
        TextView author = root.findViewById(R.id.book_preview_author);
        TextView year = root.findViewById(R.id.book_preview_year);
        TextView pages = root.findViewById(R.id.book_preview_pages);
        TextView size = root.findViewById(R.id.book_preview_size);
        BookProcessor bookProcessor = new BookProcessor(context,info.filePath);
        try {
            bookProcessor.getPreviewAsync(0,400,280).thenAccept(bitmap ->{
                if(bitmap != null){
                    root.post(()-> preview.setImageBitmap(bitmap));
                }
                else{
                    root.post(()->  preview.setImageResource(R.drawable.books_logo));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        title.setText(info.title);
        if(info.author.equals(context.getString(R.string.unknown))){
            author.setVisibility(GONE);
        }
        else{
            author.setText(info.author);
        }
        if(info.year.equals(context.getString(R.string.unknown))){
            year.setVisibility(GONE);
        }
        else{
            year.setText(String.valueOf(info.year));
        }

        if(info.pageCount == 0){
            pages.setVisibility(GONE);
        }
        else{
            pages.setText(info.pageCount + " ст.");
        }
        size.setText(FileHelper.formatSize(info.fileSize));
    }
}
