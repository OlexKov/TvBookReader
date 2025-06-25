package com.example.bookreader.constants;


public enum BookActionType {
    READ(0),
    EDIT(1),
    DELETE(2);

    private final int id;

    BookActionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static BookActionType fromId(long id) {
        for (BookActionType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}