package com.example.hoauy.carrottv.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.activity.BroadCastActivity;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 *
 *  1. 정의
 *
 *   방송을 시작하려면 방송 제목을 입력해야 함. 그 방송 제목을 입력 받는 Dialog
 *
 *   실제 방송을 시작하는 부분임
 *
 *
 *
 *  2. 구조
 *
 *   (1) BroadCastActivity에서 필요한 정보들 넘겨 받음
 *
 *       cf ) 방송 제목 같은 경우는 static 을 사용해서 데이터를 교환
 *
 *
 *   (2) 방송 시작 시
 *
 *     1) Camera Streaming start
 *
 *     2) UI 변경 : 채팅 할 수 있도록 채팅창 Visible + 방송 시작 아이콘 변경
 *
 *     3) Mariadb 의 현재 방송 중인 BJ 리스트 (table)에 방송을 시작하려는 해당 BJ의 정보 추가
 *
 *
 *
 *
 */

public class BroadCast extends Dialog{

    Context context;

    RtmpCamera1 rtmpCamera1;
    LinearLayout broadcast_chat_room;
    Handler timer;

    Button b_start_stop;
    String file_path;
    String id;
    String nickname;
    String profile;
    //방송을 시작할 BJ의 정보



    public BroadCast(Context context, RtmpCamera1 rtmpCamera1, Button b_start_stop, String file_path,
                     String id, String nickname, String profile, LinearLayout broadcast_chat_room, Handler timer){

        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.rtmpCamera1 = rtmpCamera1;
        this.b_start_stop = b_start_stop;
        this.file_path = file_path;
        this.id = id;
        this.nickname = nickname;
        this.profile = profile;
        this.broadcast_chat_room = broadcast_chat_room;
        this.timer = timer;

    }

    Button custom_start01;
    Button custom_back01;
    EditText custom_title01;
    //Dialog에 있는 버튼 및 EditText

    OkHttpClient client;
//    String title;
    //방송 제목
    String TAG = "BroadCast Dialog";
    String url = "http://kmkmkmd.vps.phps.kr/mariadb/current_broadcast_list.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;

        setContentView(R.layout.dialog_broadcast);

        client = new OkHttpClient();

        custom_start01 = (Button)findViewById(R.id.custom_start01);
        custom_back01 = (Button)findViewById(R.id.custom_back01);
        custom_start01.setOnClickListener(listener);
        custom_back01.setOnClickListener(listener);

        custom_title01 = (EditText)findViewById(R.id.custom_title01);


    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id. custom_start01 :

                    String title = custom_title01.getText().toString();
                    BroadCastActivity.broadcast_title = title;
                    //BroadCastActivity에서 BJ의 방송기록에 저장할 때, 방송 제목이 필요하다.

                    if(title.length()==0){

                        Toast.makeText(getContext(),"방송 제목은 한 글자 이상 입력해주셔야 합니다.",Toast.LENGTH_SHORT).show();
                        return;

                    }
                    //방송 제목을 입력하지 않은 경우 --> 다시 입력하게끔 유도한다.

                    broadcast_start(url, id, nickname, title, file_path, profile);
                    //현재 방송중인 BJ 목록에 이제 방송을 시작하려는 BJ 추가하기
                    b_start_stop.setBackgroundResource(R.drawable.liveend);
                    //방송 시작 아이콘 --> 방송 종료 아이콘으로 상태 변경
                    broadcast_chat_room.setVisibility(View.VISIBLE);
                    //채팅 입력하는 UI 보이게 하기
                    BroadCastActivity.myBaseTime = SystemClock.elapsedRealtime();
                    //방송 시작 시의 시스템 경과 시간 측정
                    timer.sendEmptyMessage(0);
                    //방송 시간을 측정하는 타이머 시작

                    rtmpCamera1.startStream("rtmp://kmkmkmd.vps.phps.kr:1935/dash/"+file_path);
                    //rtmp 전송 시작

                    Toast.makeText(getContext(), "방송이 시작되었습니다.", Toast.LENGTH_SHORT).show();

                    dismiss();

                    break;

                //방송을 시작하는 경우

                case R.id.custom_back01 :

                    rtmpCamera1.startPreview();
                    //카메라를 다시 킨다.
                    dismiss();

                    break;

                //방송을 시작하지 않고 돌아가는 경우

            }


        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        rtmpCamera1.startPreview();

    }

    public void broadcast_start(String url, String id, String nickname, String title, String file_path, String profile){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).add("nickname", nickname)
                .add("title",title).add("file_path",file_path).add("profile",profile).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url(url).post(requestBody).build();

        //request를 Client에 세팅하고 Server로 부터 온 Response를 처리할 Callback 작성
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e(TAG, "Connect Server Error is " + e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


            }
        });

    }
    // < 현재 방송 중인 BJ 목록 >에 이제 방송을 시작하려는 BJ의 정보를 추가



}
