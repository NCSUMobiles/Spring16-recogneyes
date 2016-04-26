package com.recognize.match.helper.filters;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A Helper class that represents a Square shaped ImageView
 * Created by Nischal on 7/21/15.
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context)
    {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }

}

