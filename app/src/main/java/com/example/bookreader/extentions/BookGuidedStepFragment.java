package com.example.bookreader.extentions;

import androidx.leanback.app.GuidedStepSupportFragment;

import com.example.bookreader.R;

public abstract class BookGuidedStepFragment extends GuidedStepSupportFragment {
    @Override
    public int onProvideTheme() {
        return R.style.App_GuidedStep;
    }
}
