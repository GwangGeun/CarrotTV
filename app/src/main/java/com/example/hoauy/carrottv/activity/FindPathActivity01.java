package com.example.hoauy.carrottv.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.customdialog.Loading;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 *  1. 정의
 *
 *  Ar 길찾기 ( 화살표로 목적지를 가르키며, 목적지까지의 거리를 사용자에게 보여줌 )
 *
 *
 *
 *  2. 구성
 *
 *
 *  (1) 카메라
 *
 *   surfaceview + camera : 크게 신경 쓸 부분은 없음
 *
 *
 *  (2) 센서
 *
 *  센서가 변함에 따라 화살표의 움직임이 있도록 구성 함
 *
 *  수학적인 부분이 많이 들어가 있음. 수학적인 부분은 이해를 못하고 그냥 가져다 씀. (주석도 마찬가지)
 *
 *
 *  (3) 위치
 *
 *  사용자의 위치를 지속적으로 받아 옴.
 *
 *  처음에 받아오는데 delay 가 있을 수 있음. ( 이 부분은 로딩 중 다이얼로그를 통해 사용자에게 미리 인지시킴 )
 *
 *
 */

public class FindPathActivity01 extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener, LocationListener{


    String TAG = "FindPathActivity01";

    private SurfaceView cameraView;
    private SurfaceHolder cameraHolder;
    private android.hardware.Camera mCamera;
    // 카메라
    ImageView image_arrow; // 화살표 이미지
    TextView text_distance; // 남은 거리
    // 카메라에 overlay 되서 보여지는 것들

    private SensorManager sensorManager;
    GeomagneticField geoField;
    private float currentDegree = 0f;
    // 센서

    LocationManager lm;
    Location lastlocation;
    float BearingToDestination;

    double lat;
    double lon;
    double distance;
    // 위치

    Loading loading;
    // 현재 위치를 받아오는 순간까지 로딩 중 화면 띄어주기

    Intent intent01;
    Location destination;
    // GoogleMap 에서 lat/lon 받아오는 intent


    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        // for the system's orientation sensor registered listeners < : ?? 추후 다시 봐볼 것 >

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

        sensorManager.unregisterListener(this);
        lm.removeUpdates(this);
        //  센서&위치 미수신할때는 해제.

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path01);

        cameraView = (SurfaceView)findViewById(R.id.cameraView);
        image_arrow = (ImageView)findViewById(R.id.image_arrow);
        text_distance = (TextView)findViewById(R.id.text_distance);

        cameraHolder = cameraView.getHolder();
        cameraHolder.addCallback(this);
        cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 카메라

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 센서
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 위치
        intent01 = getIntent();

        destination = new Location("manual");
        destination.setLatitude(intent01.getDoubleExtra("lat",0)); // 위도
        destination.setLongitude(intent01.getDoubleExtra("lon",0)); // 경도
        destination.setAltitude(0); // 고도
        // 목적지의 데이터( 위도 & 경도 ) 지정

        loading = new Loading(FindPathActivity01.this,"현재 위치를 받아오는 중...");
        loading.show();
        // 현재 위치를 받아오는 순간까지 로딩 중 화면 띄어주기

    }



    /**
     *
     *  이하 < 카메라 > 관련 call back Method
     *
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            mCamera = android.hardware.Camera.open();
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

//          image.invalidate();
//          < 이미지 뷰가 보이지 않을 경우 설정해 볼 것 >

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // View 가 존재하지 않을 때
        if (cameraHolder.getSurface() == null) {
            return;
        }

        // 작업을 위해 잠시 멈춘다
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
            // 에러가 나더라도 무시한다.
        }

        // 카메라 설정을 다시 한다.
        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);

        // View 를 재생성한다.
        try {
            mCamera.setPreviewDisplay(cameraHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        // 카메라 사용 안할 경우 --> 메모리의 효율성을 위해 release

    }

    /**
     *
     *  이하 < 센서 > 관련 call back Method
     *
     *
     *
     *  < 주의점 >
     *
     *  1) 대부분 수학적인 계산이 들어간 부분 : 정확하게 이해를 하지는 못했음
     *
     *  2) 1)의 맥락에서 볼 때, onSensorChanged() 이해 할 필요 x
     *
     *  3) 추후 지속적으로 궁금증이 생길 경우 아래 주소 참고
     *
     *  https://stackoverflow.com/questions/36474089/android-compass-point-to-my-location-instead-of-north
     *
     *
     *
     */

    @Override
    public void onSensorChanged(SensorEvent event) {

        geoField = new GeomagneticField(
                (float)lat,
                (float)lon,
                0,
                System.currentTimeMillis());

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        degree += geoField.getDeclination();

        degree = (BearingToDestination - degree) * -1;
//        degree = normalizeDegree(degree);
        degree = (degree + 360) % 360;


        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        image_arrow.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     *
     *  이하 < 위치 > 관련 call back Method
     *
     */

    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude();
        lon = location.getLongitude();

        lastlocation = location;
        BearingToDestination = location.bearingTo(destination);
        // 목적지의 방위 값

        distance = location.distanceTo(destination);
        // 목적지와 현 위치 사이의 거리 (meter)

        String convert_distance = String.format(Locale.KOREA,"%.2f", (distance/1000))+"km";
        // (1) km 로 변환
        // (2) 소수점 2번째 자리까지만
//        Log.e(TAG, "거리 : " + (distance/1000));

        text_distance.setText(convert_distance);
        // 남은 거리

        loading.dismiss();
        //현재 위치를 받아오는 순간 '로딩중' 다이얼로그 제거

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



}
