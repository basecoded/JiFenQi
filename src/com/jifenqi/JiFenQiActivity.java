package com.jifenqi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class JiFenQiActivity extends Activity {
    
    private static final int MSG_RESUME = 1;
    
    private static final int DIALOG_RESUME = 1;
    
    private View mPlayer4;
    private EditText mPlayerName1, mPlayerName2, mPlayerName3, mPlayerName4;
    private Spinner mPlayerNumberSpinner, mZhuangjiaSpinner;
    private View mShangxiaxing;
    private CheckBox mShangxiaxing_cb;
    private Button mStartButton;
    private int mPlayerNumber;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_RESUME:
                showDialog(DIALOG_RESUME);
                break;
            }
        }
    };
    
    private TextWatcher mWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateZhuangjia();
        }
        
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zipai_new);
        
        boolean needResume = Utils.getBooleanSP(this, Const.KEY_RESUME);
        if(needResume) {
            mHandler.sendEmptyMessage(MSG_RESUME);
        }
        
        mPlayerNumberSpinner = (Spinner)findViewById(R.id.player_number);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.player_numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlayerNumberSpinner.setAdapter(adapter);
        mPlayerNumberSpinner.setSelection(1); //Default to 4 players
        mPlayerNumberSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                //parent.getItemAtPosition(position);
                if(position == 0) {
                    mPlayer4.setVisibility(View.GONE);
                    mShangxiaxing.setVisibility(View.VISIBLE);
                    mPlayerNumber = 3;
                } else {
                    mPlayer4.setVisibility(View.VISIBLE);
                    mShangxiaxing.setVisibility(View.GONE);
                    mPlayerNumber = 4;
                }
                updateZhuangjia();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mPlayer4 = findViewById(R.id.player4);
        mPlayerName1 = (EditText)findViewById(R.id.player1_name);
        mPlayerName2 = (EditText)findViewById(R.id.player2_name);
        mPlayerName3 = (EditText)findViewById(R.id.player3_name);
        mPlayerName4 = (EditText)findViewById(R.id.player4_name);
        mPlayerName1.addTextChangedListener(mWatcher);
        mPlayerName2.addTextChangedListener(mWatcher);
        mPlayerName3.addTextChangedListener(mWatcher);
        mPlayerName4.addTextChangedListener(mWatcher);
        
        
        mShangxiaxing = findViewById(R.id.shangxiaxing);
        mShangxiaxing_cb = (CheckBox)findViewById(R.id.shangxiaxing_cb);
        
        mZhuangjiaSpinner = (Spinner)findViewById(R.id.zhuangjia_spinner);
        updateZhuangjia();
        
        if(true) {
            mPlayerName1.setText("p1");
            mPlayerName2.setText("p2");
            mPlayerName3.setText("p3");
            mPlayerName4.setText("p4");
        }
        
        mStartButton = (Button)findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPlayerNames()) {
                    startNewGame();
                } else {
                    showErrorDialog();
                }
            }
        });
        
        Button button = (Button)findViewById(R.id.history_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(JiFenQiActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.zipai_help_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                showHelp();
                return true;
            case R.id.about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_RESUME:
            return new AlertDialog.Builder(this)
            .setTitle(R.string.resume_game_title)
            .setMessage(R.string.resume_game_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Utils.putBooleanSP(JiFenQiActivity.this, Const.KEY_RESUME, false);
                    
                    Intent intent = new Intent();
                    intent.setClass(JiFenQiActivity.this, ZipaiGameActivity.class);
                    intent.putExtra(Const.EXTRA_RESUME_GAME, true);
                    startActivity(intent);
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Utils.putBooleanSP(JiFenQiActivity.this, Const.KEY_RESUME, false);
                }
            })
            .create();
        }
        return null;
    }
    private boolean checkPlayerNames() {
        boolean ret = false;
        if((mPlayerName1.getText() != null && mPlayerName1.getText().length() != 0)
                && (mPlayerName2.getText() != null && mPlayerName2.getText().length() != 0)
                && (mPlayerName3.getText() != null && mPlayerName3.getText().length() != 0)
                ) {
            ret = true;
        } else {
            return false;
        }
        if(mPlayerNumber == 4) {
            if((mPlayerName4.getText() != null && mPlayerName4.getText().length() != 0)) {
                ret = true;
            } else {
                ret = false;
            }
        }
        
        return ret;
    }
    
    private void startNewGame() {
        Intent intent = new Intent();
        if(mPlayerNumber == 4) {
            
            intent.setClass(this, ChooseSeatActivity.class);
        } else {
          intent.setClass(this, ZipaiGameActivity.class);
        }
        intent.putExtra(Const.EXTRA_PLAYERNUMBER, mPlayerNumber);
        
        String[] names = getPlayerNames();
        intent.putExtra(Const.EXTRA_PLAYERNAMES, names);
        
        boolean hasShangxiaxing = true;
        if(mPlayerNumber != 4) {
            hasShangxiaxing = mShangxiaxing_cb.isChecked();
        }
        intent.putExtra(Const.EXTRA_SHANGXIAXING, hasShangxiaxing);
        
        int[] startPoints = new int[mPlayerNumber];
        try {
            TextView tv = (TextView)findViewById(R.id.player1_start_point);
            if(tv.getText().length() != 0)
                startPoints[0] = Integer.parseInt(tv.getText().toString());
            tv = (TextView)findViewById(R.id.player2_start_point);
            if(tv.getText().length() != 0)
                startPoints[1] = Integer.parseInt(tv.getText().toString());
            tv = (TextView)findViewById(R.id.player3_start_point);
            if(tv.getText().length() != 0)
                startPoints[2] = Integer.parseInt(tv.getText().toString());
            if(mPlayerNumber == 4) {
                tv = (TextView)findViewById(R.id.player4_start_point);
                if(tv.getText().length() != 0)
                    startPoints[3] = Integer.parseInt(tv.getText().toString());
            }
        } catch (NumberFormatException e) {
            return;
        }
        intent.putExtra(Const.EXTRA_STARTPOINTS, startPoints);
        
        String zhuangjiaName = (String)mZhuangjiaSpinner.getSelectedItem();
        intent.putExtra(Const.EXTRA_ZHUANGJIANAME, zhuangjiaName);
        startActivity(intent);
    }
    
    private String[] getPlayerNames() {
        String[] names;
        if(mPlayerNumber == 4) {
            names = new String[4];
            names[3] = mPlayerName4.getText().toString();
        } else {
            names = new String[3];
        }
        names[0] = mPlayerName1.getText().toString();
        names[1] = mPlayerName2.getText().toString();
        names[2] = mPlayerName3.getText().toString();
        
        return names;
    }
    
    private void updateZhuangjia() {
        String[] names = getPlayerNames();
        ArrayAdapter<String> zhuangjiaAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, names);
        zhuangjiaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mZhuangjiaSpinner.setAdapter(zhuangjiaAdapter);
    }
    
    private void showErrorDialog() {
        Toast.makeText(this, R.string.cannot_start, Toast.LENGTH_SHORT).show();
    }
    
    private void showAbout() {
        Intent intent = new Intent();
        intent.setClass(this, AboutActivity.class);
        startActivity(intent);
    }
    
    private void showHelp() {
        Intent intent = new Intent();
        intent.setClass(this, HelpActivity.class);
        startActivity(intent);
    }
}