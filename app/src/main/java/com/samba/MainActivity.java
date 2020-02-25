package com.samba;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {

    private static final String TAG = "samba";

    private UniAddress mDomain;
    private NtlmPasswordAuthentication mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onAuthenticationClick(View view) {
        findViewById(R.id.btn_auth).setEnabled(false);

        // when attempts to perform a networking operation on its main thread,
        // the application would throw an exception.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip = "192.168.0.1";

                    // if you didn't set username and password for samba server,
                    // you must set the both variables to null,
                    // otherwise it would throw SmbException when login
                    String username = null;
                    String password = null;

                    mDomain = UniAddress.getByName(ip);
                    mAuthentication = new NtlmPasswordAuthentication(ip, username, password);
                    SmbSession.logon(mDomain, mAuthentication);
                } catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "samba服务访问地址出错", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.btn_auth).setEnabled(true);
                        }
                    });
                    return;
                } catch (SmbException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "访问授权samba服务出错", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.btn_auth).setEnabled(true);
                        }
                    });
                    return;
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "访问授权samba服务出错", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.btn_auth).setEnabled(true);
                        }
                    });
                    return;
                }

                // if no any exception occurs, it could said everything is ok.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "访问授权samba服务成功", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.btn_explore).setEnabled(true);
                    }
                });
            }
        }).start();
    }

    public void onExploreClick(View view) {
        // when attempts to perform a networking operation on its main thread,
        // the application would throw an exception.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 获取跟目录然后获取下面各个盘符
                    String ip = "192.168.0.1";
                    String rootPath = "smb://" + ip + "/";
                    SmbFile rootFolder = new SmbFile(rootPath, mAuthentication);
                    final String[] files = rootFolder.list();
                    for (String file : files) {
                        Log.d(TAG, file);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (files != null && files.length > 0) {
                                Toast.makeText(MainActivity.this, "根目录发现" + files.length + "个文件", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "根目录未发现文件", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (SmbException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
