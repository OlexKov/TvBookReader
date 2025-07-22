package com.example.bookreader.utility.ArchiveHelper;

import static com.example.bookreader.utility.FileHelper.getPathFileExtension;

import com.example.bookreader.utility.FileHelper;
import com.github.junrar.exception.RarException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BooksArchiveReader implements Closeable {
    public final List<String> bookExt = Arrays.asList(new String[]{"pdf", "epub", "fb2"});
    private final IArchiveReader reader;
    private final String archivePath;


    public BooksArchiveReader(String archivePath) throws RarException, IOException {
        this(new File(archivePath));
    }

    public BooksArchiveReader(File archive) throws RarException, IOException {
        this.archivePath = archive.getAbsolutePath();
        String ext = FileHelper.getFileExtension(archive);
        if(ext == null) throw new NullPointerException("File not have extension");
        reader = switch (ext){
            case "rar" -> new RarReader(archive);
            case "zip" -> new ZipReader(archive);
            default -> throw new IllegalStateException("Unexpected value: " + ext);
        };
    }

    public List<String> fileBooksPaths() {
        return reader.filePaths().stream()
                .filter(path-> bookExt.contains(getPathFileExtension(path)))
                .map(path->ArchivePathHelper.combine(archivePath,path))
                .collect(Collectors.toList());
    }

    public InputStream openFile(String path) throws IOException, RarException {
        return reader.openFile(path);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
