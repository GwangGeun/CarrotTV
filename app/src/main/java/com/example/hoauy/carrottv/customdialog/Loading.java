package com.example.hoauy.carrottv.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.hoauy.carrottv.R;

/**
 *  1. 정의
 *
 *  사용자에게 데이터를 받아오는 동안 보여주는 '로딩중' 화면 (다이얼로그)
 *
 *
 *  2. 쓰이는 곳
 *
 *   (1) FindPathActivity01
 *
 *      : AR 길찾기 시 현재위치 받아오는 동안 사용자에게 본 다이얼로그를 보여줌
 *
 *   (2) FindPathActivity01
 *
 *     : T map 길찾기 시 현재위치 받아오는 동안 사용자에게 본 다이얼로그를 보여줌
 *
 *
 */

public class Loading extends Dialog {

    Context context;
    String loading_title;

    public Loading(Context context, String loading_title){

        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
        this.loading_title = loading_title;
    }

    TextView text_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.6f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;

        setContentView(R.layout.dialog_loading);
        text_loading = (TextView)findViewById(R.id.text_loading);

        text_loading.setText(loading_title);

    }


}
