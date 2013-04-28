package com.jifenqi;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MyListView extends ListView {
    
    private OnSizeChangeListener mListener;
    
    public interface OnSizeChangeListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
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

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mListener != null) {
            mListener.onSizeChanged(w, h, oldw, oldh);
        }
    }
    
    public void setOnSizeChangeListener(OnSizeChangeListener l) {
        mListener = l;
    }
}
