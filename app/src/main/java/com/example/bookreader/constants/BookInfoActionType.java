package com.example.bookreader.constants;


public enum BookInfoActionType {
    BOOK_READ(0),
    BOOK_EDIT(1),
    BOOK_DELETE(2),
    SETTING_1(3),
    SETTING_2(4),
    SETTING_3(5),
    BOOK_TOGGLE_FAVORITE(6);

    private final int id;

    BookInfoActionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static BookInfoActionType fromId(long id) {
        for (BookInfoActionType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}