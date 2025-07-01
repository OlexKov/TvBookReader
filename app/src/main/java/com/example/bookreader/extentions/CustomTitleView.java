package com.example.bookreader.extentions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.AttrRes;
import androidx.leanback.widget.TitleViewAdapter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.listeners.HeaderButtonOnFocusListener;
import com.example.bookreader.listeners.HeaderButtonOnKeyListener;

public class CustomTitleView extends FrameLayout implements TitleViewAdapter.Provider {
    private final BookReaderApp app = BookReaderApp.getInstance();
    private TextView vTitle;
    private ImageView titleIcon;
    private ImageButton btn1;
    private ImageButton btn2;
    private ImageButton btn3;
    private final HeaderButtonOnKeyListener keyListener = new HeaderButtonOnKeyListener();
    private final View.OnFocusChangeListener focusListener = new HeaderButtonOnFocusListener();

    private final TitleViewAdapter titleViewAdapter = new TitleViewAdapter() {
        @Override
        public View getSearchAffordanceView() {
            // Повертаємо null, бо у нас немає кнопки пошуку
            return null;
        }

        @Override
        public void setTitle(CharSequence titleText) {
            CustomTitleView.this.setTitle(titleText);
        }

    };

    public CustomTitleView(Context context) {
        this(context, null, 0);
    }

    public CustomTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTitleView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setTitleIcon(Drawable icon) {
        titleIcon.setImageDrawable(icon);
    }

    public void setTitleIconHeight(int heightDp){
        // Конвертуємо dp у px
         int px = (int) (heightDp * getResources().getDisplayMetrics().density + 0.5f);
         titleIcon.setMaxHeight(px );
    }

    public void setTitleSize(float sizeSp){
        vTitle.setTextSize(sizeSp);
    }

    public void setTitleColor(int color){
        vTitle.setTextColor(color);
    }

    public void setOnButton1ClickListener(OnClickListener listener){
        keyListener.addButton(btn1);
        btn1.setOnKeyListener(keyListener);
        btn1.setOnClickListener(listener);
        btn1.setVisibility(View.VISIBLE);
        btn1.setOnFocusChangeListener(focusListener);
    }

    public void setOnButton2ClickListener(OnClickListener listener){
        keyListener.addButton(btn2);
        btn2.setOnKeyListener(keyListener);
        btn2.setOnClickListener(listener);
        btn2.setVisibility(View.VISIBLE);
        btn2.setOnFocusChangeListener(focusListener);
    }

    public void setOnButton3ClickListener(OnClickListener listener){
        keyListener.addButton(btn3);
        btn3.setOnKeyListener(keyListener);
        btn3.setOnClickListener(listener);
        btn3.setVisibility(View.VISIBLE);
        btn3.setOnFocusChangeListener(focusListener);
    }

    public void setButton1Icon(int icon){
        btn1.setImageResource(icon);
    }
    public void setButton2Icon(int icon){
        btn2.setImageResource(icon);
    }
    public void setButton3Icon(int icon){
        btn3.setImageResource(icon);
    }

    public void setButton1Background(Drawable background){
        btn1.setBackground(background);
    }

    public void setButton2Background(Drawable background){
        btn2.setBackground(background);
    }

    public void setButton3Background(Drawable background){
        btn3.setBackground(background);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.custom_header, this, true);
        vTitle = findViewById(R.id.vTitle);
        titleIcon = findViewById(R.id.title_icon);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);

        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                btn1.requestFocus();
            }
        });

        app.getGlobalEventListener().subscribe(GlobalEventType.MENU_STATE_CHANGE_START,(isMenuStartOpen)->{
            if(app.getSelectedParentCategoryHeader().getName().equals("Всі") || !(boolean) isMenuStartOpen){
                smoothDisplay(buttonContainer);
            }
            else{
                smoothHide(buttonContainer);
            }
        });


        app.getGlobalEventListener().subscribe(GlobalEventType.CATEGORY_CHANGED,(category->{
            String cat = ((IconHeader) category).getName();
            if(cat.equals("Всі") || !app.isMenuOpen()){
                smoothDisplay(buttonContainer);
            }
            else{
                smoothHide(buttonContainer);
            }
        }));

    }
    private void smoothHide(View view){
        if(view.getVisibility() != View.GONE ){
            view.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }

    }
    private void smoothDisplay(View view) {
        if (view.getVisibility() != View.VISIBLE){
            view.animate()
                    .withStartAction(() -> view.setVisibility(View.VISIBLE))
                    .alpha(1f)
                    .setDuration(250)
                    .start();
        }
    }

    private void setTitle(CharSequence title) {
        vTitle.setText(title);
    }

    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return titleViewAdapter;
    }
}
