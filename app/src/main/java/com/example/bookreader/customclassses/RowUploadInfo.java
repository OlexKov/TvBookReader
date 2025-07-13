package com.example.bookreader.customclassses;

import lombok.Data;
import lombok.Getter;

@Data
public class RowUploadInfo {
    @Getter
    private int lastUploadedElementDbIndex;
    private Long maxElements;
    private boolean isLoading;
    private Long mainCategoryId;
    private Long rowCategoryId;
    public RowUploadInfo(Long maxElements,int lastUploadedElementDbIndex,Long mainCategoryId,Long rowCategoryId){
        this.maxElements = maxElements;
        this.mainCategoryId = mainCategoryId;
        this.rowCategoryId = rowCategoryId;
        this.lastUploadedElementDbIndex = lastUploadedElementDbIndex;
    }

    public void setLastUploadedElementDbIndex(int index){
        lastUploadedElementDbIndex = Math.toIntExact(Math.min(index, maxElements));
    }


}
