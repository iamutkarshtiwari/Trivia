package io.github.iamutkarshtiwari.trivia.activity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import io.github.iamutkarshtiwari.trivia.R;

/**
 * Created by utkarshtiwari on 8/26/17.
 */

public class MyTextView extends TextView {

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readAttr(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttr(context, attrs);
    }

    public MyTextView(Context context) {
        super(context);
    }



    private void readAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyTextView);
        // Read the title and set it if any
        String fontName = a.getString(R.styleable.MyTextView_fontName) ;

        if (fontName != null) {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/" + fontName + ((fontName.compareTo("MyriadPro") == 0 ? ".ttf" : ".otf")));
            setTypeface(tf ,1);
        }

        a.recycle();
    }

}