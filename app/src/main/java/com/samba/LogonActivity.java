package com.samba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogonActivity extends Activity {

    private static final String TAG = "samba";

    private EditText mDomainEditText;
    private Button mAuthButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);
        mDomainEditText = findViewById(R.id.edt_domain);
        mAuthButton = findViewById(R.id.btn_auth);
    }

    public void onAuthenticationClick(View view) {
        final String domain = mDomainEditText.getText().toString();
        if (TextUtils.isEmpty(domain)) {
            Toast.makeText(LogonActivity.this, "samba服务访问地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        mDomainEditText.setEnabled(false);
        mAuthButton.setEnabled(false);

        // when attempts to perform a networking operation on its main thread,
        // the application would throw an exception.
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean ret = SambaModel.getModel().logon(domain);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ret) {
                            startActivity(new Intent(LogonActivity.this, ExploreActivity.class));
                            LogonActivity.this.finish();
                        } else {
                            mDomainEditText.setEnabled(true);
                            mAuthButton.setEnabled(true);
                            Toast.makeText(LogonActivity.this, "访问授权samba服务出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
