package com.example.bookreader.data.database.paganation.queryfilters;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class BookQueryFilter implements IQueryFilter{
    private StringBuilder sb;
    private List<Object> args;

    private String searchName;
    private Long categoryId;
    private Long inParentCategoryId;

    @Override
    public void prebuildQuery(boolean buildPagination) {
        sb = new StringBuilder();
        args = new ArrayList<>();
        if(buildPagination){
            sb.append("SELECT b.* FROM books b ");
        }
        else{
            sb.append("SELECT COUNT(*) FROM books b ");
        }
        sb.append("WHERE 1=1 ");

        if(this.searchName != null && !this.searchName.isEmpty()) {
            sb.append("AND name LIKE ? ");
            args.add("%" + this.searchName + "%");
        }


    }

    public SupportSQLiteQuery buildPagination(int page, int size){
        prebuildQuery(true);
        sb.append("LIMIT ? OFFSET ? ");
        args.add(size);
        args.add((page - 1) * size);
        return new SimpleSQLiteQuery(sb.toString(), args.toArray());
    }

    public SupportSQLiteQuery buildCount(){
        prebuildQuery(false);
        if (args.isEmpty()){
            return new SimpleSQLiteQuery(sb.toString());
        }
        return new SimpleSQLiteQuery(sb.toString(), args.toArray());
    }
}
