package com.huntmobi.web2app.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.huntmobi.web2app.R;

public class WebActivity extends AppCompatActivity {
    private WebView mWv;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        context = this;
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        initWebView();
    }

    private void initWebView() {
        mWv = new WebView(this);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(1, 1);
        WebSettings _setting = mWv.getSettings();
        _setting.setSupportZoom(true);//支持缩放
        _setting.setBuiltInZoomControls(true);//设置出现缩放工具
        _setting.setUseWideViewPort(false);//适应分辨率
        _setting.setJavaScriptEnabled(true);//设置能够解析Javascript
        _setting.setDomStorageEnabled(true);
        mWv.setLayoutParams(p);
        mWv.addJavascriptInterface(new JsObject((Activity) context, mWv), "injectedObject");

        //设置允许webview访问外部文件，android 11 上的问题
        _setting.setAllowFileAccess(true);
        _setting.setAllowContentAccess(true);

        _setting.setLoadWithOverviewMode(true);
        _setting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        mWv.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("wuden", "message:" + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        //h5 调用java方法
        String path = "file:///android_asset/test_js_call_java.html";
        mWv.loadUrl(path);

    }
}