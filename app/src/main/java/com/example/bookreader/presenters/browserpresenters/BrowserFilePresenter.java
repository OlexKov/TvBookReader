package com.example.bookreader.presenters.browserpresenters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.util.Consumer;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.example.bookreader.R;
import com.example.bookreader.customclassses.BrowserFile;
import com.example.bookreader.interfaces.BookProcessor;
import com.example.bookreader.utility.EpubProcessor;
import com.example.bookreader.utility.Fb2Processor;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.pdf.PdfProcessor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class BrowserFilePresenter extends Presenter {

    private final Consumer<Object> onClickListener;


    public BrowserFilePresenter(Consumer<Object> clickListener) {
        this.onClickListener = clickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.browser_file, parent, false);
        return new Presenter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if (item instanceof BrowserFile file) {
            View rootView = viewHolder.view;
            TextView textView = rootView.findViewById(R.id.browser_file_name);
            ImageView iconView = rootView.findViewById(R.id.main_folder_icon);
            ImageView checkedIcon = rootView.findViewById(R.id.folder_check_icon);
            textView.setText(file.getFile().getName());
            int visibility = file.isChecked() ? VISIBLE : GONE;
            checkedIcon.setVisibility(visibility);

            if(file.getFile().isDirectory()){
                iconView.setImageResource(R.drawable.folder);
            }
            else {
                String ext = FileHelper.getFileExtension(viewHolder.view.getContext(), file.getFile());
                BookProcessor bookProcessor;
                if(ext != null){
                    if(ext.equals("pdf")){
                        bookProcessor = new PdfProcessor(viewHolder.view.getContext());
                    }
                    else  if(ext.equals("epub")){
                        bookProcessor = new EpubProcessor(viewHolder.view.getContext());
                    }
                    else {
                        bookProcessor = new Fb2Processor(viewHolder.view.getContext());
                    }
                    try {
                        bookProcessor.getPreviewAsync(file.getFile(),0,96,70 ).thenAccept((bitmap)->{
                            if(bitmap != null){
                                viewHolder.view.post(() -> {
                                    iconView.setImageBitmap(bitmap);
                                });
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.accept(item);
                }
            });
        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }
}
