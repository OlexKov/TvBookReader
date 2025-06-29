package com.example.bookreader.extentions;

import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

public class RowPresenterSelector extends PresenterSelector {
    private final Presenter presenter;

    public RowPresenterSelector() {
        this.presenter = new ListRowPresenter();
    }

    @Override
    public Presenter getPresenter(Object item) {
        return presenter;
    }

    @Override
    public Presenter[] getPresenters() {
        return new Presenter[] { presenter };
    }
}
