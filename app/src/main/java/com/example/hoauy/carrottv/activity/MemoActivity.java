package com.example.hoauy.carrottv.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.adapter.Memo_Adapter;
import com.example.hoauy.carrottv.customdialog.Memo;
import com.example.hoauy.carrottv.item.Memo_item;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 *  1. 정의
 *
 *   메모장 MainActivity
 *
 *
 *
 *  2. 주의점
 *
 *   (1) OnRusume 에서 ListView 를 갱신한다는 점
 *
 *       ( 1단계 작품에서는 OnResume 에서 Listview 를 갱신 할 경우 에러가 떴던 기억이 있음.
 *       현재 문제는 없지만 혹시나 하는 마음에 주의점으로 적어 놓는다. )
 *
 *
 *   (2) 롱 클릭 시, 해당 아이템 삭제
 *
 *      1) 메모 추가 요청 시 : div 를 "1" ( 서버에서 div가 1일 경우 MariaDB에 해당 메모 추가 )
 *
 *      2) 메모 삭제 요청 시 : div 를 "2" ( 서버에서 div가 2일 경우 MariaDB에 해당 메모 삭제 )
 *
 *
 *   (3) Qr 코드
 *
 *    1) Intent 를 통해 Qr 코드 카메라 호출
 *
 *    2) onActivityResult 로 인식된 결과값 리턴
 *
 *    3) onActivityResult 가 호출 된 직후  onResume 거의 동시에 호출 됨.
 *
 *    4) onResume 에서는 데이터 갱신이 실시간으로 이루어짐
 *
 *    5) 4)로 인해 onActivityResult 에서 getFromServer(); 및 리스트 뷰를 초기화 해도 의미가 없음.
 *
 *       ( 사실상 동기화가 필요 함)
 *
 *    6) 임시방편으로 onActivityResult 에 다이얼로그를 추가해서
 *
 *       onResume 실시 후, onActivityResult 가 실행되도록 구현 함.
 *
 *       그러면 Qr 코드 데이터를 받았다고 가정했을 때,
 *       onResume 에서 데이터 갱신 --> 다이얼로그 생성 -->
 *       저장하기 클릭 --> 데이터 갱신 한번 더 일어남 의 로직이 실행 됨.
 *
 *
 */

public class MemoActivity extends AppCompatActivity{

    String TAG = "MemoActivity";
    String url = "http://kmkmkmd.vps.phps.kr/mariadb/memo_show.php";
    String url02 = "http://kmkmkmd.vps.phps.kr/mariadb/memo_AddOrDelete.php";

    Intent intent01;
    // 사용자의 아이디를 이전 액티비티로 부터 받아오기 위한 목적
    String id;
    // 사용자의 id

    Intent intent02;
    // 다른 액티비티로 전환 시 사용하기 위한 목적

    OkHttpClient client;

    FloatingActionMenu float_btn_menu02;
    FloatingActionButton float_btn_voice;
    FloatingActionButton float_btn_camera;
    FloatingActionButton float_btn_notepad;
    FloatingActionButton float_btn_qr;

    ArrayList<Memo_item> memo_itemList = new ArrayList<Memo_item>();
    ListView memo_listview;
    Memo_Adapter memo_adapter;
    Memo_item memo_item;
    // 리스트뷰 사용시 필요

    int MY_PERMISSIONS_RECORD_AUDIO = 1;
    // 오디오 권한 체크시 사용


    @Override
    protected void onResume() {
        super.onResume();

        memo_itemList.clear();
        getFromServer();
        // 서버에서 메모 내용 가져와서 ListView 에 보여주기

        Log.e(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG,"onPause");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        intent01 = getIntent();
        id = intent01.getStringExtra("id");

        client = new OkHttpClient();

        float_btn_menu02 = (FloatingActionMenu)findViewById(R.id.float_btn_menu02);
        float_btn_voice = (FloatingActionButton)findViewById(R.id.float_btn_voice);
        float_btn_camera = (FloatingActionButton)findViewById(R.id.float_btn_camera);
        float_btn_notepad = (FloatingActionButton)findViewById(R.id.float_btn_notepad);
        float_btn_qr = (FloatingActionButton)findViewById(R.id.float_btn_qr);

        float_btn_voice.setOnClickListener(listener02);
        float_btn_camera.setOnClickListener(listener02);
        float_btn_notepad.setOnClickListener(listener02);
        float_btn_qr.setOnClickListener(listener02);

        memo_adapter = new Memo_Adapter(this, memo_itemList);
        memo_listview = (ListView)findViewById(R.id.memo_listview);
        memo_listview.setAdapter(memo_adapter);
        memo_listview.setOnItemLongClickListener(listener01);
        // 리스트뷰를 사용하기 위한 셋팅


    }


