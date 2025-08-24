package com.example.bookreader.diffcallbacks;

import androidx.leanback.widget.DiffCallback;

import com.example.bookreader.customclassses.BrowserFile;

public class BrowserFileDiffCallback extends DiffCallback<BrowserFile> {
    @Override
    public boolean areItemsTheSame(BrowserFile oldItem, BrowserFile newItem) {
        return oldItem.getFile().getAbsolutePath().equals(newItem.getFile().getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(BrowserFile oldItem, BrowserFile newItem) {
        return oldItem.getFile().lastModified() == newItem.getFile().lastModified();
    }
}
