package com.example.bookreader.constants;


public enum ActionType {
    BOOK_READ(0),
    BOOK_EDIT(1),
    BOOK_DELETE(2),
    SETTING_1(3),
    SETTING_2(4),
    SETTING_3(5),
    BOOK_TOGGLE_FAVORITE(6);

    private final int id;

    ActionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ActionType fromId(long id) {
        for (ActionType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}