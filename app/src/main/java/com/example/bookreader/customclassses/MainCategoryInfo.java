package com.example.bookreader.customclassses;

import lombok.Data;

@Data
public class MainCategoryInfo {
    private int index;
    private  int icon;
    private String name;
    private Long id;

    public MainCategoryInfo(int index,Long id, String name, int icon) {
        this.icon = icon;
        this.index = index;
        this.name = name;
        this.id = id;
    }
}
