package com.bitcoin.blockchain.api.android.app.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Jesion on 2015-03-12.
 */
public class EditTextWithLabel extends EditText {

    private String mLabel = "";
    private int mPaddingLeft;

    public EditTextWithLabel(Context context) {
        super(context);
        mPaddingLeft = getPaddingLeft();
    }

    public EditTextWithLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaddingLeft = getPaddingLeft();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        TextPaint textPaint = getPaint();
        Rect size = new Rect();
        textPaint.getTextBounds(mLabel, 0, mLabel.length(), size);
        setPadding(mPaddingLeft + size.width() + 20, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        canvas.drawText(mLabel, mPaddingLeft + size.left, 60, textPaint);
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }
}
