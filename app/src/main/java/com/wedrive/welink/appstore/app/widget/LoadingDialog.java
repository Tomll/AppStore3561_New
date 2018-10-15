package com.wedrive.welink.appstore.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;


/**
 * Loading View
 * 
 * @author yuwei
 * 
 */
public class LoadingDialog extends Dialog {

    private MainActivity mMainActivity;
    private TextView mTip;
    private ProgressBar mProgressBar;
    
    public LoadingDialog(Context context) {
        super(context, R.style.MyDialog);
        setContentView(R.layout.dialog_loading);
        mMainActivity=(MainActivity)context;
        mTip = (TextView)findViewById(R.id.loading_tip);
        mProgressBar=(ProgressBar)findViewById(R.id.loading_progress);
    }

    public void setIndeterminate(boolean flag){
        mProgressBar.setIndeterminate(flag);
    }

    public void setCancelable(boolean flag){
        setCanceledOnTouchOutside(flag);
    }


    public void setTip(String message) {
        mTip.setText(message);
    }
    
    public void show() {
        if(mMainActivity!=null && !mMainActivity.isFinishing()){
            super.show();
        }
    }

}
