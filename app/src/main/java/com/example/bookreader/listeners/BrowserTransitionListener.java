package com.example.bookreader.listeners;

import androidx.leanback.app.BrowseSupportFragment;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.GlobalEventType;

public class BrowserTransitionListener extends BrowseSupportFragment.BrowseTransitionListener {
    private final BookReaderApp app = BookReaderApp.getInstance();
    @Override
    public void onHeadersTransitionStart(boolean withHeaders) {
        app.getGlobalEventListener().sendEvent(GlobalEventType.MENU_STATE_CHANGE_START,withHeaders);
    }

    @Override
    public void onHeadersTransitionStop(boolean withHeaders) {
        app.setMenuOpen(withHeaders);
        app.getGlobalEventListener().sendEvent(GlobalEventType.MENU_STATE_CHANGED,withHeaders);
    }
}
