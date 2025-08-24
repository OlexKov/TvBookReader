package com.example.bookreader.diffcallbacks;

import androidx.leanback.widget.DiffCallback;

import com.example.bookreader.customclassses.MainStorage;

public class MainFolderDiffCallback extends DiffCallback<MainStorage> {
    @Override
    public boolean areItemsTheSame(MainStorage oldItem, MainStorage newItem) {
        return oldItem.getFile().getAbsolutePath().equals(newItem.getFile().getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(MainStorage oldItem, MainStorage newItem) {
        return oldItem.getName().equals(newItem.getName());
    }
}
