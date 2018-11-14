package com.example.hoauy.carrottv.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 *   1. 정의
 *
 *   메모장 기능 중 그림으로 메모하는 Activity ( 그림판 개념 )
 *
 *
 *
 *   2. 주의점
 *
 *   (1) OnDraw 부분에  canvas.drawPath(pathInfo, paint); 이 부분을 제거하면
 *       손가락 움직임에 따라 실시간으로 그려지지 않음. 즉, 손가락을 떼는 순간 그려진게 보여짐
 *
 *   (2) save() Method 에서 canvas.drawColor(Color.WHITE); 이 부분이 없으면 그림판 이미지 저장 시, 배경이 검정색
 *
 *   (3) 선 한개를 그릴 때마다, Paint 와 View 의 객체를 지속적으로 생성 해줌.
 *       why ? Undo 를 할 때 한개의 선을 지움 --> 이전에 그린 선에는 영향을 주면 안됨.
 *       자세한 설명은 하단의 3.참고주소 (1) 을 참고 할 것.
 *
 *
 *
 *   3. 참고 주소
 *
 *   (1) 그림판 전체적인 예제
 *
 *   http://heepie.tistory.com/96?category=719149
 *
 *   (2) Undo
 *
 *   https://gist.github.com/asanand3/a7846ed6d76c821dfac3
 *
 *   (3) 그림판 이미지 스크린 샷 (캡쳐)
 *
 *   https://stackoverflow.com/questions/22096350/capture-bitmap-from-view-android
 *
 *
 *
 */

public class PaintActivity extends AppCompatActivity {

    String TAG = "PaintActivity";
    String url = "http://kmkmkmd.vps.phps.kr/mariadb/memo_AddOrDelete.php";
    String temp_filePath;
    String id;

    CustomView customView;

    Intent intent01;
    // 이전 액티비티로 부터 사용자 정보를 받아올 때 사용

    RelativeLayout relative_memo;
    SeekBar seekBar;

    ImageView image_black;
    ImageView image_blue;
    ImageView image_yellow;
    ImageView image_red;
    ImageView image_green;

    int color = Color.BLACK;
    float bold = 5f;
    // 초기 설정
    // 1. 선 굵기 : 50 %
    // 2. 선 색상 : 검정색

    Button btn_undo;
    Button btn_save03;
    Button btn_back03;

    TextView text_color;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        intent01 = getIntent();
        id = intent01.getStringExtra("id");

        relative_memo = (RelativeLayout)findViewById(R.id.relative_memo);
        btn_undo = (Button)findViewById(R.id.btn_undo);
        btn_back03 = (Button)findViewById(R.id.btn_back03);
        btn_save03 = (Button)findViewById(R.id.btn_save03);

        image_black = (ImageView)findViewById(R.id.image_black);
        image_blue = (ImageView)findViewById(R.id.image_blue);
        image_yellow = (ImageView)findViewById(R.id.image_yellow);
        image_red = (ImageView)findViewById(R.id.image_red);
        image_green = (ImageView)findViewById(R.id.image_green);

        text_color = (TextView)findViewById(R.id.text_color);
        // 현재 선택한 색상을 표시해주는 textView

        btn_undo.setOnClickListener(listener);
        btn_back03.setOnClickListener(listener);
        btn_save03.setOnClickListener(listener);

        image_black.setOnClickListener(listener);
        image_blue.setOnClickListener(listener);
        image_yellow.setOnClickListener(listener);
        image_red.setOnClickListener(listener);
        image_green.setOnClickListener(listener);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setProgress(50);
        // 첫 화면 : 선 굵기를 중간으로 설정한 것을 seekBar에 표시

