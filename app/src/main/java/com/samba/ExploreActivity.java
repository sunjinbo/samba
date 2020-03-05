package com.samba;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samba.dialogs.AddItemsDialog;
import com.samba.dialogs.ConfirmDeleteDialog;
import com.samba.dialogs.NewFolderDialog;
import com.samba.dialogs.NewTextFileDialog;
import com.samba.dialogs.RenameDialog;
import com.samba.dialogs.UpdateItemDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jcifs.smb.SmbFile;

public class ExploreActivity extends AppCompatActivity implements
        Runnable, AdapterView.OnItemClickListener,
        View.OnClickListener,
        AdapterView.OnItemLongClickListener,
        AddItemsDialog.DialogListener,
        NewFolderDialog.DialogListener,
        NewTextFileDialog.DialogListener,
        UpdateItemDialog.DialogListener,
        RenameDialog.DialogListener,
        ConfirmDeleteDialog.ConfirmListener{
    private static final String TAG = "samba";

    private TextView mStatusTextView;
    private ListView mListView;
    private SambaAdapter mAdapter;
    private SambaFile mCurrentFile;

    private ImageView mBtnAdd;

    private TextView mMovingText;
    private boolean mCopy;
    private View mMovingLayout;
    private String mMovingPath;
    private String mMoveingName;
    private TextView mUploadBtn;
    private TextView mCacheBtn;
    private RelativeLayout mProgressLayout;
    private ProgressBar mProgress;
    private TextView mProgressTitle;

    private static final int CHOOSE_FILE_CODE = 1;

    FileChangeListener listener;
    private String mOpenFilePath;
    private boolean isFileChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        mStatusTextView = findViewById(R.id.tv_status);
        mListView = findViewById(R.id.list_view);

        mBtnAdd = findViewById(R.id.btn_add);

        mBtnAdd.setOnClickListener(this);

        mMovingLayout = findViewById(R.id.moving_layout);
        mMovingText = (TextView) mMovingLayout.findViewById(R.id.moving_file_name);
        mMovingLayout.findViewById(R.id.accept_move).setOnClickListener(this);

        mMovingLayout.findViewById(R.id.decline_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovingLayout.setVisibility(View.GONE);
                mMovingPath = null;
            }
        });

        mUploadBtn = findViewById(R.id.btn_upload);

        mUploadBtn.setOnClickListener(this);

        mCacheBtn = findViewById(R.id.btn_cache);

        mCacheBtn.setOnClickListener(this);

        mProgressLayout = findViewById(R.id.progress_layout);
        mProgressLayout.setVisibility(View.GONE);

        mProgress = findViewById(R.id.update_progress);
        mProgress.setMax(100);

        mProgressTitle = findViewById(R.id.progress_title);

        mAdapter = new SambaAdapter(this, R.layout.item_view_smb_file);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mStatusTextView.setText("正在加载中...");
        PermisionUtils.verifyStoragePermissions(this);
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
        final SambaFile itemFile = mAdapter.getItem(i);
        final String path = itemFile.getPath();
        final String name = itemFile.getName();
        if(itemFile.isDirectory()){
            mStatusTextView.setText("正在加载中...");
            mCurrentFile = itemFile;
            new Thread(this).start();
        }else{
            //打开文件
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //缓存路径
                    final String toPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + name;
                    Log.d(TAG,"toPath="+toPath);
                    File toFile = new File(toPath);
                    if(!toFile.exists()){
                        //先下载到本地缓存
                        toFile = SambaModel.getModel().download(path, toPath, new SambaModel.OnDownloadListener() {
                            @Override
                            public void onStart() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.VISIBLE);
                                        mProgressTitle.setText("正在下载");
                                    }
                                });
                            }

                            @Override
                            public void onProgressUpdate(final Integer progress) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgress.setProgress(progress);
                                    }
                                });
                            }

                            @Override
                            public void onComplete() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    }

                    mOpenFilePath = toPath;
                    //增加文件监听，如果打开的文件内容被修改则上传最新的缓存文件替换smb服务器上的文件
                    if(listener != null){
                        listener.stopWatching();
                        listener = new FileChangeListener(toPath);
                        listener.startWatching();
                    }else{
                        listener = new FileChangeListener(toPath);
                        listener.startWatching();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                OpenFileUtils.openFile(ExploreActivity.this, toPath);
                            } catch (Exception e) {
                                Toast.makeText(ExploreActivity.this,"无可用打开方式",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"path = "+mAdapter.getItem(position).getPath());
        UpdateItemDialog.newInstance(mAdapter.getItem(position)).show(getSupportFragmentManager(), "update_item");
        return true;
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


    @Override
    public void onClick(View v) {
        if(v.getId() == mBtnAdd.getId()){

            //新建文件文件夹弹窗
            AddItemsDialog.newInstance().show(getSupportFragmentManager(), "add_items");

        }else if(v.getId() == R.id.accept_move){
            //选择粘贴按钮
            mMovingLayout.setVisibility(View.GONE);
            if (mMovingPath != null) {

                if (!mCopy) {
                    //移动
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String toPath = mCurrentFile.getPath() + mMoveingName;
                            if(!TextUtils.equals(mMovingPath,toPath)){
                                SambaModel.getModel().move(mMovingPath,toPath);
                                mMovingPath = null;
                                new Thread(ExploreActivity.this).start();
                            }
                        }
                    }).start();
                } else {
                    //复制
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String toPath = mCurrentFile.getPath() + mMoveingName;
                            SambaModel.getModel().copy(mMovingPath,toPath);
                            mMovingPath = null;
                            new Thread(ExploreActivity.this).start();
                        }
                    }).start();
                }
            }
        }else if(v.getId() == mUploadBtn.getId()){
            //文件上传按钮
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), CHOOSE_FILE_CODE);

        }else if(v.getId() == mCacheBtn.getId()){
            //展示本地缓存目录地址
            File destDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            Toast.makeText(this,destDir.getAbsolutePath(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                final Uri uri = data.getData();
                final String path = UriUtil.getPath(this,uri);
                Log.d(TAG,"choose file path="+path);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SambaModel.getModel().upload(ExploreActivity.this,uri, path,mCurrentFile.getPath(),new SambaModel.OnUploadListener() {
                            @Override
                            public void onStart() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.VISIBLE);
                                        mProgressTitle.setText("正在上传");
                                    }
                                });
                            }

                            @Override
                            public void onProgressUpdate(final Integer progress) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgress.setProgress(progress);
                                    }
                                });
                            }

                            @Override
                            public void onComplete() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                        new Thread(ExploreActivity.this).start();
                    }
                }).start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onOptionClick(int which, String path,String fname,boolean isDirectory) {
        if(isDirectory && !TextUtils.isEmpty(path) && !TextUtils.isEmpty(fname)){
            path = path.substring(0,path.length()-1);
            fname = fname.substring(0,fname.length()-1);
        }
        switch (which) {
            case R.id.new_file:
                NewTextFileDialog.newInstance().show(getSupportFragmentManager(), "new_file_dialog");
                break;
            case R.id.new_folder:
                NewFolderDialog.newInstance().show(getSupportFragmentManager(), "new_folder_dialog");
                break;
            case R.id.delete:
                ConfirmDeleteDialog.newInstance(path,isDirectory).show(getSupportFragmentManager(), "confirm_delete");
                break;
            case R.id.rename:
                RenameDialog.newInstance(path,fname).show(getSupportFragmentManager(), "rename");
                break;
            case R.id.move:
                mMovingText.setText(getString(R.string.moving_file, fname));
                mMovingPath = path;
                mMoveingName = fname;
                mCopy = false;
                mMovingLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.copy:
                mMovingText.setText(getString(R.string.copy_file, fname));
                mMovingPath = path;
                mMoveingName = fname;
                mCopy = true;
                mMovingLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.download:
                //下载
                final String finalPath = path;
                final String finalFname = fname;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String toPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + finalFname;
                        File file = SambaModel.getModel().download(finalPath,toPath,new SambaModel.OnDownloadListener() {
                            @Override
                            public void onStart() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.VISIBLE);
                                        mProgressTitle.setText("正在下载");
                                    }
                                });
                            }

                            @Override
                            public void onProgressUpdate(final Integer progress) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgress.setProgress(progress);
                                    }
                                });
                            }

                            @Override
                            public void onComplete() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressLayout.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });

                    }
                }).start();
                break;
        }
    }

    @Override
    public void onNewFolder(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String  fullPath = mCurrentFile.getPath() + "" + name;
                Log.d(TAG,"fullPath="+fullPath);
                SambaModel.getModel().createFolder(fullPath);
                new Thread(ExploreActivity.this).start();
            }
        }).start();
    }

    @Override
    public void onNewFile(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String  fullPath = mCurrentFile.getPath() + "" + name;
                Log.d(TAG,"fullPath="+fullPath);
                SambaModel.getModel().createFile(fullPath);
                new Thread(ExploreActivity.this).start();
            }
        }).start();
    }


    @Override
    public void onRename(final String fromPath, final String toPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //同时重命名缓存文件
                boolean renameSuccess = SambaModel.getModel().rename(fromPath,toPath);
                if(renameSuccess){
                    String cacheFromFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + SambaUtil.getFileName(fromPath);
                    String newFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + SambaUtil.getFileName(toPath);
                    File cacheFile = new File(cacheFromFilePath);
                    File newFile = new File(newFilePath);
                    if(cacheFile.exists()){
                        cacheFile.renameTo(newFile);
                    }
                }
                new Thread(ExploreActivity.this).start();
            }
        }).start();
    }

    @Override
    public void onConfirmDelete(final boolean isDirectory,final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //同时删除缓存文件
                String cacheFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + SambaUtil.getFileName(path);
                if(isDirectory){
                    cacheFilePath = cacheFilePath.substring(0,cacheFilePath.length()-1);
                    SambaModel.getModel().delete(path + "/");
                }else{
                    SambaModel.getModel().delete(path);
                }
                File cacheFile = new File(cacheFilePath);
                if(cacheFile.exists()){
                    cacheFile.delete();
                }
                new Thread(ExploreActivity.this).start();
            }
        }).start();
    }

    public class FileChangeListener extends FileObserver {


        public FileChangeListener(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            switch (event){
                case FileObserver.MODIFY:
                    //这里文件通过第三方app打开修改保存后会执行两次（不清楚为啥），为了避免多次更新，做个标示位
                    if(!isFileChanged){
                        isFileChanged = true;
                        //上传文件
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SambaModel.getModel().upload(ExploreActivity.this,Uri.fromFile(new File(mOpenFilePath)), mOpenFilePath,mCurrentFile.getPath(),new SambaModel.OnUploadListener() {
                                    @Override
                                    public void onStart() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressLayout.setVisibility(View.VISIBLE);
                                                mProgressTitle.setText("正在上传");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onProgressUpdate(final Integer progress) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgress.setProgress(progress);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onComplete() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressLayout.setVisibility(View.GONE);
                                            }
                                        });
                                        isFileChanged = false;
                                    }
                                });
                                new Thread(ExploreActivity.this).start();
                            }
                        }).start();
                    }
                    break;
            }
        }
    }

}
