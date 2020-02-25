package com.samba;

import android.app.Application;

public class SambaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");
    }
}
