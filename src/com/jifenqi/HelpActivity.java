package com.jifenqi;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {
    private static final String TAG = "AboutActivity";
    
    private static final String URL_HELPPGAE = "http://rayject.blog.163.com/blog/static/219379034201332893745221/";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);
        
        WebView webView = (WebView)findViewById(R.id.help_view);
        webView.loadUrl(URL_HELPPGAE);
    }

}
