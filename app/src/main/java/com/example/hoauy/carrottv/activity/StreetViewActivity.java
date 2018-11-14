package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.hoauy.carrottv.R;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 *
 *  1. 정의
 *
 *  GoogleMapActivity ( 현재 방송 중인 BJ 들의 위치를 나타내주는 Map) 에서
 *
 *  위도와 경도를 받아 구글 스트릿뷰를 제공
 *
 *
 *  2. 주의점
 *
 *  본 액티비티가 종료될 때,
 *
 *  " e/libEGL: call to OpenGL ES API with no current context (logged once per thread) google street view "
 *
 *  와 같은 로그가 찍힘.
 *
 *  현재 문제는 없어서 그대로 진행.
 *
 *
 *  3. 참고한 사이트
 *
 *  (1) http://codinginfinite.com/google-map-street-view-android-example/
 *
 *  (2) https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src
 *            /main/java/com/example/mapdemo/StreetViewPanoramaBasicDemoActivity.java
 *
 */

public class StreetViewActivity extends AppCompatActivity{

    StreetViewPanoramaFragment streetViewPanoramaFragment;

    Intent intent01;
    // GoogleMapActivity 에서 현재 방송 중인 BJ 들이 위치 해 있는 장소의
    // '위도'와 '경도'를 받기 위해 사용

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);

        intent01 = getIntent();

        streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.streetViewMap);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

                streetViewPanorama.setPosition(new LatLng(intent01.getDoubleExtra("lat",0), intent01.getDoubleExtra("lon",0)));

            }
        });
        // 구글 스트릿뷰를 사용하기 위한 일종의 규칙들을 선언한 것이라고 보면 됨.

    }



}
