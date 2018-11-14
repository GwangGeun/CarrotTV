package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.adapter.Chat_Adapter;
import com.example.hoauy.carrottv.item.Chat_item;
import com.example.hoauy.carrottv.sqlite.MySQLiteOpenHelper;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *  1. 정의
 *
 *   : 시청자들이 BJ가 송출하는 방송(실시간 스트리밍)을 보는 곳
 *
 *
 *
 *
 *  2. 개괄적인 구조
 *
 *   (1) SimpleExoPlayer를 생성
 *
 *   (2) SimpleExoPlayer를 SimpleExoPlayerView에 부착(연결)
 *
 *   (3) SimpleExoPlayer가 MPEG-DASH의 데이터를 받을 수 있도록 준비 상태 만들기
 *
 *   (4) 채팅 : 하단 부분에 자세히 설명되어 있음.
 *
 *          1) 실시간 채팅 : socket 통신
 *
 *          2) 다시보기 (vod) : Redis에 저장되어 있는 채팅을 가져온다.
 *
 *
 *
 *
 *
 *  3. 주의점
 *
 *   (1) controlbar hide 함 ( xml에서 설정 )
 *
 *       why ? 기획적인 측면에서 볼 때, 실시간 방송에서 시청자들에게 controlbar를 제공할 필요는 없음
 *
 *       단, 시청자가 "다시보기"를 보기위해 본 액티비티에 들어왔을 경우에는 controlbar를 제공
 *
 *
 *   (2) 기기마다 화면 사이즈가 다르다. 그로 인해, Exoplayer 비율도 다르게 나옴
 *
 *      : app:resize_mode="fill" (xml) 설정 함으로써 해결
 *
 *
 *   (3) 시청자들은 방에 입장하면 방송을 바로 시청할 수 있어야 한다. 즉, Exoplayer가 puase 가 아닌 auto-play 상태여야 한다.
 *
 *       : player.setPlayWhenReady(true) 이 부분을 설정함으로써 원하는 기능 구현 함.
 *
 *
 *
 *
 *  4. 참고 주소
 *
 *   (1) https://google.github.io/ExoPlayer/guide.html
 *
 *       : 공식 홈페이지 설명
 *
 *   (2) https://github.com/google/ExoPlayer/issues/2210
 *
 *      : 예제 참고한 곳
 *
 *
 */

public class ViewerActivty extends AppCompatActivity {

    String VIDEO_URI;
    // "http://www-itec.uni-klu.ac.at/ftp/datasets/DASHDataset2014/BigBuckBunny/4sec/BigBuckBunny_4s_onDemand_2014_05_09.mpd";
    // DASH 수신 되는지 테스트 주소

    private SimpleExoPlayer player;
    private SimpleExoPlayerView simpleExoPlayerView;

    private Handler mainHandler;
    private TrackSelection.Factory videoTrackSelectionFactory;
    private TrackSelector trackSelector;
    private LoadControl loadControl;
    private DataSource.Factory dataSourceFactory;
    private MediaSource videoSource;
    private Uri uri;
    private String userAgent;
    private static final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    Intent intent01;
    String purpose;
    String bj_id;
    String file_name;
    String nickname; // 시청자 nickname

    LinearLayout linear_viewer;
    //채팅을 입력 할 수 있는 채팅 창 : vod(다시보기) 의 경우에는 채팅을 입력 할 필요가 없으므로 숨긴다.
    String TAG = "ViewerActivity";

    /**
     *  이하 아래부터는
     *  Netty 채팅 관련 변수들
     *
     */
    Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "115.71.237.106";
    private static final int PORT = 5001;

    OkHttpClient client; //vod 시에 필요한 채팅 내용을 가져오기 위한 http.

    Button btn_ballon;
    EditText editText_chat02;
    Button btn_submit;
    TextView empty_space; //vod 시에 controller 의 높이 만큼 빈 공간으로 만들어 놔야 한다. : 생방송 시에는 필요 x

    ArrayList<Chat_item> chat_itemList = new ArrayList<Chat_item>();
    ListView viewer_listview;
    Chat_Adapter chat_adapter;
    Chat_item chat_item; //전역변수로 선언한 이유 : 채팅 보내는 곳과 받는 곳 2곳에서 쓰이기 때문이다.
    //채팅 내용을 보여주기 위한 Listview

    Handler exo_handler;
    int pre_exoplayertime; // 빨리감기 되감기 시 필요 ( 자세한 설명은 exo_handler() 에 있음 )
    // (1) 방송 중에 작성된 채팅 내용을 시청자에게 제공하기 위해, Exoplayer의 current position을 지속적으로 측정
    // (2) 측정 된 값 (시간)과 똑같은 시간에 쓰여진 채팅이 있으면 add

