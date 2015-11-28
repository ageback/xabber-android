/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 *
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 *
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.xabber.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.xabber.android.R;
import com.xabber.android.data.Application;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.intent.AccountIntentBuilder;

import cn.net.wesoft.android.utils.MD5;

/**
 * Dialog with password request for authentication.
 *
 * @author alexander.ivanov
 */
public class PasswordRequest extends ManagedDialog {

    private String account;

    private EditText passwordView;
    private CheckBox storePasswordView;
    private CheckBox md5EncryptView;

    public static Intent createIntent(Context context, String account) {
        return new AccountIntentBuilder(context, PasswordRequest.class)
                .setAccount(account).build();
    }

    private static String getAccount(Intent intent) {
        return AccountIntentBuilder.getAccount(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(android.R.id.button3).setVisibility(View.GONE);
        View layout = getLayoutInflater().inflate(R.layout.password_request,
                null);
        passwordView = (EditText) layout.findViewById(R.id.account_password);
        storePasswordView = (CheckBox) layout.findViewById(R.id.store_password);
        md5EncryptView=(CheckBox)layout.findViewById(R.id.account_request_password_md5);
        account = getAccount(getIntent());
        if (AccountManager.getInstance().getAccount(account) == null) {
            Application.getInstance().onError(R.string.NO_SUCH_ACCOUNT);
            finish();
            return;
        }
        setDialogTitle(AccountManager.getInstance().getVerboseName(account));
        setCustomView(layout, true);
    }

    @Override
    public void onAccept() {
        super.onAccept();
        AccountManager.getInstance().setPassword(account,
                storePasswordView.isChecked(),
                md5EncryptView.isChecked()? MD5.MD5(passwordView.getText().toString()):passwordView.getText().toString());
        finish();
    }

    @Override
    public void onDecline() {
        AccountManager.getInstance().removePasswordRequest(account);
        finish();
    }

}
