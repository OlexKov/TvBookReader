package com.example.bookreader.utility.ArchiveHelper;

import android.util.Log;

import com.example.bookreader.utility.FileHelper;
import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class BooksArchiveReader {
    private final Set<String> filesExt = Set.of("epub", "fb2");
    private static final Set<String> archivesExt = Set.of("zip", "tar","gzip","bzip2","xz");

    public List<String> fileBooksPaths(String archivePath) {
        List<String> files = new ArrayList<>();
        TFile root = new TFile(archivePath);
        listFilesRecursive(root, files);
        return files;
    }

    public InputStream openFile(String path) throws IOException{
        return new TFileInputStream(new TFile(path));
    }

    public long getFileSize(String path){
        return new TFile(path).length();
    }

    private  void listFilesRecursive(TFile file, List<String> files) {
        if (file.isDirectory()) {
            TFile[] children = file.listFiles();
            if (children != null ) {
                for (TFile child : children) {
                    listFilesRecursive(child, files);
                }
            }
        } else {

            String ext = FileHelper.getFileExtension(file);
            if (filesExt.contains(ext)) {
               files.add(file.getPath());
            }
            else if (archivesExt.contains(ext)) {
                String path = file.getPath();
                TFile nestedArchive = new TFile(path);
                listFilesRecursive(nestedArchive, files);
            }
        }
    }
    public static boolean isArchivePath(String path){
        for (String ext:archivesExt){
            if(path.contains("." + ext)){
                return new TFile(path).exists();
            }
        }
        return false;
    }
}
