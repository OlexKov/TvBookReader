package com.example.bookreader.customclassses;

import com.example.bookreader.data.database.dto.CategoryDto;
import java.io.Serializable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookCategories implements Serializable {
    public CategoryDto category;
    public CategoryDto subCategory;
}
