package com.example.hoauy.carrottv.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.customdialog.Loading;
import com.example.hoauy.carrottv.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *    1. 정의
 *
 *    이더리움 기반의 토큰을 담는 모바일 지갑
 *
 *
 *    2. 참고
 *
 *   (1) 모바일 지갑이 있다는 가정하에 진행
 *
 *
 *   (2) 참고한 주소
 *
 *      1)  https://ethereum.stackexchange.com/questions/43654/create-offline-ethereum-wallet-in-android
 *
 *      2) https://github.com/subhodi/android-web3-lightwallet/blob/master/app/src/main/java/com/persistent/subhod_i/digitallocker/HomeActivity.java
 *
 *      3) https://github.com/subhodi/android-web3-lightwallet
 *
 *       ( 여기 따라보고 만듦 )
 *
 *
 *
 *      4) https://github.com/danfinlay/human-standard-token-abi
 *
 *       ( transfer 기능 추가시 여기 보고 참고 함)
 *
 *
 *      5) https://github.com/web3j/web3j
 *
 *       ( web3j 사용법 )
 *
 *
 *       5)-1 https://github.com/web3j/web3j/blob/master/integration-tests/src/test/java/org/web3j/protocol/scenarios/HumanStandardTokenIT.java
 *
 *       5)-2 https://ethereum.stackexchange.com/questions/49401/not-getting-any-results-from-web3j-querying-of-erc20-tokens-balanceof-functio
 *
 *          ( 토큰의 양 : balanceof()를 안드로이드에서 사용하는 법 )
 *          ( 5)-1의 주소를 약간 수정 한 버전인 5)-2의 주소를 따라함 )
 *
 *
 */

public class WalletActivity extends AppCompatActivity{

    String TAG = "WalletActivity";
    String contract_address= "0x75cc747e823880b80be580baf5eb13f145c1acdf";
    Intent intent01;
    String id;

    Loading loading;
    // 현재 지갑의 이더리움 갯수 받아올 때 까지 로딩 중 화면 띄워주기

    TextView wallet_address;
    TextView my_token;

    EditText editText_getToken;
    Button btn_tokenRequest;

    Wallet wallet;
//    Credentials credentials;
//    비밀번호를 통해 기기에 저장되어 있는 지갑의 주소를 가져오려고 할 때 필요한 객체
    String bring_walletAddress;

    Web3j web3j;
//    BigInteger balance;
//    이더리움 갯수 확인할 때 필요한 객체

    SharedPreferences register;
    SharedPreferences.Editor editor;
    // (1) 사용자의 기기에 모바일 지갑이 있는지 확인
    // (2) 모바일 지갑이 없을 경우에는 모바일 지갑을 생성하도록 유도
    OkHttpClient client;
    // Okhtttp3 라이브러리 : 서버 (Mariadb) 에 사용자가 요청한 토큰 양에 대한 정보를 저장

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        init();


    }

    public void init(){

        loading = new Loading(WalletActivity.this, "지갑 정보 확인중...");
        loading.show();
        // (1) 로딩 중 화면 제공
        // (2) 지갑에 속해있는 이더리움 갯수 까지 확인하면 다이얼로그 제거

        intent01 = getIntent();
        id = intent01.getStringExtra("id");
        // 사용자 id

        register = getSharedPreferences("mobile_wallet", Context.MODE_PRIVATE);
        bring_walletAddress = register.getString("wallet_address",null);
        // 모바일 지갑 주소

        Log.e(TAG,"지갑 주소 : "+register.getString("wallet_address",null));

        client = new OkHttpClient();
        wallet = new Wallet();

//        try {
//            credentials = wallet.loadCredentials("password");
//            bring_walletAddress = credentials.getAddress();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//       패스워드를 통해 지갑의 주소를 알아오는 로직

//        editor= register.edit();
//        editor.putString("wallet_address", bring_walletAddress);
//        editor.putString("file_path", "/storage/emulated/0/Download/UTC--2018-06-12T23-57-41.426--dde98e5f56b7381f7c3a8592ee87ac2ee16673c0.json");
//        editor.apply();
//        shared에 지갑 주소랑 지갑 위치 저장하는 로직


        wallet_address = (TextView)findViewById(R.id.wallet_address);
        my_token = (TextView)findViewById(R.id.my_token);

        editText_getToken = (EditText)findViewById(R.id.editText_getToken);
        btn_tokenRequest = (Button)findViewById(R.id.btn_tokenRequest);
        btn_tokenRequest.setOnClickListener(listener);

        wallet_address.setText(bring_walletAddress);
        // 모바일 지갑 주소 UI 표시

        new LongOperation().execute();

    }



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_tokenRequest :

                    if(editText_getToken.getText().toString().length()==0){

                        Toast.makeText(getApplicationContext(),"입력한 값이 없습니다.",Toast.LENGTH_LONG).show();
                        return;

                    } else if(editText_getToken.getText().toString().equals("0")){

                        Toast.makeText(getApplicationContext(),"0 개는 요청할 수 없습니다.",Toast.LENGTH_LONG).show();
                        return;

                    }

                    String request_token = editText_getToken.getText().toString();

                    int total = Integer.parseInt(request_token)*1000;

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(WalletActivity.this);
                    alertDialog.setTitle("카카오 페이 결제");
                    alertDialog.setMessage("결제 금액은 "+total+" 원 입니다." +
                            "계속 진행하시겠습니까 ?");
                    // OK 를 누르게 되면 설정창으로 이동합니다.
                    alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {

                            dialog.cancel();

                        }
                    });
                    // Cancle 하면 종료 합니다.
                    alertDialog.setNegativeButton("진행하기", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            kakaoPay();

                        }
                    });

                    AlertDialog dialog = alertDialog.create();
                    dialog.show();

                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(WalletActivity.this, R.color.colorPrimaryDark));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(WalletActivity.this, R.color.colorPrimaryDark));
                    // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토


                    /**
                     *
                     *  추후 카카오 페이가 잘 안될 경우
                     *
                     *  아래 주석되어 있는 코드를 사용 할 것
                     *
                     * ( 카카오 페이 결제 과정을 걸치지 않고 바로 토큰을 요청하는 코드 )
                     *
                     */

