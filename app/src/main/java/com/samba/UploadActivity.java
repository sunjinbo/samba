package com.samba;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadActivity extends Activity implements View.OnClickListener, Runnable, SambaModel.OnUploadListener {
    private static final String TAG = "samba";

    private Button mUploadButton;
    private TextView mUploadTextView;
    private boolean mIsUploading = false;

    private int mUploadCount = 0;
    private int mUploadRemain = 200;

    private List<Long> mUploadSpeedList = new ArrayList<>();
    private long mStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mUploadButton = findViewById(R.id.btn_upload);
        mUploadTextView = findViewById(R.id.tv_upload_status);
        mUploadTextView.setText(mUploadCount + "/" + mUploadRemain);
        mUploadButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!mIsUploading) {
            mIsUploading = true;
            reset();
            mUploadTextView.setText(mUploadCount + "/" + mUploadRemain);
            mUploadButton.setEnabled(false);
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        try {
            File uploadImgFile = new File(UploadActivity.this.getExternalCacheDir(), "upload.jpg");
            SambaModel.getModel().upload(uploadImgFile.getAbsolutePath(),
                    "smb://192.168.0.1/sdb1/big_picture/" + mUploadCount + ".jpg",
                    UploadActivity.this);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onUploadStarted() {
        mStartTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onUploadProgressUpdated(Integer progress) {

    }

    @Override
    public void onUploadCompleted() {
        long interval = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(TAG, "完成一个图片的上传载，大约耗时" + ((float)interval) / 1000 + "秒");
        mUploadSpeedList.add(interval);

        mUploadCount += 1;
        mUploadRemain -= 1;

        if (mUploadRemain > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUploadTextView.setText(mUploadCount + "/" + mUploadRemain);
                }
            });
            new Thread(this).start();
        } else {
            long total = 0;
            long max = 0;
            long min = Long.MAX_VALUE;
            for (int i = 0; i < mUploadSpeedList.size(); ++i) {
                total += mUploadSpeedList.get(i);

                if (max < mUploadSpeedList.get(i)) {
                    max = mUploadSpeedList.get(i);
                }

                if (min > mUploadSpeedList.get(i)) {
                    min = mUploadSpeedList.get(i);
                }
            }
            final float average = (float)total / mUploadSpeedList.size() / 1000;
            final float max_value = (float)max / 1000;
            final float min_value = (float)min / 1000;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        mIsUploading = false;
                        mUploadButton.setEnabled(true);
                        mUploadTextView.setText("平均" + average + "秒 最大" + max_value + "秒 最小" + min_value + "秒");
                        }
                    });
                }
            });
        }
    }

    private void reset() {
        mUploadCount = 0;
        mUploadRemain = 200;
    }
}
