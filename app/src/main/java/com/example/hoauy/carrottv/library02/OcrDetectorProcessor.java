package com.example.hoauy.carrottv.library02;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

/**
 *
 *  1. 정의
 *
 *  Mobile Vision Api 글자 인식 Library 소스
 *
 *  OrcCaptureActivity 에서 쓰임
 *
 *
 *
 *  2. 주의점
 *
 *  (1) library package 와 구별 주의 요망
 *
 *  (2) Library를 그대로 가져온 것임. 시간이 여유로울 때 전반적인 흐름 파악해 볼 것 ( 지금은 그대로 둘 것 )
 *
 *
 *
 */

public class OcrDetectorProcessor implements Detector.Processor<TextBlock>{

    public GraphicOverlay02<OcrGraphic> graphicOverlay;

    public OcrDetectorProcessor(GraphicOverlay02<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                graphicOverlay.add(graphic);
            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }

}
