package com.example.bookreader.utility.pdf;

import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class HighlightingTextStripper  extends PDFTextStripper {
    public record Highlight(String text, TextPosition position){}

    private final String searchTerm;
    @Getter
    private final List<Highlight> highlights = new ArrayList<>();

    public HighlightingTextStripper(String searchTerm) throws IOException {
        super();
        this.searchTerm = searchTerm.toLowerCase();
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        String lowerString = string.toLowerCase();

        int index = lowerString.indexOf(searchTerm);
        if (index >= 0) {
            // Знайшли підрядок, можна далі перебрати позиції для символів
            for (int i = index; i < index + searchTerm.length(); i++) {
                highlights.add(new Highlight(String.valueOf(string.charAt(i)), textPositions.get(i)));
            }
        }

        super.writeString(string, textPositions);
    }
}
