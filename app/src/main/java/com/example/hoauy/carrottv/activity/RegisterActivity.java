package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
 *   1. 정의
 *
 *   회원가입 액티비티
 *
 *
 *   2. 주의점
 *
 *   (1) 하단의 toast() 메소드 설명 참고
 *
 *   (2) requestPost() 의 div 매개변수
 *
 *       div = 1  : 닉네임 중복체크 --> 완료 시 check01 = true;
 *
 *       div = 2  : id 중복체크 --> 완료 시 check02 = true;
 *
 *       div = 3  : 회원가입 완료
 *
 *
 */
public class RegisterActivity extends AppCompatActivity {

    String TAG = "RegisterActivity";

    EditText editText_nickname;
    EditText editText_id;
    EditText editText_password01;
    EditText editText_password02;

    Button btn_check01;
    Button btn_check02;

    boolean check01 = false;
    boolean check02 = false;

    Button btn_complete;
    Button btn_back04;

    OkHttpClient client;
    Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        client = new OkHttpClient();

        editText_nickname = (EditText)findViewById(R.id.editText_nickname);
        editText_id = (EditText)findViewById(R.id.editText_id);
        editText_password01 = (EditText)findViewById(R.id.editText_password01);
        editText_password02 = (EditText)findViewById(R.id.editText_password02);

        btn_check01 = (Button)findViewById(R.id.btn_check01);
        btn_check02 = (Button)findViewById(R.id.btn_check02);
        btn_check01.setOnClickListener(listener);
        btn_check02.setOnClickListener(listener);

        btn_complete = (Button)findViewById(R.id.btn_complete);
        btn_back04 = (Button)findViewById(R.id.btn_back04);
        btn_complete.setOnClickListener(listener);
        btn_back04.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_check01 :

                    if(editText_nickname.getText().toString().length()<=0){

                        Toast.makeText(getApplicationContext(), "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else {

                        requestPost("1", "0", editText_nickname.getText().toString(), "0");

                    } // 닉네임 중복체크


                    break;

                case R.id.btn_check02 :

                    if(editText_id.getText().toString().length()<=0){

                        Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else {

                        requestPost("2", editText_id.getText().toString(), "0", "0");

                    } // 아이디 중복체크

                    break;

                case R.id.btn_complete :

                    if(editText_nickname.getText().toString().length()<=0){

                        Toast.makeText(getApplicationContext(), "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else if(editText_id.getText().toString().length()<=0){

                        Toast.makeText(getApplicationContext(), "ID(이메일)를 입력해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else if(editText_password01.getText().toString().length()<8
                            || editText_password01.getText().toString().length()>15){

                        Toast.makeText(getApplicationContext(), "8자리에서 15자리내로 입력해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else if(!editText_password01.getText().toString().equals(editText_password02.getText().toString())){

                        Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                        return;

                    } else if(!check01){

                        Toast.makeText(getApplicationContext(), "닉네임 중복체크를 해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    } else if(!check02){

                        Toast.makeText(getApplicationContext(), "아이디 중복체크를 해주세요.", Toast.LENGTH_LONG).show();
                        return;

                    }

                    requestPost("3", editText_id.getText().toString(),
                            editText_nickname.getText().toString(), editText_password01.getText().toString());


                    break;

                case R.id.btn_back04 :

                    finish();

                    break;

            }


        }
    };
    //listener 하단


    public void requestPost(final String div, String id, String nickname, String pwd){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder()
                .add("div", div).add("id", id).add("nickname", nickname).add("pwd", pwd).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url("http://kmkmkmd.vps.phps.kr/mariadb/register_member.php").post(requestBody).build();

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
                    JSONObject obj01 = new JSONObject(result);
                    String answer =obj01.getString("answer");

                    if(div.equals("1")){

                        if(answer.equals("no")){
                            toast(1);
                        }
                        // 닉네임이 이미 존재하는 경우
                        else if(answer.equals("yes")){

                            toast(2);
                            check01 = true;

                        }
                        // 사용 가능한 닉네임인 경우

                    } // 닉네임 중복체크

                    else if(div.equals("2")){

                        if(answer.equals("no")){
                            toast(3);
                        }
                        // 아이디가 이미 존재하는 경우
                        else if(answer.equals("yes")){

                            toast(4);
                            check02 = true;

                        } // 사용 가능한 아이디인 경우

                    } // 아이디 중복체크

                    else if(div.equals("3")){

                        toast(5);

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);

                        finish();

                    } // 회원가입 완료


                } catch (JSONException e){
                    e.printStackTrace();
                }



            }
        });

    }
    //requestPost 하단


    public void toast(final int state){

        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if(state ==1){
                    Toast.makeText(getApplicationContext(),"이미 존재하는 닉네임 입니다.",Toast.LENGTH_SHORT).show();
                }

                else if (state ==2){
                    Toast.makeText(getApplicationContext(),"사용 가능한 닉네임 입니다.",Toast.LENGTH_SHORT).show();
                }

                else if(state ==3){
                    Toast.makeText(getApplicationContext(),"이미 존재하는 아이디 입니다.",Toast.LENGTH_SHORT).show();
                }

                else if(state ==4){
                    Toast.makeText(getApplicationContext(),"사용 가능한 아이디 입니다.",Toast.LENGTH_SHORT).show();
                }
                else if(state==5){
                    Toast.makeText(getApplicationContext(),"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                }


            }
        }, 0);

    }
    //Toast 메세지는 원칙적으로 main thread에서 해야 함. 그러나 현재 위치는 okhttp가 제공하는 스레드 내부임
    //따라서, Main thread가 가지고 있는 looper를 가져와서 Toast 메시지를 띄움.
}
