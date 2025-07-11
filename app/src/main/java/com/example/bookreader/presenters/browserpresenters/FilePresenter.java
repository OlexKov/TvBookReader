package com.example.bookreader.presenters.browserpresenters;

import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.example.bookreader.R;
import com.example.bookreader.interfaces.BookProcessor;
import com.example.bookreader.utility.EpubProcessor;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.pdf.PdfProcessor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class FilePresenter extends Presenter {
    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_folder, parent, false);
        return new Presenter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if (item instanceof File file) {
            View rootView = viewHolder.view;
            TextView textView = rootView.findViewById(R.id.main_folder_name);
            ImageView iconView = rootView.findViewById(R.id.main_folder_icon);

            textView.setText(file.getName());
            if(file.isDirectory()){
                iconView.setImageResource(R.drawable.folder);
            }
            else {
                String ext = FileHelper.getFileExtension(viewHolder.view.getContext(), file);
                BookProcessor bookProcessor;
                if(ext != null){
                    if(ext.equals("pdf")){
                        bookProcessor = new PdfProcessor();
                    }
                    else{
                        bookProcessor = new EpubProcessor();
                    }
                    try {
                        bookProcessor.getPreviewAsync(file,0,96,72 ).thenAccept((bitmap)->{
                            iconView.post(() -> {
                                iconView.setImageDrawable(new BitmapDrawable(rootView.getResources(), bitmap));
                            });
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }
}
