package com.example.hoauy.carrottv.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 *  1. 정의
 *
 *   Sqlite 를 쉽게 쓸수 있는 Helper
 *
 *
 *
 *  2. Sqlite 사용 목적 ( ViewerActivity 에서만 사용 )
 *
 *
 *   (1) ViewActivity 에서 시청자가 다시보기(vod) 를 시청할 경우.
 *
 *   (2) 생방송 중에 사용자들이 작성한 채팅내용을 Redis에서 불러온다.
 *
 *   (3) Redis에서 불러온 채팅 내용들을 Sqlite에 저장
 *
 *   (4) Exoplayer의 controller 의 위치를 핸들러를 통해 ViewerActivity 에서 0.5초 간격으로 받아온다.
 *
 *   (5) 0.5초 간격으로 Exoplayer의 controller의 위치에 들어가야 될 채팅이 있는지 sqlite에 select 문을 날린다.
 *
 *
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper{

    public MySQLiteOpenHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 최초에 데이터베이스가 없을경우, 데이터베이스 생성을 위해 호출됨

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 바뀌었을 때 호출되는 콜백 메서드
        // 버전 바뀌었을 때 기존데이터베이스를 어떻게 변경할 것인지 작성한다
        // 각 버전의 변경 내용들을 버전마다 작성해야함
        onCreate(db);
        //db 다시 생성

    }




}
