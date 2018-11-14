package com.example.hoauy.carrottv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.hoauy.carrottv.R;

/**
 *
 *  1. 정의
 *
 *  CrawlingActivity 에서 전달받은 기사 주소를 load 해서 webview 로 보여준다.
 *
 *
 *
 */

public class WebViewActivity extends AppCompatActivity{

    WebView webview01;

    Intent intent01;
    // 이전 액티비티에서 url 및 사용자의 id 를 받을 때 사용
    Intent intent02;
    // 본 액티비티에서 해당 작업이 완료 후 다른 액티비티로 이동 할 때 사용.

    String link;
    // CrawlingActivity Or WalletActivity 로 부터 사용자가 클릭한 해당 기사의 url 을 받아온다.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        intent01 = getIntent();
        link = intent01.getStringExtra("link");

        webview01 = (WebView) findViewById(R.id.webview01);
        webview01.getSettings().setJavaScriptEnabled(true);

        webview01.loadUrl(link);
        // 처음 웹뷰에 보여줄 url load

        // webView01.setWebChromeClient(new WebChromeClient());
        // 크롬으로 강제해서 띄워주고 싶은 경우 사용

        webview01.setWebViewClient(new WebViewClientClass());
        // 이걸 안해주면 새창이 뜸


    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.e("check URL",url);

            if(url.contains("kmkmkmd.vps.phps.kr/kakaopay_success.php")){

                intent02 = new Intent();
                setResult(3000, intent02);
                finish();

            }
            // 결제가 정상적으로 이루어 질 경우.
            // WalletActivity 로 넘어가서 토큰 요청 실시.

            view.loadUrl(url);

            return true;

        }

    }


}
