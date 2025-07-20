package com.example.bookreader.utility.fb2parser.fonts;

public class Font {

    protected final int startIndex, finishIndex;

    protected Font(String emphasis, String p) {
        startIndex = p.indexOf(emphasis);
        finishIndex = startIndex + emphasis.length();
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getFinishIndex() {
        return finishIndex;
    }
}
