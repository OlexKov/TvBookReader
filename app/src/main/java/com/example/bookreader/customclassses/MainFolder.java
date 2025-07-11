package com.example.bookreader.customclassses;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainFolder {
    private int iconRId;
    private String name;
    private File file;
}
