package com.example.bookreader.diffcallbacks;

import androidx.leanback.widget.DiffCallback;

import com.example.bookreader.customclassses.BrowserFile;

public class BrowserFileDiffCallback extends DiffCallback<BrowserFile> {
    @Override
    public boolean areItemsTheSame(BrowserFile oldItem, BrowserFile newItem) {
        // Чи це той самий об’єкт (для ID, шляху тощо)
        return oldItem.getFile().getAbsolutePath().equals(newItem.getFile().getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(BrowserFile oldItem, BrowserFile newItem) {
        // Чи вміст однаковий (наприклад, за датою модифікації)
        return oldItem.getFile().lastModified() == newItem.getFile().lastModified();
    }
}
