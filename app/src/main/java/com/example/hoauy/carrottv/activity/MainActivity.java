package com.example.hoauy.carrottv.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.adapter.AutoScroll_Adapter;
import com.example.hoauy.carrottv.adapter.BroadcastList_Adapter;
import com.example.hoauy.carrottv.customdialog.Preview;
import com.example.hoauy.carrottv.customdialog.Profile;
import com.example.hoauy.carrottv.item.BroadcastList_item;
import com.example.hoauy.carrottv.wallet.Wallet;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    /**
     * 1. 정의
     *
     *    Login 이후 처음 보여지는 화면
     *
     *
     * 2. 구성
     *
     *  (1) Tab01( 실시간 생방송 ) / Tab02( vod ) / Tab03 (내 공간)  : TabHost
     *
     *  (2) 액션바에 관한 내용 정의
     *
     *  (3) 실시간 방송 시작 시 필요한 카메라 권한
     *
     *  (4) 새로고침
     *
     *  cf ) (1) / (2) / (3) / (4) 의 해당 하는 곳을 시각적으로 구분해 놓았음.
     *
     *
     *
     * 3. 추가 작업 필요한 것 ( 18.05.16 ) --> 계획 취소
     *
     *  (1) Listview 에서 overmenu 기능 추가해야 함 : 즐겨찾기 / bj 방송국 가기
     *
     *  (2) (1)의 기능 이용 시 : PopupMenu 로 검색해서 찾아볼 것
     *
     *
     */

    static {
        System.loadLibrary("native-lib");
    }
    // Used to load the 'native-lib' library on application startup.
    // : 'native-lib' library 를 앱 시작시에 메모리에 올림

    TabHost main_tabhost;
    TabHost.TabSpec tab01;
    TabHost.TabSpec tab02;
    TabHost.TabSpec tab03;

    Intent intent01;
    // 화면 이동 시 사용 ( ex, MainActivity --> BroadCastActivity )
    Intent intent02;
    // LoginActivity에서 데이터 받을 때 사용

    String id;
    String pwd;
    String nickname;
    String profile;
    int purchase_ballon;
    int receive_ballon;
    //LoginActivity 로 부터 받아 온 사용자의 정보

    String TAG = "Main";
    OkHttpClient client;
    Toast toast;
    long backKeyPressedTime = 0;//back 버튼 이벤트 발생시 사용할 변수.
    //전체

    ArrayList<BroadcastList_item> broadcast_itemList = new ArrayList<BroadcastList_item>();
    ListView tab01_listview01;
    BroadcastList_Adapter broadcastList_adapter;

    Button btn_location;

    SwipeRefreshLayout mSwipeRefreshLayout; //새로고침
    AutoScrollViewPager autoViewPager; //광고
    AutoScroll_Adapter scrollAdapter; //광고
    //Tab01

    ArrayList<BroadcastList_item> review_itemList = new ArrayList<BroadcastList_item>();
    ListView tab02_listview01;
    BroadcastList_Adapter broadcastList_adapter02;
    //Tab02

    FloatingActionMenu float_btn_menu01;

    Button btn_liveStart;
    Button btn_logout;
    Button btn_profile;
    FloatingActionButton btn_crawling;
    FloatingActionButton btn_ArUnity;
    FloatingActionButton btn_wallet;
    FloatingActionButton btn_notepad;

    CircleImageView my_profile;

    Uri photoUri;
    final int RESULT_PHOTO = 2;
    final int RESULT_ALBUM = 3;
    final int RESULT_CROP = 4;
    Bitmap photo;
    String temp_filePath; //crop 된 이미지 임시적으로 저장
    // 앨범에서 사진 가져올 때 쓰임

    int RESULT_PERMISSIONS = 100; // 카메라 권한 확인 시 사용하는 변수
    //Tab03



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }


    /**
     *
     *  < init() 함수 >
     *
     *  1. findviewbyid 로 변수 참조
     *
     *  2. okhttp 선언
     *
     *  3. 서버에서 tab01 ( 실시간 방송 ) / tab02 ( vod 및 공지사항 ) 정보 가져와서
     *
     *     listview 에 가져온 데이터 add
     *
     */
    public void init(){

        client = new OkHttpClient();

        intent02 = getIntent();
        id = intent02.getStringExtra("id");
        pwd = intent02.getStringExtra("pwd");
        nickname = intent02.getStringExtra("nickname");
        profile = intent02.getStringExtra("profile");
        purchase_ballon = intent02.getIntExtra("purchase_ballon" ,0);
        receive_ballon = intent02.getIntExtra("receive_ballon", 0);
        //로그인 한 사용자 정보

        Log.e(TAG, "Login success id :"+id +"pwd : " + pwd);

        main_tabhost = (TabHost)findViewById(R.id.main_tabhost);
        main_tabhost.setup();
        //전체

        tab01 = main_tabhost.newTabSpec("Tab Spec 1");
        tab01.setContent(R.id.tab01);
        tab01.setIndicator("실시간");
        main_tabhost.addTab(tab01);
        //tab 설정

        broadcastList_adapter = new BroadcastList_Adapter(this, broadcast_itemList, listener);
        tab01_listview01 = (ListView)findViewById(R.id.tab01_listview01);
        tab01_listview01.setAdapter(broadcastList_adapter);
        tab01_listview01.setOnItemClickListener(item_listener); // 생방송 (실시간 방송) 보기
        tab01_listview01.setOnItemLongClickListener(long_item_listener); //방송 미리보기 ( 하단의 long_item_listener 설명 참고 )
        // ( 현재 방송중인 BJ 리스트를 표시하기 위한 listview )

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this); // 새로고침 용도

        btn_location = (Button)findViewById(R.id.btn_location);
        btn_location.setOnClickListener(listener); // 버튼 클릭시, 구글맵으로 이동

        autoViewPager = (AutoScrollViewPager)findViewById(R.id.autoViewPager); //광고
        scrollAdapter = new AutoScroll_Adapter(this);
        autoViewPager.setAdapter(scrollAdapter); //Auto Viewpager에 Adapter 장착
        autoViewPager.setInterval(5000); // 페이지 넘어갈 시간 간격 설정
        autoViewPager.startAutoScroll(); //Auto Scroll 시작

        // 첫 번째 Tab. (탭 표시 텍스트:"실시간"), (페이지 뷰:"tab01")




        broadcastList_adapter02 = new BroadcastList_Adapter(this, review_itemList, listener);
        tab02_listview01 = (ListView)findViewById(R.id.tab02_listview01);
        tab02_listview01.setAdapter(broadcastList_adapter02);
        tab02_listview01.setOnItemClickListener(item_listener);// 다시보기 (vod) 보기
        // ( 방송 다시보기 리스트를 표시하기 위한 listview )

        tab02 = main_tabhost.newTabSpec("Tab Spec 2");
        tab02.setContent(R.id.tab02);
        tab02.setIndicator("vod");
        main_tabhost.addTab(tab02);
        // 두 번째 Tab. (탭 표시 텍스트:"vod"), (페이지 뷰:"tab02")


        tab03 = main_tabhost.newTabSpec("Tab Spec 3");
        tab03.setContent(R.id.tab03);
        tab03.setIndicator("내 공간");
        main_tabhost.addTab(tab03);

        my_profile = (CircleImageView)findViewById(R.id.my_profile); // 프로필 이미지

        float_btn_menu01 = (FloatingActionMenu)findViewById(R.id.float_btn_menu01);
        btn_profile = (Button)findViewById(R.id.btn_profile);
        btn_liveStart = (Button)findViewById(R.id.btn_liveStart);
        btn_crawling = (FloatingActionButton)findViewById(R.id.btn_crawling);
        btn_ArUnity = (FloatingActionButton)findViewById(R.id.btn_ArUnity);
        btn_wallet = (FloatingActionButton)findViewById(R.id.btn_wallet);
        btn_logout = (Button)findViewById(R.id.btn_logout);
        btn_notepad = (FloatingActionButton)findViewById(R.id.btn_notepad);

        btn_profile.setOnClickListener(listener);
        btn_liveStart.setOnClickListener(listener);
        btn_crawling.setOnClickListener(listener);
        btn_ArUnity.setOnClickListener(listener);
        btn_wallet.setOnClickListener(listener);
        btn_logout.setOnClickListener(listener);
        btn_notepad.setOnClickListener(listener);

        if(!profile.equals("0")){

            Glide.with(this).load("http://kmkmkmd.vps.phps.kr/image/"+profile).
                    diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).
                    into(my_profile);

        } //프로필 사진이 있는 경우

        // 세 번째 Tab. (탭 표시 텍스트:"내 공간"), (페이지 뷰:"tab03")

        getFromServer();
        //서버에서 데이터 가져와서 listview에 add

    }

    /**
     *
     *  1. 정의
     *
     *     Tab01 ( 실시간 방송 리스트 ) swipe 시 새로고침
     *
     *
     *
     *  2. 참고
     *
     *   Tab01 ( 실시간 방송 리스트 ) 를 새로고침하면 Tab02 (다시보기) 도 새로고침 하게 만들었음
     *
     *   -->  실제 서비스에서는 Tab02 의 경우도 따로 swipeRefreshLayout 을 만들어서 새로고침을 구현해
     *
     *   주는 것이 바람직하다.
     *
     *   why ? 해당 Tab 에 해당하는 데이터만을 가져오는 것이 사용자의 data 사용 및 buffering 을 줄이는 것에 유리하기 때문이다.
     *
     */
    @Override
    public void onRefresh() {

        broadcast_itemList.clear();
        broadcastList_adapter.notifyDataSetChanged();

        review_itemList.clear();
        broadcastList_adapter02.notifyDataSetChanged();

        getFromServer();
        //서버에서 데이터 가져와서 listview에 add

        mSwipeRefreshLayout.setRefreshing(false);
        //이 부분이 없으면 새로고침 아이콘이 사라지지 않는다.
    }
    // 새로고침 기능
    // (1) Tab01 의 실시간 방송리스트 swipe 시 tab01(생방송 리스트) & tab02 (vod) 새로고침 된다.
    // (2) 상용 서비스에서는 tab02 (vod) 도 따로 SwipeRefresh 를 만들어주는게 바람직하다.



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_location :

                    intent01 = new Intent(MainActivity.this, GoogleMapActivity.class);
                    startActivity(intent01);

                    break;

                //TAB 01


                //TAB 02

                case R.id.btn_profile :

                    Profile profile_dialog = new Profile(MainActivity.this, id);
                    profile_dialog.show();

                    break;
                    //사용자의 프로필 사진 설정

                case R.id.btn_liveStart :

                    requestPermissionCamera();

                    break;
                    //사용자에게 카메라 권한 승인 받기 : 승인 받았을 경우 자동적으로 카메라로 넘어감.

                case R.id.btn_notepad :

                    float_btn_menu01.close(true);
                    // float button menu 닫기

                    intent01 = new Intent(MainActivity.this, MemoActivity.class);
                    intent01.putExtra("id",id);
                    startActivity(intent01);

                    break;


                case R.id.btn_crawling :

                    float_btn_menu01.close(true);
                    // float button menu 닫기

                    intent01 = new Intent(MainActivity.this, CrawlingActivity.class);
                    startActivity(intent01);

                    break;

                case R.id.btn_ArUnity :

                    float_btn_menu01.close(true);
                    // float button menu 닫기

                    intent01 = new Intent(MainActivity.this, UnityPlayerActivity.class);
                    startActivity(intent01);

                    break;

                case R.id.btn_wallet :

                    float_btn_menu01.close(true);
                    // float button menu 닫기

                    final SharedPreferences register = getSharedPreferences("mobile_wallet", Context.MODE_PRIVATE);
                    // (1) 사용자의 기기에 모바일 지갑이 있는지 확인
                    // (2) 모바일 지갑이 없을 경우에는 모바일 지갑을 생성하도록 유도


                    if(register.getString("wallet_address",null) ==null){

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("모바일 지갑이 없습니다");
                        alertDialog.setMessage("모바일 지갑을 생성하시겠습니까 ?");
                        // OK 를 누르게 되면 설정창으로 이동합니다.
                        alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {

                                dialog.cancel();

                            }
                        });
                        // Cancle 하면 종료 합니다.
                        alertDialog.setNegativeButton("생성하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Wallet wallet = new Wallet();

                                try {

                                    String file_Path = wallet.createWallet();
                                    Credentials credentials = wallet.loadCredentials("password", file_Path);
                                    String bring_walletAddress = credentials.getAddress(); // 지갑 주소
                                    // 지갑 생성
                                    // (1) return 값은 file_path
                                    // (2) file_path를 통해 지갑 주소를 획득해야 된다.

                                    SharedPreferences.Editor editor= register.edit();
                                    editor.putString("wallet_address", bring_walletAddress);
                                    editor.putString("file_path", file_Path);
                                    editor.apply();
                                    // SharedPreference에 지갑 정보 저장 완료

                                    Toast.makeText(getApplicationContext(),"지갑이 생성되었습니다.",Toast.LENGTH_LONG).show();

                                    Log.e(TAG,"생성된 지갑 주소 : "+bring_walletAddress);

                                } catch (Exception e){
                                    e.printStackTrace();
                                }



                            }
                        });

                        AlertDialog dialog = alertDialog.create();
                        dialog.show();

                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor
                                (ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor
                                (ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
                        // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

                    }
                    // 현재 모바일 지갑이 없는 경우
                    else {

                        intent01 = new Intent(MainActivity.this, WalletActivity.class);
                        intent01.putExtra("id",id);
                        startActivity(intent01);

                    }
                    // 현재 모바일 지갑이 있는 경우 : 모바일지갑 Acitivty 로 이동


                    break;


                case R.id.btn_logout :

                    intent01 = new Intent(MainActivity.this, LoginActivity.class);
                    intent01.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent01.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //LoginActivity 를 root stack 으로 설정함.
                    startActivity(intent01);

                    break;

                //TAB03

            }

        }
    };

    AdapterView.OnItemClickListener item_listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long no) {

            if(parent.getAdapter().equals(broadcastList_adapter)){

//                Log.e(TAG, "selected adapter 01 " );
//
//                Log.e("클릭된 정보 ", "id : " + broadcast_itemList.get(position).getHidden_bjId());
//                Log.e("클릭된 정보 ", "file name : " + broadcast_itemList.get(position).getHidden_fileName());

                intent01 = new Intent(MainActivity.this, ViewerActivty.class);
                intent01.putExtra("purpose", "firstview");
                intent01.putExtra("nickname",nickname);
                intent01.putExtra("bj_id", broadcast_itemList.get(position).getHidden_bjId());
                intent01.putExtra("file_name", broadcast_itemList.get(position).getHidden_fileName());
                startActivity(intent01);

            } //Tab01 ( 실시간 방송 중인 BJ 리스트 ) 를 클릭한 경우

            else if(parent.getAdapter().equals(broadcastList_adapter02)){

//                Log.e(TAG, "selected adapter 02 " );
//
//                Log.e("클릭된 정보 ", "id : " + review_itemList.get(position).getHidden_bjId());
//                Log.e("클릭된 정보 ", "file name : " + review_itemList.get(position).getHidden_fileName());

                intent01 = new Intent(MainActivity.this, ViewerActivty.class);
                intent01.putExtra("purpose","review");
                intent01.putExtra("nickname",nickname);
                intent01.putExtra("bj_id",review_itemList.get(position).getHidden_bjId());
                intent01.putExtra("file_name",review_itemList.get(position).getHidden_fileName());
                startActivity(intent01);


            } //Tab02 ( 방송 다시보기 리스트 ) 를 클릭한 경우


        }
    };
    //리스트뷰 아이템을 순간 클릭하고 때는 경우 : '실시간 방송' or 'vod' 를 제공하는 activity 로 이동

    AdapterView.OnItemLongClickListener long_item_listener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            if(parent.getAdapter().equals(broadcastList_adapter)){

                Toast.makeText(getApplicationContext(),"미리보기 중 입니다.",Toast.LENGTH_SHORT).show();

                Preview preview = new Preview(MainActivity.this,
                        broadcast_itemList.get(position).getBroadcast_title() //방송의 제목
                        , broadcast_itemList.get(position).getBroadcast_nickname(), //방송하는 BJ의 닉네임
                        broadcast_itemList.get(position).getHidden_fileName()); // 방송 파일 이름

                preview.show();

            }

            return true;
            //false 를 return 하면 item_listener 도 호출 된다.

        }
    };
    // (1) 리스트뷰 아이템을 길게 클릭하고 때는 경우 : '방송 미리보기' 를 제공
    // (2) Tab01 실시간 방송 목록에서만 사용 (Tab02 에서는 사용 할 이유가 없다)


    /**
     *
     *  1. 정의
     *
     *     액션바에 검색&설정 메뉴 추가하기 위함.
     *
     *  2. 구성
     *
     *  (1) onCreateOptionsMenu() 에서 R.menu 에 정의해놓은 메뉴를 시각적으로 구성
     *
     *  (2) onOptionsItemSelected 에서 액션바에 있는 아이콘 클릭 시 발생하는 이벤트 처리
     *
     */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;

    }
    //R.menu.actionbar_menu (액션 바에 표시 될 아이콘 등을 커스텀 해 놓은 곳) 에 있는 내용을 액션바에 표시하는 함수


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_search :


                intent01 = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent01);

                return true;

            case R.id.action_setting :

                return true;


        }

        return super.onOptionsItemSelected(item);

    }
    //액션바에 이벤트(터치 등)가 발생 했을 경우, 이벤트를 처리하는 함수

    /**
     *
     *  1. 정의
     *
     *     사용자에게 권한(Camera) 물어보기
     *
     *  2. 구성
     *
     *  (1) requestPermissionCamera() 에서 사용자 기기 알아 냄.
     *
     *  (2) 마시멜로 이상일 경우
     *
     *      --> onRequestPermissionsResult() 에서 사용자의 권한
     *          승인/거부에 따라 처리
     *
     *  (3) 권한 승인 시, 방송하기(BroadcastActivty.java)로 이동
     *
     */

    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

