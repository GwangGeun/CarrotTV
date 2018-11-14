package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.hoauy.carrottv.R;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.adapter.Chat_Adapter;
import com.example.hoauy.carrottv.customdialog.BroadCast;
import com.example.hoauy.carrottv.item.Chat_item;
import com.pedro.encoder.input.gl.render.filters.AndroidViewFilterRender;
import com.pedro.encoder.input.gl.render.filters.BasicDeformationFilterRender;
import com.pedro.encoder.input.gl.render.filters.BeautyFilterRender;
import com.pedro.encoder.input.gl.render.filters.BlurFilterRender;
import com.pedro.encoder.input.gl.render.filters.BrightnessFilterRender;
import com.pedro.encoder.input.gl.render.filters.CartoonFilterRender;
import com.pedro.encoder.input.gl.render.filters.ColorFilterRender;
import com.pedro.encoder.input.gl.render.filters.ContrastFilterRender;
import com.pedro.encoder.input.gl.render.filters.DuotoneFilterRender;
import com.pedro.encoder.input.gl.render.filters.EarlyBirdFilterRender;
import com.pedro.encoder.input.gl.render.filters.EdgeDetectionFilterRender;
import com.pedro.encoder.input.gl.render.filters.ExposureFilterRender;
import com.pedro.encoder.input.gl.render.filters.FireFilterRender;
import com.pedro.encoder.input.gl.render.filters.GammaFilterRender;
import com.pedro.encoder.input.gl.render.filters.GreyScaleFilterRender;
import com.pedro.encoder.input.gl.render.filters.HalftoneLinesFilterRender;
import com.pedro.encoder.input.gl.render.filters.Image70sFilterRender;
import com.pedro.encoder.input.gl.render.filters.LamoishFilterRender;
import com.pedro.encoder.input.gl.render.filters.MoneyFilterRender;
import com.pedro.encoder.input.gl.render.filters.NegativeFilterRender;
import com.pedro.encoder.input.gl.render.filters.NoFilterRender;
import com.pedro.encoder.input.gl.render.filters.PixelatedFilterRender;
import com.pedro.encoder.input.gl.render.filters.PolygonizationFilterRender;
import com.pedro.encoder.input.gl.render.filters.RainbowFilterRender;
import com.pedro.encoder.input.gl.render.filters.RippleFilterRender;
import com.pedro.encoder.input.gl.render.filters.SaturationFilterRender;
import com.pedro.encoder.input.gl.render.filters.SepiaFilterRender;
import com.pedro.encoder.input.gl.render.filters.SharpnessFilterRender;
import com.pedro.encoder.input.gl.render.filters.TemperatureFilterRender;
import com.pedro.encoder.input.gl.render.filters.ZebraFilterRender;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.encoder.utils.gl.GifStreamObject;
import com.pedro.encoder.utils.gl.ImageStreamObject;
import com.pedro.encoder.utils.gl.TextStreamObject;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.view.OpenGlView;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 *     BJ가 방송을 하는 화면 ( RTMP Library 통해 구현 )
 *
 *
 *  2. 구조
 *
 *     (1) 액션바
 *
 *         : 카메라 필터 기능 ( Open GL )
 *
 *     (2) 카메라 기능을 다루는 버튼
 *
 *     (3) 채팅
 *
 *
 *  3. 주의점
 *
 *
 *    (1) 기존 library 에서 사용한 editText(rtmp 주소 입력창) / record 부분은 xml 에서 gone 처리함
 *
 *        혹시 모를 필요성에 대비해서 코드자체는 남겨 놓음.
 *
 *
 *    (2) DASH 파일을 서버에 저장시 파일 명(Stream key) : "BJ아이디_날짜"
 *
 *
 *    (3) 방송 하기 --> 방송 제목 입력하는 다이얼로그  --> 되돌아가기
 *                  1)                              2)
 *
 *       <1> problem : 다이얼로그에서 돌아가기 or back버튼 클릭 시, 다시 본 Activity로 돌아오면
 *
 *                      카메라가 정지되어 있음. 즉, 방송을 하려면 MainActivity를 다녀와야 함.
 *
 *       <2> 해결방안 : 다이얼로그로 넘어가기전에 rtmpCamera1.stopPreview(); 를 호출
 *
 *                      다이얼로그에서는 돌아가기 or back버튼 클릭시에 다시 카메라를 startpreview() 시킴
 *
 *                      단, 방송제목을 입력하고 정상적으로 방송을 시작하려는 경우에는 startpreview()를
 *
 *                      시키면 앱이 죽음. startStreaming 만 시켜도 작동 함.
 *
 *                      ( Library의 규칙인 걸로 일단 이해 )
 *
 *
 *       <3> 문제 생길 시 : 다이얼로그로 넘어가기 전에 rtmpCamera1.stopPreview(); 를 호출하지 말고 (제거)
 *
 *                         다이얼로그에서 "돌아가기" or back버튼에 있는 startpreview()를 삭제
 *
 *                         시연 시에, "돌아가기" or back버튼을 클릭하지 말 것
 *
 *
 *     (4) 채팅
 *
 *       현재 구조 : 방송을 시작하지 않아도 BroadCastActivity에 접속하는 순간에 '소켓' 연결이 되도록 설계
 *       (단, 채팅을 입력하는 창은 방송이 시작한 후에 등장한다.)
 *
 *
 *
 *  4. 참고 주소
 *
 *      https://github.com/pedroSG94/rtmp-rtsp-stream-client-java
 *
 *
 *
 */




