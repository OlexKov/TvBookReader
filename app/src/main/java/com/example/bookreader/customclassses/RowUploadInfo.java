package com.example.bookreader.customclassses;

import lombok.Data;
import lombok.Getter;

@Data
public class RowUploadInfo {
    @Getter
    private int lastUploadedElementDbIndex;
    private Long maxElementsDb;
    private boolean isLoading;
    private Long mainCategoryId;
    private Long rowCategoryId;

    public RowUploadInfo(Long maxElementsDb, int lastUploadedElementDbIndex, Long mainCategoryId, Long rowCategoryId){
        this.maxElementsDb = maxElementsDb;
        this.mainCategoryId = mainCategoryId;
        this.rowCategoryId = rowCategoryId;
        this.lastUploadedElementDbIndex = lastUploadedElementDbIndex;
    }

    public void setLastUploadedElementDbIndex(int index){
        lastUploadedElementDbIndex = Math.toIntExact(Math.min(index, maxElementsDb));
    }
}
