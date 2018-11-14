package com.example.hoauy.carrottv.customdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.adapter.Memo_Adapter;
import com.example.hoauy.carrottv.item.Memo_item;

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
 *   (1) 메모장 --> 음성으로 메모하기 클릭 시 사용되는 다이얼로그
 *   (2) 다이얼로그가 뜨는 순간 SST 가 시작 된다.
 *
 * 2. 주의점
 *
 *   오랫동안 말하지 않거나 SST 실행 중에 다이얼로그를 끄려고 할 경우,
 *   recognitionListener 의 OnError 가 호출된다.
 *
 *
 *
 *
 */

public class Memo extends Dialog {

    String TAG = "Memo Dialog";
    String url01 = "http://kmkmkmd.vps.phps.kr/mariadb/memo_AddOrDelete.php";
    String url02 = "http://kmkmkmd.vps.phps.kr/mariadb/memo_show.php";

    Activity activity;
    Memo_item memo_item;
    ArrayList<Memo_item> memo_itemList;
    Memo_Adapter memo_adapter;
    String id;

    public Memo(Activity activity,String id, Memo_item memo_item,
                ArrayList<Memo_item> memo_itemList, Memo_Adapter memo_adapter){

        super(activity, android.R.style.Theme_Translucent_NoTitleBar);

        this.id = id;
        this.activity = activity;
        this.memo_item = memo_item;
        this.memo_itemList = memo_itemList;
        this.memo_adapter = memo_adapter;

    }

    Intent intent01; // SST 를 사용하기 위한 목적
    OkHttpClient client;
    SpeechRecognizer mRecognizer;

    TextView memo_title;
    TextView memo_result;
    TextView hidden_no; // (1) Mariadb DB 의 Memo 테이블에 저장되어 있는 해당 메모의 고유 no
                        // (2) 메모 삭제 시에 필요

    Button btn_save;
    Button btn_retry;
    ImageView memo_imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;
        //Dialog 크기 설정

        setContentView(R.layout.dialog_memo);

        client = new OkHttpClient();

        memo_title = (TextView)findViewById(R.id.memo_title);
        memo_result = (TextView)findViewById(R.id.memo_result);
        hidden_no = (TextView)findViewById(R.id.hidden_no);
        btn_save = (Button)findViewById(R.id.btn_save);
        btn_retry = (Button)findViewById(R.id.btn_retry);
        memo_imageView = (ImageView)findViewById(R.id.memo_imageView);

        memo_result.setVisibility(View.GONE);
        btn_save.setVisibility(View.GONE);
        btn_retry.setVisibility(View.GONE);
        memo_imageView.setVisibility(View.VISIBLE);
        // (1) 메모 결과 & 저장 및 다시하기 버튼 처음에는 보이지 않게 한다.
        // (2) 처음에는 "말해주세요" 멘트 & 마이크 이미지만 보임
        // (3) 음성 인식이 끝난 후, (1) 은 보이게 (2) 중 마이크 이미지는 보이지 않게 한다.
        // (4) recognitionListener 의 onResults 참고

        btn_save.setOnClickListener(listener);
        btn_retry.setOnClickListener(listener);

        intent01 = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent01.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        intent01.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        mRecognizer.setRecognitionListener(recognitionListener);
        // SST를 사용하기 위한 셋팅

        mRecognizer.startListening(intent01);
        // (1) SST 시작
        // (2) 결과는 recognitionListener 의 onResults 에서 처리한다.

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(memo_imageView.getVisibility() == View.VISIBLE){
            if(mRecognizer != null){
                mRecognizer.stopListening();
            }
        }
        mRecognizer.destroy();
        // (1) 음성 녹음 중에 다이얼로그를 나갈 경우
        // (2) 음성 녹음을 Stop : 하지 않으면 onError 호출 된다.

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){


                case R.id.btn_save :

                    SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy.MM.dd", Locale.KOREA );
                    Date date = new Date();
                    String currentTime = sdf.format(date);
                    // 메모를 작성하는 시간

                    memoToServer(url01, "1" // 글 작성 시에는 1 & 글 삭제 시에는 2 를 요청
                            , id, memo_result.getText().toString(), currentTime ,"0");
                    //서버에 해당 메모 저장

                    break;

                case R.id.btn_retry :

                    Toast.makeText(activity, "메모 내용을 말해주세요.",Toast.LENGTH_LONG).show();

                    memo_title.setText("말해주세요.");
                    memo_result.setVisibility(View.GONE);
                    btn_save.setVisibility(View.GONE);
                    btn_retry.setVisibility(View.GONE);
                    memo_imageView.setVisibility(View.VISIBLE);

                    mRecognizer.startListening(intent01);
                    // (1) SST 시작
                    // (2) 결과는 recognitionListener 의 onResults 에서 처리한다.

                    break;



            }

        }
    };


    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

            Log.e(TAG,"error : "+error);

            mRecognizer.stopListening();
            dismiss();
            //error 발생 시, stop

        }

        @Override
        public void onResults(Bundle results) {

            String key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            if(mResult != null){

                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);

                Log.e(TAG,"결과 : "+rs[0]);

                memo_title.setText("음성인식 결과");
                memo_result.setVisibility(View.VISIBLE);
                memo_result.setText(rs[0]);
                // 음성 인식 결과를 TextView 에 표시
                btn_save.setVisibility(View.VISIBLE);
                btn_retry.setVisibility(View.VISIBLE);
                memo_imageView.setVisibility(View.GONE);



            } else {

                Toast.makeText(activity,"다시 시도해주세요.", Toast.LENGTH_LONG).show();

            }


        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


    public void memoToServer(String url, String div, String id, String content, String day, String file_path){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("div",div).add("id", id).add("content", content)
                .add("day",day).add("file_path", file_path).build();

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

                getFromServer();

            }
        });

    }
    // (1) 메모 내용을 Maria DB의 memo 테이블에 추가
    // (2) 메모 내용 추가 후, MemoActivity 의 ListView 다시 셋팅 (변경 된 내용을 반영하기 위한 목적)

    public void getFromServer(){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url(url02).post(requestBody).build();

        //request를 Client에 세팅하고 Server로 부터 온 Response를 처리할 Callback 작성
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e(TAG, "Connect Server Error is " + e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        memo_itemList.clear();
                        memo_adapter.notifyDataSetChanged();

                    }
                });
                // (1) 새로운 내용이 추가 된 데이터를 서버에서 전체 다 가져온다
                // (2) 기존의 리스트뷰에 뿌려져있는 데이터들은 삭제한다.
                // (3) Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
                // (4) 따라서 UI 쓰레드를 가동

                String result = response.body().string();

                Log.e(TAG,"결과02 :"+result);

                try {

                    JSONArray jsonArray = new JSONArray(result);

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject obj01 = jsonArray.getJSONObject(i);

                        String no = obj01.getString("no");
                        //String id = obj01.getString("id");
                        String content = obj01.getString("content");
                        String day = obj01.getString("day");
                        String file_path = obj01.getString("file_path");

                        //
                        memo_item = new Memo_item(content,
                            day, file_path, no);
                        memo_itemList.add(memo_item);

                        activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            memo_adapter.notifyDataSetChanged();

                         }
                        });
                        // (1) Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
                        // (2) 따라서 UI 쓰레드를 가동
                    }

                    dismiss();
                    // 모든 작업이 완료 되면 다이얼로그 닫음.

                } catch (JSONException e){
                    e.printStackTrace();
                }


            }
        });

    } // gerServerFrom() 하단



}