    /**
     *  이하 아래부터는
     *  Sqlite를 사용하기 위해 선언한 변수들
     *
     */
    private MySQLiteOpenHelper helper;
    String dbName = "chat_content.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    Cursor c;
    // Vod (다시보기) 시에 채팅 내용을 저장 및 불러오기 위해 사용
    // 자세한건 sqlite (package)의 MySQLiteOpenHelper 클래스 설명 참조.


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        simpleExoPlayerView = (SimpleExoPlayerView)findViewById(R.id.player_view);
        linear_viewer = (LinearLayout)findViewById(R.id.linear_viewer);
        empty_space = (TextView)findViewById(R.id.empty_space);

        chat_adapter = new Chat_Adapter(this, chat_itemList);
        viewer_listview = (ListView)findViewById(R.id.viewer_listview);
        viewer_listview.setAdapter(chat_adapter);
        //리스트뷰 초기화

        intent01 = getIntent();
        purpose = intent01.getStringExtra("purpose");
        bj_id = intent01.getStringExtra("bj_id");
        file_name = intent01.getStringExtra("file_name");
        nickname = intent01.getStringExtra("nickname");

        VIDEO_URI = "http://kmkmkmd.vps.phps.kr/dash/"+file_name+"/index.mpd";

        if(purpose.equals("firstview")){

            simpleExoPlayerView.setUseController(false);
            linear_viewer.setVisibility(View.VISIBLE);
            empty_space.setVisibility(View.GONE);

            chat_setting();
            //채팅 환경을 만들어 준다 < vod 시에는 필요 없다 >

        }
        // (1) 시청자가 생방송 보는 경우 : controller 숨김 --> empty_space가 필요 없다
        // (2) 채팅을 입력 할 수 있는 채팅창 보임
        // (3) 소켓 연결 및 채팅 환경 구성

        else {

            sqlite_setting();
            // 다시보기(vod) 시, Redis에서 가져온 채팅 내용을 저장하기 위한 sqlite 초기화 (Table 생성 포함)

            get_chatting(file_name);
            // 다시보기(vod) 시, 필요한 생방송 중 기록된 채팅 내용 가져오기
            // < 주의점 >  : get_chatting 에 sqlite insert가 있음
            //              --> get_chatting()은 sqlite_setting()보다 항상 뒤에 있어야 한다.

            simpleExoPlayerView.setUseController(true);
            linear_viewer.setVisibility(View.INVISIBLE);
            empty_space.setVisibility(View.VISIBLE);

            sync_chatting();
            // 가져온 채팅을 바탕으로 동영상과 싱크를 맞춰서 채팅을 제공

        }
        // (1) 시청자가 vod(다시보기) 보는 경우 : controller 보임 --> empty_space가 필요 함.
        // (2) 시청자가 vod(다시보기) 보는 경우 : Redis에서 가져 온 채팅 내용을 동영상의 싱크에 맞게 시청자에게 제공
        // (3) 채팅을 입력할 수 있는 채팅창을 숨김
        // (4) Redis 에서 생방송 중 기록된 채팅 내용을 가져온다
        // (5) (4)의 채팅 내용을 sqlite에 저장한다.
        // (6) handler 에서 sqlite에 저장된 채팅 내용을 지속적으로 가져와서 동영상과 채팅의 sync를 맞춰준다.

