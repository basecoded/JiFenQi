package com.jifenqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class UserPokeLayout extends LinearLayout {
	
	private onUserPokeListener mListener;
	
	public static interface onUserPokeListener {
		void onUserPoker();
	}

	public UserPokeLayout(Context context) {
        super(context);
    }

    public UserPokeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public UserPokeLayout(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }
    
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(mListener != null) {
    		mListener.onUserPoker();
    	}
    	return super.onInterceptTouchEvent(ev);
    }
    
    public void setOnUserPokeListener(onUserPokeListener listener) {
    	mListener = listener;
    }
}
