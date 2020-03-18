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

public class DownloadActivity extends Activity implements View.OnClickListener, Runnable, SambaModel.OnDownloadListener {
    private static final String TAG = "samba";

    private Button mDownloadButton;
    private TextView mDownloadTextView;
    private boolean mIsDownloading = false;

    private int mDownloadCount = 0;
    private int mDownloadRemain = 200;

    private List<Long> mDownloadSpeedList = new ArrayList<>();
    private long mStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mDownloadButton = findViewById(R.id.btn_download);
        mDownloadTextView = findViewById(R.id.tv_download_status);
        mDownloadTextView.setText(mDownloadCount + "/" + mDownloadRemain);
        mDownloadButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!mIsDownloading) {
            mIsDownloading = true;
            reset();
            mDownloadTextView.setText(mDownloadCount + "/" + mDownloadRemain);
            mDownloadButton.setEnabled(false);
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        try {
            File downloadImgFile = new File(DownloadActivity.this.getExternalCacheDir(), mDownloadCount + ".jpg");
            SambaModel.getModel().download("smb://192.168.0.1/sdb1/big_picture/0.jpg",
                    downloadImgFile.getAbsolutePath(),
                    DownloadActivity.this);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDownloadStarted() {
        mStartTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onDownloadProgressUpdated(Integer progress) {

    }

    @Override
    public void onDownloadCompleted() {
        long interval = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(TAG, "完成一个图片的下载，大约耗时" + ((float)interval / 1000) + "秒");
        mDownloadSpeedList.add(interval);

        mDownloadCount += 1;
        mDownloadRemain -= 1;

        if (mDownloadRemain > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadTextView.setText(mDownloadCount + "/" + mDownloadRemain);
                }
            });
            new Thread(this).start();
        } else {
            long total = 0;
            long max = 0;
            long min = Long.MAX_VALUE;
            for (int i = 0; i < mDownloadSpeedList.size(); ++i) {
                total += mDownloadSpeedList.get(i);

                if (max < mDownloadSpeedList.get(i)) {
                    max = mDownloadSpeedList.get(i);
                }

                if (min > mDownloadSpeedList.get(i)) {
                    min = mDownloadSpeedList.get(i);
                }
            }
            final float average = total / mDownloadSpeedList.size() / 1000;
            final float max_value = max / 1000;
            final float min_value = min / 1000;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsDownloading = false;
                            mDownloadButton.setEnabled(true);
                            mDownloadTextView.setText("平均" + average + "秒 最大" + max_value + "秒 最小" + min_value + "秒");
                        }
                    });
                }
            });
        }
    }

    private void reset() {
        mDownloadCount = 0;
        mDownloadRemain = 200;
    }
}
