package com.example.bookreader.customclassses;

import com.example.bookreader.data.database.dto.CategoryDto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NewCategoryList {
    public List<CategoryDto> categories = new ArrayList<>();
}