        customView = new CustomView(this);
        customView.setPaintInfo(Color.BLACK, 5);
        relative_memo.addView(customView);
        // (1) CustomView 를 일종의 스케치북이라고 이해하면 됨
        // (2) 스케치북을 relative 뷰에 붙인거임


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                bold = (float)seekBar.getProgress() / 10;
                customView.setPaintInfo(color, bold);

            }
        });
        //사용자가 선의 굵기를 변경할 때 반영하기 위한 목적

    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_undo :

                    customView.onClickUndo();

                    break;

                case R.id.image_black :

                    color = Color.BLACK;
                    customView.setPaintInfo(color, bold);
                    text_color.setText("검정색");
                    text_color.setTextColor(color);

                    break;

                case R.id.image_blue :

                    color = Color.BLUE;
                    customView.setPaintInfo(color, bold);
                    text_color.setText("파란색");
                    text_color.setTextColor(color);

                    break;

                case R.id.image_yellow :

                    color = Color.YELLOW;
                    customView.setPaintInfo(color, bold);
                    text_color.setText("노란색");
                    text_color.setTextColor(color);

                    break;

                case R.id.image_green :

                    color = Color.GREEN;
                    customView.setPaintInfo(color, bold);
                    text_color.setText("초록색");
                    text_color.setTextColor(color);

                    break;

                case R.id.image_red :

                    color = Color.RED;
                    customView.setPaintInfo(color, bold);
                    text_color.setText("빨간색");
                    text_color.setTextColor(color);

                    break;

                case R.id.btn_back03 :

                    finish();

                    break;

                case R.id.btn_save03 :

                    Bitmap screenshot = save(customView);
                    // 그림판 screenshot 찍어서 bitmap 으로 반환

                    temp_filePath = saveBitmapToJpeg(PaintActivity.this, screenshot, "temp02");
                    // (1) 서버에 이미지 저장하기 위해 임시 파일생성
                    // (2) temp_filePath 는 임시 파일 저장경로
                    // (3) 임시파일은 이미지가 서버에 전송 된 이후에 삭제된다. memoToServer() 참고

                    Date date = new Date();

                    SimpleDateFormat sdf = new SimpleDateFormat ("yyyy.MM.dd", Locale.KOREA );
                    String currentTime = sdf.format(date);
                    // 메모를 작성하는 시간

                    SimpleDateFormat sdf02 = new SimpleDateFormat ("yyMMddHHmmss", Locale.KOREA );
                    String currentTime02 = sdf02.format(date);
                    // 이미지 저장시 이미지 이름을 (id + 현재 시간) 으로 만들기 위한 용도

                    memoToServer(temp_filePath, "1" // 글 작성 시에는 1 & 글 삭제 시에는 2 를 요청
                            , id, "0" // 메모 중 이미지를 저장하는 경우 : 메모 내용은 0
                            , currentTime , "1" // 이미지를 저장하는 경우 : file path 1
                                                // ( 이 부분은 div 변수를 활용해서 쫌 더 쉽게 구조를 짤 수 있는 것을 실수한 부분 )
                            , currentTime02
                    );
