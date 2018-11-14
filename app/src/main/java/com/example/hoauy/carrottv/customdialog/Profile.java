package com.example.hoauy.carrottv.customdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.activity.FaceFilterActivity;

/**
 *  1. 정의
 *
 *    사용자가 MainActivity 에서 프로필 이미지 변경을 목적으로 "사진 변경" 버튼을 눌렀을 경우
 *    띄어지는 다이얼로그.
 *
 *
 *  2. 구성
 *
 *    1) 사진촬영(마스크)
 *
 *    : Mobile vision 사용
 *
 *    2) 앨범
 *
 *    3) 기본이미지
 *
 *
 */

public class Profile extends Dialog{


    Activity activity;
    String id;

    public Profile(Activity activity, String id){

        super(activity, android.R.style.Theme_Translucent_NoTitleBar);

        this.activity = activity;
        this.id = id;

    }

    Intent intent01;
    final int RESULT_PHOTO = 2;
    final int RESULT_ALBUM = 3;

    Button btn_takePhoto;
    Button btn_fromAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;

        setContentView(R.layout.dialog_profile);

        btn_takePhoto = (Button)findViewById(R.id.btn_takePhoto);
        btn_takePhoto.setOnClickListener(listener);
        btn_fromAlbum = (Button)findViewById(R.id.btn_fromAlbum);
        btn_fromAlbum.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_takePhoto :

                    intent01 = new Intent(activity, FaceFilterActivity.class);
                    intent01.putExtra("id",id);
                    activity.startActivityForResult(intent01, RESULT_PHOTO);

                    dismiss();
                    break;

                case R.id.btn_fromAlbum :

                    intent01 = new Intent(Intent.ACTION_PICK);
                    intent01.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent01.setType("image/*");
                    activity.startActivityForResult(intent01, RESULT_ALBUM);

                    dismiss();
                    break;



            }

        }
    };




}
