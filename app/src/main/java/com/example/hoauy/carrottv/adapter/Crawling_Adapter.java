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
import com.example.hoauy.carrottv.item.Crawling_item;

import java.util.ArrayList;

/**
 * Created by hoauy on 2018-05-25.
 */

public class Crawling_Adapter extends BaseAdapter{

    Context context;
    ArrayList<Crawling_item> crawling_itemList;

    /**
     * ViewHolder 의 목적
     *
     * 1. findviewbyID()의 호출을 줄이기 위함.
     *    (사용하지 않아도 문제는 생기지 않음)
     *
     * 2. 1로 인한 효과 : listview 의 속도 증가
     *
     */
    static class ViewHolder03{

        ImageView crawling_image;

        TextView crawling_title;
        TextView crawling_cotent;
        TextView crawling_company;
        TextView hidden_url;

    }

    public Crawling_Adapter(Context context, ArrayList<Crawling_item> crawling_itemList){

        this.context = context;
        this.crawling_itemList = crawling_itemList;

    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    //지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현


    @Override
    public Object getItem(int position) {
        return crawling_itemList.get(position);
    }
    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현


    @Override
    public int getCount() {
        return crawling_itemList.size();
    }
    //Adapter에 사용 되는 데이터의 개수를 리턴 : 필수 구현

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context con = parent.getContext();
        ViewHolder03 holder = new ViewHolder03();

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.crawling_item, parent, false);

            holder.crawling_image = (ImageView)convertView.findViewById(R.id.crawling_image);
            holder.crawling_title = (TextView)convertView.findViewById(R.id.crawling_title);
            holder.crawling_cotent = (TextView)convertView.findViewById(R.id.crawling_content);
            holder.crawling_company = (TextView)convertView.findViewById(R.id.crawling_company);
            holder.hidden_url = (TextView)convertView.findViewById(R.id.hidden_url);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder03)convertView.getTag();

        }

        Crawling_item crawling_item = crawling_itemList.get(position);
        // Data Set(crawling_itemList)에서 position 에 위치한 데이터 참조 획득

        if(crawling_item != null){

            Glide.with(context)
                    .load(crawling_item.getCrawling_image()) // 기사 이미지(썸네일) 주소
                    .centerCrop() // imageview의 크기에 맞게 resize.
                    .into(holder.crawling_image);

            holder.crawling_title.setText(crawling_item.getCrawling_title());
            holder.crawling_cotent.setText(crawling_item.getCrawling_content());
            holder.crawling_company.setText(crawling_item.getCrawling_company());

            holder.hidden_url.setText(crawling_item.getHidden_url()); //기사 링크

        }


        return convertView;

    }


}
