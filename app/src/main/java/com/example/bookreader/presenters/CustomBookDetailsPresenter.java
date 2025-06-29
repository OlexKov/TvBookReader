package com.example.bookreader.presenters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.Presenter;

public class CustomBookDetailsPresenter extends FullWidthDetailsOverviewRowPresenter {

    private int mPreviousState = STATE_FULL;

    public CustomBookDetailsPresenter(final Presenter detailsPresenter) {
        super(detailsPresenter);
        setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_FULL);
    }

    @SuppressLint("PrivateResource")
    @Override
    protected void onLayoutLogo(final ViewHolder viewHolder, final int oldState, final boolean logoChanged) {
        final View v = viewHolder.getLogoViewHolder().view;
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

        lp.setMarginStart(v.getResources().getDimensionPixelSize(
                androidx.leanback.R.dimen.lb_details_v2_logo_margin_start));
        lp.topMargin = v.getResources().getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_blank_height) - lp.height / 2;

        switch (viewHolder.getState()) {
            case STATE_FULL:
            default:
                if (mPreviousState == STATE_HALF) {
                    v.animate().translationY(0);
                }

                break;
            case STATE_HALF:
                if (mPreviousState == STATE_FULL) {
                    final float offset = v.getResources().getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_actions_height) +
                            v.getResources().getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_description_margin_top) + lp.height / 2;
                    v.animate().translationY(offset);
                }

                break;
        }
        mPreviousState = viewHolder.getState();
        v.setLayoutParams(lp);
    }
}
