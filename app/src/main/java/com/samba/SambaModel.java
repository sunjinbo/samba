package com.samba;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

public class SambaModel {
    private static final String TAG = "samba";

    private static SambaModel sModel = new SambaModel();

    private UniAddress mDomain;
    private NtlmPasswordAuthentication mAuthentication;
    private boolean mConnected = false;

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
            String ip = mDomain.getHostAddress();
            String rootPath = "smb://" + ip + "/";
            SmbFile rootFolder = new SmbFile(rootPath, mAuthentication);
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

        String ip = mDomain.getHostAddress();
        String rootPath = "smb://" + ip + "/";
        return rootPath;
    }
}
