package com.example.bookreader.extentions;

import androidx.leanback.widget.HeaderItem;

public class IconHeader extends HeaderItem {
    public int iconResId;
    public IconHeader(long id, String name, int iconResId) {
        super(id, name);
        this.iconResId = iconResId;
    }
}
