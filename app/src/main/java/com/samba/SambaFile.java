package com.samba;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

// SmbFile类中很多数据需要在后台线程中获取，在UI线程中直接使用SmbFile非常不方便，
// 另外直接获取SmbFile类的父文件夹也不是非常方便，
// 基于以上两点, 重新定义一个新的SambaFile类解决以上问题。
public class SambaFile {

    private SmbFile mSmbFile;
    private SambaFile mParentFile;
    private String mName;
    private String mPath;
    private boolean mIsDirectory;

    // 构造函数，需要在非UI线程中调用
    public SambaFile(SmbFile smbFile, SambaFile parentFile) throws SambaException {
        try {
            mSmbFile = smbFile;
            mParentFile = parentFile;
            mName = getName(mSmbFile);
            mPath = mSmbFile.getPath();
            mIsDirectory = smbFile.isDirectory();
        } catch (SmbException e) {
            throw new SambaException(e.getMessage());
        }
    }

    public SmbFile getSmbFile() {
        return mSmbFile;
    }

    public SambaFile getParent() {
        return mParentFile;
    }

    public String getName() {
//        if(isDirectory()){
//            if (!TextUtils.isEmpty(mName)) {
//                mName = mName.substring(0, mName.length() - 1);
//            }
//        }
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public String getFullPath() {
        return getPath() + getName();
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public boolean isImage() {
        String name = getName().toLowerCase();
        return name.endsWith(".png") ||
                name.endsWith(".jpg") ||
                name.endsWith(".jpeg") ||
                name.endsWith(".bmp") ||
                name.endsWith(".gif");
    }

    private static String getName(SmbFile smbFile) {
        String name = smbFile.getName();
        return name;
    }
}
