package io.github.iamutkarshtiwari.trivia;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by utkarshtiwari on 8/26/17.
 */

public class MyTextView extends TextView {

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MyTextView, defStyle, 0);
        String fontName = a.getString(R.styleable.MyTextView_fontName);
        a.recycle();
        init(fontName);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context) {
        super(context);
    }

    public void init(String font) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/" + font + ((font.compareTo("MyriadPro") == 0 ? ".ttf" : ".otf")));
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/JustTell.otf");
        setTypeface(tf ,1);

    }
}