package com.leadeon.zxing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {


    private Toolbar toolbar;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        webview = (WebView) findViewById(R.id.web);
        webview.getSettings().setJavaScriptEnabled(true);
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        getResult(result);

    }
    public void getResult(String result){
        if (result!=null&&result.startsWith("http")){
            webview.loadUrl(result);
        }else{
            Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
        }
    }


}
