package com.example.bookreader.customclassses;

import lombok.Data;

@Data
public class MainCategoryInfo {
    private int index;
    private  int icon;
    private String name;

    public MainCategoryInfo(int index, String name, int icon) {
        this.icon = icon;
        this.index = index;
        this.name = name;
    }
}
