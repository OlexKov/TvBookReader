package com.example.bookreader.customclassses;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrowserFile {
    private File file;
    private boolean checked;
}
