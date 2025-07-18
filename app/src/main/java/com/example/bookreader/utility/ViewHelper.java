package com.example.bookreader.utility;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewParent;

public class ViewHelper {

    public static boolean isDescendant(View child, View parent) {
        while (child != null && child != parent) {
            ViewParent parentView = child.getParent();
            if (parentView instanceof View) {
                child = (View) parentView;
            } else {
                return false;
            }
        }
        return child == parent;
    }

    public static  int calculateSpanCount(Context context, int itemWidthDp) {
        // Отримати ширину екрана в dp
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;

        // Порахувати кількість колонок
        int columnCount = (int) (screenWidthDp / itemWidthDp);
        int totalSpacingDp = 30 * (columnCount - 1);
        int availableDp = (int) screenWidthDp - totalSpacingDp;
        int spanCount = availableDp / itemWidthDp;

        // Гарантувати мінімум 1 колонку
        return Math.max(spanCount, 1);
    }
}
