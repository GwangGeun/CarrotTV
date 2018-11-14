package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;

import org.json.JSONException;
import org.json.JSONObject;

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
 *  1. 정의
 *
 *   로그인 화면
 *
 *
 *
 *  2. 구성
 *
 *  (1) 로그인
 *
 *     : 성공 할 경우, 서버에서 불러온 회원 정보들을 갖고 MainAcitivtiy 로 이동
 *
 *
 *  (2) 회원가입
 *
 *     : 클릭 시, RegisterActivity 로 이동
 *
 *
 *
 *
 *  3. 라이브러리 사용
 *
 *  (1) 명칭
 *
 *    Okhttp3 : http 통신을 위한 라이브러리
 *
 *  (2) 사용법
 *
 *   requestPost() 에 상세히 적어 놓음.
 *
 *  (3) 주의사항
 *
 *   Okhttp3 는 새로운 스레드를 갖고 있음 (서버와 통신하려면 Main thread에서 하면 안되니까)
 *   따라서, 서버에서 데이터를 받아오는 Okhttp3의 onrespond() 안에서 Toast 메세지(Main thread 에서만 가능)
 *   를 띄울 수 없다. 이 부분을 Handler를 통해 극복. (자세한건 아래 소스 참고)
 *
 *
 */

public class LoginActivity extends AppCompatActivity {

    EditText login_id;
    EditText login_pwd;
    Button btn_login;

    Button btn_register;
    CheckBox check_remember;

    Intent intent01;
    OkHttpClient client;
    // Okhtttp3 라이브러리

    String TAG = "LoginActivity";
    Handler mHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init_findViewById();

    }



    public void init_findViewById(){

        client = new OkHttpClient();

        login_id = (EditText)findViewById(R.id.login_id);
        login_pwd = (EditText)findViewById(R.id.login_pwd);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_register = (Button)findViewById(R.id.btn_register);
        check_remember = (CheckBox)findViewById(R.id.check_remember);

        btn_login.setOnClickListener(listener);
        btn_register.setOnClickListener(listener);
        check_remember.setOnClickListener(listener);

    }




    public void requestPost(String url, String id, String pwd){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).add("pwd", pwd).build();

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

//                Log.e(TAG, "Response Body is " + response.body().string());
                String result = response.body().string();

                try {

                    JSONObject obj01 = new JSONObject(result);

                    String id = obj01.getString("id");
                    String pwd = obj01.getString("pwd");
                    String nickname = obj01.getString("nickname");
                    String profile = obj01.getString("profile");
                    int purchase_ballon = Integer.parseInt(obj01.getString("purchase_ballon"));
                    int receive_ballon = Integer.parseInt(obj01.getString("receive_ballon"));

                    if(id.equals("no")){

                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(getApplicationContext(),"아이디 또는 비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                                //Toast 메세지는 원칙적으로 main thread에서 해야 함. 그러나 현재 위치는 okhttp가 제공하는 스레드 내부임
                                //따라서, Main thread가 가지고 있는 looper를 가져와서 Toast 메시지를 띄움.

                            }
                        }, 0);


                    } //회원정보가 일치하지 않는 경우

                    else if(!id.equals("no")){

                        intent01 = new Intent(LoginActivity.this, MainActivity.class);
                        intent01.putExtra("id",id);
                        intent01.putExtra("pwd",pwd);
                        intent01.putExtra("nickname",nickname);
                        intent01.putExtra("profile",profile);
                        intent01.putExtra("purchase_ballon",purchase_ballon);
                        intent01.putExtra("receive_ballon",receive_ballon);

                        intent01.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent01.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //MainActivity를 root stack 으로 설정함.

                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(getApplicationContext(),"로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                                startActivity(intent01);
                                //Toast 메세지는 원칙적으로 main thread에서 해야 함. 그러나 현재 위치는 okhttp가 제공하는 스레드 내부임
                                //따라서, Main thread가 가지고 있는 looper를 가져와서 Toast 메시지를 띄움.

                            }
                        }, 0);



                    } //회원정보가 일치하는 경우


                } catch (JSONException e){

                    e.printStackTrace();

                }


            }
        });

    }



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_login :

//                    new Thread() {
//                        public void run() {
                            requestPost("http://kmkmkmd.vps.phps.kr/mariadb/login.php",
                                    login_id.getText().toString(), login_pwd.getText().toString());

//                        }
//                    }.start();

                    break;

                case R.id.btn_register :

                    intent01 = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent01);

                    break;

                case R.id.check_remember :

                    break;


            }


        }
    };



}
