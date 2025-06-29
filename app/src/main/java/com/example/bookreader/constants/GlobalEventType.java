package com.example.bookreader.constants;

public enum GlobalEventType {
    ROW_CHANGED(0),
    BOOK_EDITED(1),
    BOOK_DELETED(2),
    BOOK_ADDED(3);

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
