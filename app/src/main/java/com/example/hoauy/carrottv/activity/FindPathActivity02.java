package com.example.hoauy.carrottv.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.customdialog.Loading;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

/**
 *
 *
 *  1. 정의
 *
 *  T map 을 통해 사용자의 위치로 부터 목적지 ( BJ의 방송지 ) 까지의 경로를 그려줌
 *
 *
 *
 *
 *  2. 주의점
 *
 *  (1) T map 사용 시, T map 홈페이지에서 발급받은 Server Key 가 필요 함
 *
 *
 *  (2) T map 의 모든 메소드들은 매개변수가 ( 위도, 경도 ) 순서이다.
 *
 *      그러나, tMapView.setCenterPoint(start_lon, start_lat); 를 보면
 *
 *      T map 의 focus 를 이동 시키는 메소드만 ( 경도, 위도 ) 순서이다.
 *
 *      이 부분 헷갈리지 않도록 주의 할 것
 *
 *
 *  (3) 사용자의 현재 위치를 받아오기 전 : 로딩 중 Dialog 를 제공
 *
 *      사용자의 현재 위츠를 받아온 후 : 로딩 중 Dialog 를 제거
 *
 *
 *
 *  3. 참고
 *
 *   http://tmapapi.sktelecom.com/main.html#android/guide/androidGuide.sample3
 *
 *   < T map 개발자 Guide 공식 홈페이지 >
 *
 *
 */

public class FindPathActivity02 extends AppCompatActivity implements LocationListener{

    String Tmap_Key = "e4586b62-c01d-48b2-a0ee-f66f5de1dd96";
    // Tmap 사용시 필요한 Server Key
    String TAG = "FindPathActivity02";

    LinearLayout linearLayoutTmap;
    TMapView tMapView;

    TMapPoint tMapPointStart;
    TMapPoint tMapPointEnd;
    // Tmap 출발지 & 목적지 지정

    TMapPolyLine tMapPolyLine;
    // Tmap 경로 그려주는 객체

    TMapMarkerItem markerItem01; // 목적지
    TMapMarkerItem markerItem02; // 출발지
    TMapPoint tMapPoint01; // 목적지 지점
    TMapPoint tMapPoint02; // 출발지 지점
    Bitmap bitmap;
    // Tmap 마커를 커스텀하기 위한 아이템

    LocationManager lm;

    double lat;
    double lon;
    // 현재 위치

    Intent intent01;
    double go_lat;
    double go_lon;
    //  GoogleMap 으로부터 목적지의 위치 받아오기

    Loading loading;
    // 현재 위치를 받아오는 순간까지 로딩 중 화면 띄어주기

    @Override
    protected void onResume() {
        super.onResume();

        try {

            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    50, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m)
                    this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    50, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m)
                    this);


        } catch (SecurityException e){
            e.printStackTrace();
        }
        // 현재 위치 받아오기 start

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(bitmap !=null){
            bitmap.recycle();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path02);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 위치

        linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        tMapView = new TMapView(this);
        // Tmap 생성

        intent01 = getIntent();
        go_lat = intent01.getDoubleExtra("lat",0);
        go_lon = intent01.getDoubleExtra("lon",0);
        tMapPointEnd = new TMapPoint(go_lat,go_lon);
        // 목적지의 위치
        // cf) 출발지의 위치는 onLocationChanged() 에서 지정

        markerItem01 = new TMapMarkerItem();
        markerItem02 = new TMapMarkerItem();
        tMapPoint01 = new TMapPoint(go_lat, go_lon);
        // (1) 마커를 커스텀 하기 위한 목적
        // (2) 목적지는 여기서 / 출발지는 findPathData() 에서 설정해 준다.

        tMapView.setSKTMapApiKey( Tmap_Key );
        linearLayoutTmap.addView( tMapView );
        // Tmap LinearLayout 에 부착 ( Tmap이 실제로 나타나는 부분 )

        loading = new Loading(FindPathActivity02.this,"현재 위치를 받아오는 중...");
        loading.show();
        // 현재 위치를 받아오는 순간까지 로딩 중 화면 띄어주기

    }


    /**
     *
     *  이하 < 위치 > 관련 call back Method
     *
     */
    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude(); // 현재 위도
        lon = location.getLongitude(); // 현재 경도

        findPathData(lat,lon);
        //경로 그려주기

        lm.removeUpdates(this);
        // (1) 지속적으로 위치를 받아올 필요는 없음.
        // (2) 위치 한번 받아오고 끝.

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void findPathData(final double start_lat, final double start_lon) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    tMapPointStart = new TMapPoint(start_lat, start_lon);
                    // 현재 사용자의 위치 ( Tmap 경로의 출발지 )

                    tMapPolyLine = new TMapData().findPathData(tMapPointStart, tMapPointEnd);
                    tMapPolyLine.setLineColor(Color.BLUE);
                    tMapPolyLine.setLineWidth(2);
                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                    // 지도에 경로 그리기 ( 출발지 --> 목적지 )

                    tMapPoint02 = new TMapPoint(start_lat, start_lon);
                    // (1) 마커를 커스텀 하기 위한 목적
                    // (2) 자세한건 marker_custom() 메소드 참조

                    tMapView.setCenterPoint(start_lon, start_lat);
                    tMapView.setZoomLevel(13);
                    // (1) T map 포커스를 출발지 지점으로 변경
                    // (2) T mapPoint 와는 달리 위도 경도 매개변수 위치가 다르다는 것에 주의

                    marker_custom();
                    // 마커를 커스텀한 이미지로 대체

                    loading.dismiss();
                    // 현재 위치를 받아오는 순간 로딩 중 화면 없애기

                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        }).start();
    }
    // 현재 위치로 부터 목적지(BJ 위치) 까지 경로 그려주기

    public void marker_custom(){

        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.carrot);
        bitmap = resizeBitmap(bitmap);


        markerItem01.setIcon(bitmap); // 마커 아이콘 지정
        markerItem01.setPosition(0.5f, 0.5f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem01.setTMapPoint( tMapPoint01 ); // 마커의 좌표 지정
        markerItem01.setName("목적지"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem01", markerItem01); // 지도에 마커 추가

        markerItem02.setIcon(bitmap); // 마커 아이콘 지정
        markerItem02.setPosition(0.5f, 0.5f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem02.setTMapPoint( tMapPoint02 ); // 마커의 좌표 지정
        markerItem02.setName("출발지"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem02", markerItem02); // 지도에 마커 추가


    }

    public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = 100;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }
    // (1) Tmap의 아이콘은 비트맵으로만 받음 & resize 기능이 없음
    // (2) bitmap 이미지를 resize 해주는 기능



}
