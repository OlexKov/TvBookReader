package com.example.bookreader.utility.ArchiveHelper;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RarReader implements IArchiveReader{
    private Archive archive;
    private Map<String, FileHeader> fileMap = new HashMap<>();

    public RarReader(File rarFile) throws IOException, RarException {
        archive = new Archive(rarFile);
        for (FileHeader header : archive.getFileHeaders()) {
            if (!header.isDirectory()) {
                fileMap.put(header.getFileName().replace("\\", "/"), header);
            }
        }
    }

    @Override
    public List<String> filePaths() {
        return fileMap.entrySet().stream()
                .filter(rar->!rar.getValue().isDirectory())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream openFile(String path) throws IOException, RarException {
        FileHeader header = fileMap.get(path);
        if (header == null) throw new FileNotFoundException(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        archive.extractFile(header, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public void close() {
        try { archive.close(); } catch (Exception ignored) {}
    }
}