        userAgent = Util.getUserAgent(this,"SimpleDashExoPlayer");
        createPlayer();
        attachPlayerView();
        preparePlayer();

    }

    // Create TrackSelection Factory, Track Selector, Handler, Load Control, and ExoPlayer Instance
    public void createPlayer(){

        mainHandler = new Handler();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(this,trackSelector,loadControl);

    }

    // Set player to SimpleExoPlayerView
    public void attachPlayerView(){
        simpleExoPlayerView.setPlayer(player);
    }

    // Build Data Source Factory, Dash Media Source, and Prepare player using videoSource
    public void preparePlayer(){

        player.setPlayWhenReady(true);
        //Player 자동 시작

        if(!purpose.equals("firstview")){
            player.seekTo(0);
            //controller 의 seek bar 위치를 0으로 지정한다
            //why? 사용자가 동영상을 처음부터 보게 하기 위한 목적
        }
        //생방송이 아닌 다시보기인 경우

        uriParse();
        dataSourceFactory = buildDataSourceFactory(bandwidthMeter);
        videoSource = new DashMediaSource(uri,buildDataSourceFactory(null),new DefaultDashChunkSource.Factory(dataSourceFactory),mainHandler,null);
        player.prepare(videoSource);

    }

    // Parse VIDEO_URI and Save at uri variable
    public void uriParse(){
        uri = Uri.parse(VIDEO_URI);
    }

    // Build Data Source Factory using DefaultBandwidthMeter and HttpDataSource.Factory
    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter){
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    // Build Http Data Source Factory using DefaultBandwidthMeter
    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter){
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }


    /**
     *
     *  1. 정의
     *
     *     아래부터 채팅 관련 Method : 실시간 방송 시에만 필요하다.
     *                                vod(다시보기) 시에는 Redis에서 채팅 내용을 가져 옴
     *
     *  2. 구성
     *
     *   (1) 메세지를 수신
     *
     *    checkUpdate / receive / showUpdate
     *
     *   (2) 메세지를 전송
     *
     *   전송 버튼 클릭 --> SendmsgTask
     *
     *
     */

    public void chat_setting(){


        btn_ballon = (Button)findViewById(R.id.btn_ballon);
        btn_ballon.setOnClickListener(listener);
        btn_submit = (Button)findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(listener);
        editText_chat02 = (EditText)findViewById(R.id.editText_chat02);

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                } catch (Exception ioe) {
                    Log.e(TAG, ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }

                checkUpdate.start();
                // 채팅 메세지를 수신하는 Thread

            }
        }).start();


    }


    private Thread checkUpdate = new Thread() {

        public void run() {
            try {

                receive();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기

                Log.e("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();

                handler.post(showUpdate);

            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }


    private Runnable showUpdate = new Runnable() {

        public void run() {

            Log.e("receive data02", "msg :" + data);
            //받은 데이터 처리하는 곳 : Listview Item 추가를 이곳에서 해야 함.

            try{

                JSONObject jsonObject = new JSONObject(data);

                String from_nickname = jsonObject.getString("user_nickname");
                String from_bjId = jsonObject.getString("bj_id");
                //현재 내가 접속해 있는 방의 BJ_ID 랑 상대방이 속해 있는 방의 BJ_ID가 같아야 한다.
                //(방 구분의 개념)
                String from_content = jsonObject.getString("chat_content");

                if(!bj_id.equals(from_bjId)){
                    return;
                }

                chat_item = new Chat_item(from_nickname, from_content, "");
                // 3번째 매개변수의 용도 : BJ가 Redis에 채팅 내용을 저장할 때 필요한 채팅 작성 시간
                // 시청자 입장에서는 필요없다. ( BroadCastActivity 에서만 필요하다 )
                chat_itemList.add(chat_item);
                chat_adapter.notifyDataSetChanged();
                //다른 시청자가 작성한 채팅 내용을 현재 시청자의 화면에 add

            }catch (JSONException e){
                e.printStackTrace();
            }
            // < 채팅 > : 채팅을 입력한 유저의 id & 채팅내용 2가지를 상대방에게 보낸다.



        }

    };
    //실제로 수신한 메세지를 처리하는 곳


    public class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {

                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8"));
                //BJ가 입력한 채팅 메세지 서버로 전송

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    editText_chat02.setText("");
                    //메세지 전송 후, 다시 채팅 메세지를 입력받기 위해 공백으로 만들어준다.

                }
            });
        }
    }
    //채팅 관련 Method 끝




    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_ballon :

                    break;

                case R.id.btn_submit :

                    String chat_msg = editText_chat02.getText().toString();

                    if(chat_msg.length()==0){

                        Toast.makeText(getApplicationContext(), "채팅 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    JSONObject jsonObject = new JSONObject();
                    try{

                        jsonObject.put("user_nickname", nickname);
                        jsonObject.put("bj_id", bj_id);
                        //채팅 수신자 입장 : 수신자가 접속해 있는 방의 BJ_ID 랑 발신자가 속해 있는 방의 BJ_ID가 같은지 체크하기 위해 보낸다.
                        //(방 구분의 개념을 위해 전송한다. )
                        jsonObject.put("chat_content", chat_msg);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    // < 채팅 > : 채팅을 입력한 유저의 id & 채팅내용 2가지를 상대방에게 보낸다.

                    chat_item = new Chat_item(nickname, chat_msg, "");
                    // 3번째 매개변수의 용도 : BJ가 Redis에 채팅 내용을 저장할 때 필요한 채팅 작성 시간
                    // 시청자 입장에서는 필요없다. ( BroadCastActivity 에서만 필요하다 )
                    chat_itemList.add(chat_item);
                    chat_adapter.notifyDataSetChanged();
                    //시청자가 작성한 채팅 내용을 다른 시청자에게 보내기 전에 채팅을 작성한
                    // 시청자의 화면에 먼저 채팅 내용 add

                    new SendmsgTask().execute(jsonObject.toString());
                    //채팅 메세지 보내기

                    break;

            }

        }
    };


    public void sqlite_setting(){

        helper = new MySQLiteOpenHelper(
                ViewerActivty.this,  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        try {

            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문

            String sql = "create table mytable (id integer primary key autoincrement" +
                    ", user_nickname text, chat_content text, hidden_time text);";
            db.execSQL(sql);
            //다시보기(vod)일 경우 : Redis 에서 가져온 채팅 내용을 저장할 table 생성

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

    }
    //다시보기 (vod) 시에 필요한 sqlite : helper 초기화 & 테이블 생성



    public void get_chatting(String file_path){

        client = new OkHttpClient();

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("file_path",file_path).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url("http://kmkmkmd.vps.phps.kr/redis/give_chat.php")
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

                if(result.equals("")){
                    return;
                }
                //채팅 내용이 없으면 return.

                Log.e("채팅내용 가져 왔다", "내용 : "+ result);

                try {

                    JSONArray array = new JSONArray(result);

                    for(int i=0; i<array.length(); i++){

                        String arg01 = array.getJSONObject(i).getString("user_nickname");
                        String arg02 = array.getJSONObject(i).getString("chat_content");
                        String arg03 = array.getJSONObject(i).getString("hidden_time");

                        db.execSQL("insert into mytable (user_nickname, chat_content, hidden_time) " +
                                "values('"+arg01+"', '"+arg02+"','"+arg03+"');");
                        //채팅 내용을 sqlite에 insert

                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }



            }
            //OnRespond 하단
        });

    }
    // (1) Redis에서 저장되어 있는 채팅 내용을 가져옴
    // (2) (1)의 채팅을 Sqlite에 저장한다.


    public void sync_chatting(){

        exo_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                    super.handleMessage(msg);

                int exoplayer_position = (int)(player.getCurrentPosition()/1000);
                //(int)(player.getCurrentPosition()/1000)) : 현재 controller 위치

                if(Math.abs(pre_exoplayertime - exoplayer_position)>=3){

                    chat_itemList.clear();
                    chat_adapter.notifyDataSetChanged();

                }
                // (1) Math.abs() : 절대값
                // (2) 1초전의 위치 - 현재의 위치
                // (3) 현재의 exoplayer controller 위치와 1초전의 exoplayer controller의 차이가 3초 이상 나면
                //     빨리감기 or 되감기를 한 것이다. 따라서, chat_itemList 를 초기화 시킨다.

                pre_exoplayertime = exoplayer_position;
                // 바로 위의 설명 참고

                String sql = "select * from mytable where hidden_time = "+exoplayer_position+";";
                c = db.rawQuery(sql, null);

                while(c.moveToNext()) {

                    String arg01 = c.getString(1); // nickname
                    String arg02 = c.getString(2); // content
//                            String arg03 = c.getString(3); // hidden_time : 여기서는 필요 없다

                    chat_item = new Chat_item(arg01, arg02, "");
                    // 3번째 매개변수의 용도 : BJ가 Redis에 채팅 내용을 저장할 때 필요한 채팅 작성 시간
                    // ( BroadCastActivity 에서만 필요하다 )
                    chat_itemList.add(chat_item);
                    chat_adapter.notifyDataSetChanged();
                    //시청자가 작성한 채팅 내용을 다른 시청자에게 보내기 전에 채팅을 작성한
                    // 시청자의 화면에 먼저 채팅 내용 add

                }

                exo_handler.sendEmptyMessageDelayed(0,1000);
                //비어있는 메세지를 Handler에게 전송

            }
        };

        exo_handler.sendEmptyMessage(0);

    }
    //채팅 내용을 동영상 싱크랑 맞춰서 제공




    @Override
    public void onStop(){
        super.onStop();

        player.release();
        //실시간 방송이든 vod 든 퇴장 시, player release()
        if(purpose.equals("firstview")){

            try {

                if(socketChannel != null){
                    socketChannel.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //실시간 방송에 접속 해 있는 경우 ( 채팅 방에서 퇴장 시, socket 연결 끊어 준다 )

        else if(!purpose.equals("firstview")){

            if(c != null){
                c.close();
            }

            exo_handler.removeMessages(0);
            db.execSQL("drop table mytable");

        }
        // 시청자가 다시보기(vod)를 보고 있는 경우
        // (1) 채팅 내용을 제공하기 위한 handler remove
        // (2) sqlite table drop : 사용자의 핸드폰 용량을 위해.
        // (3) Cursor 를 닫아준다 (메모리를 위해)

    }
    // Activity onStop, player must be release because of memory saving


}
