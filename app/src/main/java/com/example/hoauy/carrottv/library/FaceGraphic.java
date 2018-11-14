package com.example.hoauy.carrottv.library;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.example.hoauy.carrottv.library.GraphicOverlay;
import com.example.hoauy.carrottv.R;
import com.google.android.gms.vision.face.Face;

/**
 *
 *  1. 정의
 *
 *  Mobile Vision Api 얼굴 인식 Library 소스
 *
 *  FaceFilterActivity 에서 쓰임
 *
 *
 *
 *  2. 주의점
 *
 *  (1) 마스크에 종류 5가지 --> bitmap 5개 선언
 *
 *  (2) (1)의 비트맵 이미지 resize 해서 다시 bitmap 을 만듦
 *
 *  (3) 사용자의 마스크 선택에 따라 변화에 따라 updateFace() 의 stat 값이 계속 변화한다.
 *
 *  (4) (3)의 stat 값을 Draw() 에서도 쓰기 위해 updateFace() 의 stat 매개 변수를  전역변수 status 에 담는다.
 *
 *
 *
 */


public class FaceGraphic  extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float GENERIC_POS_OFFSET = 20.0f;
    private static final float GENERIC_NEG_OFFSET = -20.0f;

    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    Bitmap bitmap01;
    Bitmap bitmap02;
    Bitmap bitmap03;
    Bitmap bitmap04;
    Bitmap bitmap05;
    // (1) 마스크 종류

    Bitmap mask_01;
    Bitmap mask_02;
    Bitmap mask_03;
    Bitmap mask_04;
    Bitmap mask_05;
    // (1) 마스크 종류의 bitmap 이미지를 카메라 화면에 출력 할 수 있도록 resize 해서 다시 지정

    int status;
    // updateFace stat 매개변수로 인해 값이 계속 변화한다. ( 사용자의 마스크 선택에 따라 변화 )

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        status =0;
        // 처음에는 0 으로 초기화 ( 마스크 없음 )
        // updateFace stat 매개변수로 인해 값이 계속 변화한다. ( 사용자의 마스크 선택에 따라 변화 )


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;


        bitmap01 = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op05, options);
        mask_01 = bitmap01;

        bitmap02 = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op02, options);
        mask_02 = bitmap02;

        bitmap03 = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op06, options);
        mask_03 = bitmap03;

        bitmap04 = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op04, options);
        mask_04 = bitmap04;

        bitmap05 = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op, options);
        mask_05 = bitmap05;


    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face, int stat) {
        mFace = face;

        status = stat;
        //FaceFilterActivity 에서 받아 온 stat 의 변수 (마스크 종류) 를 draw 에서도 사용하기 위해 전역변수에 새롭게 할당

        if(stat ==1){

            mask_01 = Bitmap.createScaledBitmap(bitmap01, (int) scaleX(face.getWidth()),
                    (int) scaleY(((bitmap01.getHeight() * face.getWidth()) / bitmap01.getWidth())), false);


        }

        else if (stat ==2){

            mask_02 = Bitmap.createScaledBitmap(bitmap02, (int) scaleX(face.getWidth()),
                    (int) scaleY(((bitmap02.getHeight() * face.getWidth()) / bitmap02.getWidth())), false);
        }

        else if(stat == 3){

            mask_03 = Bitmap.createScaledBitmap(bitmap03, (int) scaleX(face.getWidth()),
                    (int) scaleY(((bitmap03.getHeight() * face.getWidth()) / bitmap03.getWidth())), false);
        }

        else if (stat ==4 ){

            mask_04 = Bitmap.createScaledBitmap(bitmap04, (int) scaleX(face.getWidth()),
                    (int) scaleY(((bitmap04.getHeight() * face.getWidth()) / bitmap04.getWidth())), false);
        }

        else if(stat ==5){

            mask_05 = Bitmap.createScaledBitmap(bitmap05, (int) scaleX(face.getWidth()),
                    (int) scaleY(((bitmap05.getHeight() * face.getWidth()) / bitmap05.getWidth())), false);
        }


        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }


        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, mBoxPaint); // 얼굴을 인식해서 정사각형을 그려줌.

        if(status ==1){
            canvas.drawBitmap(mask_01, left+25, top+180, new Paint());
        }
        else if(status ==2){
            canvas.drawBitmap(mask_02, left, top+180, new Paint());
        }
        else if(status ==3){
            canvas.drawBitmap(mask_03, left, top+180, new Paint());
        }
        else if(status ==4){
            canvas.drawBitmap(mask_04, left, top, new Paint());
        }
        else if(status ==5){
            canvas.drawBitmap(mask_05, left, top, new Paint());
        }


    }

    private float getNoseAndMouthDistance(PointF nose, PointF mouth) {
        return (float) Math.hypot(mouth.x - nose.x, mouth.y - nose.y);
    }
}
