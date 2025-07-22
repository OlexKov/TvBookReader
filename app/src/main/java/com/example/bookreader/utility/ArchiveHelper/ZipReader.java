package com.example.bookreader.utility.ArchiveHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipReader implements IArchiveReader{

    private final ZipFile zipFile;
    private final Map<String, ZipEntry> entryMap = new HashMap<>();

    public ZipReader(File zipFile) throws IOException {
        this.zipFile = new ZipFile(zipFile, Charset.forName("CP866"));
        Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            entryMap.put(entry.getName(), entry);
        }
    }

    @Override
    public List<String> filePaths() {
        return entryMap.values().stream()
                .filter(zip->!zip.isDirectory())
                .map(ZipEntry::getName)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream openFile(String path) throws IOException {
        ZipEntry entry = entryMap.get(path);
        if (entry == null || entry.isDirectory()) throw new FileNotFoundException(path);
        return zipFile.getInputStream(entry);
    }

    @Override
    public void close() {
        try { zipFile.close(); } catch (IOException ignored) {}
    }
}
