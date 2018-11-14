package com.example.hoauy.carrottv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.item.Memo_item;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 *
 * 1. 정의
 *
 *  MemoActivity 에서 사용하는 ListView 용 Adpater
 *
 * 2. 목적
 *
 *  메모 내용을 리스트뷰로 뿌려주기 위한 목적
 *
 *
 */

public class Memo_Adapter extends BaseAdapter{

    Context context;
    ArrayList<Memo_item> memo_itemList;

    public Memo_Adapter(Context context, ArrayList<Memo_item> memo_itemList){

        this.context = context;
        this.memo_itemList = memo_itemList;

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
    static class ViewHolder04{

        TextView text_memoContent;
        TextView text_memoDay;
        TextView hidden_no;
        ImageView image_memo;

    }


    @Override
    public int getCount() {
        return memo_itemList.size();
    }
    //Adapter에 사용 되는 데이터의 개수를 리턴 : 필수 구현

    @Override
    public Object getItem(int position) {
        return memo_itemList.get(position);
    }
    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현

    @Override
    public long getItemId(int position) {
        return position;
    }
    //지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context con = parent.getContext();
        ViewHolder04 holder = new ViewHolder04();

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater) con.getSystemService(con.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.memo_item, parent, false);

            holder.text_memoContent = (TextView)convertView.findViewById(R.id.text_memoContent);
            holder.text_memoDay = (TextView)convertView.findViewById(R.id.text_memoDay);
            holder.hidden_no = (TextView)convertView.findViewById(R.id.hidden_no);
            holder.image_memo = (ImageView)convertView.findViewById(R.id.image_memo);

            convertView.setTag(holder);


        } else {

            holder = (ViewHolder04)convertView.getTag();

        }

        Memo_item memo_item = memo_itemList.get(position);
        // Data Set(memo_itemList)에서 position 에 위치한 데이터 참조 획득

        if(memo_item != null){

            holder.text_memoContent.setText(memo_item.getText_memoContent());
            holder.text_memoDay.setText(memo_item.getText_memoDay());
            holder.hidden_no.setText(memo_item.getHidden_no());

            if(memo_item.getImage_memo().equals("0")){

                holder.text_memoContent.setVisibility(View.VISIBLE);
                // 메모 내용은 보인다
                holder.image_memo.setVisibility(View.GONE);
                // 사진은 보이지 않게 한다

            } // 사진이 없는 경우

            else {

                holder.text_memoContent.setVisibility(View.GONE);
                // 메모 내용은 보이지 않게 한다. ( 어차피 사진이 있을 경우 : 메모 내용은 "0" )
                holder.image_memo.setVisibility(View.VISIBLE);
                // 사진 보이게 한다.

                Glide.with(context)
                        .load("http://kmkmkmd.vps.phps.kr/memo_image/"+memo_item.getImage_memo())
                        .override(200,150) // 서버에서 가져온 이미지 resizing
                        .centerCrop() // imageview의 크기에 맞게 서버에서 가져 온 이미지를 조정 해 준다.
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .into(holder.image_memo);

            } // 사진이 있는 경우

        }

        return convertView;
    }


}
