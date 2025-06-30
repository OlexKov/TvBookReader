package com.example.bookreader.listeners;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeaderButtonOnKeyListener implements View.OnKeyListener {
    private int currentButtonIndex = 0;
    private final List<View> buttons;

    public HeaderButtonOnKeyListener(View... buttons) {
        this.buttons = new ArrayList<>(Arrays.asList(buttons));
    }
    public void addButton(View button){
        buttons.add(button);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (buttons.size() > 1) {
            for (int i = 0; i < buttons.size(); i++) {
                if (buttons.get(i).isFocused()) {
                    currentButtonIndex = i;
                    break;
                }
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (currentButtonIndex < buttons.size() - 1) {
                        currentButtonIndex++;
                        buttons.get(currentButtonIndex).requestFocus();
                        return true;
                    }
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (currentButtonIndex > 0) {
                        currentButtonIndex--;
                        buttons.get(currentButtonIndex).requestFocus();
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }
}
