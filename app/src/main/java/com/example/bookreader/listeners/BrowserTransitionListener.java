package com.example.bookreader.listeners;

import android.util.Log;

import androidx.leanback.app.BrowseSupportFragment;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.GlobalEventType;

public class BrowserTransitionListener extends BrowseSupportFragment.BrowseTransitionListener {
//    @Override
//    public void onHeadersTransitionStart(boolean withHeaders) {
//        Log.d("Transition", "Почався перехід: з заголовками = " + withHeaders);
//    }

    @Override
    public void onHeadersTransitionStop(boolean withHeaders) {
         BookReaderApp.getInstance().setMenuOpen(withHeaders);
         BookReaderApp.getInstance().getGlobalEventListener().sendEvent(GlobalEventType.MENU_STATE_CHANGE,withHeaders);
    }
}
