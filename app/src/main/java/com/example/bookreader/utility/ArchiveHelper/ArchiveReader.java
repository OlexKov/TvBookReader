package com.example.bookreader.utility.ArchiveHelper;

import com.example.bookreader.utility.FileHelper;
import com.github.junrar.exception.RarException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class ArchiveReader implements Closeable {

    private IArchiveReader reader;

    public ArchiveReader(File archive) throws RarException, IOException {
        String ext = FileHelper.getFileExtension(archive);
        reader = switch (ext){
            case "rar" -> new RarReader(archive);
            case "zip" -> new ZipReader(archive);
            default -> throw new IllegalStateException("Unexpected value: " + ext);
        };
    }

    public List<String> filePaths() {
        return reader.filePaths();
    }

    public InputStream openFile(String path) throws IOException, RarException {
        return reader.openFile(path);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