    AdapterView.OnItemLongClickListener listener01 = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id02) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MemoActivity.this);
            alertDialog.setTitle("메모 삭제");
            alertDialog.setMessage("선택한 메모를 삭제하시겠습니까 ? ");
            // OK 를 누르게 되면 설정창으로 이동합니다.
            alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    dialog.cancel();

                }
            });
            // Cancle 하면 종료 합니다.
            alertDialog.setNegativeButton("삭제하기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    memoToServer(url02, "2" // 삭제 요청 시 : 2  cf) 추가 요청시 : 1
                            ,memo_itemList.get(position).getHidden_no() // 해당 메모의 고유 no ( DB Table의 고유 no )
                            ,id // 사용자의 아이디 : 삭제시에는 필요 없음
                            ,"0" // 메모 추가 시 내용 : 삭제시에는 필요 없음
                            ,"0" // 메모 추가 시 작성시간 : 삭제시에는 필요 없음
                            ,memo_itemList.get(position).getImage_memo() // 해당 메모가 이미지인지 여부
                    );

//                    memo_itemList.remove(position);
//                    memo_adapter.notifyDataSetChanged();
//                    // 메모가 삭제 --> 리스트뷰에 반영

                }
            });

            AlertDialog dialog = alertDialog.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).
                    setTextColor(ContextCompat.getColor(MemoActivity.this, R.color.colorPrimaryDark));
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(ContextCompat.getColor(MemoActivity.this, R.color.colorPrimaryDark));
            // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

            return true;
            //false 를 return 하면 item_listener 도 호출 된다.
        }
    };
    // 리스트뷰 아이템 롱 클릭 되었을 경우
    // 다이얼로그 제공 : 해당 아이템을 삭제할 건지


    View.OnClickListener listener02 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.float_btn_voice :

                    if (ContextCompat.checkSelfPermission(MemoActivity.this,
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(MemoActivity.this,
                                Manifest.permission.RECORD_AUDIO)) {

                        } else {
                            ActivityCompat.requestPermissions(MemoActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                            );
                        }
                    }
                    // Audio 권한

                    float_btn_menu02.close(true);
                    // float button menu 닫기

                    Toast.makeText(getApplicationContext(), "메모 내용을 말해주세요.",Toast.LENGTH_LONG).show();
                    // 다이얼로그 뜨자마자 음성메세지 시작이니까 이 부분을 사용자에게 알려주기

                    Memo memo = new Memo(MemoActivity.this, id, memo_item, memo_itemList, memo_adapter);
                    memo.show();
                    // (1) 음성 메모를 하는 Dialog 보여주기
                    // (2) Dialog : 뜨자마자 바로 음성 메모 시작하는 구조

                    break;

                case R.id.float_btn_camera :

                    AlertDialog.Builder alert = new AlertDialog.Builder(MemoActivity.this);
                    alert.setMessage("글자가 인식되면 직사각형이 형성됩니다. 직사각형을 클릭 시, 글자인식이 완료됩니다. ");
                    alert.setPositiveButton("시작하기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            float_btn_menu02.close(true);
                            // float button menu 닫기

                            intent02 = new Intent(MemoActivity.this, OcrCaptureActivity.class);
                            intent02.putExtra("id",id);
                            startActivity(intent02);

                            dialog.dismiss();     //닫기
                        }
                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();

                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).
                            setTextColor(ContextCompat.getColor(MemoActivity.this, R.color.colorPrimaryDark));
                    // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

                    break;

                case R.id.float_btn_notepad :

                    float_btn_menu02.close(true);
                    // float button menu 닫기

                    intent02 = new Intent(MemoActivity.this, PaintActivity.class);
                    intent02.putExtra("id",id);
                    startActivity(intent02);

                    break;

                case R.id.float_btn_qr :


                    float_btn_menu02.close(true);
                    // float button menu 닫기

                    new IntentIntegrator(MemoActivity.this).initiateScan();

                    break;

            }

        }
    };



    public void getFromServer(){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).build();

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

                String result = response.body().string();

                try {

                    JSONArray jsonArray = new JSONArray(result);

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject obj01 = jsonArray.getJSONObject(i);

                        String no = obj01.getString("no");
                        //String id = obj01.getString("id");
                        String content = obj01.getString("content");
                        String day = obj01.getString("day");
                        String file_path = obj01.getString("file_path");

                        memo_item = new Memo_item(content,
                                day, file_path, no);
                        memo_itemList.add(memo_item);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                memo_adapter.notifyDataSetChanged();

                            }
                        });
                        //Okhttp 특성상 UI를 변경 해줄 때는 OnResponse thread 에서 error 를 발생
                        //따라서 UI 쓰레드를 가동
                    }

                    response.close();
                    //memory leak 방지

                } catch (JSONException e){
                    e.printStackTrace();
                }


            }
        });

    } // gerServerFrom() 하단


    public void memoToServer(String url, String div, String no,
                             String id, String content, String day, String file_path){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("div",div)
                .add("no",no).add("id",id).add("content",content).add("day",day)
                .add("file_path", file_path).build();

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

                memo_itemList.clear();
                getFromServer();
                // 서버에서 메모 내용 가져와서 ListView 에 보여주기

                response.close();
                //memory leak 방지

            }
        });

    }
    //  아래의 2가지 용도로 쓰임.
    //
    // 1. 선택한 해당 메모 내용을 Maria DB의 memo 테이블에서 제거
    // 2. QR 코드로 메모를 Maria DB에 추가

    /**
     *
     *  인식 된 QR 코드를 처리하는 부분
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

//                Log.e(TAG, ">>> requestCode = " + requestCode + ", resultCode = " + resultCode);
//                Log.e(TAG, ">>> result.getContents()   :  " + result.getContents());
//    //            실제 Qr 코드에 담겨 있는 내용을 가져오는 부분
//                Log.e(TAG, ">>> result.getFormatName()   :  " + result.getFormatName());


        if (requestCode == IntentIntegrator.REQUEST_CODE) {

            if(resultCode ==RESULT_OK){


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MemoActivity.this);
                alertDialog.setTitle("Qr 코드 인식 완료");
                alertDialog.setMessage("인식 된 내용은 \'룰루랄라 QR 코드\' 입니다. 저장하시겠습니까?");
                // OK 를 누르게 되면 설정창으로 이동합니다.
                alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {

                        dialog.cancel();

                    }
                });
                // Cancle 하면 종료 합니다.
                alertDialog.setNegativeButton("저장하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy.MM.dd", Locale.KOREA );
                        Date date = new Date();
                        String currentTime = sdf.format(date);
                        // 메모를 작성하는 시간

                        memoToServer(url02, "1" // 추가 요청 시 : 1  cf) 삭제 요청시 : 1
                                ,"0" // 해당 메모의 고유 no ( DB Table의 고유 no ) : 삭제 시에만 필요함 (여기서는 필요 없음)
                                ,id // 사용자의 아이디 : 삭제 시에는 필요 없음
                                ,"룰루랄라 QR 코드" // 메모 추가 시 내용 : 삭제 시에는 필요 없음
                                ,currentTime  // 메모를 작성하는 시간 : 삭제 시에는 필요 없음
                                ,"0" // 해당 메모가 이미지인지 여부
                        );

                    }
                });

                AlertDialog dialog = alertDialog.create();
                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).
                        setTextColor(ContextCompat.getColor(MemoActivity.this, R.color.colorPrimaryDark));
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).
                        setTextColor(ContextCompat.getColor(MemoActivity.this, R.color.colorPrimaryDark));
                // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

            }
            // 1. resultCode --> OK 인 경우
            // 2. Back 버튼 눌러서 돌아올 경우는 위의 로직이 실행 되면 안되기 때문에 지정.

        }


    }
    // onActivityResult 하단


}

