package com.example.bookreader.diffcallbacks;

import androidx.leanback.widget.DiffCallback;

import com.example.bookreader.customclassses.MainStorage;

public class MainFolderDiffCallback extends DiffCallback<MainStorage> {
    @Override
    public boolean areItemsTheSame(MainStorage oldItem, MainStorage newItem) {
        // Чи це той самий об’єкт (для ID, шляху тощо)
        return oldItem.getFile().getAbsolutePath().equals(newItem.getFile().getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(MainStorage oldItem, MainStorage newItem) {
        // Чи вміст однаковий (наприклад, за датою модифікації)
        return oldItem.getName().equals(newItem.getName());
    }
}
