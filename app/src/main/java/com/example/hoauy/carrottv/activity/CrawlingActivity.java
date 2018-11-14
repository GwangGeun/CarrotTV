package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.hoauy.carrottv.R;
import com.example.hoauy.carrottv.adapter.Crawling_Adapter;
import com.example.hoauy.carrottv.item.Crawling_item;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 *  1. 정의
 *
 *  네이버 기사 중 여행/레저에 해당 하는 부분을 크롤링 해옴.
 *
 *
 *  2. 참고
 *
 *  (1) Jsoup 라이브러리 사용
 *
 *  (2) AsynTask 를 이용해서 NetworkExeption 방지
 *
 */

public class CrawlingActivity extends AppCompatActivity{

    ArrayList<Crawling_item> crawling_itemList = new ArrayList<Crawling_item>();
    ListView crawling_listview;
    Crawling_Adapter crawling_adapter;
    Crawling_item crawling_item;
    //리스트뷰 사용시 필요


    Elements ele01;
    //  해당 기사의 이미지 (썸네일)
    Elements ele02 ;
    //  기사 제목
    Elements ele03;
    //  기사 내용
    Elements ele04;
    //  기사를 쓴 회사 이름
    Elements ele05;
    //  해당 기사의 링크
    Intent intent01;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crawling);



        crawling_adapter = new Crawling_Adapter(this, crawling_itemList);
        crawling_listview = (ListView)findViewById(R.id.crawling_listview);
        crawling_listview.setAdapter(crawling_adapter);
        crawling_listview.setOnItemClickListener(listener);

        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
        //기사를 실질적으로 크롤링해서 가져오는 부분


    }



    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            intent01 = new Intent(CrawlingActivity.this, WebViewActivity.class);
            intent01.putExtra("link", crawling_itemList.get(position).getHidden_url());
            startActivity(intent01);
            // 선택된 기사의 링크를 WebViewActivity에 전달

        }
    };


    public class MyAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                Document url = Jsoup.connect("http://news.naver.com/main/list.nhn?mode=LS2D&mid=shm&sid1=103&sid2=237").get();
                // 네이버 기사 ( 여행/레저 카테고리 )
                ele01 = url.select("dt.photo a img");
                //  해당 기사의 이미지 (썸네일)
                ele02 = url.select("ul.type06_headline li dl a img");
                //  기사 제목
                ele03 = url.select("span.lede");
                //  기사 내용
                ele04 = url.select("span.writing");
                //기사를 쓴 회사 이름
                ele05 = url.select("dt.photo a");
                //  해당 기사의 링크

            } catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

                for(int i=0; i< ele02.size(); i++) {

                    crawling_item = new Crawling_item(ele01.get(i).attr("src"), ele02.get(i).attr("alt"),
                            ele03.get(i).text(), ele04.get(i).text(), ele05.get(i).attr("href"));
                    crawling_itemList.add(crawling_item);

                    crawling_adapter.notifyDataSetChanged();

                }


        }



    }

}





