package com.example.hoauy.carrottv.item;

/**
 * 1. 정의
 *
 *    메모 리스트를 보여주는 Item
 *
 *
 * 2. 구성
 *
 *   1) 메모 내용 ( 음성 or 사진으로 기록했을 경우에 사용 됨 )
 *   2) 메모 작성 날짜
 *   3) 메모 사진 ( 1)과는 달리 사진으로 메모를 기록했을 경우 사용 됨 )
 *
 *
 * 3. 연관해서 볼 것들
 *
 *   (1) memo_item.xml
 *   (2) Memo_Adapter.java
 *
 */

public class Memo_item {

    String text_memoContent;
    String text_memoDay;
    String image_memo;
    String hidden_no;

    public Memo_item(String text_memoContent, String text_memoDay, String image_memo, String hidden_no){

        this.text_memoContent = text_memoContent;
        this.text_memoDay = text_memoDay;
        this.image_memo = image_memo;
        this.hidden_no = hidden_no;

    }

    public void setHidden_no(String hidden_no) {
        this.hidden_no = hidden_no;
    }

    public String getHidden_no() {
        return hidden_no;
    }

    public void setImage_memo(String image_memo) {
        this.image_memo = image_memo;
    }

    public void setText_memoContent(String text_memoContent) {
        this.text_memoContent = text_memoContent;
    }

    public void setText_memoDay(String text_memoDay) {
        this.text_memoDay = text_memoDay;
    }

    public String getImage_memo() {
        return image_memo;
    }

    public String getText_memoContent() {
        return text_memoContent;
    }

    public String getText_memoDay() {
        return text_memoDay;
    }
}

