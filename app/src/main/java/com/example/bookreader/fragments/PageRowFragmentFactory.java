package com.example.bookreader.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.PageRow;

public class PageRowFragmentFactory extends BrowseSupportFragment.FragmentFactory<Fragment> {

    @NonNull
    @Override
    public Fragment createFragment(Object rowObj) {
        if(!(rowObj instanceof PageRow row)) return new Fragment();
        HeaderItem header = row.getHeaderItem();

        // Передаємо назву або ID категорії в PageRowsFragment
        PageRowsFragment fragment = new PageRowsFragment();
        Bundle args = new Bundle();
        args.putString("category", header.getName()); // або header.getId() як long
        fragment.setArguments(args);
        return fragment;
    }
}