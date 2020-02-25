package com.samba;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {

    private UniAddress mDomain;
    private NtlmPasswordAuthentication mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onAuthenticationClick(View view) {
        try {
            String ip = "192.168.0.1";
            String username = "";
            String password = "";

            mDomain = UniAddress.getByName(ip);
            mAuthentication = new NtlmPasswordAuthentication(ip, username, password);
            SmbSession.logon(mDomain, mAuthentication);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

    public void onExploreClick(View view) {
        try {
            // 获取跟目录然后获取下面各个盘符
            String ip = "192.168.0.1";
            String rootPath = "smb://" + ip + "/";
            SmbFile mRootFolder = new SmbFile(rootPath);
            // 匿名登录即无需登录
//            if (mSpu.isAnonymous()) {
//                mRootFolder = new SmbFile(rootPath);
//            } else {
//                mRootFolder = new SmbFile(rootPath, mAuthentication);
//            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
