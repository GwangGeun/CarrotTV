package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hoauy.carrottv.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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
 * 1. 정의
 *
 *  (1) 메모장 기능 중 카메라로 메모하기 기능을 위한 목적
 *
 *  (2) 모바일 비전 Text 인식 된 글자가 넘어오는 경우 보여지는 Activity
 *
 *  (3) 본 Activity 에서는 인식 된 글자를 파파고 Api 를 통해 한글로 번역 해준다.
 *
 *
 * 2. 주의점
 *
 *  파파고 Api 를 사용한 부분은 공식 홈페이지의 예제 코드를 그대로 인용해 온 것이다.
 *
 *  따라서, 수정 X
 *
 *
 *
 */

public class TranslateActivity extends AppCompatActivity {

    String TAG = "TranslateActivity";
    String url = "http://kmkmkmd.vps.phps.kr/mariadb/memo_AddOrDelete.php";

    TextView before_translate;
    TextView after_translate;

    Button btn_save02;
    Button btn_back02;

    Intent intent01;
    String id;
    String content;

    OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        client = new OkHttpClient();

        before_translate = (TextView)findViewById(R.id.before_translate);
        after_translate = (TextView)findViewById(R.id.after_translate);

        intent01 = getIntent();
        id = intent01.getStringExtra("id");
        content = intent01.getStringExtra("content");
        before_translate.setText(content);

        btn_save02 = (Button)findViewById(R.id.btn_save02);
        btn_back02 = (Button)findViewById(R.id.btn_back02);
        btn_save02.setOnClickListener(listener);
        btn_back02.setOnClickListener(listener);

        NaverTranslateTask asyncTask = new NaverTranslateTask();
        // NaverTranslateTask 는 네이버 공식 홈페이지에서 제공하는 예제 그대로 가져온 것.
        asyncTask.execute(before_translate.getText().toString());
        // 영어로 가져온 내용을 한글로 번역해주기

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_save02 :

                    SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy.MM.dd", Locale.KOREA );
                    Date date = new Date();
                    String currentTime = sdf.format(date);
                    // 메모를 작성하는 시간

                    String total_memo = before_translate.getText().toString()+"\n"+after_translate.getText().toString();
                    // 번연 전 & 번역 후 내용 모두 포함

                    memoToServer(url, "1" // 글 작성 시에는 1 & 글 삭제 시에는 2 를 요청
                            , id, total_memo, currentTime ,"0");
                    //서버에 해당 메모 저장

                    break;

                case R.id.btn_back02 :

                    finish();

                    break;

            }

        }
    };


    public class NaverTranslateTask extends AsyncTask<String, Void, String> {

        String clientId = "SWo9HKNYAuOYbQDqMZJC";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "93dnlkAVGN";//애플리케이션 클라이언트 시크릿값";
        //언어선택도 나중에 사용자가 선택할 수 있게 옵션 처리해 주면 된다.
        String sourceLang = "en";
        String targetLang = "ko";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String sourceText = strings[0];

            try {

                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/language/translate";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                //System.out.println(response.toString());
                return response.toString();

            } catch (Exception e) {
                //System.out.println(e);
                Log.e("error", e.getMessage());
                return null;
            }
        }

        //번역된 결과를 받아서 처리
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

           // Log.e(TAG, result.toString()); //네이버에 보내주는 응답결과가 JSON 데이터이다.

            try {

                JSONObject obj01 = new JSONObject(result);
                String translate_result = obj01.getJSONObject("message").getJSONObject("result").getString("translatedText");

                after_translate.setText(translate_result);
                // 번역된 글자 보여주기

            } catch (JSONException e){
                e.printStackTrace();
            }


        }


    }
    // AsynTask () 하단
    // 공식 홈페이지에서 그대로 가져온 것 : 따로 수정할 필요는 없음.

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

                finish();

            }
        });

    }
    // 메모 내용을 Maria DB의 memo 테이블에 추가

}
