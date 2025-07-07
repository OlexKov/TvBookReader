package com.example.bookreader.utility.eventlistener;

public enum GlobalEventType {
    ROW_CHANGED(0),
    BOOK_UPDATED(1),
    BOOK_DELETED(2),
    BOOK_ADDED(3),
    DATABASE_DONE(4),
    MENU_STATE_CHANGED(5),
    MENU_STATE_CHANGE_START(6),
    CATEGORY_CHANGED(7),
    ROW_SELECTED_CHANGE(8),
    ITEM_SELECTED_CHANGE(9),
    CATEGORY_CASH_UPDATED(10),
    BOOK_FAVORITE_UPDATED(11),
    NEED_DELETE_CATEGORY(12);


    private final int id;

    GlobalEventType(int id) {
        this.id = id;
    }

    public static int getLength(){return GlobalEventType.values().length;}

    public static GlobalEventType fromId(long id) {
        for (GlobalEventType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}