//            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);

            } //권한이 없는 경우

            else {

                intent01 = new Intent(MainActivity.this, BroadCastActivity.class);
                intent01.putExtra("id",id);
                intent01.putExtra("nickname",nickname);
                intent01.putExtra("profile",profile);
                startActivity(intent01);

            } //권한이 이미 주어진 경우

        }

        else{

            intent01 = new Intent(MainActivity.this, BroadCastActivity.class);
            intent01.putExtra("id",id);
            intent01.putExtra("nickname",nickname);
            intent01.putExtra("profile",profile);
            startActivity(intent01);

        } // version 6 이하일때

        return true;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (RESULT_PERMISSIONS == requestCode) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가시

                intent01 = new Intent(MainActivity.this, BroadCastActivity.class);
                intent01.putExtra("id",id);
                intent01.putExtra("nickname",nickname);
                intent01.putExtra("profile",profile);
                startActivity(intent01);

            } else {
                // 권한 거부시

                Toast.makeText(getApplicationContext(), "권한 거부 시, 방송을 할 수 없습니다.", Toast.LENGTH_SHORT).show();

            }
            return;
        }

    }


    /**
     *
     *   1. 정의
     *
     *     Server 에서 data 가져와서 Tab01 (실시간 방송 리스트) & Tab02(vod 리스트)
     *     의 Listview 에 data 를 add
     *
     *
     *
     *   2. 주의점
     *
     *      Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
     *      따라서 UI 쓰레드를 따로 만들어줘야 한다.
     *
     */

    public void getFromServer(){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url("http://kmkmkmd.vps.phps.kr/mariadb/main.php")
                .post(requestBody).build();

        //request를 Client에 세팅하고 Server로 부터 온 Response를 처리할 Callback 작성
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e(TAG, "Connect Server Error is " + e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();


                try{

                    JSONArray jsonArray01 = new JSONObject(result).getJSONArray("tab01");
                    BroadcastList_item tab01_item;
                    //tab01에 표시할 정보
                    JSONArray jsonArray02 = new JSONObject(result).getJSONArray("tab02");
                    BroadcastList_item tab02_item;
                    //tab02에 표시할 정보

                    for(int i=0; i<jsonArray01.length(); i++){

                        JSONObject tab01 = jsonArray01.getJSONObject(i);

                        String bj_id = tab01.getString("id");
                        String nickname = tab01.getString("nickname");
                        String title = tab01.getString("title");
                        String file_path = tab01.getString("file_path");
                        String bj_profile = tab01.getString("profile");

                        if(bj_id.equals("no")){

                            break;

                        }
                        //현재 방송 중인 BJ가 없을 경우.

                        tab01_item = new BroadcastList_item(bj_profile, "[생] "+title, nickname
                        , bj_id, file_path, "live");

                        broadcast_itemList.add(tab01_item);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                broadcastList_adapter.notifyDataSetChanged();

                            }
                        });
                        //Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
                        //따라서 UI 쓰레드를 가동

                    }
                    //Tab01 ( 생방송 중인 BJ 목록 )에 표시할 데이터

                    /**
                     *
                     *  1. 정의
                     *
                     *   더미데이터 : 시연을 위해 임의로 추가한 item 들 (TAB 01)
                     *
                     *  2. 주의점
                     *
                     *   (1) 방송 이미지 adapter 에 따로 지정해 놓음
                     *
                     *   (2) 문제가 생길 시에는 지워도 상관없음.
                     *
                     *
                     */
                    tab01_item = new BroadcastList_item("gamst", "[생] 언넝 들어와라", "gamst"
                            , "gamst", "", "live");
                    broadcast_itemList.add(tab01_item);
                    tab01_item = new BroadcastList_item("jd", "[생] 다들 오랜만^^", "jd"
                            , "jd", "", "live");
                    broadcast_itemList.add(tab01_item);
                    tab01_item = new BroadcastList_item("flash", "[생] 컴온", "flash"
                            , "flash", "", "live");
                    broadcast_itemList.add(tab01_item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            broadcastList_adapter.notifyDataSetChanged();

                        }
                    });
                    // Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
                    // 따라서 UI 쓰레드를 가동
                    // 더미데이터 끝


                    for(int i=0; i<jsonArray02.length(); i++){

                        JSONObject tab02 = jsonArray02.getJSONObject(i);

                        String bj_id02 = tab02.getString("id");
                        String file_name02 = tab02.getString("file_name");
                        String thumbnail02 = tab02.getString("thumbnail");
                        String nickname02 = tab02.getString("nickname");
                        String title02 = tab02.getString("title");

                        if(bj_id02.equals("no")){

                            break;

                        }

                        tab02_item = new BroadcastList_item(thumbnail02, "[생] "+title02, nickname02
                                , bj_id02, file_name02, "다시보기");

                        review_itemList.add(tab02_item);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                broadcastList_adapter02.notifyDataSetChanged();

                            }
                        });
                        //Okhttp 특성상 UI를 재변경 해줄 때는 OnResponse thread 에서 error 를 발생
                        //따라서 UI 쓰레드를 가동


                    }
                    //Tab02 ( 방송 다시보기 목록 )에 표시할 데이터


                } catch (JSONException e){
                    e.printStackTrace();
                }


            }
        });

    }



    /**
     *
     *  1. 정의
     *
     *   프로필 사진 설정 --> 앨범 선택 시
     *
     *   앨범에서 가져온 사진을 Imageview 에 지정 및 서버에 이미지를 전달
     *
     *
     *  2. 구조
     *
     *  (1) Profile Dialog 에서 '앨범 선택' 버튼을 눌렀을 경우 onActivityResult 로 반환
     *
     *  (2) cropimage
     *
     *  (3) saveBitmapToJpeg : crop 된 이미지를 임시적으로 저장
     *
     *  (4) profile_to_Server : 프로필 이미지를 서버에 저장
     *
     *
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case RESULT_PHOTO :

                profile = id+".jpg";
                // Login 시에 가져온 접속 유저의 profile 내용을 변경해준다.

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e){
//                    e.printStackTrace();
//                }

                Glide.with(MainActivity.this)
                        .load("http://kmkmkmd.vps.phps.kr/image/"+profile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .into(my_profile);


                break;

            case RESULT_ALBUM :

                if(data == null){
                    return;
                }

                photoUri = data.getData();

                cropImage();
                //앨범에서 가져온 후 이미지 crop

                break;


            case RESULT_CROP :

                if(data == null){
                    return;
                }

                final Bundle extras = data.getExtras();
                // crop된 이미지를 저장하기 위한 파일 경로

                if(extras != null) {
                    photo = extras.getParcelable("data");
                }

                my_profile.setImageBitmap(photo);
                // Tab03의 이미지뷰(프로필 사진) 지정

                temp_filePath = saveBitmapToJpeg(this, photo, "temp");
                // 임시적으로 파일 저장

                profile_to_Server(temp_filePath);
                // (1) 임시파일에 저장된 (앨범에서 가져온 후 crop 된) 이미지를 서버에 전송한다.
                // (2) 저장 후 임시파일 삭제
                // (3) Login 시에 가져온 profile 변수 change

                break;

        }


    }


    public void cropImage(){

        Intent intent02 = new Intent("com.android.camera.action.CROP");
        intent02.setDataAndType(photoUri, "image/*");

//        intent05.putExtra("outputX", 200); // crop한 이미지의 x축 크기
//        intent05.putExtra("outputY", 200); // crop한 이미지의 y축 크기
//        intent05.putExtra("aspectX", 2); // crop 박스의 x축 비율
//        intent05.putExtra("aspectY", 1); // crop 박스의 y축 비율
        intent02.putExtra("scale", true);
        intent02.putExtra("return-data", true);
        startActivityForResult(intent02, RESULT_CROP);

    }
    // 프로필 지정 --> 앨범 선택 --> 이미지 크롭 시 사용되는 method

    public String saveBitmapToJpeg(Context context, Bitmap bitmap, String name){

        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로
        String fileName = name + ".jpg";  // 파일이름은 마음대로!
        File tempFile = new File(storage,fileName);

        try{

            tempFile.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴
    }
    // 앨범에서 가져온 이미지 crop 후 임시 파일로 만들어서 저장
    // why? 서버에 업로드하려면 임시적으로 저장되어 있어야 한다.


    public void profile_to_Server(final String temp_filePath){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", id)
                .addFormDataPart("upload", id+".jpg", RequestBody.create(MultipartBody.FORM, new File(temp_filePath)))
                .build();

        Request request = new Request.Builder()
                .url("http://kmkmkmd.vps.phps.kr/mariadb/profile_image.php")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                profile = id+".jpg";
                // Login 시에 가져온 접속 유저의 profile 내용을 변경해준다.

                File f = new File(temp_filePath);
                if(f.delete()){
                    Log.e(TAG,"임시 파일 삭제");
                } // 임시 파일 삭제
                else {
                    Log.e(TAG,"임시 파일 삭제 fail");
                } // 임시 파일 삭제 실패 시

            }
        });

    }
    // 앨범에서 가져와서 crop 된 이미지를 서버에 보낸다. (프로필 사진 지정 시)


    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {

            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(getApplicationContext(), "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();

        }

        else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {

            toast.cancel();

            moveTaskToBack(true);
            // (1) 현재 Activity 를 backgroud로 이동 시킴
            // (2) 실제 상용 서비스에서는 service 를 통해 background 로 이동시키는게 안전하다고 함.
            // (3) 문제가 생길 경우 아래의 코드로 실행 해 볼 것

//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            startActivity(intent);

        }

    }
    //Back 버튼 눌렀을 때의 이벤트 처리



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


}
