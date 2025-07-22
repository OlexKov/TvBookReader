package com.example.bookreader.utility.ArchiveHelper;

import com.github.junrar.exception.RarException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IArchiveReader extends Closeable {
    List<String> filePaths();
    InputStream openFile(String path) throws IOException, RarException;
    void close(); // якщо потрібно
}
