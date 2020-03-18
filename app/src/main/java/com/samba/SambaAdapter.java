package com.samba;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import jcifs.smb.SmbFile;

public class SambaAdapter extends ArrayAdapter<SambaFile> {
    private static final String TAG = "samba";

    private int mResourceId;
    private Activity mActivity;

    public SambaAdapter(@NonNull Activity activity, int resourceId) {
        super(activity, resourceId);
        mActivity = activity;
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SambaFile smbFile = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (smbFile.isDirectory()) {
            Log.d(TAG, "Load directory icon");
            holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.folder));
        } else {
            if (smbFile.isImage()) {
                // 如果是图片，则应该显示该图片的缩略图，
                // 但是由于不能在UI线程中调用SmbFile.getInputStream()来获取输入流，
                // 这里我们需要启动异步调用来实现该需求。
                asyncLoadThumbnail(holder.icon, smbFile);
            } else {
                Log.d(TAG, "Load document icon");
                holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.document));
            }
        }

        String name = smbFile.getName();
        if(smbFile.isDirectory()){
            if (!TextUtils.isEmpty(name)) {
                name = name.substring(0, name.length() - 1);
            }
        }
        holder.name.setText(name);

        return convertView;
    }

    private void asyncLoadThumbnail(final ImageView imageView, final SambaFile sambaFile) {
        Log.d(TAG, "Async load thumbnail");

        final String tag = Md5Utils.md5(sambaFile.getFullPath());
        imageView.setTag(tag);
        imageView.setImageDrawable(null);

        File thumbnailFile = new File(getContext().getExternalCacheDir(), tag);
        if (thumbnailFile.exists()) { // 查看本地是否缓存了该图片的缩略图
            // 如果存在，直接打开图片文件并设置给ImageView
            Log.d(TAG, "Load local thumbnail");
            Bitmap thumb = BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());
            imageView.setImageBitmap(thumb);
        } else {
            // 如果不存在，则启动一个后台线程来下载该图片
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    SmbFile smbFile = sambaFile.getSmbFile();

                    String smbTag = Md5Utils.md5(sambaFile.getFullPath());
                    String imgTag = (String)imageView.getTag();
                    if (TextUtils.equals(imgTag, smbTag)) {
                        InputStream inputStream = null;
                        try {
                            inputStream = smbFile.getInputStream();

                            Log.d(TAG, "Start to load samba thumbnail - " + sambaFile.getName());
                            Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                            Log.d(TAG, "Successfully load samba thumbnail - " + sambaFile.getName());

                            final Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap,
                                    bitmap.getWidth() / 8,
                                    bitmap.getHeight() / 8);
                            saveBitmap(thumb, tag);

                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }

                            if (!mActivity.isFinishing() && !mActivity.isDestroyed()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "set thumbnail - " + sambaFile.getName());
                                        imageView.setImageBitmap(thumb);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            try {
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                }
            };
            new Thread(runnable).start();
        }
    }

    private void saveBitmap(Bitmap bitmap, String tag) {
        Log.e(TAG, "保存图片");
        File f = new File(getContext().getExternalCacheDir(), tag);
        if (f.exists()) {
            f.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.d(TAG, "save thumbnail - " + f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int bytes = read();
                    if (bytes < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    private class ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolder(View view) {
            icon = view.findViewById(R.id.icon);
            name = view.findViewById(R.id.name);
        }
    }
}
