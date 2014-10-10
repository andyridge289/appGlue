package com.appglue.layout.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class WeightedExpandAnimation extends Animation {

    private final float mStartWeight;
    private final float mDeltaWeight;
    private View mContent;

    public WeightedExpandAnimation(View v, float startWeight, float endWeight) {
        mStartWeight = startWeight;
        mDeltaWeight = endWeight - startWeight;
        mContent = v;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContent.getLayoutParams();
        lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
        mContent.setLayoutParams(lp);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
