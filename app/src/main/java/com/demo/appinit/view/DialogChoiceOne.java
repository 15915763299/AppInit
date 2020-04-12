package com.demo.appinit.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.demo.appinit.R;


/**
 * 单选弹窗
 */
public abstract class DialogChoiceOne extends Dialog implements View.OnClickListener {

    private CharSequence info;

    public DialogChoiceOne(Context context) {
        super(context, R.style.BaseDialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choice_one);
        setViewParams();

        TextView txInfo = findViewById(R.id.tx_info);
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(this);
        txInfo.setText(info);
        setCanceledOnTouchOutside(false);
    }

    private void setViewParams() {
        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn) {
            this.dismiss();
            click();
        }
    }

    public void setInfo(CharSequence info) {
        this.info = info;
    }

    public abstract void click();
}