package com.example.bookreader.constants;

public final class Constants {
    public static long FAVORITE_CATEGORY_ID = 111111111111111110L;
    public static long ALL_BOOKS_CATEGORY_ID = 111111111111111111L;
    public static long UNSORTED_BOOKS_CATEGORY_ID = 111111111111111112L;
    public static long SETTINGS_CATEGORY_ID = 111111111111111113L;
    public static long SIMILAR_ROW_ID = 111111111111111114L;

    // Row page setting

    public static int UPLOAD_SIZE = 5;
    public static int INIT_ADAPTER_SIZE = UPLOAD_SIZE * 3;
    public static int UPLOAD_THRESHOLD = UPLOAD_SIZE;

    //settings actions ids

    public static final int ACTION_ID_TITLE = 111111111;
    public static final int ACTION_ID_ICON = 111111112;
    public static final int ACTION_ID_PARENT_CATEGORY = 111111113;
    public static final int ACTION_ID_SAVE = 111111114;
    public static final int ACTION_ID_CANCEL = 111111115;
    public static final int ACTION_ID_DIVIDER = 111111116;
    public static final int ACTION_ID_CATEGORY = 111111117;
    public static final int ACTION_ID_SUBCATEGORY = 11111118;
    public static final int ACTION_ID_ADD_CATEGORY = 111111119;
    public static final int ACTION_ID_YEAR = 111111120;
    public static final int ACTION_ID_TAGS = 111111121;
    public static final int ACTION_ID_AUTHOR = 111111122;
    public static final int ACTION_ID_NEW_TAG = 11111123;
    public static final int ACTION_ID_CLEAR_TAGS = 11111124;
    public static final long ACTION_ID_NO_ACTION = -1;


    public static final int BOOK_THUMB_WIDTH = 270;
    public static final int BOOK_THUMB_HEIGHT = 400;

    public static final String PREVIEWS_DIR = "previews";

    public static final float READER_PAGE_ASPECT_RATIO = 2f / 3f;
    public static final int READER_MAX_ADAPTER_PAGES = 5; //min 4
    public static final int READER_SCROLL_Y = 200;
    public static final float READER_SCALE_STEP = 0.2f;
}
