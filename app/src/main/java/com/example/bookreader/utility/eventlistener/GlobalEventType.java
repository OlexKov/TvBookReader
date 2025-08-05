package com.example.bookreader.utility.eventlistener;

public enum GlobalEventType {
    ROW_CHANGED(0),
    BOOK_UPDATED(1),
    BOOK_DELETED(2),
    BOOK_ADDED(3),
    DATABASE_DONE(4),
    MENU_STATE_CHANGED(5),
    MENU_STATE_CHANGE_START(6),
    CATEGORY_SELECTION_CHANGED(7),
    ROW_SELECTED_CHANGE(8),
    ITEM_SELECTED_CHANGE(9),
    CATEGORY_CASH_UPDATED(10),
    BOOK_FAVORITE_UPDATED(11),
    DELETE_MAIN_CATEGORY_COMMAND(12),
    FILEBROWSER_MAIN_FOLDER_SELECTION_CHANGE(13),
    LOAD_BOOK_UPDATED(14),
    ADD_MAIN_CATEGORY_COMMAND(15),
    UPDATE_CATEGORIES_COMMAND(16);



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
