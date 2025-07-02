package com.example.bookreader.customclassses;

import lombok.Data;

@Data
public class RowUploadInfo {
    private int startUploadPage = 1;
    private Long maxElements;
}
