package com.example.hoauy.carrottv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.item.Marker_Item;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;

/**
 *  1. 정의
 *
 *  현재 생방송 중인 BJ 들의 리스트를 구글 맵 (클러스터링 활용) 을 통해 보여준다.
 *
 *
 *
 *  2. 주의점
 *
 *
 *  (1) 커스텀 마커를 사용
 *
 *   class ClusterRenderer extends DefaultClusterRenderer<Marker_Item> 를 재정의 함으로써 커스텀 마커 사용
 *
 *
 *  (2) 시연을 위해 데이터들은 더미 데이터들로만 구성. 추후 실제 서비스에서 이용하려면 서버에서 lat / lon 받아와야 한다.
 *
 *      실제 서비스의 경우 : getSampleMarkerItems 의 Marker_Item 의 3번째 매개변수에 이미지 주소를 넣어야 한다.
 *
 *
 *  (3) 마커를 클릭 할 경우 --> Ar 길찾기 or T map 길찾기 제공
 *
 *  (4) 추가 작업 필요한 부분
 *
 *      --> 위치 권한을 요청하는 부분
 *
 *
 */

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    View marker_root_view;
    ImageView tv_marker;
    LinearLayout linear_findPath;
    ScrollView map_scrollview;
    Button btn_ArPath;
    Button btn_Tmap;
    Button btn_streetView;

    double clicked_lat;
    double clicked_lon;
    String clicked_image; // 선택된 이미지 주소는 FindPathAcitivty02 (T map) 에서만 사용
    // 길찾기 Acitivty (Tmap or Camera) 에 넘길 위도,경도, 이미지 주소

    public GoogleMap mMap;
    ClusterManager<Marker_Item> mClusterManager; // --> 실제 클러스터링을 해줌.
    int test_data =1; // 더미데이터 추가를 위한 변수 : ClusterRenderer 내부 참고 할 것.

    LocationManager myLocationManager;
    Boolean isInternetGPSEnabled;
    Boolean isGpsEnabled;
    //GPS 권한 획득을 위한 변수 선언.

    Intent intent02;
    // 길찾기 Activity 로 이동할 때 필요한 intent

    String image_address;
    // 실제 서비스에서 사용 됨 : 서버에서 BJ 의 이미지 주소를 받아오는 변수

    MapFragment mapFragment;
    // 구글맵 Fragment


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlemap);

        android.app.FragmentManager fragmentManager = getFragmentManager();

        mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);
        // 구글맵을 사용하기 위한 일종의 규칙을 선언한 것이라고 보면 됨. ( Api 코드 그대로 인용 )

        myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //위치 서비스 권한 받아오기

        btn_ArPath = (Button)findViewById(R.id.btn_ArPath);
        btn_Tmap = (Button)findViewById(R.id.btn_Tmap);
        btn_streetView = (Button)findViewById(R.id.btn_streetView);
        btn_ArPath.setOnClickListener(listener);
        btn_Tmap.setOnClickListener(listener);
        btn_streetView.setOnClickListener(listener);

        map_scrollview = (ScrollView)findViewById(R.id.map_scrollview);
        map_scrollview.setVisibility(View.GONE);
        linear_findPath = (LinearLayout)findViewById(R.id.linear_findPath);
        linear_findPath.setVisibility(View.GONE);
        // 사용자가 마커를 클릭 했을 경우만 보이게 만듦

    }



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_ArPath :

                    checkGpsAndMove(1);

                    break;

                case R.id.btn_Tmap :

                    checkGpsAndMove(2);

                    break;

                case R.id.btn_streetView :

                    intent02 = new Intent(GoogleMapActivity.this, StreetViewActivity.class);
                    intent02.putExtra("lat", clicked_lat);
                    intent02.putExtra("lon", clicked_lon);
                    startActivity(intent02);
                    // Bj의 위치 정보만 필요하기 때문에, 위의 두개 버튼과는 달리
                    // 사용자의 현재 위치 동의를 구하지 않음.

                    break;


            }


        }
    };

    public void checkGpsAndMove(int toActivtity){

        isInternetGPSEnabled = myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // 시스템 > 설정 > 위치 및 보안 > 무선 네트워크 사용 여부 체크.
        isGpsEnabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 시스템 > 설정 > 위치 및 보안 > GPS 위성 사용 여부 체크.

        if((!isInternetGPSEnabled) || (!isGpsEnabled)){

            showSettingsAlert();
            return;

        }
        //GPS 기능이 꺼져있는 경우, 사용자에게 사용할 것인지 물어 봄.

        else {

            if(toActivtity ==1){

                intent02 = new Intent(GoogleMapActivity.this, FindPathActivity01.class);
                intent02.putExtra("lat", clicked_lat);
                intent02.putExtra("lon", clicked_lon);
                startActivity(intent02);

            }
            // Ar 길찾기 Activity 로 이동
            else if(toActivtity ==2){

                intent02 = new Intent(GoogleMapActivity.this, FindPathActivity02.class);
                intent02.putExtra("lat", clicked_lat);
                intent02.putExtra("lon", clicked_lon);
                startActivity(intent02);

            }
            //Tmap 길찾기 Activity 로 이동

        }
        //GPS 기능이 켜져 있는 경우, 정상적으로 길찾기 Activity 로 이동

    }

    public void showSettingsAlert(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GoogleMapActivity.this);
        alertDialog.setTitle("위치 서비스 사용");
        alertDialog.setMessage("지도를 사용하기 위해서는 단말기의 설정에서 '위치 서비스' 사용을 허용 해주세요. " +
                "허용하지 않을 시, 길찾기 서비스는 사용할 수 없습니다.");
        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("돌아가기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                dialog.cancel();

            }
        });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("설정하기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });

        AlertDialog dialog = alertDialog.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        // 이 부분을 설정하지 않으면 버튼이 보이지 않는다. : 그 이유는 추후 검토

    }
    //GPS 기능을 사용하기 위해 사용자에게 알림창을 띄워줌.


    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.537523, 126.96558), 14));
        // 구글 맵 초기 화면지정
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        setCustomMarkerView();
        //커스텀한 마커를 view로 만들어 주는 작업

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        ClusterRenderer clusterRenderer = new ClusterRenderer(getApplicationContext(), map, mClusterManager);
        // (1) clusterManager.setRenderer 를 원래 선언해야 되나 여기서는 선언할 필요가 없다.
        // (2) why ? 클래스 내부 생성자에서 선언하고 있기 때문

        getSampleMarkerItems();
        // (1) 데이터 추가하는 부분
        // (2) 실제 서비스에서는 서버에서 데이터를 가져와서 여기에 lat/lon 넣어줘야한다
        // (3) 시연 시 에는 더미데이터로만 진행

    }

    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.googlemap_marker, null);
        tv_marker = (ImageView) marker_root_view.findViewById(R.id.tv_marker);
    }

    private void getSampleMarkerItems() {

        double lat;
        double lng;

        for(int i=0; i<5; i++) {
            lat = 37.537523 + (i / 200d);
            lng = 126.96558 + (i / 200d);
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));
        }

            lat = 37.527523;
            lng = 126.96568;
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));

            lat = 37.549523;
            lng = 126.96568;
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));

            lat = 37.538523;
            lng = 126.9576;
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));

            lat = 35.17944;
            lng = 129.07556;
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));
            //부산

            lat = 36.35111;
            lng = 127.38500;
            mClusterManager.addItem(new Marker_Item(lat,lng, ""));
            //대전

    }
    // (1) 시연을 위한 더미데이터
    // (2) 실제 서비스에서는 서버에서 받아온 값을 지정해 주면 된다.


    private Bitmap createDrawableFromView(Context context, View view)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    // (1) 커스텀 마커 --> Bitmap으로 변환하는 코드
    // (2) (1)이 왜 필요한가? --> ClusterRenderer 의 내부의 markeroption 에서 icon을 bitmap 으로만 받기 때문이다.


    @Override
    public void onMapClick(LatLng latLng) {

        map_scrollview.setVisibility(View.GONE);
        linear_findPath.setVisibility(View.GONE);

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);

        clicked_lat = marker.getPosition().latitude; // 위도
        clicked_lon = marker.getPosition().longitude; // 경도
        clicked_image = marker.getTitle(); // (1) 이미지 주소 (시연 시에는, R.drawble.이미지 를 사용 함)
                                           // (2) FindPathAcitivty02 (T map) 에서만 사용
        // 길찾기 Activity에 넘길 값들

        map_scrollview.setVisibility(View.VISIBLE);
        linear_findPath.setVisibility(View.VISIBLE);
        // 길찾기 버튼 보이기

        return true;

    }
    //마커 클릭시 이벤트 처리


    class ClusterRenderer extends DefaultClusterRenderer<Marker_Item> {

        ClusterRenderer(Context context, GoogleMap map, ClusterManager<Marker_Item> clusterManager) {
            super(context, map, clusterManager);
            clusterManager.setRenderer(this);
        }


        @Override
        protected void onBeforeClusterItemRendered(Marker_Item markerItem, MarkerOptions markerOptions) {

            LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());
            image_address = markerItem.getImage_address();
            // (1) 실제 서버에서 이미지를 가져올 경우, BJ의 프로필 이미지 주소임.


//        Glide.with(this)
//                .load("http://kmkmkmd.vps.phps.kr/image/"+image_address)
//                .centerCrop() // imageview의 크기에 맞게 서버에서 가져 온 이미지를 조정 해 준다.
//                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
//                .into(tv_marker);
//
//    실제 서비스에서는 위의 코드 (Glide)를 이용해 방송 중인 BJ의 썸네일 이미지를 받아온다.

            if( (test_data%5) ==1){

                tv_marker.setImageResource(R.drawable.battle);

            } else if((test_data%5) ==2){

                tv_marker.setImageResource(R.drawable.gamst);

            } else if((test_data%5) ==3){

                tv_marker.setImageResource(R.drawable.flash);

            } else if ((test_data%5) ==4){

                tv_marker.setImageResource(R.drawable.jd);

            } else if((test_data%5) ==0){

                tv_marker.setImageResource(R.drawable.star);
            }
            // 시연을 위한 더미데이터들

            test_data++;
            // 시연 이미지들을 다양하게 지정하기 위해 사용

            markerOptions.title(image_address);
//            실제 서비스에서 사용하는 이미지 주소

            markerOptions.position(position);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(GoogleMapActivity.this, marker_root_view)));

            markerOptions.visible(true);

        }

    }
    //마커를 커스텀 하는 부분


}
