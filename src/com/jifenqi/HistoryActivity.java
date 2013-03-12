package com.jifenqi;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HistoryActivity extends Activity implements AdapterView.OnItemClickListener
        , View.OnClickListener{

    private static final int MODE_VIEW = 0;
    private static final int MODE_DELETE = 1;
    
    private static final int PROGRESS_DELETE_ID = 1;
    
    private ListView mListView;
    private BaseAdapter mListAdapter;
    private BaseAdapter mDeleteAdapter;
    private Button mBatchButton, mCancelButton;
    private View mDeleteLayout;
    
    private int mMode = MODE_VIEW;
    DeleteTask mCurrentDeleteTask;
    
    private class DeleteTask extends AsyncTask<int[], Void, Void> {

        @Override
        public void onPreExecute() {
            showDialog(PROGRESS_DELETE_ID);
        }
        
        @Override
        protected Void doInBackground(int[]... params) {
            int[] positions = params[0];
            for(int position = 0; position < positions.length; position++) {
                deleteHistory(position);
            }
            return null;
        }
        
        @Override
        public void onPostExecute(Void result) {
            mCurrentDeleteTask = null;
            dismissDialog(PROGRESS_DELETE_ID);
            updateFileAdapter();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_layout);
        
        mListView = (ListView)findViewById(R.id.list);
        mDeleteLayout = findViewById(R.id.delete_layout);
        mBatchButton = (Button)findViewById(R.id.delete_select);
        mCancelButton = (Button)findViewById(R.id.cancel_delete);
        mBatchButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mBatchButton.setEnabled(false);
        
        updateFileAdapter();
    }
    
    private void updateFileAdapter() {
        View view = findViewById(R.id.no_history);
        String zipaiDir = Utils.getZipaiDir();
        File historyPath = new File(zipaiDir);
        String[] historyFiles = historyPath.list(new ZipaiHistoryFileFilter());
        if(historyFiles == null || historyFiles.length == 0) {
            view.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mDeleteLayout.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            
            mListAdapter = new HistoryAdapter(this,
                    android.R.layout.simple_list_item_1, historyFiles);
            mDeleteAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_multiple_choice, historyFiles);
            mListView.setOnCreateContextMenuListener(this);
            updateList();
        }
    }
    
    private void updateButtons() {
        int count = 0;
        SparseBooleanArray positions = mListView.getCheckedItemPositions();
        if(positions != null) {
            for(int i = 0; i < mListView.getCount(); i++) {
                if(positions.get(i)) {
                    count++;
                }
            }
        }
        if(count == 0) {
            mBatchButton.setEnabled(false);
        } else {
            mBatchButton.setEnabled(true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mMode != MODE_VIEW)
            return false;
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.zipai_history_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.batch_delete:
                mMode = MODE_DELETE;
                updateList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if(mMode != MODE_VIEW) {
            return;
        }
        if(mListView.getCount() == 0) {
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.zipai_history_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                int[] positions = new int[1];
                positions[0] = info.position;
                getDeleteTask().execute(positions);
                
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    private void deleteHistory(int position) {
        String fileName = (String)mListView.getItemAtPosition(position);
        String filePath = Utils.getFilePath(fileName);
        PersistenceUtils.deleteHistory(filePath);
    }
    
    
    private void updateList() {
        if(mMode == MODE_VIEW) {
            mDeleteLayout.setVisibility(View.GONE);
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(this);
        } else {
            mDeleteLayout.setVisibility(View.VISIBLE);
            mListView.setAdapter(mDeleteAdapter);
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mListView.setOnItemClickListener(this);
        }
        updateButtons();
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if(mMode == MODE_VIEW) {
            String fileName = (String)parent.getAdapter().getItem(position);
            Intent intent= new Intent();
            intent.putExtra(Const.EXTRA_HISTORY, true);
            intent.putExtra(Const.EXTRA_HISTORY_FILENAME, fileName);
            intent.setClass(this, ZipaiGameActivity.class);
            startActivity(intent);
        } else {
            updateButtons();
        }
    }
    
    private static class HistoryAdapter extends ArrayAdapter<String> {

        public HistoryAdapter(Context context, int textViewResourceId,
                String[] objects) {
            super(context, textViewResourceId, objects);
        }
        
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = super.getView(position, convertView, parent);
//            
//            return view;
//        }
        
//        public String getItem(int position) {
//            String fileName = (String)super.getItem(position);
//            
//            int start = Const.ZIPAI.length() + 1;
//            int end = fileName.lastIndexOf(".");
//            String displayName = fileName.substring(start, end);
//            
//            return displayName;
//        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
        case PROGRESS_DELETE_ID:
            ProgressDialog deleteDialog = new ProgressDialog(this);
            deleteDialog.setMessage(getResources().getText(R.string.deleting));
            deleteDialog.setIndeterminate(true);
            deleteDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(mCurrentDeleteTask != null) {
                        mCurrentDeleteTask.cancel(true);
                    }
                }
            });
            dialog = deleteDialog;
            break;
        }
        
        return dialog;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
        case R.id.delete_select:
            doBacthDelete();
            break;
        case R.id.cancel_delete:
            mMode = MODE_VIEW;
            updateList();
            break;
        }
    }
    
    private void doBacthDelete() {
        SparseBooleanArray positions = mListView.getCheckedItemPositions();
        int[] array = new int[mListView.getCount()];
        int j = 0;
        for(int i = 0; i < mListView.getCount(); i++) {
            if(positions.get(i)) {
                array[j++] = i;
            }
        }
        int[] params = new int[j];
        System.arraycopy(array, 0, params, 0, j);
        getDeleteTask().execute(params);
    }
    
    private DeleteTask getDeleteTask() {
        mCurrentDeleteTask = new DeleteTask();
        return mCurrentDeleteTask;
    }
}
