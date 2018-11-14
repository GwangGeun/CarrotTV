package com.example.hoauy.carrottv.item;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 1. 정의
 *
 *   방송 목록을 보여 줄 때 쓰기 위한 item 요소들 정의
 *
 *
 *
 * 2. 사용하는 곳
 *
 *   1) tab01 (홈)
 *   2) tab02 (다시보기)
 *
 *
 * 3. 연관해서 볼 것들
 *
 *   (1) broadcastlist_item.xml
 *   (2) BroadcastList_Adapter
 *
 *
 *
 * 4. 참고
 *
 *  서버에서 받아온 bj의 아이디를 hidden 값으로 저장
 *
 */

public class BroadcastList_item {


    String broadcast_image;
    // 원래는 ffmpeg 를 통해 썸네일 하려 했으나 --> BJ의 프로필 이미지로 대체
    String broadcast_title;
    String broadcast_nickname;

    String hidden_bjId;
    String hidden_fileName;
    String distribute; // live 인지 '다시보기' 인지 구분

    public BroadcastList_item(String broadcast_image,  String broadcast_title,
                              String broadcast_nickname, String hidden_bjId, String hidden_fileName,
                              String distribute){

        this.broadcast_image = broadcast_image;

        this.broadcast_title = broadcast_title;
        this.broadcast_nickname = broadcast_nickname;

        this.hidden_bjId = hidden_bjId;
        this.hidden_fileName = hidden_fileName;

        this.distribute = distribute;


    }

    public void setBroadcast_image(String broadcast_image) {
        this.broadcast_image = broadcast_image;
    }


    public void setHidden_fileName(String hidden_fileName) {
        this.hidden_fileName = hidden_fileName;
    }

    public void setBroadcast_title(String broadcast_title) {
        this.broadcast_title = broadcast_title;
    }

    public void setBroadcast_nickname(String broadcast_nickname) {
        this.broadcast_nickname = broadcast_nickname;
    }

    public void setHidden_bjId(String hidden_bjId) {
        this.hidden_bjId = hidden_bjId;
    }


    public void setDistribute(String distribute) {
        this.distribute = distribute;
    }

    public String getBroadcast_image() {
        return broadcast_image;
    }


    public String getBroadcast_title() {
        return broadcast_title;
    }

    public String getBroadcast_nickname() {
        return broadcast_nickname;
    }

    public String getHidden_fileName() {
        return hidden_fileName;
    }

    public String getHidden_bjId() {
        return hidden_bjId;
    }

    public String getDistribute() {
        return distribute;
    }
}
