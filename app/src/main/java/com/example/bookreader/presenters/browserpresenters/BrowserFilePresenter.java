package com.example.bookreader.presenters.browserpresenters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
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
import com.example.bookreader.customclassses.BrowserFile;
import com.example.bookreader.utility.bookutils.BookProcessor;
import com.example.bookreader.utility.FileHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class BrowserFilePresenter extends Presenter {

    private final Consumer<Object> onClickListener;

    public BrowserFilePresenter(Consumer<Object> clickListener) {
        this.onClickListener = clickListener;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.browser_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if (item instanceof BrowserFile file) {
            View rootView = viewHolder.view;
            TextView textView = rootView.findViewById(R.id.browser_file_name);
            ImageView iconView = rootView.findViewById(R.id.browser_file_icon);
            ImageView checkedIcon = rootView.findViewById(R.id.browser_file_check_icon);
            textView.setText(file.getFile().getName());
            int visibility = file.isChecked() ? VISIBLE : GONE;
            checkedIcon.setVisibility(visibility);
            Context context = rootView.getContext();

            if(!file.getFile().isDirectory()) {
                String fileExt = FileHelper.getFileExtension(file.getFile());
                if(fileExt != null){
                    int resource = switch (fileExt){
                        case "zip" -> R.drawable.zip_file;
                        case "tar" -> R.drawable.tar_file;
                        case "gzip" -> R.drawable.gzip_file;
                        case "bzip2" -> R.drawable.bzip2_file;
                        case "xz" -> R.drawable.xz_file;
                        default -> 0;
                    };
                    if(resource != 0){
                        iconView.setImageResource(resource);
                    }
                    else{
                        try {
                            new BookProcessor(context,file.getFile()).getPreviewAsync(0,96,70 ).thenAccept((bitmap)->{
                                rootView.post(() -> {
                                    if (bitmap != null) {
                                        iconView.setImageBitmap(bitmap);
                                    } else {
                                        iconView.setImageResource(R.drawable.e_book);
                                    }
                                });
                            });
                        } catch (NullPointerException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            else{
                var files = file.getFile().listFiles();
                int resource = files != null && files.length > 0
                        ? R.drawable.not_empty_folder
                        : R.drawable.folder;
                rootView.post(() -> {
                    iconView.setImageResource(resource);
                });
            }

            rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int color =  hasFocus ? Color.parseColor("#BB494949") : ContextCompat.getColor(context,android.R.color.transparent);
                    v.setBackgroundColor(color);
                }
            });

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.accept(item);
                }
            });
        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        View rootView = viewHolder.view;
        ImageView iconView = rootView.findViewById(R.id.browser_file_icon);
        iconView.setImageResource(R.drawable.e_book);
    }
}