//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(WalletActivity.this);
//                    alertDialog.setTitle("토큰 요청 확인");
//                    alertDialog.setMessage("현재 관리자에게 요청한 토큰 \n갯수는 "+request_token+" 개 입니다." +
//                            "계속 진행하시겠습니까 ?");
//                    // OK 를 누르게 되면 설정창으로 이동합니다.
//                    alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog,int which) {
//
//                            dialog.cancel();
//
//                        }
//                    });
//                    // Cancle 하면 종료 합니다.
//                    alertDialog.setNegativeButton("진행하기", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            requestPost(id, bring_walletAddress, editText_getToken.getText().toString());
//                            // (1) 사용자 ID
//                            // (2) 사용자의 기기에 있는 모바일 지갑 주소
//                            // (3) 관리자에게 요청 할 토큰 갯수
//
//                            editText_getToken.setText("");
//                            // 원래는 서버에 토큰 저장이 정상적으로 완료 되었을 경우
//                            // okhttp onresponse()에서 처리해줘야 함. 여기서는 편의상 이곳에서 처리.
//
//                            Toast.makeText(getApplicationContext(),"토큰 요청이 완료되었습니다." +
//                                    " 관리자의 승인까지 약간의 시간이 소요될 수 있습니다.",Toast.LENGTH_LONG).show();
//
//                            finish();
//
//                        }
//                    });
//
//                    AlertDialog dialog = alertDialog.create();
//                    dialog.show();
//
//                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(WalletActivity.this, R.color.colorPrimaryDark));
//                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(WalletActivity.this, R.color.colorPrimaryDark));
//                    // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

                    break;


            }

        }
    };



    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                        web3j = wallet.constructWeb3("https://ropsten.infura.io/"+contract_address);
                        //  매개변수 설명
                        // (1) https://ropsten.infura.io/ : ropsten 의 테스트넷에 접속
                        // (2) 0x75cc747e823880b80be580baf5eb13f145c1acdf" : token  (계약서) 주소

                        // EthGetBalance ethGetBalance = web3j.ethGetBalance(ethereum, DefaultBlockParameterName.LATEST).sendAsync().get();
                        // balance = ethGetBalance.getBalance();
                        // 보유 중인 이더리움 갯수 확인하는 로직

                        return "success";


            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }


        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result.equals("success")){

                    confirmBalance(bring_walletAddress, contract_address);
                    // 보유 중인 토큰 갯수 확인
                    Log.e(TAG,"Success : onPostExecute");
                }
                else {
                    Log.e(TAG,"Error : onPostExecute");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     *
     *  이하 현재 지갑에 갖고 있는 이더리움 갯수를 확인하는
     *  Method 들. ( 메소드 하나하나 이해 할 필요 없음 )
     *
     *
     */
    private void confirmBalance(
            String address, String contractAddress) throws Exception {
        Function function = balanceOf(address);
        String responseValue = callSmartContractFunction(function, contractAddress);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());

       // for (int i = 0; i < response.size(); i++) {

            if(response.get(0).getValue().toString().equals("0")){

                Log.e(TAG,"보유 중인 이더리움이 없다.");
                my_token.setText("0 개");
                loading.dismiss();
                // 토큰 갯수까지 확인하면 다이얼로그 제거
                return;

            }

            Log.e(TAG,"보유 중인 이더리움 갯수" + response.get(0).getValue());

            int total_length = response.get(0).getValue().toString().length()-18;
            // (1) 가져온 토큰 값은 10의 18승이 되어있음
            // (2) 따라서 10의 18승 부분을 제거해줘야 한다.

            String token_amount = response.get(0).getValue().toString().substring(0,total_length)+" 개";
            // 10의 18승 부분을 제거 한 토큰 값
            my_token.setText(token_amount);
            // 가져온 토큰 값 지정

            loading.dismiss();
            // 토큰 갯수까지 확인하면 다이얼로그 제거

        //}
        // 토큰 종류 1개만 소유하고 있다고 가정하고 만든 로직
        // 토큰 종류가 2개 이상 되면 반복문 실행

    }

    private Function balanceOf(String owner) {
        return new Function(
                "balanceOf",
                Arrays.<Type>asList(new Address(owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
    }

    private String callSmartContractFunction(
            Function function, String contractAddress) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        bring_walletAddress, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        return response.getValue();
    }


    /**
     *
     *  사용자가 요청한 토큰의 갯수를 서버에 저장
     *
     */
    public void requestPost(String id, String wallet_address, String request_token){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).
                add("wallet_address", wallet_address).add("request_token", request_token).build();
        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url("http://kmkmkmd.vps.phps.kr/mariadb/token_request.php").post(requestBody).build();

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

    /**
     *
     *  *** 카카오 페이 결제 부분 ***
     *
     *
     *  1. 과정
     *
     *  사용자가 이더리움 요청 --> 카카오 페이 결제로 진행 됨 --> 결제 완료 되어야 토큰 요청이 진행 됨.
     *
     *
     *  2. 참고
     *
     *  (1) 테스트 모드임 : 실제 결제가 이루어지는 것은 아님
     *
     *  (2) 공기계 카톡이 없는 곳에서 카카오페이가 진행 됨
     *
     *      --> 1) 카톡이 설치 되어 있으면 mobile_url 을 이용하면 되지만
     *
     *          2) 카톡이 설치 되어 있지 않아서 pc_url 을 받아서 webView 로 제공
     *
     *
     */

     public void kakaoPay(){


         //Request Body에 서버에 보낼 데이터 작성
         RequestBody requestBody = new FormBody.Builder()
                 .add("cid", "TC0ONETIME").
                         add("partner_order_id", "partner_order_id").
                         add("partner_user_id", "partner_user_id").
                         add("item_name", "이더리움").
                         add("quantity", "1").
                         add("total_amount", "2000").
                         add("vat_amount", "200").
                         add("tax_free_amount", "0").
                         add("approval_url", "http://kmkmkmd.vps.phps.kr/kakaopay_success.php").
                         add("fail_url", "http://kmkmkmd.vps.phps.kr/kakaopay.php").
                         add("cancel_url", "http://kmkmkmd.vps.phps.kr/kakaopay.php").build();


         //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
         Request request = new Request.Builder().addHeader("Authorization","KakaoAK 7e7d49c7204c479cb63caad2029bada0")
                 .url("https://kapi.kakao.com/v1/payment/ready").post(requestBody).build();

         //request를 Client에 세팅하고 Server로 부터 온 Response를 처리할 Callback 작성
         client.newCall(request).enqueue(new Callback() {
             @Override
             public void onFailure(Call call, IOException e) {
                 Log.e("Hi", "Connect Server Error is " + e.toString());
             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

                 String result;

                 if(response.body() != null){

                     result =  response.body().string();

                     try {

                         JSONObject s = new JSONObject(result);

                         Intent intent = new Intent(WalletActivity.this, WebViewActivity.class);
                         intent.putExtra("link",s.getString("next_redirect_pc_url"));
                         startActivityForResult(intent,2000);

                     } catch (JSONException e){
                         e.printStackTrace();
                     }

                 } else {
                     Log.e(TAG," KakaoPay : null");
                 }


             }
         });

     }
     // kakaoPay() 하단


    /**
     *
     *  카카오페이 결제가 성공적으로 이루어지면
     *
     *  WebViewAcitivty 에서 성공 메세지를 받음.
     *
     *  그 후, 토큰 요청을 실시한다.
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 3000){

            Toast.makeText(getApplicationContext(), "결제 및 토큰 요청이 정상적으로 완료되었습니다.", Toast.LENGTH_LONG).show();

            requestPost(id, bring_walletAddress, editText_getToken.getText().toString());
            // (1) 사용자 ID
            // (2) 사용자의 기기에 있는 모바일 지갑 주소
            // (3) 관리자에게 요청 할 토큰 갯수

        }


    }

}
