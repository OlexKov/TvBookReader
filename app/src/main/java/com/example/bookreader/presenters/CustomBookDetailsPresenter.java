package com.example.bookreader.presenters;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.DetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CustomBookDetailsPresenter extends FullWidthDetailsOverviewRowPresenter {

    private int mPreviousState = STATE_FULL;
    private TextView tagsView;
    private DetailsOverviewRow detailsRow;
    private RowPresenter.ViewHolder viewHolder;

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
        if (viewHolder.getState() == STATE_HALF) {
            if (mPreviousState == STATE_FULL) {
                final float offset = v.getResources().getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_actions_height) +
                        v.getResources().getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_description_margin_top) +
                        (float) lp.height / 2;
                v.animate().translationY(offset);
            }
        }
        else if (mPreviousState == STATE_HALF) {
                v.animate().translationY(0);
        }
        mPreviousState = viewHolder.getState();
        v.setLayoutParams(lp);
    }

    @Override
    protected void onBindRowViewHolder(@NonNull RowPresenter.ViewHolder holder, @NonNull Object item) {
        super.onBindRowViewHolder(holder, item);
        this.viewHolder = holder;
        FullWidthDetailsOverviewRowPresenter.ViewHolder dovh = (FullWidthDetailsOverviewRowPresenter.ViewHolder) holder;
        ViewGroup descFrame = dovh.getDetailsDescriptionFrame();
        if (descFrame == null || descFrame.getChildCount() == 0) return;

        // усередині FrameLayout перша дитина — лейаут з title/subtitle/body
        ViewGroup descLayout = (ViewGroup) descFrame.getChildAt(0);

        TextView body = descLayout.findViewById(androidx.leanback.R.id.lb_details_description_body);
        if (body == null) return;

        tagsView = descLayout.findViewById(R.id.lb_details_description_tags);
        if (tagsView == null) {
            // ще не вставляли — додаємо ПЕРЕД body
            View tags = LayoutInflater.from(descLayout.getContext())
                    .inflate(R.layout.lb_details_tags, descLayout, false);
            int insertIndex = descLayout.indexOfChild(body);
            descLayout.addView(tags, insertIndex);
            tagsView = tags.findViewById(R.id.lb_details_description_tags);
        }

        if (detailsRow == null && item instanceof DetailsOverviewRow row ) {
            detailsRow = row;
            updateTags();
        }
    }

    public void updateTags(){
        if (detailsRow != null && detailsRow.getItem() instanceof BookDto book) {
            new TagRepository().getByBookIdAsync(book.id).thenAccept(tags->{
                String tagsText = tags.stream().map(tag->tag.name).collect(Collectors.joining(" • "));
                viewHolder.view.post(()->{
                    if(TextUtils.isEmpty(tagsText)){
                        tagsView.setVisibility(View.GONE);
                    }
                    else{
                        tagsView.setText(tagsText);
                        tagsView.setVisibility(View.VISIBLE);
                    }
                });
            });
        }
    }
}
