package com.samba;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;

public class ExploreActivity extends Activity implements Runnable, AdapterView.OnItemClickListener {
    private static final String TAG = "samba";

    private TextView mStatusTextView;
    private ListView mListView;
    private SambaAdapter mAdapter;
    private SambaFile mCurrentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        mStatusTextView = findViewById(R.id.tv_status);
        mListView = findViewById(R.id.list_view);
        mAdapter = new SambaAdapter(this, R.layout.item_view_smb_file);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        mStatusTextView.setText("正在加载中...");
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            final String root = SambaModel.getModel().root();

            SmbFile[] smbFiles;
            if (mCurrentFile == null) {
                smbFiles = SambaModel.getModel().listFiles();
            } else {
                smbFiles = SambaModel.getModel().listFiles(mCurrentFile.getSmbFile());
            }

            final List<SambaFile> sambaFileList = new ArrayList<>();
            if (smbFiles != null) {
                for (SmbFile smbFile : smbFiles) {
                    sambaFileList.add(new SambaFile(smbFile, mCurrentFile));
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentFile == null) {
                        mStatusTextView.setText(root);
                    } else {
                        mStatusTextView.setText(mCurrentFile.getPath());
                    }

                    mAdapter.clear();
                    mAdapter.addAll(sambaFileList);
                    mAdapter.notifyDataSetChanged();
                }
            });
        } catch (SambaException e) {
            Log.e(TAG, e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExploreActivity.this, "加载文件列表发生错误", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mCurrentFile = mAdapter.getItem(i);
        mStatusTextView.setText("正在加载中...");
        new Thread(this).start();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFile == null) {
            super.onBackPressed();
        } else {
            if (mCurrentFile.getParent() == null) {
                mCurrentFile = null;
                mStatusTextView.setText("正在加载中...");
                new Thread(this).start();
            } else {
                mCurrentFile = mCurrentFile.getParent();
                mStatusTextView.setText("正在加载中...");
                new Thread(this).start();
            }
        }
    }
}
