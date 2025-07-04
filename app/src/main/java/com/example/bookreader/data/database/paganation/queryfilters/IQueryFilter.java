package com.example.bookreader.data.database.paganation.queryfilters;

import androidx.sqlite.db.SupportSQLiteQuery;

public interface IQueryFilter {
    void prebuildQuery(boolean buildPagination);
    SupportSQLiteQuery buildPagination(int page, int size);
    public SupportSQLiteQuery buildCount();
}
