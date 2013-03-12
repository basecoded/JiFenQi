package com.jifenqi;

import android.app.AlertDialog;
import android.content.Context;

public class RecordRoundDialog extends AlertDialog{
    
    private int mPlayerId;

    protected RecordRoundDialog(Context context) {
        super(context);
    }
    
    public void setPlayerId(int id) {
        mPlayerId = id;
    }
    
    public int getPlayerId() {
        return mPlayerId;
    }

}
