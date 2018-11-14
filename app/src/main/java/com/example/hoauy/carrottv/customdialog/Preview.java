package com.example.hoauy.carrottv.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.hoauy.carrottv.R;
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

/**
 *
 *
 *  1. 정의
 *
 *    Tab01의 생방송 롱클릭 --> 방송 미리보기를 제공하는 Dialog
 *
 *
 *
 *  2. 주의점
 *
 *   lpWindow.dimAmount = 0.3f; 를 높이면 배경이 되는 BroadCastActivity 와
 *   Exoplayer 2개 모두 화면이 어두워진다.
 *
 *
 *
 */

public class Preview extends Dialog {

    Context context;
    String title;
    String nickname;
    String file_name;

    public Preview(Context context, String title, String nickname, String file_name){

        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.title = title;
        this.nickname = nickname;
        this.file_name = file_name;

    }

    /**
     *
     *  이하 아래부터는 Exoplayer 관련 변수들
     *
     */

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

    TextView preview_title;
    TextView preview_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.3f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;

        setContentView(R.layout.dialog_preview);

        init();

    }

    public void init(){

        simpleExoPlayerView = (SimpleExoPlayerView)findViewById(R.id.player_preview);
        simpleExoPlayerView.setUseController(false);

        preview_title = (TextView)findViewById(R.id.preview_title);
        preview_nickname = (TextView)findViewById(R.id.preview_nickname);

        preview_title.setText(title);
        preview_nickname.setText(nickname);

        VIDEO_URI = "http://kmkmkmd.vps.phps.kr/dash/"+file_name+"/index.mpd";

        userAgent = Util.getUserAgent(context,"SimpleDashExoPlayer");
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
        player = ExoPlayerFactory.newSimpleInstance(context,trackSelector,loadControl);

    }

    // Set player to SimpleExoPlayerView
    public void attachPlayerView(){
        simpleExoPlayerView.setPlayer(player);
    }

    // Build Data Source Factory, Dash Media Source, and Prepare player using videoSource
    public void preparePlayer(){

        player.setPlayWhenReady(true);
        //Player 자동 시작

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
        return new DefaultDataSourceFactory(context, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    // Build Http Data Source Factory using DefaultBandwidthMeter
    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter){
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        player.release();

    }
}
