package com.jifenqi;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NumberPicker extends LinearLayout implements View.OnClickListener{
    private int mCurrentValue;
    private int mBaseValue;
    private int mStep;
    private boolean mCanNegative;
    private View mIncrementButton;
    private View mDecrementButton;
    private TextView mValueTextView;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Number, defStyle, 0);
        mBaseValue = a.getInt(R.styleable.Number_base, 0);
        mStep = a.getInt(R.styleable.Number_step, 1);
        mCanNegative = a.getBoolean(R.styleable.Number_can_negative, false);
        
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker, this, true);
        
        mIncrementButton = findViewById(R.id.increment);
        mIncrementButton.setOnClickListener(this);

        mDecrementButton = findViewById(R.id.decrement);
        mDecrementButton.setOnClickListener(this);
        
        mValueTextView = (TextView)findViewById(R.id.value);
        setValue(mBaseValue);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.increment:
                changeValue(true);
                break;
            case R.id.decrement:
                changeValue(false);
                break;
        }
    }
    
    private void changeValue(boolean isIncrement) {
        int newValue = mCurrentValue;
        if(isIncrement) {
            newValue += mStep;
        } else {
            newValue -= mStep;
            if(!mCanNegative) {
	            if(newValue < 0) {
	                newValue = 0;
	            }
            }
        }
        setValue(newValue);
    }
    
    public void setValue(int value) {
        mCurrentValue = value;
        mValueTextView.setText(Integer.toString(value));
    }
    
    public int getValue() {
        return mCurrentValue;
    }
    
    public void resetValue() {
        setValue(mBaseValue);
    }

}
