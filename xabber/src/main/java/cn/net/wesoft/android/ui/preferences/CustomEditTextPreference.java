package cn.net.wesoft.android.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.xabber.android.R;

import java.util.Date;

import cn.net.wesoft.android.utils.MD5;

/**
 * Created by Peter on 2015/11/25.
 */
public class CustomEditTextPreference extends DialogPreference {
    private LinearLayout mView;
    private EditText pswdTxt;
    private CheckBox chkMD5;

    private String newPassword="";
    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.account_change_password);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mView = (LinearLayout) view;
        pswdTxt=(EditText)mView.findViewById(R.id.account_new_password);
        chkMD5=(CheckBox)mView.findViewById(R.id.account_change_password_md5);
    }



    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            // get value from editFields, do whatever you want here :)
            // you can acces them through mView variable very easily
            String plainPsd=pswdTxt.getText().toString();
            newPassword=chkMD5.isChecked()? MD5.MD5(plainPsd):plainPsd;
            persistString(newPassword);
        }
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, index);
    }
}
