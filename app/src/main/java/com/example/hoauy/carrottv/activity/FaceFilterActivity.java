package com.example.hoauy.carrottv.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.library.CameraSourcePreview;
import com.example.hoauy.carrottv.library.FaceGraphic;
import com.example.hoauy.carrottv.library.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



/**
 *
 *  1. 정의
 *
 *  Mobile Vision Api 를 사용해서 얼굴 윤곽을 인식 한 후
 *
 *  마스크를 씌워준다.
 *
 *
 *  2. 구성
 *
 *  (1) Mobile Vision Api 코드들 : 카메라 시작 및 library 아래의 다른 클래스와의 연결
 *
 *  (2) 마스크 선택 시 실시간으로 반영 할 수 있도록 change_mask 라는 변수를 둠
 *
 *  (3) 사진 촬영시 서버에 업로드 & MainActivity 로 back
 *
 *  (4) 카메라 권한
 *
 *
 *  3. 주의점
 *
 *  bitmap 이미지들을 사용 후에 recycle() 시켜주지 않으면
 *
 *  Out of Memory 에러가 발생한다.
 *
 *
 */


public class FaceFilterActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    int RESULT_PERMISSIONS = 100;

    Intent intent01;
    String id;

    Button btn_takepicture;
    int change_mask; // 선택한 마스크를 알기 위한 변수

    Button mask_01;
    Button mask_02;
    Button mask_03;
    Button mask_04;
    Button mask_05;
    // 마스크 종류 5가지

    Bitmap loadedImage;
    Bitmap picture;
    Bitmap overlay;
    Bitmap result;
    // onPictureTaken 에 사용 되는 Bitmap 들


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        change_mask = 0;
        intent01 = getIntent();
        id = intent01.getStringExtra("id");
        // 사용자의 id : 서버에 이미지 저장할 시에 필요

        mPreview = (CameraSourcePreview)findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        mask_01 = (Button)findViewById(R.id.mask_01);
        mask_01.setOnClickListener(listener);
        mask_02 = (Button)findViewById(R.id.mask_02);
        mask_02.setOnClickListener(listener);
        mask_03 = (Button)findViewById(R.id.mask_03);
        mask_03.setOnClickListener(listener);
        mask_04 = (Button)findViewById(R.id.mask_04);
        mask_04.setOnClickListener(listener);
        mask_05 = (Button)findViewById(R.id.mask_05);
        mask_05.setOnClickListener(listener);

        btn_takepicture = (Button)findViewById(R.id.btn_takePicture);
        btn_takepicture.setOnClickListener(listener);


        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestPermissionCamera();
        }
        // 카메라 권한 체크


    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.mask_01 :

                    change_mask =1;

                    break;

                case R.id.mask_02 :

                    change_mask = 2;

                    break;

                case R.id.mask_03 :

                    change_mask = 3;

                    break;

                case R.id.mask_04 :

                    change_mask = 4;

                    break;

                case R.id.mask_05 :

                    change_mask = 5;

                    break;

                case R.id.btn_takePicture :


                    mCameraSource.takePicture(null,new CameraSource.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes) {

                                    Exif x = new Exif();

                                    int orientation = x.getOrientation(bytes);

                                     // Generate the Face Bitmap
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 12;
                                    loadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                    // 1/8 의 크기로 이미지를 받아온다 ( out of memery 오류 방지를 위해서 )

                                    picture = rotateImage(loadedImage,orientation);
                                    // (1) 이미지를 회전 및 좌우 반전
                                    // (2) 아래의 코드를 위의 rotateImage() 함수를 통해 해결


//                                    Matrix rotateMatrix = new Matrix();
//                                    rotateMatrix.postRotate(270);
//                                    Bitmap rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
//                                            loadedImage.getWidth(), loadedImage.getHeight(),
//                                            rotateMatrix, false);
//                                    //회전
//
//                                    Matrix rotateMatrix02 = new Matrix();
//                                    rotateMatrix02.setScale(-1,1);
//                                    Bitmap rotatedBitmap02 = Bitmap.createBitmap(rotatedBitmap, 0, 0,
//                                            rotatedBitmap.getWidth(), rotatedBitmap.getHeight(),
//                                            rotateMatrix02, false);
                                    //좌우 반전


                                    // Generate the Eyes Overlay Bitmap
                                    mPreview.setDrawingCacheEnabled(true);
                                    overlay = mPreview.getDrawingCache();

                                    result = mergeBitmaps(picture, overlay);
                                    // 마스크랑 얼굴을 합쳐주는 곳

                                    String temp_filePath = saveBitmapToJpeg(FaceFilterActivity.this, result, "temp");
                                    // 임시파일에 촬영한 사진을 저장 해 준다.

                                    profile_to_Server(temp_filePath);
                                    // (1) 서버에 촬영한 사진을 업로드 해 준다.
                                    // (2) 임시 파일을 삭제 해 준다.
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);

                                    finish();

                        }
                    });

                    break;


            }

        }
    };


    /**
     *
     *  1. 정의
     *
     *  얼굴은 인식하는 카메라를 생성하는 Method
     *
     *  < 공식 홈페이지 주석 >
     *  Creates and starts the camera.  Note that this uses a higher resolution in comparison
     *  to other detection examples to enable the barcode detector to detect small barcodes
     *  at long distances.
     *
     *
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.e(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

//        System.gc();
        startCameraSource();

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(loadedImage != null){

            loadedImage.recycle();
            loadedImage =null;
            picture.recycle();
            picture = null;
            overlay.recycle();
            overlay = null;
            result.recycle();
            result = null;
        }
        // 사진을 촬영하지 않고 back 버튼 눌러서 생기는
        // NullPoint 예외 방지

    }
    // Out of memory 를 방지하기 위해서 onStop 시에 bitmap 이미지들을 다 clear 해준다.

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }




    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     *
     * 1. 정의
     *
     *  카메라를 start
     *
     *
     *  < 공식 홈페이지 주석 >
     *
     *  Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     *  (e.g., because onResume was called before the camera source was created), this will be called
     *  again when the camera source is created.
     *
     *
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     *  1. 정의
     *
     *  face tracker를 생성하기 위한 factory
     *
     *
     *   < 공식 홈페이지 주석 >
     *
     *     Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     *     uses this factory to create face trackers as needed -- one for each individual.
     *
     *
     *
     *
     *  2. 주의점
     *
     *   onUpdate() 에서
     *
     *   선택한 마스크를 mFaceGraphic 에 넘겨준다.
     *
     *
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face, change_mask);
            // 선택한 마스크 (change_mask) 를 FaceGraphic에 전달한다.

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }





    public Bitmap mergeBitmaps(Bitmap face, Bitmap overlay) {
        // Create a new image with target size
        int width = face.getWidth();
        int height = face.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Rect faceRect = new Rect(0,0,width,height);
        Rect overlayRect = new Rect(0,0,overlay.getWidth(),overlay.getHeight());

        // Draw face and then overlay (Make sure rects are as needed)
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(face, faceRect, faceRect, null);
        canvas.drawBitmap(overlay, overlayRect, faceRect, null);
        return newBitmap;
    }
    // 얼굴 & 마스크를 합쳐주는 코드


    private Bitmap rotateImage(Bitmap bm, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bm;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            default:
                return bm;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            bm.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }



    public class Exif {
        private static final String TAG = "CameraExif";

        // Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
        public int getOrientation(byte[] jpeg) {
            if (jpeg == null) {
                return 0;
            }

            int offset = 0;
            int length = 0;

            // ISO/IEC 10918-1:1993(E)
            while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
                int marker = jpeg[offset] & 0xFF;

                // Check if the marker is a padding.
                if (marker == 0xFF) {
                    continue;
                }
                offset++;

                // Check if the marker is SOI or TEM.
                if (marker == 0xD8 || marker == 0x01) {
                    continue;
                }
                // Check if the marker is EOI or SOS.
                if (marker == 0xD9 || marker == 0xDA) {
                    break;
                }

                // Get the length and check if it is reasonable.
                length = pack(jpeg, offset, 2, false);
                if (length < 2 || offset + length > jpeg.length) {
                    Log.e(TAG, "Invalid length");
                    return 0;
                }

                // Break if the marker is EXIF in APP1.
                if (marker == 0xE1 && length >= 8 &&
                        pack(jpeg, offset + 2, 4, false) == 0x45786966 &&
                        pack(jpeg, offset + 6, 2, false) == 0) {
                    offset += 8;
                    length -= 8;
                    break;
                }

                // Skip other markers.
                offset += length;
                length = 0;
            }

            // JEITA CP-3451 Exif Version 2.2
            if (length > 8) {
                // Identify the byte order.
                int tag = pack(jpeg, offset, 4, false);
                if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                    Log.e(TAG, "Invalid byte order");
                    return 0;
                }
                boolean littleEndian = (tag == 0x49492A00);

                // Get the offset and check if it is reasonable.
                int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
                if (count < 10 || count > length) {
                    Log.e(TAG, "Invalid offset");
                    return 0;
                }
                offset += count;
                length -= count;

                // Get the count and go through all the elements.
                count = pack(jpeg, offset - 2, 2, littleEndian);
                while (count-- > 0 && length >= 12) {
                    // Get the tag and check if it is orientation.
                    tag = pack(jpeg, offset, 2, littleEndian);
                    if (tag == 0x0112) {
                        // We do not really care about type and count, do we?
                        int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                        switch (orientation) {
                            case 1:
                                return 0;
                            case 3:
                                return 3;
                            case 6:
                                return 6;
                            case 8:
                                return 8;
                        }
                        Log.i(TAG, "Unsupported orientation");
                        return 0;
                    }
                    offset += 12;
                    length -= 12;
                }
            }

            Log.i(TAG, "Orientation not found");
            return 0;
        }

        private int pack(byte[] bytes, int offset, int length,
                                boolean littleEndian) {
            int step = 1;
            if (littleEndian) {
                offset += length - 1;
                step = -1;
            }

            int value = 0;
            while (length-- > 0) {
                value = (value << 8) | (bytes[offset] & 0xFF);
                offset += step;
            }
            return value;
        }
    }


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
    // (1) 카메라로 촬영한 사진을 임시 저장경로에 저장
    // (2) why? 서버에 업로드하려면 임시적으로 저장되어 있어야 한다.

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


                File f = new File(temp_filePath);
                if(f.delete()){
                    Log.e(TAG,"error?: " + response.body().string());
                } // 임시 파일 삭제
                else {
                    Log.e(TAG,"임시 파일 삭제 fail");
                } // 임시 파일 삭제 실패 시

            }
        });

    }
    // 카메라로 촬영 한 이미지를 서버에 보낸다. (프로필 사진 지정 시)


    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

//            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(FaceFilterActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);

            } //권한이 없는 경우

            else {

                createCameraSource();

            } //권한이 이미 주어진 경우

        }

        else{

            createCameraSource();

        } // version 6 이하일때

        return true;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (RESULT_PERMISSIONS == requestCode) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가시

                createCameraSource();


            } else {
                // 권한 거부시

                Toast.makeText(getApplicationContext(), "권한 거부 시, 방송을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }




}

