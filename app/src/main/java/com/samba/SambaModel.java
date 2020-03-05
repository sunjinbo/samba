package com.samba;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;

public class SambaModel {

    private static final String TAG = "samba";

    public final static String SMB_URL_LAN = "smb://";

    public final static int IO_BUFFER_SIZE = 1 * 1024;

    private static SambaModel sModel = new SambaModel();

    private UniAddress mDomain;
    private NtlmPasswordAuthentication mAuthentication;
    private boolean mConnected = false;

    private OnDownloadListener downloadListener;
    private OnUploadListener uploadListener;

    private SambaModel() {}

    public static SambaModel getModel() {
        return sModel;
    }

    // 连接没有设置任何密码的Samba服务，注意必须在后台线程内调用
    public boolean logon(String domain) {
        return logon(domain, null, null);
    }

    // 连接设置有用户名和密码的Samba服务，注意必须在后台线程内调用
    public boolean logon(String domain, String username, String password) {
        try {
            mDomain = UniAddress.getByName(domain);
            mAuthentication = new NtlmPasswordAuthentication(domain, username, password);
            SmbSession.logon(mDomain, mAuthentication);
            mConnected = true;
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
        } catch (SmbException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return mConnected;
    }


    // 获取根目录下SMB文件列表，注意必须在后台线程内调用
    public SmbFile[] listFiles() throws SambaException {
        if (!mConnected) {
            throw new SambaException("Bad authentication!");
        }

        SmbFile[] smbFiles;

        try {
            SmbFile rootFolder = new SmbFile(root(), mAuthentication);
            smbFiles = rootFolder.listFiles();
        } catch (MalformedURLException e) {
            throw new SambaException(e.getMessage());
        } catch (SmbException e) {
            throw new SambaException(e.getMessage());
        }

        return smbFiles;
    }

    // 获取指定目录下SMB文件列表，注意必须在后台线程内调用
    public SmbFile[] listFiles(SmbFile smbFile) throws SambaException {
        if (!mConnected) {
            throw new SambaException("Bad authentication!");
        }

        SmbFile[] smbFiles;

        try {
            smbFiles = smbFile.listFiles();
        } catch (SmbException e) {
            throw new SambaException(e.getMessage());
        }

        return smbFiles;
    }

    // 获取根目录下URL地址连接
    public String root() throws SambaException {
        if (!mConnected) {
            throw new SambaException("Bad authentication!");
        }

        String rootPath = new StringBuilder(SMB_URL_LAN).append(mAuthentication.getDomain()).append("/").toString();;

        Log.d(TAG,"rootPath = "+rootPath);

        return rootPath;
    }

    public SambaFile getFile(String path) {
        try {
            SmbFile smbFile = new SmbFile(path,mAuthentication);
            SambaFile sambaFile = new SambaFile(smbFile, null);
            return sambaFile;
        } catch (SambaException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 创建文件
    public void createFile(String fullPath) {
        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile(fullPath,mAuthentication);
            if (!remoteFile.exists()) {
                remoteFile.createNewFile();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    // 创建文件夹
    public void createFolder(String fullPath) {
        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile(fullPath,mAuthentication);
            if (!remoteFile.exists()) {
                remoteFile.mkdir();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    // 删除文件
    public void delete(String path) {
        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile(path,mAuthentication);
            if (remoteFile.exists()) {
                remoteFile.delete();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    // 文件重命名
    public boolean rename(String fromPath, String toPath) {
        SmbFile fromFile = null;
        SmbFile renameFile = null;
        try {
            fromFile = new SmbFile(fromPath,mAuthentication);
            renameFile = new SmbFile(toPath,mAuthentication);
            if(renameFile.exists()){
                //无法重命名，此名称文件已存在
                return false;
            }

            if(fromFile.exists()){
                fromFile.renameTo(renameFile);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 文件移动
    public void move(String mMovingPath, String toPath) {
        SmbFile moveFile = null;
        SmbFile toFile = null;
        try {
            moveFile = new SmbFile(mMovingPath,mAuthentication);
            toFile = new SmbFile(toPath,mAuthentication);

            if(moveFile.exists()){
                moveFile.copyTo(toFile);
                moveFile.delete();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    // 文件复制
    public void copy(String mMovingPath, String toPath) {
        SmbFile moveFile = null;
        SmbFile toFile = null;
        try {
            moveFile = new SmbFile(mMovingPath,mAuthentication);
            toFile = new SmbFile(toPath,mAuthentication);

            if(moveFile.exists()){
                moveFile.copyTo(toFile);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    public File download(String fromPath, String toPath, OnDownloadListener listener) {
        this.downloadListener = listener;
        return download(fromPath,toPath);
    }

    //文件下载
    public File download(String fromPath, String toPath) {
        File toFile = null;
        SmbFile fromFile = null;
        try {
            fromFile = new SmbFile(fromPath,mAuthentication);

            toFile = new File(toPath);
            if(!toFile.exists()){
                toFile.createNewFile();
            }
            final SmbFileInputStream inS = new SmbFileInputStream(fromFile);
            final FileOutputStream outS = new FileOutputStream(toFile);
            final long size = fromFile.length();

            if(downloadListener != null){
                downloadListener.onStart();
            }

            writeAndCloseStream(inS, outS, size);

            if(downloadListener != null){
                downloadListener.onComplete();
            }

            return toFile;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return toFile;
    }

    public  void writeAndCloseStream(InputStream ins, OutputStream outs, final long totalSize) throws IOException {
        byte[] tmp = new byte[IO_BUFFER_SIZE];
        int length;
        float uploaded = 0f;
        final long startMills = System.currentTimeMillis();
        final long LOG_PRINT_DURATION_DIVIDER = 500;
        long lastMills = startMills;
        try {
            while ((length = ins.read(tmp)) != -1) {
                outs.write(tmp, 0, length);
                uploaded += length;

                //DELTA TIME
                final long currentMill = System.currentTimeMillis();
                final long deltaMill = currentMill - lastMills;
                if ((deltaMill <= LOG_PRINT_DURATION_DIVIDER && uploaded < totalSize) || deltaMill <= 0) {
                    continue;
                }
                lastMills = currentMill;

                //SPEED
                final float speed = ((float) length) / deltaMill;
                final float avegSpeed = uploaded / (currentMill - startMills);

                //PROGRESS
                float progress = (totalSize <= 0) ? -1 : (uploaded * 100) / totalSize;

                if(downloadListener != null){
                    downloadListener.onProgressUpdate((int)progress);
                }
                if(uploadListener != null){
                    uploadListener.onProgressUpdate((int)progress);
                }

                Log.d(TAG, "writeAndCloseStream progress:" + progress + "   transfered=" + uploaded + "    speed=" + speed + "/" + avegSpeed);

            }
        } catch (Exception e) {
            throw e;
        } finally {
            ins.close();
            outs.flush();
            outs.close();
        }
    }

    public void upload(Context ctx,Uri uri , String sdCardFilePath, String smbCurPath, OnUploadListener listener) {
        this.uploadListener = listener;
        upload(ctx,uri,sdCardFilePath,smbCurPath);
    }

    //文件上传
    public void upload(Context ctx,Uri uri,String sdCardFilePath, String smbCurPath) {

        ParcelFileDescriptor parcelFileDescriptor = null;
        FileInputStream inS = null;
        File sdFile = null;
        SmbFile toFile = null;
        try {
            sdFile = new File(sdCardFilePath);
            String toPath = smbCurPath + sdFile.getName();
            toFile = new SmbFile(toPath,mAuthentication);
            if(!toFile.exists()){
                toFile.createNewFile();
            }
            parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(uri, "r", null);
            inS = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            SmbFileOutputStream outS = new SmbFileOutputStream(toFile);
            try {
                if(uploadListener != null){
                    uploadListener.onStart();
                }
                writeAndCloseStream(inS, outS, sdFile.length());

                if(uploadListener != null){
                    uploadListener.onComplete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public interface OnDownloadListener{
        void onStart();
        void onProgressUpdate(Integer progress);
        void onComplete();
    }

    public interface OnUploadListener{
        void onStart();
        void onProgressUpdate(Integer progress);
        void onComplete();
    }

}
