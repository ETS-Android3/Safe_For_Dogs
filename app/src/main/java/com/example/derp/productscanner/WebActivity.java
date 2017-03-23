package com.example.derp.productscanner;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends Activity{

    private WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        //create an object of the class WebView
        browser = (WebView)findViewById(R.id.web_view);
        browser.setWebViewClient(new MyBrowser());
        open();
    }


    public void open(){
        String url = "http://www.dogfoodproject.com/?page=badingredients";
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setSupportZoom(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        browser.loadUrl(url);

    }
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
