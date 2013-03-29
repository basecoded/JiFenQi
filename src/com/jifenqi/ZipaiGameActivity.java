package com.jifenqi;

import java.util.ArrayList;

import com.jifenqi.GameInfo.RoundInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ZipaiGameActivity extends Activity implements View.OnClickListener,
    View.OnLongClickListener,
    AdapterView.OnItemClickListener{

    private static final int RECORDROUNDDIALOG_ID = 1;
    private static final int NEWGAME_CONTINUE_ID = 2;
    private static final int FINISHGAME_ID = 3;
    private static final int STARTPOINTS_ID = 4;
    private static final int ERRORNUMBER_ID = 5;
    private static final int CHANGE_PLAYER_NAME_ID = 6;
    private static final int DISCARDGAME_ID = 7;
    private static final int ERRORDIALOG_ID = 8;
    private static final int PROGRESS_LOAD_ID = 9;
    private static final int EDIT_ROUND_ID = 10;
    private GameInfo mGameInfo;
    private ListView mRoundList;
    private View[] mPlayersView;
    private boolean mGameSaved;
    private boolean mIsHistory;
    private ColorStateList mInitTextColor;
    
    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        @Override
        public void onPreExecute() {
            showDialog(PROGRESS_LOAD_ID);
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            String fileName = params[0];
            String filePath = Utils.getFilePath(fileName);
            mGameInfo = PersistenceUtils.doLoad(filePath);
            
            boolean ret = false;
            if(mGameInfo != null) {
                mGameInfo.refreshPointsCache(0);
                ret = true;
            }
            
            return ret;
        }
        
        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                dismissDialog(PROGRESS_LOAD_ID);
                initView();
            } else {
                Toast.makeText(ZipaiGameActivity.this, R.string.error_load, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private class RoundAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        
        public RoundAdapter(Context context) {
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            //The first item is the start points.
            return mGameInfo.mPointsCache.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.zipai_round, parent, false);
            } else {
                view = convertView;
            }
            
            TextView tv = (TextView)view.findViewById(R.id.round);
            tv.setText(Integer.toString(position));
            int[] points = mGameInfo.mPointsCache.get(position);
            tv = (TextView)view.findViewById(R.id.player1_point);
            tv.setText(Integer.toString(points[0]));
            tv = (TextView)view.findViewById(R.id.player2_point);
            tv.setText(Integer.toString(points[1]));
            tv = (TextView)view.findViewById(R.id.player3_point);
            tv.setText(Integer.toString(points[2]));
            tv = (TextView)view.findViewById(R.id.player4_point);
            if(mGameInfo.mPlayerNumber == 4) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(Integer.toString(points[3]));
            } else {
                tv.setVisibility(View.GONE);
            }
            
            //background
            if(position % 2 == 0) {
                view.setBackgroundResource(R.drawable.item_bg);
            } else {
                view.setBackgroundResource(R.drawable.item_bg_2);
            }
            
            return view;
        }
        
    }
    
    private static class FangPaoPair {
        public String playerName;
        public int playerId;
        
        public FangPaoPair(String name, int id) {
            playerName = name;
            playerId = id;
        }
        
        public String toString() {
            return playerName;
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zipai_game);
        
        setupGame();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        if(!mGameSaved) {
            saveLastGame();
        }
        super.onPause();
    }
    
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(Const.KEY_RESUME, true);
        if(!mIsHistory) {
            Utils.putBooleanSP(this, Const.KEY_RESUME, true);
        }
    }
    
    @Override
    public void onBackPressed () {
        if(!mIsHistory) {
            showDialog(FINISHGAME_ID);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mIsHistory) {
            return false;
        }
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.zipai_menu, menu);
        return true;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        View view = null;
        LayoutInflater factory = LayoutInflater.from(this);
        switch(id) {
        case NEWGAME_CONTINUE_ID:
            view = factory.inflate(R.layout.zipai_new_game_continue, null);
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.newgame_continue)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            newGameContinue((Dialog)dialog);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
    
                        }
                    })
                    .create();
        case FINISHGAME_ID:
            return new AlertDialog.Builder(this)
                .setTitle(R.string.finish_game_title)
                .setMessage(R.string.finish_game_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        saveGame();
                        Utils.putBooleanSP(ZipaiGameActivity.this, Const.KEY_RESUME, false);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                    }
                })
                .create();
        case DISCARDGAME_ID:
            return new AlertDialog.Builder(this)
            .setTitle(R.string.discard_game)
            .setMessage(R.string.discard_game_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Utils.putBooleanSP(ZipaiGameActivity.this, Const.KEY_RESUME, false);
                    finish();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            })
            .create();
        case STARTPOINTS_ID:
            view = factory.inflate(R.layout.zipai_start_points, null);
            return new AlertDialog.Builder(this)
            .setTitle(R.string.hint_start_point)
            .setView(view)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    editStartPoints((Dialog)dialog);
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            })
            .create();
        case ERRORNUMBER_ID:
            return Utils.getErrorDigitDialog(this);
        case ERRORDIALOG_ID:
            return Utils.getErrorDigitDialog(this);
        case CHANGE_PLAYER_NAME_ID:
            view = factory.inflate(R.layout.zipai_change_player_name, null);
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.change_player_name_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Dialog d = (Dialog)dialog;
                            TextView tv2 = (TextView) d.findViewById(R.id.change_player_name);
                            CharSequence name = tv2.getText();
                            if(name != null && name.length() != 0) {
                                int viewId = (Integer)tv2.getTag();
                                View v = findViewById(viewId);
                                TextView tv = (TextView)v.findViewById(R.id.player_name);
                                tv.setText(name);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
    
                        }
                    })
                    .create();
        case RECORDROUNDDIALOG_ID:
            view = factory.inflate(R.layout.zipai_record_round, null);
            RecordRoundDialog dialog = new RecordRoundDialog(this);
            dialog.setTitle(R.string.record_point);
            dialog.setView(view);
            CharSequence text = getResources().getText(android.R.string.ok);
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    RecordRoundDialog ad = (RecordRoundDialog)dialog;
                    doRecordRound(ad);
                }
            });
            text = getResources().getText(android.R.string.cancel);
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    
                }
            });
            mRecordRoundView = view;
            return dialog;
        case PROGRESS_LOAD_ID:
            ProgressDialog deleteDialog = new ProgressDialog(this);
            deleteDialog.setMessage(getResources().getText(R.string.loading));
            deleteDialog.setIndeterminate(true);
