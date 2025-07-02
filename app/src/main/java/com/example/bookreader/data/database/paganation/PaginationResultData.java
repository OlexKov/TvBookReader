package com.example.bookreader.data.database.paganation;

import java.util.List;

import lombok.Data;

@Data
public class PaginationResultData<T> {
    List<T> data;
    long total;
}
