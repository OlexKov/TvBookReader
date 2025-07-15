package com.example.bookreader.presenters.browserpresenters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.leanback.widget.Presenter;

import com.example.bookreader.R;
import com.example.bookreader.customclassses.MainStorage;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;



public class MainFolderPresenter extends Presenter {

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if (item instanceof MainStorage folder) {
            View rootView = viewHolder.view;
            TextView textView = rootView.findViewById(R.id.main_folder_name);
            ImageView iconView = rootView.findViewById(R.id.main_folder_icon);

            textView.setText(folder.getName());
            iconView.setImageResource(folder.getIconRId());

        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }
}
