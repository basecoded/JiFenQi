package com.jifenqi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChooseSeatActivity extends Activity implements OnTouchListener
    , OnClickListener {
    private static final String TAG = "ChooseSeatActivity";
    
    private static final int FINISHGAME_ID = 3;
    
    private static final boolean ABOVE_API11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private TextView mPlayer1, mPlayer2, mPlayer3, mPlayer4;
    private TextView mSeat1, mSeat2, mSeat3, mSeat4;
    private Button mStartBtn, mRechooseBtn;
    
    private View mDragView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_seat);
        
        mPlayer1 = (TextView)findViewById(R.id.player1_name);
        mPlayer2 = (TextView)findViewById(R.id.player2_name);
        mPlayer3 = (TextView)findViewById(R.id.player3_name);
        mPlayer4 = (TextView)findViewById(R.id.player4_name);
        mSeat1 = (TextView)findViewById(R.id.seat1);
        mSeat2 = (TextView)findViewById(R.id.seat2);
        mSeat3 = (TextView)findViewById(R.id.seat3);
        mSeat4 = (TextView)findViewById(R.id.seat4);
        mPlayer1.setOnTouchListener(this);
        mPlayer2.setOnTouchListener(this);
        mPlayer3.setOnTouchListener(this);
        mPlayer4.setOnTouchListener(this);
        mStartBtn = (Button)findViewById(R.id.start_btn);
        mRechooseBtn = (Button)findViewById(R.id.reset_btn);
        mStartBtn.setOnClickListener(this);
        mStartBtn.setEnabled(false);
        mRechooseBtn.setOnClickListener(this);
        
        Intent intent = getIntent();
        CharSequence[] names = intent.getCharSequenceArrayExtra(Const.EXTRA_PLAYERNAMES);
        mPlayer1.setText(names[0]);
        mPlayer2.setText(names[1]);
        mPlayer3.setText(names[2]);
        mPlayer4.setText(names[3]);
    }

    float mDragOriginX;
    float mDragOriginY;
    float mPrevX;
    float mPrevY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        float x = event.getRawX();
        float y = event.getRawY();
        Log.d(TAG, "action = " + action);
        //Log.d(TAG, "mPrevX = " + mPrevX +", mPrevY = " + mPrevY);
        //Log.d(TAG, "x = " + x + ", y = " + y);
        if(mDragView != null && mDragView != v) {
            return true;
        }
        switch(action) {
        case MotionEvent.ACTION_DOWN:
            mDragView = v;
            startDrag(mDragView);
            break;
        case MotionEvent.ACTION_MOVE:
            if(mDragView != null) {
                float deltaX = x - mPrevX;
                float deltaY = y - mPrevY;
                moveView(mDragView, deltaX, deltaY);
            }
            break;
        case MotionEvent.ACTION_UP:
            if(mDragView != null) {
                dropView(mDragView);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            cancelDrag(mDragView, mDragOriginX, mDragOriginY);
            break;
        default:
            return true;
        }
        mPrevX = x;
        mPrevY = y;
        return true;
    }
    
    private void startDrag(View v) {
        if(ABOVE_API11) {
            mDragOriginX = v.getX();
            mDragOriginY = v.getY();
        } else {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v
                    .getLayoutParams();
            mDragOriginX = p.leftMargin;
            mDragOriginY = p.topMargin;
        }
    }
    
    private void moveView(View v, float deltaX, float deltaY) {
        Log.d(TAG, "moveView deltaX = " + deltaX + ", deltaY = " + deltaY);
//        v.setTranslationX(deltaX);
//        v.setTranslationY(deltaY);
        if(ABOVE_API11) {
            v.setX(v.getX() + deltaX);
            v.setY(v.getY() + deltaY);
        } else {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v
                    .getLayoutParams();
            int l = p.leftMargin + (int)deltaX;
            int t = p.topMargin + (int)deltaY;
            p.leftMargin = l;
            p.topMargin = t;
            v.setLayoutParams(p);
        }
        //v.invalidate();
    }
    
    private void dropView(View v) {
        TextView tv = (TextView)v;
        if(isInSeat(mSeat1, v)) {
            mSeat1.setText(tv.getText());
        } else if(isInSeat(mSeat2, v)) {
            mSeat2.setText(tv.getText());
        } else if(isInSeat(mSeat3, v)) {
            mSeat3.setText(tv.getText());
        } else if(isInSeat(mSeat4, v)) {
            mSeat4.setText(tv.getText());
        } else {
            cancelDrag(v, mDragOriginX, mDragOriginY);
            return;
        }
        
        if(checkAllSit()) {
            mStartBtn.setEnabled(true);
        }
        cancelDrag(v, mDragOriginX, mDragOriginY);
        v.setVisibility(View.INVISIBLE);
    }
    
    private void cancelDrag(View v, float x, float y) {
        if(v == null)  {
            return;
        }
        
        if(ABOVE_API11) {
            v.setX(x);
            v.setY(y);
        } else {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v
                    .getLayoutParams();
            p.leftMargin = (int)x;
            p.topMargin = (int)y;
            v.setLayoutParams(p);
        }
        clearDrag();
    }
    
    private void clearDrag() {
        mDragView = null;
        mDragOriginX = 0;
        mDragOriginY = 0;
        mPrevX = 0;
        mPrevY = 0;
    }
    
    int distanceAllow = 30;
    private boolean isInSeat(View seat, View player) {
        if(ABOVE_API11) {
            float seatX = seat.getX();
            float seatY = seat.getY();
            float playerX = player.getX();
            float playerY = player.getY();
            float distanceX = Math.abs(seatX - playerX);
            float distanceY = Math.abs(seatY - playerY);
            if((distanceX < distanceAllow)
                && (distanceY < distanceAllow)) {
                return true;
            }
        } else {
            float seatX = seat.getLeft();
            float seatY = seat.getTop();
            float playerX = player.getLeft();
            float playerY = player.getTop();
            float distanceX = Math.abs(seatX - playerX);
            float distanceY = Math.abs(seatY - playerY);
            if((distanceX < distanceAllow)
                && (distanceY < distanceAllow)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkAllSit() {
        CharSequence text1 = mSeat1.getText();
        CharSequence text2 = mSeat2.getText();
        CharSequence text3 = mSeat3.getText();
        CharSequence text4 = mSeat4.getText();
        if(text1 != null && text1.length() > 0
                && text2 != null && text2.length() > 0
                && text3 != null && text3.length() > 0
                && text4 != null && text4.length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch(id) {
        case R.id.start_btn:
            startNewGame();
            break;
        case R.id.reset_btn:
            resetSeat();
            break;
        }
    }
    
    private void startNewGame() {
        Intent intent = new Intent(getIntent());
        //intent.setClass(this, ZipaiGameActivity.class);
        intent.setClass(this, ZipaiGameActivity.class);
        
        String[] names = new String[4];
        names[0] = mSeat1.getText().toString();
        names[1] = mSeat2.getText().toString();
        names[2] = mSeat3.getText().toString();
        names[3] = mSeat4.getText().toString();
        intent.putExtra(Const.EXTRA_PLAYERNAMES, names);
        startActivity(intent);
        finish();
    }
    
    private void resetSeat() {
        mPlayer1.setVisibility(View.VISIBLE);
        mPlayer2.setVisibility(View.VISIBLE);
        mPlayer3.setVisibility(View.VISIBLE);
        mPlayer4.setVisibility(View.VISIBLE);
        mSeat1.setText("");
        mSeat2.setText("");
        mSeat3.setText("");
        mSeat4.setText("");
        
        mStartBtn.setEnabled(false);
    }
    
    @Override
    public void onBackPressed () {
        showDialog(FINISHGAME_ID);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case FINISHGAME_ID:
            return new AlertDialog.Builder(this)
                .setTitle(R.string.finish_game_title)
                .setMessage(R.string.finish_game_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                    }
                })
                .create();
        }
        
        return null;
    }
}
