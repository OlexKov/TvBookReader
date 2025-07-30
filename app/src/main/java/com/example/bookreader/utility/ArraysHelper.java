package com.example.bookreader.utility;

import java.util.ArrayList;
import java.util.List;

public class ArraysHelper {
    public static <T> List<List<T>> partitionList(List<T> list, int maxSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += maxSize) {
            partitions.add(list.subList(i, Math.min(i + maxSize, list.size())));
        }
        return partitions;
    }
}
