package com.example.hoauy.carrottv.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hoauy.carrottv.R;

import java.util.ArrayList;

/**
 *   1. 정의
 *
 *   Tab01 의 실시간 방송 리스트 화면 상단에 보이게 되는 광고
 *
 *   5초 간격으로 3장의 광고가 각각 1장씩 등장한다.
 *
 *
 *
 *
 *
 */

public class AutoScroll_Adapter extends PagerAdapter {

    Context context;

    public AutoScroll_Adapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        //뷰페이지 슬라이딩 할 레이아웃 인플레이션
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.auto_item,null);
        ImageView image_container = (ImageView) v.findViewById(R.id.image_container);
//        Glide.with(context).load(data.get(position)).into(image_container);

        if(position == 0){
            image_container.setImageResource(R.drawable.star);
        }
        else if (position==1){
            image_container.setImageResource(R.drawable.lol02);
        }
        else if (position == 2){
            image_container.setImageResource(R.drawable.battle);
        }

        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View)object);

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }



}
