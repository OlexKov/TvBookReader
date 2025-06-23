package com.example.bookreader;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.PageRow;

public class PageRowFragmentFactory extends BrowseSupportFragment.FragmentFactory {

    @NonNull
    @Override
    public Fragment createFragment(Object rowObj) {
        if (rowObj instanceof PageRow) {
            PageRow row = (PageRow) rowObj;
            HeaderItem header = row.getHeaderItem();

            // Передаємо назву або ID категорії в PageRowsFragment
            PageRowsFragment fragment = new PageRowsFragment();
            Bundle args = new Bundle();
            args.putString("category", header.getName()); // або header.getId() як long
            fragment.setArguments(args);
            return fragment;
        }

        return new Fragment(); // fallback
    }
}