/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)

public class BroadCastActivity extends AppCompatActivity implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

    private RtmpCamera1 rtmpCamera1;
    private Button b_start_stop;
    private Button bRecord;
    Button switchCamera;
    private EditText etUrl;

    private String currentDateAndTime = "";
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/rtmp-rtsp-stream-client-java");

    Intent intent01;
    String id;
    //BJ의 아이디 : DASH 파일을 서버에 저장시 < BJ아이디_날짜 > 로 저장함.
    String nickname;
    String file_name;
    String profile;
    //BJ의 정보

    public static String broadcast_title;
    //Dialog에서 입력한 방송제목 --> 목적 : 다시보기 방송 목록에 저장할 때 사용
    //Memory 문제가 생길 경우에는 다른 방법으로 처리 해 볼 것.

    LinearLayout broadcast_chat_room;
    //방송 중에 BJ에게 보여지는 채팅방

    OkHttpClient client;
    String TAG = "BroadCastActivity";

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

    EditText editText_chat;
    Button btn_submit;
    TextView broadcast_time;
    Handler timer;
    //방송 시간을 표시 : vod 제공 시 채팅의 sync 를 맞추려면 각 채팅 메세지에 시간이 기록되어 있어야 한다.
    //                  때문에, 방송을 하면서 실시간으로 타이머를 켜놔야 한다.

    public static long myBaseTime;
    //방송을 시작 할때의 시스템 경과 시간을 기록
    // --> 방송 시작을 BroadCast 에서 한다 --> 따라서, BroadCast(dialog)에서 초기화해야 한다.

    ArrayList<Chat_item> chat_itemList = new ArrayList<Chat_item>();
    ListView broadcast_listview;
    Chat_Adapter chat_adapter;
    Chat_item chat_item; //전역변수로 선언한 이유 : 채팅 보내는 곳과 받는 곳 2곳에서 쓰이기 때문이다.
    //채팅 내용을 보여주기 위한 Listview


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activty_broadcast);

        init();
        // Intent로 받아온 정보 + 카메라 + 기타 필요한 UI 초기화

        set_chatting();
        // Socket 통신 시작 + 채팅 관련 변수 초기화

    }


    public void init(){

        intent01 = getIntent();
        id = intent01.getStringExtra("id");
        nickname = intent01.getStringExtra("nickname");
        profile = intent01.getStringExtra("profile");
        //BJ의 정보

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMdd_HHmmss", Locale.KOREA );
        Date currentTime = new Date ();
        String mTime = mSimpleDateFormat.format ( currentTime );

        file_name = id+"_"+mTime;
        //서버에 저장될 DASH 파일명

        client = new OkHttpClient();

        broadcast_chat_room = (LinearLayout)findViewById(R.id.broadcast_chat_room);
        broadcast_chat_room.setVisibility(View.GONE);
        //방송 중에 BJ에게 보여지는 채팅방 : 방송 시작하기 전에는 보이면 안됨.

        OpenGlView openGlView = (OpenGlView)findViewById(R.id.surfaceView);

        b_start_stop = (Button)findViewById(R.id.b_start_stop);
        b_start_stop.setOnClickListener(this);
        bRecord = (Button)findViewById(R.id.b_record);
        bRecord.setOnClickListener(this);

        switchCamera = (Button)findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);

        etUrl = (EditText)findViewById(R.id.et_rtp_url);
        etUrl.setHint("rtmp");

        rtmpCamera1 = new RtmpCamera1(openGlView, this);
        openGlView.getHolder().addCallback(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.gl_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.e_d_fxaa:
                Toast.makeText(this, "FXAA " + (rtmpCamera1.isAAEnabled() ? " enabled" : "disabled"),
                        Toast.LENGTH_SHORT).show();
                rtmpCamera1.enableAA(!rtmpCamera1.isAAEnabled());
                return true;
            //stream object
            case R.id.text:
                setTextToStream();
                return true;
            case R.id.image:
                setImageToStream();
                return true;
            case R.id.gif:
                setGifToStream();
                return true;
            case R.id.clear:
                rtmpCamera1.clearStreamObject();
                return true;
            //filters. NOTE: You can change filter values on fly without re set the filter.
            // Example:
            // ColorFilterRender color = new ColorFilterRender()
            // rtmpCamera1.setFilter(color);
            // color.setRGBColor(255, 0, 0); //red tint
            case R.id.no_filter:
                rtmpCamera1.setFilter(new NoFilterRender());
                return true;
            case R.id.android_view:
                AndroidViewFilterRender androidViewFilterRender = new AndroidViewFilterRender();
                androidViewFilterRender.setView(findViewById(R.id.activity_example_rtmp));
                rtmpCamera1.setFilter(androidViewFilterRender);
                return true;
            case R.id.basic_deformation:
                rtmpCamera1.setFilter(new BasicDeformationFilterRender());
                return true;
            case R.id.beauty:
                rtmpCamera1.setFilter(new BeautyFilterRender());
                return true;
            case R.id.blur:
                rtmpCamera1.setFilter(new BlurFilterRender());
                return true;
            case R.id.brightness:
                rtmpCamera1.setFilter(new BrightnessFilterRender());
                return true;
            case R.id.cartoon:
                rtmpCamera1.setFilter(new CartoonFilterRender());
                return true;
            case R.id.color:
                rtmpCamera1.setFilter(new ColorFilterRender());
                return true;
            case R.id.contrast:
                rtmpCamera1.setFilter(new ContrastFilterRender());
                return true;
            case R.id.duotone:
                rtmpCamera1.setFilter(new DuotoneFilterRender());
                return true;
            case R.id.early_bird:
                rtmpCamera1.setFilter(new EarlyBirdFilterRender());
                return true;
            case R.id.edge_detection:
                rtmpCamera1.setFilter(new EdgeDetectionFilterRender());
                return true;
            case R.id.exposure:
                rtmpCamera1.setFilter(new ExposureFilterRender());
                return true;
            case R.id.fire:
                rtmpCamera1.setFilter(new FireFilterRender());
                return true;
            case R.id.gamma:
                rtmpCamera1.setFilter(new GammaFilterRender());
                return true;
            case R.id.grey_scale:
                rtmpCamera1.setFilter(new GreyScaleFilterRender());
                return true;
            case R.id.halftone_lines:
                rtmpCamera1.setFilter(new HalftoneLinesFilterRender());
                return true;
            case R.id.image_70s:
                rtmpCamera1.setFilter(new Image70sFilterRender());
                return true;
            case R.id.lamoish:
                rtmpCamera1.setFilter(new LamoishFilterRender());
                return true;
            case R.id.money:
                rtmpCamera1.setFilter(new MoneyFilterRender());
                return true;
            case R.id.negative:
                rtmpCamera1.setFilter(new NegativeFilterRender());
                return true;
            case R.id.pixelated:
                rtmpCamera1.setFilter(new PixelatedFilterRender());
                return true;
            case R.id.polygonization:
                rtmpCamera1.setFilter(new PolygonizationFilterRender());
                return true;
            case R.id.rainbow:
                rtmpCamera1.setFilter(new RainbowFilterRender());
                return true;
            case R.id.ripple:
                rtmpCamera1.setFilter(new RippleFilterRender());
                return true;
            case R.id.saturation:
                rtmpCamera1.setFilter(new SaturationFilterRender());
                return true;
            case R.id.sepia:
                rtmpCamera1.setFilter(new SepiaFilterRender());
                return true;
            case R.id.sharpness:
                rtmpCamera1.setFilter(new SharpnessFilterRender());
                return true;
            case R.id.temperature:
                rtmpCamera1.setFilter(new TemperatureFilterRender());
                return true;
            case R.id.zebra:
                rtmpCamera1.setFilter(new ZebraFilterRender());
                return true;
            default:
                return false;
        }

    }

    private void setTextToStream() {
        try {
            TextStreamObject textStreamObject = new TextStreamObject();
            textStreamObject.load("Hello world", 22, Color.RED);
            rtmpCamera1.setTextStreamObject(textStreamObject);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageToStream() {
        try {
            ImageStreamObject imageStreamObject = new ImageStreamObject();
            imageStreamObject.load(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            rtmpCamera1.setImageStreamObject(imageStreamObject);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setGifToStream() {
        try {
            GifStreamObject gifStreamObject = new GifStreamObject();
            gifStreamObject.load(getResources().openRawResource(R.raw.banana));
            rtmpCamera1.setGifStreamObject(gifStreamObject);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(BroadCastActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
//                Toast.makeText(BroadCastActivity.this, "방송이 시작되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadCastActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                        .show();
                rtmpCamera1.stopStream();
                b_start_stop.setBackgroundResource(R.drawable.livestart);

            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(BroadCastActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                Toast.makeText(BroadCastActivity.this, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadCastActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadCastActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.b_start_stop:


                if (!rtmpCamera1.isStreaming()) {

                    if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {

                        rtmpCamera1.stopPreview();
                        //다이얼로그로 넘어 갈 때는 카메라를 stop

                        BroadCast broadCast = new BroadCast(BroadCastActivity.this, rtmpCamera1, b_start_stop
                        ,file_name, id, nickname, profile, broadcast_chat_room, timer);
                        //방송제목을 입력하라는 다이얼로그
                        broadCast.show();

//                       b_start_stop.setBackgroundResource(R.drawable.liveend);
//                       rtmpCamera1.startStream("rtmp://kmkmkmd.vps.phps.kr:1935/dash/"+file_name);
//                       //방송 시작

                    } else {

                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                Toast.LENGTH_SHORT).show();

                    }

                }
                // 방송 시작 하기 전 (Before) : 방송을 시작 한다.

                else {

                    b_start_stop.setBackgroundResource(R.drawable.livestart);
                    rtmpCamera1.stopStream();
                    //스트림 stop
                    timer.removeMessages(0);
                    //타이머를 stop
                    broadcast_chat_room.setVisibility(View.GONE);
                    //채팅 입력하는 UI 없애기

                    broadcast_end(id,file_name,nickname, broadcast_title, chat_to_Server(), profile);
                    // (1) 현재 방송 중인 BJ 리스트 (table) 에서 해당 BJ 삭제
                    // (2) BJ들의 방송 기록 (table)에 해당 BJ 가 방송했던 파일명 업로드

                }
                // 방송을 하고 있는 경우 (After) : 방송을 멈춘다.

                break;


            case R.id.switch_camera:

                try {

                    rtmpCamera1.switchCamera();

                } catch (CameraOpenException e) {

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

                break;

            case R.id.b_record:
                //당장 사용 할 일 없음 (필요 할 경우를 대비해서 남겨 놓음) : 현재 hidden 시켜놓음.

                if (!rtmpCamera1.isRecording()) {

                    try {

                        if (!folder.exists()) {
                            folder.mkdir();
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        currentDateAndTime = sdf.format(new Date());
                        rtmpCamera1.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                        bRecord.setText("stop record");
                        Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        rtmpCamera1.stopRecord();
                        bRecord.setText("start_record");
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

                else {

                    rtmpCamera1.stopRecord();
                    bRecord.setText("start_record");
                    Toast.makeText(this,
                            "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                    currentDateAndTime = "";

                }

                break;



            default:
                break;

        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        if (rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
            bRecord.setText("start_record");
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }

        if (rtmpCamera1.isStreaming()) {

            b_start_stop.setBackgroundResource(R.drawable.livestart);
            rtmpCamera1.stopStream();
            //스트림 stop
            timer.removeMessages(0);
            //타이머를 stop
            broadcast_chat_room.setVisibility(View.GONE);
            //채팅 입력하는 UI 없애기

            broadcast_end(id,file_name,nickname, broadcast_title, chat_to_Server(), profile);
            // (1) 현재 방송 중인 BJ 리스트 (table) 에서 해당 BJ 삭제
            // (2) BJ들의 방송 기록 (table)에 해당 BJ 가 방송했던 파일명 업로드


        }

        rtmpCamera1.stopPreview();

    }



    public void broadcast_end(String id02, String file_path02, String nickname02, String title02, String chatting02, String profile02){

        //Request Body에 서버에 보낼 데이터 작성
        RequestBody requestBody = new FormBody.Builder().add("id", id02)
                .add("file_path",file_path02).add("nickname",nickname02).
                        add("title",title02).add("chatting",chatting02).
                        add("profile", profile02).build();

        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder().url("http://kmkmkmd.vps.phps.kr/mariadb/delete_broadcast_list.php")
                .post(requestBody).build();

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
    // (1) 현재 방송 중인 BJ 리스트 (table) 에서 해당 BJ 삭제
    // (2) BJ들의 방송 기록 (table)에 해당 BJ 가 방송했던 파일명 업로드


    /**
     *
     *  1. 정의
     *
     *     아래부터 채팅 관련 Method
     *
     *
     *  2. 구성
     *
     *   (1) 메세지를 수신
     *
     *        checkUpdate / receive / showUpdate
     *
     *   (2) 메세지를 전송
     *
     *        전송 버튼 클릭 --> SendmsgTask
     *
     */


    public void set_chatting(){

        chat_adapter = new Chat_Adapter(this, chat_itemList);
        broadcast_listview = (ListView)findViewById(R.id.broadcast_listview);
        broadcast_listview.setAdapter(chat_adapter);
        //리스트뷰 초기화

        editText_chat = (EditText)findViewById(R.id.editText_chat);
        btn_submit = (Button)findViewById(R.id.btn_submit);
        broadcast_time = (TextView)findViewById(R.id.broadcast_time);
        timer = new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
                broadcast_time.setText(getTimeOut(1));

                timer.sendEmptyMessage(0);
                //비어있는 메세지를 Handler에게 전송

            }
        };

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String chat_msg = editText_chat.getText().toString();

                if(chat_msg.length()==0){

                    Toast.makeText(getApplicationContext(), "채팅 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }


                JSONObject jsonObject = new JSONObject();

                try{

                    jsonObject.put("user_nickname", nickname);
                    jsonObject.put("chat_content", chat_msg);
                    jsonObject.put("bj_id", id);
                    //채팅 수신자 입장 : 수신자가 접속해 있는 방의 BJ_ID 랑 발신자가 속해 있는 방의 BJ_ID가 같은지 체크하기 위해 보낸다.
                    //(방 구분의 개념을 위해 전송한다. )

                }catch (JSONException e){
                    e.printStackTrace();
                }

                chat_item = new Chat_item(nickname, chat_msg, getTimeOut(2));
                // 3번째 매개변수의 용도 : BJ가 Redis에 채팅 내용을 저장할 때 필요한 채팅 작성 시간
                // getTimeOut(2) 가 현재 경과된 방송 시간
                chat_itemList.add(chat_item);
                chat_adapter.notifyDataSetChanged();
                //BJ가 작성한 채팅 내용을 시청자에게 보내기 전에 BJ의 화면에 먼저 채팅 내용 add

//                new SendmsgTask().execute(chat_msg);
                new SendmsgTask().execute(jsonObject.toString());
                //채팅 메세지 보내기

            }
        });

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

            //  Log.e("BroadCastActivity", "msg :" + data);
            //받은 데이터 처리하는 곳 : Listview Item 추가를 이곳에서 해야 함.

            try {

                JSONObject jsonObject = new JSONObject(data);

                String from_nickname = jsonObject.getString("user_nickname");
                String from_content = jsonObject.getString("chat_content");
                String from_bjId = jsonObject.getString("bj_id");

                if(!id.equals(from_bjId)){
                    return;
                }
                // 시청자가 속해 있는 방의 bj id와 현재 방송 중인 bj의 id가 같아야 한다.
                //( 방 구분의 개념 )

                chat_item = new Chat_item(from_nickname, from_content , getTimeOut(2));
                chat_itemList.add(chat_item);
                chat_adapter.notifyDataSetChanged();
                //시청자가 작성한 채팅 내용을 BJ의 화면에 add


            } catch (JSONException e){
                e.printStackTrace();
            }


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

                    editText_chat.setText("");
                    //메세지 전송 후, 다시 채팅 메세지를 입력받기 위해 공백으로 만들어준다.

                }
            });
        }
    }

    //채팅 관련 메소드 및 클래스 끝

    String getTimeOut(int div){

        long now = SystemClock.elapsedRealtime();
        //애플리케이션이 실행되고나서 실제로 경과된 시간
        long outTime = now - myBaseTime;

        if(div==1){
            String easy_outTime = String.format(Locale.KOREA,"%02d:%02d:%02d", outTime/1000/3600, (outTime/1000/60)%60 ,(outTime/1000)%60);
            return easy_outTime;
        }

        else if (div ==2){
            return String.valueOf(outTime/1000);
        }

        return "";
    }
    // easy_outTime = "getTimeOut 이 호출될 때 시스템 경과시간 - 방송이 시작 될때의 시스템 경과시간"
    // 매개변수 1 : 방송 시간을 보여주는 핸들러에 사용
    // 매개변수 2 : 채팅 메세지의 시간을 저장

    public String chat_to_Server(){

        JSONArray Json_Array = new JSONArray();
        //채팅을 담을 Array

        for(int i=0; i<chat_itemList.size(); i++){

            JSONObject jsonObject = new JSONObject();
            //채팅 1개(Listview의 한개의 아이템)를 Object에 담는다

            try {

                jsonObject.put("user_nickname",chat_itemList.get(i).getChat_nickname());
                jsonObject.put("chat_content",chat_itemList.get(i).getChat_content());
                jsonObject.put("hidden_time",chat_itemList.get(i).getHidden_time());

            } catch (JSONException e){
                e.printStackTrace();
            }

            Json_Array.put(jsonObject);
            //Array에 채팅 1개의 내용 insert
        }

        return Json_Array.toString();

    }
    //방송이 종료되면 채팅 내용들을 Json에 담아서 Server에 보낸다. ( Redis에 저장 )
    //broadcast_end() 메서드에 채팅 내용 담아서 서버에 보낸다.


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //본 액티비티를 나가는 순간에 소켓 연결을 해제해준다.
        //더 이상 채팅을 하지 않기 때문.

    }




}








