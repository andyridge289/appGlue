package com.appglue.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

import com.appglue.R;

public class VerticalTextView extends TextView {

    final boolean rightDown;

    public VerticalTextView(Context context) {
        this(context, null);
    }

    public VerticalTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalTextView);
        rightDown = !a.getBoolean(R.styleable.VerticalTextView_face_right, true);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        return super.setFrame(l, t, l + (b - t), t + (r - l));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (rightDown) {
            canvas.translate(getHeight(), 0);
            canvas.rotate(90);
        } else {
            canvas.translate(0, getWidth());
            canvas.rotate(-90);
        }
        canvas.clipRect(0, 0, getWidth(), getHeight(), android.graphics.Region.Op.REPLACE);
        super.draw(canvas);
    }
}