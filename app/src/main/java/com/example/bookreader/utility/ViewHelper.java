package com.example.bookreader.utility;

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
}