//                    서버에 해당 메모(이미지) 저장

                    break;

            }

        }
    };


    public Bitmap save(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
            // 이 부분이 없으면 이미지가 저장될 경우, Background 가 검정색임
        }
        view.draw(canvas);
        return bitmap;
    }
    // 그림판 ScreenShot 찍어서 Bitmap 으로 반환

    public class CustomView extends View {

        Paint paint;
        // '붓' 에 해당하는 paint 클래스 변수 선언
        PathInfo pathInfo;

        ArrayList<PathInfo> data = new ArrayList<PathInfo>();
        ArrayList<PathInfo> undonePaths = new ArrayList<PathInfo>();
        // 위치 값들을 저장하기 위한 List

        int color;
        float bold;
        // 1. 선 굵기
        // 2. 선 색상

        public CustomView(Context context) {
            super(context);

        }

        public void onClickUndo () {
            if (data.size()>0)
            {
                undonePaths.add(data.remove(data.size()-1));
                invalidate();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "지울게 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        // 바로 직전의 선을 지우려고 할 때 사용 ( Undo )

        public void setPaintInfo(int color, float bold) {

            this.color =color;
            this.bold = bold;

            paint = new Paint();
            paint.setColor(color);
            // 선 색상 설정
            paint.setStyle(Paint.Style.STROKE);
            // 선 을 그린다는 것을 설정
            paint.setStrokeWidth(bold);
            // 선 두께 설정
            pathInfo = new PathInfo();
            pathInfo.setPaint(paint);

        }
        // 색상 or 선의 굵기를 변경하려고 할 때 호출 됨

        public void createObject(){

            paint = new Paint();
            paint.setColor(color);
            // 선 색상 설정
            paint.setStyle(Paint.Style.STROKE);
            // 선 을 그린다는 것을 설정
            paint.setStrokeWidth(bold);
            // 선 두께 설정
            pathInfo = new PathInfo();
            pathInfo.setPaint(paint);

        }
        // (1) Paint와 PathInfo 객체 재생성
        //
        //     why ? 선 1개를 그릴 때 마다 독립성을 부여해야 undo 가 가능
        //           독립성 부여를 위해 선을 한개 그리고 난 직후 새롭게 객체를 생성해준다.
        //
        // (2) Paint 의 color와 bold 는 이전 속성 유지


        @Override
        protected void onDraw(Canvas canvas) {

            for (PathInfo p : data) {
                // 선 Draw
                canvas.drawPath(p, p.getPaint());
            }

            canvas.drawPath(pathInfo, paint);
            // *** 이 부분이 중요 ***
            //
            // (1) 이 부분이 없을 경우 Undo 실행은 되나
            // (2) 그림을 그리려는 경우, 손가락 움직임에 따라 그려지는 것이 아니라
            //     그림을 다 그린 후에 화면에 표시 됨

//            super.onDraw(canvas);

        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    // View를 처음 클릭 시 실행되는 곳

                    pathInfo.moveTo(event.getX(), event.getY());

                    break;

                case MotionEvent.ACTION_MOVE:
                    // View를 누르고 이동할 경우 호출

                    pathInfo.lineTo(event.getX(), event.getY());

                    break;

                case MotionEvent.ACTION_UP:
                    // View에서 터치를 뗄 경우 호출

                    data.add(pathInfo);

                    customView.createObject();
                    // 선을 다 그릴 때마다 객체를 생성해준다.
                    // 자세한 설명은 createObject() 메소드 참고

                    break;

            }

//            data.add(pathInfo);

            invalidate();
            // 화면을 강제로 그리기 위해 호출하는 메소드

            return true;

        }
    }
    //CustomView 하단

    /**
     *  Path 마다 Paint 객체를 사용하기 위해 Path 클래스 상속
     */
    class PathInfo extends Path {

        private Paint paint;

        PathInfo() {
            paint = new Paint();
        }

        public Paint getPaint() {
            return paint;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

    }
    //PathInfo 하단

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
    // (1) 그림판에서 캡쳐한 이미지를 임시 파일로 만들어서 저장
    // (2) why? 서버에 업로드하려면 임시적으로 저장되어 있어야 한다.


    public void memoToServer(final String temp_filePath,  String div, String id,
                                  String content, String day, String file_path, String forImageName){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", id)
                .addFormDataPart("div", div)
                .addFormDataPart("content", content)
                .addFormDataPart("day", day)
                .addFormDataPart("file_path", file_path)
                .addFormDataPart("upload", id+forImageName+".jpg", RequestBody.create(MultipartBody.FORM, new File(temp_filePath)))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                File f = new File(temp_filePath);
                if(f.delete()){
                    Log.e(TAG,"임시 파일 삭제");
                } // 임시 파일 삭제
                else {
                    Log.e(TAG,"임시 파일 삭제 fail");
                } // 임시 파일 삭제 실패 시

                finish();

            }
        });

    }
    // 이미지 서버에 저장


}
