package com.jifenqi;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MyListView extends ListView {
    
    private OnMyLayoutChangeListener mListener;
    
    public interface OnMyLayoutChangeListener {
        void onLayoutChange(int left, int top, int right, int bottom);
    }

    public MyListView(Context context) {
        this(context, null);
    }
    
    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed && mListener != null) {
            mListener.onLayoutChange(left, top, right, bottom);
        }
    }

    public void setOnMyLayoutChangeListener(OnMyLayoutChangeListener l) {
        mListener = l;
    }
}
