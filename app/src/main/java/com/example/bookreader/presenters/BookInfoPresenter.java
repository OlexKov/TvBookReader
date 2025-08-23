package com.example.bookreader.presenters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import androidx.leanback.widget.Presenter;
import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.data.database.repository.TagRepository;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.stream.Collectors;

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
        TextView bookTags = root.findViewById(R.id.book_preview_tags);
        TextView category = root.findViewById(R.id.book_preview_category);
        TextView descExist = root.findViewById(R.id.book_preview_description_exist);
        BookProcessor bookProcessor = new BookProcessor(context,info.filePath);
        bookProcessor.getPreviewAsync(0,400,280).thenAccept(bitmap ->{
            if(bitmap != null){
                root.post(()-> preview.setImageBitmap(bitmap));
            }
            else{
                root.post(()-> preview.setImageResource(R.drawable.books_logo));
            }
        });

        setText(context,title,info.title);
        setText(context,author,info.author);
        setText(context,year,info.year);
        setText(context,pages,info.pageCount == 0 ? "" : String.valueOf(info.pageCount));
        size.setText(FileHelper.formatSize(info.fileSize));
        int descVisibility = info.description.isBlank() ? GONE : VISIBLE;
        descExist.setVisibility(descVisibility);

        if(!info.tagsIds.isEmpty()){
            new TagRepository().getByIdsAsync(info.tagsIds).thenAccept((tags)->{
                String description =  tags.stream().map(tag->tag.name).collect(Collectors.joining(" | "));
                root.post(()->{
                    setText(context,bookTags,description);
                });
            });
        }
        else{
            bookTags.setVisibility(GONE);
        }

        if(info.categoryId != null){
            CategoryRepository categoryRepository = new CategoryRepository();
            categoryRepository.getByIdAsync(info.categoryId,(bookCategory->{
                String categoryText = "";
                if(bookCategory.parentId != null){
                    var parentCategory = categoryRepository.getCategoryByIdAsyncCF(bookCategory.parentId).join();
                    categoryText += parentCategory.name + " - ";
                }
                categoryText += bookCategory.name;
                String finalCategoryText = categoryText;
                root.post(()->{
                    setText(context,category, finalCategoryText);
                });
            }));
        }
        else{
            category.setVisibility(GONE);
        }
    }

    private void setText(Context context,TextView textView, String text){
        if(text.isBlank() || text.equals(context.getString(R.string.unknown))){
            textView.setVisibility(GONE);
        }
        else{
            textView.setVisibility(VISIBLE);
            textView.setText(text);
        }
    }
}
