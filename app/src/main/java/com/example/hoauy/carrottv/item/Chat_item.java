package com.example.hoauy.carrottv.item;

/**
 * 1. 정의
 *
 *    채팅을 시청자 및 BJ 에게 보여 줄 때 쓰기 위한 item 요소들 정의
 *
 *
 * 2. 사용하는 곳
 *
 *   1) BroadCastActivity
 *   2) ViewerActivity
 *
 *
 * 3. 연관해서 볼 것들
 *
 *   (1) Chat_item.xml
 *   (2) Chat_Adapter
 *
 *
 *
 */
public class Chat_item {

    String chat_nickname;
    String chat_content;
    String hidden_time; // 동영상과 싱크를 맞추기 위해 생방송 시 저장한 Exoplayer의 position 값

    public Chat_item(String chat_nickname, String chat_content, String hidden_time){

        this.chat_nickname = chat_nickname;
        this.chat_content = chat_content;
        this.hidden_time = hidden_time;

    }


    public void setChat_nickname(String chat_nickname) {
        this.chat_nickname = chat_nickname;
    }

    public void setChat_content(String chat_content) {
        this.chat_content = chat_content;
    }

    public void setHidden_time(String hidden_time) {
        this.hidden_time = hidden_time;
    }

    public String getChat_nickname() {
        return chat_nickname;
    }

    public String getChat_content() {
        return chat_content;
    }

    public String getHidden_time() {
        return hidden_time;
    }
}
