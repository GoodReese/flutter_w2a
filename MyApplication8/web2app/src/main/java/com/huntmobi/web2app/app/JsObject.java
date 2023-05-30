package com.huntmobi.web2app.app;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.huntmobi.web2app.bean.landreadinfo;
import com.huntmobi.web2app.hm;

public class JsObject {
    private Activity ctxContext;
    private WebView web_content;
    public JsObject() {
    }
    public JsObject(Activity ctx, WebView web) {
        ctxContext = ctx;
        web_content = web;
    }

    //能够被JS调用的无参方法
    @JavascriptInterface
    public void jsCallJavaNoParam() {
        Toast.makeText(ctxContext, "JS成功调用JAVA！", Toast.LENGTH_SHORT).show();
    }
    //能够被JS调用的有参方法
    @JavascriptInterface
    public void jsCallJavaHaveParam(String param) {


    }
    //能够被JS调用的带返回值的方法
    @JavascriptInterface
    public String jsCallJavaHaveReturn() {
        Double num = Math.random();
        return ""+num;
    }
    @JavascriptInterface
    public String jsCallJavaHaveReturnAndParam(String param) {

        try{
            landreadinfo info = JSON.parseObject(param, landreadinfo.class);
//            Toast.makeText(ctxContext, "JS成功调用JAVA有参方法！参数为："+info.getAO(), Toast.LENGTH_SHORT).show();
            if (hm.getInstance() != null){
                hm.getInstance().mcall.callback(info);
//                hm.getInstance().mlandinfo = info;
            }
            Log.d("1111", info.getAO());
        }catch (Exception e){
            e.printStackTrace();
        }
        ctxContext.finish();
        return "";
    }
}
