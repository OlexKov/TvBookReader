//package com.example.bookreader.utility.bookutils.pdf;
//
//import android.graphics.RectF;
//
//import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
//import com.tom_roush.pdfbox.text.TextPosition;
//
//public class PdfHighlightMapper {
//    public static RectF mapTextPositionToBitmapRect(TextPosition textPosition, PDRectangle pageBox, int bitmapWidth, int bitmapHeight) {
//        float pageWidth = pageBox.getWidth();
//        float pageHeight = pageBox.getHeight();
//
//        float scaleX = (float) bitmapWidth / pageWidth;
//        float scaleY = (float) bitmapHeight / pageHeight;
//
//        float x = textPosition.getX() * scaleX;
//        float y = (pageHeight - textPosition.getY()) * scaleY; // інверсія по Y
//
//        float width = textPosition.getWidth() * scaleX;
//        float height = textPosition.getHeight() * scaleY;
//
//        return new RectF(x, y, x + width, y + height);
//    }
//}
