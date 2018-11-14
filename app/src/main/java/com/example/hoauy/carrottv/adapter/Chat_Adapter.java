package com.example.hoauy.carrottv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.item.Chat_item;

import java.util.ArrayList;

/**
 * 1. 정의
 *
 *   채팅을 보여 줄 때 쓰기 위한 Adapter
 *
 *
 * 2. 사용하는 곳
 *
 *   tab01 ( 실시간 방송 ) --> ViewerActivity : 시청자들이 생방송을 볼 때, 채팅 내용을 제공해 줌.
 *
 *   tab02 ( vod ) --> ViewerActivity : 시청자들이 다시보기 할 때, 싱크 맞춰서 채팅 내용을 제공해줘야 한다.
 *
 *   tab03 ( 방송하기 ) --> BroadcastActivity : BJ가 방송할 때, 채팅 내용을 제공해 줌.
 *
 *
 * 3. 연관해서 볼 것들
 *
 *    : chat_item.xml
 *
 *
 */

public class Chat_Adapter extends BaseAdapter {

    Context context;
    ArrayList<Chat_item> chat_itemList;

    public Chat_Adapter(Context context, ArrayList<Chat_item> chat_itemList){

        this.context = context;
        this.chat_itemList = chat_itemList;

    }


    /**
     * ViewHolder 의 목적
     *
     * 1. findviewbyID()의 호출을 줄이기 위함.
     *    (사용하지 않아도 문제는 생기지 않음)
     *
     * 2. 1로 인한 효과 : listview 의 속도 증가
     *
     */
    static class ViewHolder02{

        TextView chat_nickname;
        TextView chat_content;
        TextView hidden_time;

    }


    @Override
    public long getItemId(int position) {
        return position;
    }
    //지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현


    @Override
    public Object getItem(int position) {
        return chat_itemList.get(position);
    }
    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현


    @Override
    public int getCount() {
        return chat_itemList.size();
    }
    //Adapter에 사용 되는 데이터의 개수를 리턴 : 필수 구현


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context con = parent.getContext();
        ViewHolder02 holder = new ViewHolder02();

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater) con.getSystemService(con.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);

            holder.chat_nickname = (TextView)convertView.findViewById(R.id.chat_nickname);
            holder.chat_content = (TextView)convertView.findViewById(R.id.chat_content);
            holder.hidden_time = (TextView)convertView.findViewById(R.id.hidden_time);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder02)convertView.getTag();

        }

        Chat_item chat_item = chat_itemList.get(position);
        // Data Set(chat_itemList)에서 position 에 위치한 데이터 참조 획득

        if(chat_item != null){

            holder.chat_nickname.setText(chat_item.getChat_nickname());
            holder.chat_content.setText(chat_item.getChat_content());
            holder.hidden_time.setText(chat_item.getHidden_time());


        }


        return convertView;

    }

}
