package com.example.hoauy.carrottv.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.activity.MainActivity;
import com.example.hoauy.carrottv.item.BroadcastList_item;

import java.util.ArrayList;

/**
 * 1. 정의
 *
 *   방송 목록을 보여 줄 때 쓰기 위한 Adapter
 *
 * 2. 사용하는 곳
 *
 *   tab01 ( 실시간 방송 )
 *
 *   tab02 ( vod )
 *
 * 3. 연관해서 볼 것들
 *
 *   (1) broadcastlist_item.xml
 *   (2) BroadcastList_Adapter
 *
 * 4. 참고
 *
 *  서버에서 받아온 bj의 아이디를 hidden 값으로 저장
 *
 */


public class BroadcastList_Adapter extends BaseAdapter{

    Context context;
    ArrayList<BroadcastList_item> broadcast_itemList;
    View.OnClickListener listener;

    public BroadcastList_Adapter(Context context, ArrayList<BroadcastList_item> broadcast_itemList, View.OnClickListener listener){

        this.context = context;
        this.broadcast_itemList = broadcast_itemList;
        this.listener = listener;

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
    static class ViewHolder{

        ImageView broadcast_image;
        ImageView image_state;

        TextView broadcast_title;
        TextView broadcast_nickname;
        TextView hidden_bjId;
        TextView hidden_fileName;

    }



    @Override
    public long getItemId(int position) {
        return position;
    }
    //지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현


    @Override
    public Object getItem(int position) {
        return broadcast_itemList.get(position);
    }
    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현

    @Override
    public int getCount() {
        return broadcast_itemList.size();
    }
    //Adapter에 사용 되는 데이터의 개수를 리턴 : 필수 구현






    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context con = parent.getContext();
        ViewHolder holder = new ViewHolder();

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater) con.getSystemService(con.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.broadcastlist_item, parent, false);

            holder.broadcast_image = (ImageView)convertView.findViewById(R.id.broadcast_image);
            holder.image_state = (ImageView)convertView.findViewById(R.id.image_state);
            holder.broadcast_title = (TextView)convertView.findViewById(R.id.broadcast_title);
            holder.broadcast_nickname = (TextView)convertView.findViewById(R.id.broadcast_nickname);
            holder.hidden_fileName = (TextView)convertView.findViewById(R.id.hidden_fileName);
            holder.hidden_bjId = (TextView)convertView.findViewById(R.id.hidden_bjId);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder)convertView.getTag();

        }

        BroadcastList_item broadcastList_item = broadcast_itemList.get(position);
        // Data Set(broadcast_itemList)에서 position 에 위치한 데이터 참조 획득
        /**
         * 바로 아랫부분의 구조
         *
         * 1. 프로필 사진의 존재 여부에 따라 기본 프로필 이미지를 설정할지 말지를 결정함.
         *
         * 2. Live 인지 vod 인지에 따라 이미지를 다르게 부착 함.
         *
         */

        if(broadcastList_item != null){

            if(broadcastList_item.getBroadcast_image().equals("0")){

                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                holder.broadcast_image.setImageBitmap(icon);

            } //(프로필 or 썸네일) 사진이 없는 경우

            else if(broadcastList_item.getBroadcast_image().equals("gamst")){
                holder.broadcast_image.setImageResource(R.drawable.gamst);
            } // 더미데이터 : 시연을 위해 임의로 넣은 데이터들

            else if(broadcastList_item.getBroadcast_image().equals("flash")){
                holder.broadcast_image.setImageResource(R.drawable.flash);
            } // 더미데이터 : 시연을 위해 임의로 넣은 데이터들

            else if(broadcastList_item.getBroadcast_image().equals("jd")){
                holder.broadcast_image.setImageResource(R.drawable.jd);
            } // 더미데이터 : 시연을 위해 임의로 넣은 데이터들

            else {

                Glide.with(context)
                        .load("http://kmkmkmd.vps.phps.kr/image/"+broadcastList_item.getHidden_bjId()+".jpg")
                        .centerCrop() // imageview의 크기에 맞게 서버에서 가져 온 이미지를 조정 해 준다.
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .into(holder.broadcast_image);


            } //(프로필 or 썸네일) 사진이 있는 경우
              // 실시간 방송 : broadcastList_item.getBroadcast_image() : 본인 아이디
              // vod : broadcastList_item.getBroadcast_image() : '1'


            if(broadcastList_item.getDistribute().equals("live")){

                Bitmap state = BitmapFactory.decodeResource(context.getResources(), R.drawable.live);
                holder.image_state.setImageBitmap(state);

            }
            //live방송 인 경우

            else if(broadcastList_item.getDistribute().equals("다시보기")){

                Bitmap state = BitmapFactory.decodeResource(context.getResources(), R.drawable.vod);
                holder.image_state.setImageBitmap(state);

            }
            //다시보기(vod) 인 경우


            holder.broadcast_title.setText(broadcastList_item.getBroadcast_title());
            holder.broadcast_nickname.setText(broadcastList_item.getBroadcast_nickname());

            holder.hidden_fileName.setText(broadcastList_item.getHidden_fileName());
            holder.hidden_bjId.setText(broadcastList_item.getHidden_bjId());

        }
        //데이터 지정해주는 부분

        if(listener != null){


        }
        //이 부분의 listener 는 MainActivity 에서 넘어 온 것임 : Main 에서 처리해야 함.
        //(필요 시에 사용 할 것. 현재는 필요 없음)

        return convertView;

    }
    //실제로 시각화 되어 보여지는 부분 - view(원하는대로 만들어서) 를 리턴 : 필수 구현


}
