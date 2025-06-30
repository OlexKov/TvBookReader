package com.example.bookreader.listeners;
import android.view.KeyEvent;
import android.view.View;

public class HeaderButtonOnKeyListener implements View.OnKeyListener {
    private int currentButtonIndex = 0;
    private final View[] buttons;
    public HeaderButtonOnKeyListener(View... buttons) {
        this.buttons = buttons;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isFocused()) {
                currentButtonIndex = i;
                break;
            }
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if(currentButtonIndex < buttons.length-1){
                    currentButtonIndex++;
                    buttons[currentButtonIndex].requestFocus();
                    return true;
                }
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if(currentButtonIndex > 0 ){
                    currentButtonIndex--;
                    buttons[currentButtonIndex].requestFocus();
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
