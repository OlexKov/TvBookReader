package com.example.bookreader.utility.ArchiveHelper;

public class ArchivePathHelper {
    public static final String SEPARATOR = ">>>";

    public static String combine(String archivePath, String internalPath) {
        if (archivePath == null) throw new IllegalArgumentException("archivePath == null");
        if (internalPath == null) internalPath = "";
        return archivePath + SEPARATOR + internalPath;
    }

    public static boolean isArchivePath(String path) {
        return path != null && path.contains(SEPARATOR);
    }

    public static String archivePath(String combined) {
        int i = indexOfSeparator(combined);
        return (i >= 0) ? combined.substring(0, i) : combined;
    }

    public static String internalPath(String combined) {
        int i = indexOfSeparator(combined);
        return (i >= 0) ? combined.substring(i + SEPARATOR.length()) : "";
    }

    private static int indexOfSeparator(String s) {
        return (s == null) ? -1 : s.indexOf(SEPARATOR);
    }
}
