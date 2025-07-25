package com.example.bookreader.utility;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HashHelper {
    private static final int SEEDER = 0x9747b28c;

    public static int getFileHash(File file) throws Exception {
        InputStream stream = new FileInputStream(file);
        int hash = getFileHash(stream);
        stream.close();
        return hash;
    }

    public static int getFileHash(InputStream stream) throws Exception {
        XXHashFactory factory = XXHashFactory.fastestInstance();
        byte[] buffer = new byte[8192];
        int hash = SEEDER;
        int len;

        while ((len = stream.read(buffer)) != -1) {
            hash = factory.hash32().hash(buffer, 0, len, hash);
        }
        return hash;
    }
    public static int getStringHash(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        XXHashFactory factory = XXHashFactory.fastestInstance();
        XXHash32 hash32 = factory.hash32();
        return hash32.hash(data, 0, data.length,  SEEDER);
    }
}