//            deleteDialog.setOnDismissListener(new OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                }
//            });
            return deleteDialog;
        }
        return null;
    }
    
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch(id) {
        case NEWGAME_CONTINUE_ID:
            prepareNewGameDialog(dialog);
            break;
        case CHANGE_PLAYER_NAME_ID:
            int viewId = args.getInt(Const.EXTRA_PLAYERVIEW_ID);
            View v = findViewById(viewId);
            TextView tv = (TextView)v.findViewById(R.id.player_name);
            TextView tv2 = (TextView) dialog.findViewById(R.id.change_player_name);
            tv2.setText(tv.getText());
            tv2.setTag(viewId);
            break;
        case RECORDROUNDDIALOG_ID:
            prepareRecordRoundDialog(dialog, args);
            break;
        case STARTPOINTS_ID:
            prepareStartPointsDialog(dialog);
            break;
        }
    }
    
    
    protected int[] getRemainPoints(Dialog dialog) {
        try {
            int[] startPoints = new int[mGameInfo.mPlayerNumber];
            TextView tv = (TextView)dialog.findViewById(R.id.player1_start_point);
            startPoints[0] = Integer.parseInt(tv.getText().toString());
            
            tv = (TextView)dialog.findViewById(R.id.player2_start_point);
            startPoints[1] = Integer.parseInt(tv.getText().toString());
            
            tv = (TextView)dialog.findViewById(R.id.player3_start_point);
            startPoints[2] = Integer.parseInt(tv.getText().toString());
            
            if(mGameInfo.mPlayerNumber == 4) {
                tv = (TextView)dialog.findViewById(R.id.player4_start_point);
                startPoints[3] = Integer.parseInt(tv.getText().toString());
            }
            return startPoints;
        } catch(NumberFormatException e) {
            
        }
        
        return null;
    }

    private void prepareNewGameDialog(Dialog dialog) {
        int[] remainPoints = mGameInfo.getRemainPoints();
        int total = 0;
        for(int point : remainPoints) {
            total += point;
        }
        TextView wrong_points = (TextView)dialog.findViewById(R.id.wrong_remain_points);
        if(total != 0) {
            wrong_points.setVisibility(View.VISIBLE);
        } else {
            wrong_points.setVisibility(View.GONE);
        }
        
        TextView tv = (TextView)dialog.findViewById(R.id.player1_name);
        tv.setText(mGameInfo.mPlayerNames[0]);
        tv = (TextView)dialog.findViewById(R.id.player1_start_point);
        tv.setText(Integer.toString(remainPoints[0]));
        
        tv = (TextView)dialog.findViewById(R.id.player2_name);
        tv.setText(mGameInfo.mPlayerNames[1]);
        tv = (TextView)dialog.findViewById(R.id.player2_start_point);
        tv.setText(Integer.toString(remainPoints[1]));
        
        tv = (TextView)dialog.findViewById(R.id.player3_name);
        tv.setText(mGameInfo.mPlayerNames[2]);
        tv = (TextView)dialog.findViewById(R.id.player3_start_point);
        tv.setText(Integer.toString(remainPoints[2]));
        
        View v = dialog.findViewById(R.id.player4);
        CheckedTextView autoSeatView = (CheckedTextView)dialog.findViewById(R.id.auto_change_seat);
        autoSeatView.setOnClickListener(this);
        if(mGameInfo.mPlayerNumber == 4) {
            tv = (TextView)dialog.findViewById(R.id.player4_name);
            tv.setText(mGameInfo.mPlayerNames[3]);
            tv = (TextView)dialog.findViewById(R.id.player4_start_point);
            tv.setText(Integer.toString(remainPoints[3]));
        } else {
            v.setVisibility(View.GONE);
            autoSeatView.setVisibility(View.GONE);
            autoSeatView.setChecked(false);
        }
        
        Spinner zhuangjiaSpinner = (Spinner)dialog.findViewById(R.id.zhuangjia_spinner);
        CharSequence[] names = mGameInfo.mPlayerNames.clone();
        ArrayAdapter<CharSequence> zhuangjiaAdapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, names);
        zhuangjiaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zhuangjiaSpinner.setAdapter(zhuangjiaAdapter);
    }
    
    private void prepareRecordRoundDialog(Dialog dialog, Bundle args) {
        RecordRoundDialog rDialog = (RecordRoundDialog) dialog;
        RoundInfo ri = null;
        int type = args.getInt(Const.EXTRA_ROUND_DIALOG_TYPE);
        if(type == Const.ROUND_DIALOG_TYPE_NEW) {
            int hupaiPlayerId = args.getInt(Const.EXTRA_HUPAIPLAYER_ID, -1);
            ri = new RoundInfo();
            int zhuangjiaId = mGameInfo.getLastZhuangjiaId();
            ri.zhuangjiaId = zhuangjiaId;
            ri.hupaiPlayerId = hupaiPlayerId;
            
            rDialog.mHupaiPlayerId = hupaiPlayerId;
        } else if(type == Const.ROUND_DIALOG_TYPE_UPDATE) {
            int roundId = args.getInt(Const.EXTRA_ROUND_ID, -1);
            ri = mGameInfo.mRoundInfos.get(roundId);
            
            rDialog.mRoundId = roundId;
        }
        rDialog.mType = type;
        
        initRoundDialog(dialog, type, ri);
    }
    
    private void initRoundDialog(Dialog dialog, int type, RoundInfo ri) {
        View view = dialog.findViewById(R.id.record_round_layout);
        EditText huziView = (EditText)dialog.findViewById(R.id.huzishu);
        EditText shangxingView = (EditText)dialog.findViewById(R.id.shangxing);
        View xiaxingLayout = dialog.findViewById(R.id.xiaxing_layout);
        EditText xiaxingView = (EditText)dialog.findViewById(R.id.xiaxing);
        CheckBox zimoCheckBox = (CheckBox)dialog.findViewById(R.id.zimo);
        CheckBox huangzhuangCheckBox = (CheckBox)dialog.findViewById(R.id.huangzhuang);
        Spinner fangpaoSpinner = (Spinner)dialog.findViewById(R.id.fangpao_player_spinner);
        
        //Clear
        huziView.setText("");
        shangxingView.setText("");
        xiaxingView.setText("");
        zimoCheckBox.setChecked(false);
        huangzhuangCheckBox.setChecked(false);
        if(mGameInfo.mPlayerNumber == 3) {
            if(!mGameInfo.mHasShangxiaxing) {
                xiaxingLayout.setVisibility(View.GONE);
                TextView tv = (TextView)view.findViewById(R.id.shangxing_name);
                tv.setText(R.string.xing);
            }
        }
        
        huangzhuangCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                Spinner sp = (Spinner) mRecordRoundView.findViewById(R.id.fangpao_player_spinner);
                sp.setEnabled(!isChecked);
                if(isChecked) {
                    sp.setSelection(0); // Nobody fang pao.
                }
            }
        });
        
        ArrayList<FangPaoPair> items = new ArrayList<FangPaoPair>();
        items.add(new FangPaoPair("нч", -1));
        int zhuangjiaId = ri.zhuangjiaId;
        int shuxingPlayerId = Utils.getShuxingPlayer(zhuangjiaId);
        int faopaoPlayerPos = 0;
        for(int i = 0; i < mGameInfo.mPlayerNumber; i++) {
            if(ri.hupaiPlayerId == i
                    || (mGameInfo.mPlayerNumber == 4 && shuxingPlayerId == i)) {
                continue;
            }
            FangPaoPair fp = new FangPaoPair(mGameInfo.mPlayerNames[i].toString(), i);
            items.add(fp);
            if(ri.fangpaoPlayerId == i) {
                faopaoPlayerPos = i + 1; //Start from the second position
            }
        }
        ArrayAdapter<FangPaoPair > adapter = new ArrayAdapter<FangPaoPair>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fangpaoSpinner.setAdapter(adapter);
        fangpaoSpinner.setSelection(faopaoPlayerPos);
        fangpaoSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                FangPaoPair pair = (FangPaoPair)parent.getItemAtPosition(position);
                CheckBox cb = (CheckBox)mRecordRoundView.findViewById(R.id.zimo);
                if(pair.playerId != -1) {
                    cb.setEnabled(false);
                    cb.setChecked(false);
                } else {
                    cb.setEnabled(true);
                }
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        if(type == Const.ROUND_DIALOG_TYPE_UPDATE) {
            huziView.setText(Integer.toString(ri.huzishu));
            if(ri.shangxing != 0) {
                shangxingView.setText(Integer.toString(ri.shangxing));
            }
            if(ri.xiaxing != 0) {
                xiaxingView.setText(Integer.toString(ri.xiaxing));
            }
            if(ri.huzishu != 0) {
                huziView.setText(Integer.toString(ri.huzishu));
            }
            zimoCheckBox.setChecked(ri.zimo);
            huangzhuangCheckBox.setChecked(ri.hupaiPlayerId == 0 ? true : false);
        } else {
            
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.newgame_continue:
                showDialog(NEWGAME_CONTINUE_ID);
                return true;
            case R.id.discard_game:
                discardGame();
                return true;
            case R.id.history_game:
                Intent intent = new Intent();
                intent.setClass(this, HistoryActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        if(info.position == 0) {
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.zipai_context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_round:
                deleteRound(info.position);
                return true;
            case R.id.edit_round:
                editRound(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    private void setupGame() {
        Intent intent = getIntent();
        
        mIsHistory = intent.getBooleanExtra(Const.EXTRA_HISTORY, false);
        boolean isResumeGame = intent.getBooleanExtra(Const.EXTRA_RESUME_GAME, false);
        if(isResumeGame){
            String fileName = Const.LASTGAME_NAME;
            LoadTask task = new LoadTask();
            task.execute(fileName);
        } else if(mIsHistory) {
            String fileName = intent.getStringExtra(Const.EXTRA_HISTORY_FILENAME);
//            String filePath = Utils.getFilePath(fileName);
//            mGameInfo = PersistenceUtils.doLoad(filePath);
//            if(mGameInfo == null) {
//                Toast.makeText(this, R.string.error_history, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//            mGameInfo.refreshPointsCache(0);
            
            LoadTask task = new LoadTask();
            task.execute(fileName);
        } else {
            mGameInfo = new GameInfo();
            int playerNumber = intent.getIntExtra(Const.EXTRA_PLAYERNUMBER, 3);
            String[] playerNames = intent.getStringArrayExtra(Const.EXTRA_PLAYERNAMES);
            int[] startPoints = intent.getIntArrayExtra(Const.EXTRA_STARTPOINTS);
            if(startPoints == null) {
                startPoints = new int[playerNumber];
            }
            
            mGameInfo.mPlayerNumber = playerNumber;
            mGameInfo.mPlayerNames = playerNames;
            mGameInfo.mStartPoints = startPoints;
            mGameInfo.mHasShangxiaxing = intent.getBooleanExtra(Const.EXTRA_SHANGXIAXING, false);
            String zhuangjiaName = intent.getStringExtra(Const.EXTRA_ZHUANGJIANAME);
            for(int i = 0; i < mGameInfo.mPlayerNames.length; i++) {
                if(zhuangjiaName.equals(mGameInfo.mPlayerNames[i])) {
                    mGameInfo.mStartZhuangjiaId = i;
                    break;
                }
            }
            
            int[] ps = new int[mGameInfo.mPlayerNumber];
            System.arraycopy(startPoints, 0, ps, 0, ps.length);
            mGameInfo.initPoints(ps);
            
            initView();
        }
    }
    
    private void initView() {
        mPlayersView = new View[mGameInfo.mPlayerNumber];
        TextView tv;
        
        View view = findViewById(R.id.player1_info);
        mPlayersView[0] = view;
        tv = (TextView)view.findViewById(R.id.player_name);
        tv.setText(mGameInfo.mPlayerNames[0]);
        
        view = findViewById(R.id.player2_info);
        mPlayersView[1] = view;
        tv = (TextView)view.findViewById(R.id.player_name);
        tv.setText(mGameInfo.mPlayerNames[1]);
        
        view = findViewById(R.id.player3_info);
        mPlayersView[2] = view;
        tv = (TextView)view.findViewById(R.id.player_name);
        tv.setText(mGameInfo.mPlayerNames[2]);
        
        if(mGameInfo.mPlayerNumber == 4) {
            view = findViewById(R.id.player4_info);
            mPlayersView[3] = view;
            tv = (TextView)view.findViewById(R.id.player_name);
            tv.setText(mGameInfo.mPlayerNames[3]);
        } else {
            view = findViewById(R.id.player4_info);
            view.setVisibility(View.GONE);
            findViewById(R.id.player4_name_div).setVisibility(View.GONE);
        }
        
        if(!mIsHistory) {
            for(int i = 0; i < mPlayersView.length; i++) {
                mPlayersView[i].setOnClickListener(this);
                mPlayersView[i].setOnLongClickListener(this);
            }
        }
        
        tv = (TextView)findViewById(R.id.start_points);
        if(!mIsHistory) {
            tv.setOnClickListener(this);
        }
        
        mRoundList = (ListView)findViewById(R.id.list);
        RoundAdapter adapter = new RoundAdapter(this);
        mRoundList.setAdapter(adapter);
        //registerForContextMenu(mRoundList);
        if(!mIsHistory) {
            mRoundList.setOnCreateContextMenuListener(this);
            //init the last game
            saveLastGame();
        }
        
        mRoundList.setOnItemClickListener(this);
        
        //mInitTextColor = mPlayersView[0].getTextColors();
        updatePlayersView();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
        case R.id.player1_info:
        case R.id.player2_info:
        case R.id.player3_info:
        case R.id.player4_info:
            int hupaiPlayerId = getPlayerId(id);
            Bundle args = new Bundle();
            args.putInt(Const.EXTRA_ROUND_DIALOG_TYPE, Const.ROUND_DIALOG_TYPE_NEW);
            args.putInt(Const.EXTRA_HUPAIPLAYER_ID, hupaiPlayerId);
            showDialog(RECORDROUNDDIALOG_ID, args);
//            showRoundDialog(hupaiPlayerId);
            break;
        case R.id.start_points:
            showDialog(STARTPOINTS_ID);
            break;
        case R.id.auto_change_seat:
            CheckedTextView cv = (CheckedTextView)v;
            cv.toggle();
            break;
        }
    }
    
    private void updatePlayersView() {
        if(mIsHistory) {
            return;
        }
        
        int zhuangjiaId = mGameInfo.getLastZhuangjiaId();
        int shuxingPlayerId = Utils.getShuxingPlayer(zhuangjiaId);
        for (int i = 0; i < mPlayersView.length; i++) {
            TextView tv = (TextView)mPlayersView[i].findViewById(R.id.player_status);
            if (zhuangjiaId == i) {
                //mPlayersView[i].setBackgroundResource(R.drawable.zhuangjia_bg);
                //mPlayersView[i].setTextColor(0xFFFF8800);
                tv.setVisibility(View.VISIBLE);
                tv.setText(R.string.zhuang);
            } else {
                //mPlayersView[i].setBackgroundResource(R.drawable.item_bg_2);
                //mPlayersView[i].setTextColor(mInitTextColor);
                tv.setVisibility(View.GONE);
            }
            if (mGameInfo.mPlayerNumber == 4) {
                if (shuxingPlayerId == i) {
                    mPlayersView[i].setEnabled(false);
                    mPlayersView[i].findViewById(R.id.player_name).setEnabled(false);
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(R.string.xing);
                } else {
                    mPlayersView[i].setEnabled(true);
                    mPlayersView[i].findViewById(R.id.player_name).setEnabled(true);
                }
            }
        }
    }
    
    private View mRecordRoundView;
    private void showRoundDialog(final int hupaiPlayerId) {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.zipai_record_round, null);
        mRecordRoundView = view;
        if(mGameInfo.mPlayerNumber == 3) {
            if(!mGameInfo.mHasShangxiaxing) {
                view.findViewById(R.id.xiaxing_layout).setVisibility(View.GONE);
                TextView tv = (TextView)view.findViewById(R.id.shangxing_name);
                tv.setText(R.string.xing);
            }
        }
        CheckBox cb = (CheckBox)view.findViewById(R.id.zimo);
        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                Spinner sp = (Spinner) mRecordRoundView.findViewById(R.id.fangpao_player_spinner);
                sp.setEnabled(!isChecked);
                if(isChecked) {
                    sp.setSelection(0); // Nobody fang pao.
                }
            }
        });
        
        Spinner sp = (Spinner)view.findViewById(R.id.fangpao_player_spinner);
        ArrayList<FangPaoPair> items = new ArrayList<FangPaoPair>();
        items.add(new FangPaoPair("нч", -1));
        int zhuangjiaId = mGameInfo.getLastZhuangjiaId();
        int shuxingPlayerId = Utils.getShuxingPlayer(zhuangjiaId);
        for(int i = 0; i < mGameInfo.mPlayerNumber; i++) {
            if(hupaiPlayerId == i
                    || (mGameInfo.mPlayerNumber == 4 && shuxingPlayerId == i)) {
                continue;
            }
            FangPaoPair fp = new FangPaoPair(mGameInfo.mPlayerNames[i].toString(), i);
            items.add(fp);
        }
        ArrayAdapter<FangPaoPair > adapter = new ArrayAdapter<FangPaoPair>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                FangPaoPair pair = (FangPaoPair)parent.getItemAtPosition(position);
                CheckBox cb = (CheckBox)mRecordRoundView.findViewById(R.id.zimo);
                if(pair.playerId != -1) {
                    cb.setEnabled(false);
                    cb.setChecked(false);
                } else {
                    cb.setEnabled(true);
                }
            }
            
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.record_point)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AlertDialog ad = (AlertDialog)dialog;
                        
                        try {
                            TextView huziView = (TextView)ad.findViewById(R.id.huzishu);
                            CharSequence huziString = huziView.getText();
                            int huzi = 0;
                            if(huziString != null && huziString.length() != 0) {
                                huzi = Integer.parseInt(huziString.toString());
                            }
                            TextView tv = (TextView)ad.findViewById(R.id.shangxing);
                            CharSequence point = tv.getText();
                            int shangxing = 0;
                            if(point != null && point.length() != 0) {
                                shangxing = Integer.parseInt(point.toString());
                            }
                            tv = (TextView)ad.findViewById(R.id.xiaxing);
                            point = tv.getText();
                            int xiaxing = 0;
                            if(point != null && point.length() != 0) {
                                xiaxing = Integer.parseInt(point.toString());
                            }
                            CheckBox cb = (CheckBox)ad.findViewById(R.id.zimo);
                            boolean isZimo = cb.isChecked();
                            int fangpaoPlayer = -1;
                            Spinner sp = (Spinner) ad.findViewById(R.id.fangpao_player_spinner);
                            FangPaoPair pair = (FangPaoPair)sp.getSelectedItem();
                            fangpaoPlayer = pair.playerId;
                            cb = (CheckBox)ad.findViewById(R.id.huangzhuang);
                            int theHupaiPlayerId = hupaiPlayerId;
                            if(cb.isChecked()) {
                                theHupaiPlayerId = -1;
                                huzi = 0;
                                shangxing = 0;
                                xiaxing = 0;
                                isZimo = false;
                                fangpaoPlayer = -1;
                            }
                            if(huzi < 0) {
                                //cha huzi
                                shangxing = 0;
                                xiaxing = 0;
                                isZimo = false;
                                fangpaoPlayer = -1;
                            }
                            int zhuangjiaId = mGameInfo.getLastZhuangjiaId();
                            recordRound(zhuangjiaId, theHupaiPlayerId, huzi, shangxing, xiaxing, isZimo, fangpaoPlayer);
                        } catch(NumberFormatException e) {
                            showDialog(ERRORNUMBER_ID);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        
                    }
                })
                .create().show();
    }
    
    private void recordRound(int zhuangjiaId, int hupaiPlayerId, int huzi, int shangxing, int xiaxing, boolean isZimo, int fangpaoPlayer) {
        mGameInfo.addRoundResult(zhuangjiaId, hupaiPlayerId, huzi, shangxing, xiaxing, isZimo, fangpaoPlayer);

        updatePlayersView();
        refreshRoundList();
    }
    
    private void recordRound(RoundInfo ri) {
        mGameInfo.addRoundResult(ri);
        
        updatePlayersView();
        refreshRoundList();
    }
    
    private void updateRound(int roundId, RoundInfo ri) {
        mGameInfo.updateRound(roundId, ri);
        
        updatePlayersView();
        refreshRoundList();
        updateDetailView(roundId);
    }
    
    private int getPlayerId(int viewId) {
        int playerId = 0;
        if(viewId == R.id.player1_info) {
            playerId = 0;
        } else if(viewId == R.id.player2_info) {
            playerId = 1;
        } else if(viewId == R.id.player3_info) {
            playerId = 2;
        } else if(viewId == R.id.player4_info) {
            playerId = 3;
        }
        
        return playerId;
    }

    private void refreshRoundList() {
        BaseAdapter adapter = (BaseAdapter) mRoundList.getAdapter();
        adapter.notifyDataSetChanged();
        mRoundList.invalidate();
    }
    
    private void updateDetailView(int position) {
        View view = mRoundList.getChildAt(position);
        View detailView = view.findViewById(R.id.round_detail_layout);
        updateRoundDetail(position, detailView);
    }
    
    private void saveLastGame() {
        PersistenceUtils.saveZipaiLastGame(mGameInfo);
    }
    
    private void newGameContinue(Dialog dialog) {
        int[] remainPoints = getRemainPoints(dialog);
        if(remainPoints == null) {
            showDialog(ERRORNUMBER_ID);
            return;
        }
        int[] startPoints = remainPoints;
        
        CheckedTextView autoSeatView = (CheckedTextView) dialog.findViewById(R.id.auto_change_seat);
        boolean isAutoChangeSeat = autoSeatView.isChecked();
        
        preNewGameContinue();
        
        int playerNumber = mGameInfo.mPlayerNumber;
        Intent newIntent = new Intent();
        
        if(playerNumber == 4 && !isAutoChangeSeat) {
            newIntent.setClass(this, ChooseSeatActivity.class);
        } else {
            newIntent.setClass(this, ZipaiGameActivity.class);
        }
        
        Spinner zhuangjiaSpinner = (Spinner)dialog.findViewById(R.id.zhuangjia_spinner);
        CharSequence zhuangjiaName = (CharSequence)zhuangjiaSpinner.getSelectedItem();
        
        String[] playerNames = mGameInfo.mPlayerNames;
        if(isAutoChangeSeat) {
            int zhuangjiaIndex;
            for(zhuangjiaIndex = 0; zhuangjiaIndex < playerNumber; zhuangjiaIndex++) {
                if(zhuangjiaName.equals(mGameInfo.mPlayerNames[zhuangjiaIndex])) {
                    break;
                }
            }
            String[] newPlayerNames = new String[playerNumber];
            int[] newStartPoints = new int[playerNumber];
            for(int i = 0; i < playerNumber; i++) {
                int nextIndex = (i+1) % playerNumber;
                if(nextIndex == zhuangjiaIndex) {
                    nextIndex++;
                    nextIndex %= playerNumber;
                } else if(i == zhuangjiaIndex){
                    nextIndex = i;
                }
                newPlayerNames[nextIndex] = mGameInfo.mPlayerNames[i];
                newStartPoints[nextIndex] = startPoints[i];
            }
            startPoints = newStartPoints;
            playerNames = newPlayerNames;
        }
        newIntent.putExtra(Const.EXTRA_STARTPOINTS, startPoints);
        newIntent.putExtra(Const.EXTRA_PLAYERNAMES, playerNames);
        newIntent.putExtra(Const.EXTRA_PLAYERNUMBER, playerNumber);
        newIntent.putExtra(Const.EXTRA_SHANGXIAXING, mGameInfo.mHasShangxiaxing);
        newIntent.putExtra(Const.EXTRA_ZHUANGJIANAME, zhuangjiaName);
        
        startActivity(newIntent);
        finish();
    }
    
    private void preNewGameContinue() {
        saveGame();
    }

    private void saveGame() {
        //Don't save if there is only the start points
        if(mGameInfo.mRoundInfos.size() >= 2) {
            String filePath = Const.ZIPAI + "_" + mGameInfo.mStartTime + ".xml";
            PersistenceUtils.saveGame(mGameInfo, filePath);
            mGameSaved = true;
        }
    }
    
    private void discardGame() {
        showDialog(DISCARDGAME_ID);
    }
    
    private void showAbout() {
        Intent intent = new Intent();
        intent.setClass(this, AboutActivity.class);
        startActivity(intent);
    }
    
    private void deleteRound(int position) {
        //TODO: move to another thread?
        mGameInfo.deleteRound(position);
        
        updatePlayersView();
        refreshRoundList();
    }
    
    private void editRound(int position) {
        Bundle args = new Bundle();
        args.putInt(Const.EXTRA_ROUND_DIALOG_TYPE, Const.ROUND_DIALOG_TYPE_UPDATE);
        args.putInt(Const.EXTRA_ROUND_ID, position);
        showDialog(RECORDROUNDDIALOG_ID, args);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        View detailView = view.findViewById(R.id.round_detail_layout);
        if(detailView.getVisibility() == View.VISIBLE) {
            detailView.setVisibility(View.GONE);
        } else {
            detailView.setVisibility(View.VISIBLE);
            updateRoundDetail(position, detailView);
        }
    }

    private void updateRoundDetail(int position, View detailView) {
        TextView tv;
        RoundInfo ri = mGameInfo.mRoundInfos.get(position);
        tv = (TextView)detailView.findViewById(R.id.zhuangjia_name);
        tv.setText(mGameInfo.mPlayerNames[ri.zhuangjiaId]);
        
        if(position == 0) {
            View xiaxingLayout = detailView.findViewById(R.id.xiaxing_layout);
            if(mGameInfo.mHasShangxiaxing) {
                xiaxingLayout.setVisibility(View.VISIBLE);
                tv = (TextView)detailView.findViewById(R.id.xiaxing);
            } else {
                xiaxingLayout.setVisibility(View.GONE);
            }
            
            tv = (TextView)detailView.findViewById(R.id.zimo);
            tv.setVisibility(View.GONE);
        } else {
            
            
            tv = (TextView)detailView.findViewById(R.id.hupai_player_name);
            if(ri.hupaiPlayerId != -1) {
                tv.setText(mGameInfo.mPlayerNames[ri.hupaiPlayerId]);
            } else {
                tv.setText("нч");
            }
            tv = (TextView)detailView.findViewById(R.id.huzishu);
            tv.setText(Integer.toString(ri.huzishu));
            
            tv = (TextView)detailView.findViewById(R.id.shangxing);
            tv.setText(Integer.toString(ri.shangxing));
            if(!mGameInfo.mHasShangxiaxing) {
                tv = (TextView)detailView.findViewById(R.id.shangxing_title);
                tv.setText(R.string.xing);
            }
            
            View xiaxingLayout = detailView.findViewById(R.id.xiaxing_layout);
            if(mGameInfo.mHasShangxiaxing) {
                xiaxingLayout.setVisibility(View.VISIBLE);
                tv = (TextView)detailView.findViewById(R.id.xiaxing);
                tv.setText(Integer.toString(ri.xiaxing));
            } else {
                xiaxingLayout.setVisibility(View.GONE);
            }
            
            tv = (TextView)detailView.findViewById(R.id.zimo);
            if(ri.zimo) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(R.string.zimo);
            } else if(ri.hupaiPlayerId < 0){
                tv.setVisibility(View.VISIBLE);
                tv.setText(R.string.huangzhuang);
            } else if(ri.huzishu < 0){
                tv.setVisibility(View.VISIBLE);
                tv.setText(R.string.chahuzi);
            } else {
                tv.setVisibility(View.GONE);
            }
            if(ri.fangpaoPlayerId != -1) {
                tv = (TextView)detailView.findViewById(R.id.fangpao_player);
                tv.setText(mGameInfo.mPlayerNames[ri.fangpaoPlayerId]);
            } else {
                
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        switch(id) {
        case R.id.player1_info:
        case R.id.player2_info:
        case R.id.player3_info:
        case R.id.player4_info:
            Bundle args = new Bundle();
            args.putInt(Const.EXTRA_PLAYERVIEW_ID, id);
            showDialog(CHANGE_PLAYER_NAME_ID, args);
            return true;
        }
        return false;
    }
    
//    private void setupIntent(Intent intent, GameInfo gi, boolean isAutoChangeSeat) {
//        String[] playerNames;
//        intent.putExtra(Const.EXTRA_PLAYERNUMBER, gi.mPlayerNumber);
//        intent.putExtra(Const.EXTRA_PLAYERNAMES, gi.mPlayerNames);
//        intent.putExtra(Const.EXTRA_SHANGXIAXING, gi.mHasShangxiaxing);
//    }
    
    private void doRecordRound(RecordRoundDialog dialog) {
        try {
            View view = dialog.findViewById(R.id.record_round_layout);
            
            TextView huziView = (TextView)dialog.findViewById(R.id.huzishu);
            CharSequence huziString = huziView.getText();
            int huzi = 0;
            if(huziString != null && huziString.length() != 0) {
                huzi = Integer.parseInt(huziString.toString());
            }
            TextView tv = (TextView)dialog.findViewById(R.id.shangxing);
            CharSequence point = tv.getText();
            int shangxing = 0;
            if(point != null && point.length() != 0) {
                shangxing = Integer.parseInt(point.toString());
            }
            tv = (TextView)dialog.findViewById(R.id.xiaxing);
            point = tv.getText();
            int xiaxing = 0;
            if(point != null && point.length() != 0) {
                xiaxing = Integer.parseInt(point.toString());
            }
            CheckBox cb = (CheckBox)dialog.findViewById(R.id.zimo);
            boolean isZimo = cb.isChecked();
            int fangpaoPlayer = -1;
            Spinner sp = (Spinner) dialog.findViewById(R.id.fangpao_player_spinner);
            FangPaoPair pair = (FangPaoPair)sp.getSelectedItem();
            fangpaoPlayer = pair.playerId;
            cb = (CheckBox)dialog.findViewById(R.id.huangzhuang);
            
            int type = dialog.mType;
            RoundInfo ri = null;
            int theHupaiPlayerId;
            int zhuangjiaId;
            if(type == Const.ROUND_DIALOG_TYPE_NEW) {
                ri = new RoundInfo();
                theHupaiPlayerId = dialog.mHupaiPlayerId;
                zhuangjiaId = mGameInfo.getLastZhuangjiaId();
            } else {
                ri = mGameInfo.mRoundInfos.get(dialog.mRoundId);
                theHupaiPlayerId = ri.hupaiPlayerId;
                zhuangjiaId = ri.zhuangjiaId;
            }
            if(cb.isChecked()) {
                theHupaiPlayerId = -1;
                huzi = 0;
                shangxing = 0;
                xiaxing = 0;
                isZimo = false;
                fangpaoPlayer = -1;
            }
            if(huzi < 0) {
                //cha huzi
                shangxing = 0;
                xiaxing = 0;
                isZimo = false;
                fangpaoPlayer = -1;
            }
            
            ri.zhuangjiaId = zhuangjiaId;
            ri.hupaiPlayerId = theHupaiPlayerId;
            ri.huzishu = huzi;
            ri.shangxing = shangxing;
            ri.xiaxing = xiaxing;
            ri.zimo = isZimo;
            ri.fangpaoPlayerId = fangpaoPlayer;
            
            if(type == Const.ROUND_DIALOG_TYPE_NEW) {
                recordRound(ri);
            } else if(type == Const.ROUND_DIALOG_TYPE_UPDATE){
                updateRound(dialog.mRoundId, ri);
            }
//            recordRound(zhuangjiaId, theHupaiPlayerId, huzi, shangxing, xiaxing, isZimo, fangpaoPlayer);
        } catch(NumberFormatException e) {
            showDialog(ERRORNUMBER_ID);
        }
    }
    
    
    private void prepareStartPointsDialog(Dialog dialog) {
        try {
            TextView tv = (TextView)dialog.findViewById(R.id.player1_name);
            tv.setText(mGameInfo.mPlayerNames[0] +": ");
            tv = (TextView)dialog.findViewById(R.id.player1_start_point);
            tv.setText(Integer.toString(mGameInfo.mStartPoints[0]));
            tv = (TextView)dialog.findViewById(R.id.player2_name);
            tv.setText(mGameInfo.mPlayerNames[1] +": ");
            tv = (TextView)dialog.findViewById(R.id.player2_start_point);
            tv.setText(Integer.toString(mGameInfo.mStartPoints[1]));
            tv = (TextView)dialog.findViewById(R.id.player3_name);
            tv.setText(mGameInfo.mPlayerNames[2] +": ");
            tv = (TextView)dialog.findViewById(R.id.player3_start_point);
            tv.setText(Integer.toString(mGameInfo.mStartPoints[2]));
            if(mGameInfo.mPlayerNumber == 4) {
                tv = (TextView)dialog.findViewById(R.id.player4_name);
                tv.setText(mGameInfo.mPlayerNames[3] +": ");
                tv = (TextView)dialog.findViewById(R.id.player4_start_point);
                tv.setText(Integer.toString(mGameInfo.mStartPoints[3]));
            } else {
                dialog.findViewById(R.id.player4_name).setVisibility(View.GONE);
            }
            tv = (TextView)dialog.findViewById(R.id.start_zhuangjia_name);
            tv.setText(mGameInfo.mPlayerNames[mGameInfo.mStartZhuangjiaId]);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void editStartPoints(Dialog dialog) {
        int[] startPoints;
        startPoints = new int[mGameInfo.mPlayerNumber];
        try {
            TextView tv = (TextView)dialog.findViewById(R.id.player1_start_point);
            startPoints[0] = Integer.parseInt(tv.getText().toString());
            tv = (TextView)dialog.findViewById(R.id.player2_start_point);
            startPoints[1] = Integer.parseInt(tv.getText().toString());
            tv = (TextView)dialog.findViewById(R.id.player3_start_point);
            startPoints[2] = Integer.parseInt(tv.getText().toString());
            if(mGameInfo.mPlayerNumber == 4) {
                tv = (TextView)dialog.findViewById(R.id.player4_start_point);
                startPoints[3] = Integer.parseInt(tv.getText().toString());
            }
            mGameInfo.mStartPoints = startPoints;
            mGameInfo.refreshPointsCache(0);
            refreshRoundList();
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
