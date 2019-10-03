package com.mycheckins;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    // Display the webview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        ((WebView)findViewById(R.id.help_webview)).setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                return false;
            }
        });

        ((WebView)findViewById(R.id.help_webview)).loadUrl("https://www.wikihow.com/Check-In-on-Facebook");
    }
}
