/**
 * 该模块，主要提供了 五个方法以静态的形式提供对外调用， 具体参数看具体方法的注释
 * 1，init
 * 2，Purchase
 * 3，EventPost
 * 4，UserDataUpdate
 * 5，AdvDataRead
 * @author zhou1
 * @time 2022/12/1 14:43
 */
package com.huntmobi.web2app;

import static com.huntmobi.web2app.utils.HttpClientConnector.ERROR_TIMEOUT_EXCEPTION;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.huntmobi.web2app.app.WebActivity;
import com.huntmobi.web2app.bean.InstallInfo;
import com.huntmobi.web2app.bean.advinfo;
import com.huntmobi.web2app.bean.eventpostinfo;
import com.huntmobi.web2app.bean.landingreadresponseinfo;
import com.huntmobi.web2app.bean.landreadinfo;
import com.huntmobi.web2app.bean.purchaseinfo;
import com.huntmobi.web2app.bean.requestcache;
import com.huntmobi.web2app.bean.sessioninfo;
import com.huntmobi.web2app.bean.updatedatainfo;
import com.huntmobi.web2app.utils.AsyncCallback;
import com.huntmobi.web2app.utils.DataCallback;
import com.huntmobi.web2app.utils.HttpClientConnector;
import com.huntmobi.web2app.utils.NetCallback;
import com.huntmobi.web2app.utils.NetInfo;
import com.huntmobi.web2app.utils.NetworkUtil;
import com.huntmobi.web2app.utils.OutNetCallback;
import com.huntmobi.web2app.utils.PreferUtil;
import com.huntmobi.web2app.utils.UrlConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import android.util.Log;

public class hm {
    public  Application mApplication;
    public static String domainName = "http://w2a.xxxx.link";
    public String mInstallEventName = "CompleteRegistration";
    public String debugStr = "";
    static hm mInstance = null;
    private static boolean bWebviewEnable = false;
    public AsyncCallback mcall = new AsyncCallback() {
        @Override
        public void callback(landreadinfo info) {
            String url = domainName + UrlConfig.LANDINGPAGEREAD;
            String content = JSON.toJSONString(info);;
            HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                @Override
                public void callbackDealwith(int currentType, Object info) {
                    if (info !=null&&info instanceof NetInfo){
                        NetInfo realInfo = (NetInfo) info;
                        if (realInfo.getCode() == 0){
                            if (!TextUtils.isEmpty(realInfo.getData())){
                                landingreadresponseinfo childObj = JSON.parseObject(realInfo.getData(), landingreadresponseinfo.class);
                                PreferUtil.getInstance().putString("w2a", childObj.getW2a_data_encrypt());
                                callOninstall(childObj.getW2a_data_encrypt(), new NetCallback() {
                                    @Override
                                    public void callbackDealwith(int currentType, Object info) {

                                        if (info !=null&&info instanceof NetInfo){
                                            NetInfo realInfo = (NetInfo) info;
                                            if (realInfo.getCode() == 0){
                                                if (!TextUtils.isEmpty(realInfo.getData())){
                                                    PreferUtil.getInstance().putString("adv", realInfo.getData());
                                                    if (mdatacall != null){
                                                        mdatacall.CallbackDealwith(AdvDataRead(realInfo.getData()));
                                                    }
                                                }else{
                                                    if (mdatacall != null){
                                                        mdatacall.CallbackDealwith(null);
                                                    }
                                                }
                                            }else{
                                                if (mdatacall != null){
                                                    mdatacall.CallbackDealwith(null);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    };
    private DataCallback mdatacall;
    hm(){

    }
    hm(Application ap , String Gateway, String installEventName){
        mApplication = ap;
        if (!TextUtils.isEmpty(Gateway)){
            domainName = Gateway;
        }

        if (!TextUtils.isEmpty(installEventName)){
            mInstallEventName = installEventName;
        }
    }
    private void inerInit(DataCallback callback){
        mdatacall = callback;
        if (!PreferUtil.getInstance().getBoolean("isFirst", false)){
            Log.i("W2A", "第一次安装");
            //第一次安装
            PreferUtil.getInstance().putBoolean("isFirst", true);
            //判断剪切板中是否有落地数据，
            String w2aStr = getclipboard();
            if (!TextUtils.isEmpty(w2aStr)&&w2aStr.startsWith("w2a_data:")){
                Log.i("W2A", "剪切板中有落地数据");
                //有落地数据，写入本地，调用 新装api，把拿到的advdata数据写入本地
                PreferUtil.getInstance().putString("w2a", w2aStr);
                String url = domainName + UrlConfig.INSTALL;
                InstallInfo info = new InstallInfo();
                info.setW2a_data_encrypt(w2aStr);
                InstallInfo.DeviceId deviceId = new InstallInfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setDevice_id(deviceId);
                InstallInfo.DeviceInfo deviceInfo = new InstallInfo.DeviceInfo();
                deviceInfo.setBrand(NetworkUtil.getBrand());
                deviceInfo.setLanguage(NetworkUtil.getLanguage());
                deviceInfo.setModel(NetworkUtil.getModel());
                deviceInfo.setScreenSize(NetworkUtil.getScreenSize());
                deviceInfo.setOsVersion(NetworkUtil.getOsVersion());

                info.setDevice_info(deviceInfo);
                info.setEvent_name(mInstallEventName);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(1, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (info !=null&&info instanceof NetInfo){
                            NetInfo realInfo = (NetInfo) info;

                            if (realInfo.getCode() == 0){
                                if (!TextUtils.isEmpty(realInfo.getData())){
//                                    advinfo realObj = JSON.parseObject(realInfo.getData(), advinfo.class);
                                    PreferUtil.getInstance().putString("adv", realInfo.getData());
                                    if (callback != null){
                                        callback.CallbackDealwith(AdvDataRead(realInfo.getData()));
                                    }

                                }else{
                                    if (callback != null){
                                        callback.CallbackDealwith(null);
                                    }
                                }
                            }else{
                                if (callback != null){
                                    callback.CallbackDealwith(null);
                                }
                            }
                        }
                    }
                });
            }else{
                //调用落地页读取api，获取到w2a_data 数据写入本地,同时调用新装api
                Log.i("W2A", "剪切板中没有落地数据 调用落地页读取api");
                if (bWebviewEnable){
                    testprint();
                }else{
                    String url = domainName + UrlConfig.LANDINGPAGEREAD;
                    landreadinfo info = new landreadinfo();
                    String content = JSON.toJSONString(info);;
                    HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                        @Override
                        public void callbackDealwith(int currentType, Object info) {
                            Log.i("W2A", "调用 LANDINGPAGEREAD 完毕");
                            if (info !=null&&info instanceof NetInfo){
                                Log.i("W2A", "LANDINGPAGEREAD返回的数据：" + JSON.toJSONString(info));
                                NetInfo realInfo = (NetInfo) info;
                                if (realInfo.getCode() == 0){
                                    Log.i("W2A", "调用 LANDINGPAGEREAD 正确");
                                    if (!TextUtils.isEmpty(realInfo.getData())){
                                        landingreadresponseinfo childObj = JSON.parseObject(realInfo.getData(), landingreadresponseinfo.class);
                                        PreferUtil.getInstance().putString("w2a", childObj.getW2a_data_encrypt());
                                        Log.i("W2A", "调用 ONINSTALL");
                                        callOninstall(childObj.getW2a_data_encrypt(), new NetCallback() {
                                            @Override
                                            public void callbackDealwith(int currentType, Object info) {
                                                Log.i("W2A", "调用 ONINSTALL 完成");
                                                if (info !=null&&info instanceof NetInfo){
                                                    Log.i("W2A", "ONINSTALL返回的数据：" + JSON.toJSONString(info));
                                                    NetInfo realInfo = (NetInfo) info;
                                                    if (realInfo.getCode() == 0){
                                                        Log.i("W2A", "调用 ONINSTALL 正确");
                                                        if (!TextUtils.isEmpty(realInfo.getData())){
                                                            PreferUtil.getInstance().putString("adv", realInfo.getData());
                                                            if (mdatacall != null){
                                                                mdatacall.CallbackDealwith(AdvDataRead(realInfo.getData())); // 报错
                                                            }
                                                        }else{
                                                            if (mdatacall != null){
                                                                mdatacall.CallbackDealwith(null);
                                                            }
                                                        }
                                                    }else{
                                                        Log.i("W2A", "调用 ONINSTALL 出错");
                                                        if (mdatacall != null){
                                                            mdatacall.CallbackDealwith(null);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }


            }
        }else{
            Log.i("W2A", "非新装用户，判断是是否本地有落地数据，有调用会话api");
            //非新装用户，判断是是否本地有落地数据，有调用会话api
            String w2aStr = PreferUtil.getInstance().getString("w2a","");
            if(!TextUtils.isEmpty(w2aStr)){
                //调用会话api
                String url = domainName + UrlConfig.SESSION;
                Log.i("W2A", "调用链接" + url);
                sessioninfo info = new sessioninfo();
                sessioninfo.DeviceId deviceId = new sessioninfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                info.setDevice_id(deviceId);
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setW2a_data_encrypt(w2aStr);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {

                        if (info !=null&&info instanceof NetInfo){
                            Log.i("W2A", "返回的数据：" + JSON.toJSONString(info));
                            NetInfo realInfo = (NetInfo) info;
                            if (realInfo.getCode() == 0){

                            }
                        }
                    }
                });
            }else{
                //没有则返回。

            }
        }
    }
    private void inerInit(){
        mdatacall = null;
        if (!PreferUtil.getInstance().getBoolean("isFirst", false)){
            //第一次安装
            PreferUtil.getInstance().putBoolean("isFirst", true);
            //判断剪切板中是否有落地数据，
            String w2aStr = getclipboard();
            if (!TextUtils.isEmpty(w2aStr)&&w2aStr.startsWith("w2a_data:")){
                //有落地数据，写入本地，调用 新装api，把拿到的advdata数据写入本地
                PreferUtil.getInstance().putString("w2a", w2aStr);
                String url = domainName + UrlConfig.INSTALL;
                InstallInfo info = new InstallInfo();
                info.setW2a_data_encrypt(w2aStr);
                InstallInfo.DeviceId deviceId = new InstallInfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setDevice_id(deviceId);
                InstallInfo.DeviceInfo deviceInfo = new InstallInfo.DeviceInfo();
                deviceInfo.setBrand(NetworkUtil.getBrand());
                deviceInfo.setLanguage(NetworkUtil.getLanguage());
                deviceInfo.setModel(NetworkUtil.getModel());
                deviceInfo.setScreenSize(NetworkUtil.getScreenSize());
                deviceInfo.setOsVersion(NetworkUtil.getOsVersion());

                info.setDevice_info(deviceInfo);
                info.setEvent_name(mInstallEventName);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(1, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (info !=null&&info instanceof NetInfo){
                            NetInfo realInfo = (NetInfo) info;
                            if (realInfo.getCode() == 0){
                                if (!TextUtils.isEmpty(realInfo.getData())){
//                                    advinfo realObj = JSON.parseObject(realInfo.getData(), advinfo.class);
                                    PreferUtil.getInstance().putString("adv", realInfo.getData());
                                }
                            }
                        }
                    }
                });
            }else{
                if (bWebviewEnable){
                    testprint();
                }else{
                    //调用落地页读取api，获取到w2a_data 数据写入本地,同时调用新装api
                    String url = domainName + UrlConfig.LANDINGPAGEREAD;
                    landreadinfo info = new landreadinfo();
                    String content = JSON.toJSONString(info);;
                    HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                        @Override
                        public void callbackDealwith(int currentType, Object info) {
                            if (info !=null&&info instanceof NetInfo){
                                NetInfo realInfo = (NetInfo) info;
                                if (realInfo.getCode() == 0){
                                    if (!TextUtils.isEmpty(realInfo.getData())){
                                        landingreadresponseinfo childObj = JSON.parseObject(realInfo.getData(), landingreadresponseinfo.class);
                                        PreferUtil.getInstance().putString("w2a", childObj.getW2a_data_encrypt());
                                        callOninstall(childObj.getW2a_data_encrypt(), new NetCallback() {
                                            @Override
                                            public void callbackDealwith(int currentType, Object info) {
                                                if (info !=null&&info instanceof NetInfo){
                                                    NetInfo realInfo = (NetInfo) info;
                                                    if (realInfo.getCode() == 0){
                                                        if (!TextUtils.isEmpty(realInfo.getData())){
                                                            PreferUtil.getInstance().putString("adv", realInfo.getData());
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }


            }
        }else{
            //非新装用户，判断是是否本地有落地数据，有调用会话api
            String w2aStr = PreferUtil.getInstance().getString("w2a","");
            if(!TextUtils.isEmpty(w2aStr)){
                //调用会话api
                String url = domainName + UrlConfig.SESSION;
                sessioninfo info = new sessioninfo();
                sessioninfo.DeviceId deviceId = new sessioninfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                info.setDevice_id(deviceId);
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setW2a_data_encrypt(w2aStr);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (info !=null&&info instanceof NetInfo){
                            NetInfo realInfo = (NetInfo) info;
                            if (realInfo.getCode() == 0){

                            }
                        }
                    }
                });
            }else{
                //没有则返回。
            }
        }
    }
    private void callOninstall(String w2aStr, NetCallback callback){
        String url = domainName + UrlConfig.INSTALL;
        InstallInfo info = new InstallInfo();
        info.setW2a_data_encrypt(w2aStr);
        InstallInfo.DeviceId deviceId = new InstallInfo.DeviceId();
        deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
        deviceId.setImei(NetworkUtil.getImei());
        deviceId.setIdfa("");
        deviceId.setIdfv("");
        deviceId.setAdvertiser_ID("");
        info.setDevice_id(deviceId);
        InstallInfo.DeviceInfo deviceInfo = new InstallInfo.DeviceInfo();
        deviceInfo.setBrand(NetworkUtil.getBrand());
        deviceInfo.setLanguage(NetworkUtil.getLanguage());
        deviceInfo.setModel(NetworkUtil.getModel());
        deviceInfo.setScreenSize(NetworkUtil.getScreenSize());
        deviceInfo.setOsVersion(NetworkUtil.getOsVersion());
        info.setDevice_info(deviceInfo);
        info.setEvent_name(mInstallEventName);
        String content = JSON.toJSONString(info);
        HttpClientConnector.HttpConnectCommonAsync(1, url, content, callback);
    }
    public static  hm getInstance(){
        return mInstance;
    }
    /**
     * @方法名称: Init
     * @描述: hm 模块初始化方法
     * @参数: [ap：应用程序上下文，必填，不能为空，否则影响后续所有的调用,
     * Gateway：网关名称, installEventName：事件名称]
     * @返回值 boolean ：初始化失败返回false
     * @创建时间：2022/12/1 11:59
     * @作者 zhou1
     */
    public static boolean Init(Application ap, String Gateway, String installEventName) {
        boolean bRet = false;
        if (ap == null){
            return false;
        }

        try{
            if (mInstance == null){
                mInstance = new hm(ap, Gateway, installEventName);
            }
            mInstance.inerInit();
            //add 2022/12/9 缓存因超时产生的缓存请求
            mInstance.cacheRequest();
            bRet = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return bRet;
    }
    /**
     * @方法名称: Init
     * @描述: hm 模块初始化方法
     * @参数: [ap：应用程序上下文，必填，不能为空，否则影响后续所有的调用,
     * Gateway：网关名称, installEventName：事件名称, callback: 回调]
     * @返回值 boolean ：初始化失败返回false
     * @创建时间：2022/12/1 11:59
     * @作者 zhou1
     */
    public static boolean Init(Application ap, String Gateway, String installEventName, DataCallback callback) {
        boolean bRet = false;
        if (ap == null){
            return false;
        }

        try{
            if (mInstance == null){
                mInstance = new hm(ap, Gateway, installEventName);
            }
            if (callback == null){
                mInstance.inerInit();
            }else{
                mInstance.inerInit(callback);
            }

            //add 2022/12/9 缓存因超时产生的缓存请求
            mInstance.cacheRequest();
            bRet = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return bRet;
    }
    /**
     * @方法名称: Purchase
     * @描述: 购买事件上报
     * @参数: [event_name：事件名称,
     * currency：货币符号--USD RMB, value:为保留小数点后两位的数字形式的字符串,
     * content_type, content_ids,
     * callback：回调，上报执行后的反馈]
     * @返回值 void
     * @创建时间：2022/12/1 12:02
     * @作者 zhou1
     */
    public static void Purchase(String event_name, String currency, String value, String content_type, List<String>content_ids, OutNetCallback callback){
        try{
            if(callback != null){
                String url = domainName + UrlConfig.PURCHASE;
                purchaseinfo info = new purchaseinfo();
                purchaseinfo.DeviceId deviceId = new purchaseinfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setDevice_id(deviceId);
                String w2aStr = PreferUtil.getInstance().getString("w2a","");
                if (TextUtils.isEmpty(w2aStr)){
                    return;
                }
                info.setW2a_data_encrypt(w2aStr);
                purchaseinfo.CustomData customData = new purchaseinfo.CustomData();
                customData.setEvent_id("");
                if (!TextUtils.isEmpty(currency)){
                    customData.setCurrency(currency);
                }else{
                    customData.setCurrency("");
                }

                if (!TextUtils.isEmpty(event_name)){
                    customData.setEvent_name(event_name);
                }else{
                    customData.setEvent_name("");
                }
                if (!TextUtils.isEmpty(content_type)){
                    customData.setContent_type(content_type);
                }else{
                    customData.setContent_type("product");
                }
                if (!TextUtils.isEmpty(value)){
                    customData.setValue(value);
                }else{
                    customData.setValue("0.0");
                }

//                List<String> list = new ArrayList<String>();
//                list.add("");
                customData.setContent_ids(content_ids);
                info.setCustom_data(customData);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(3, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (callback != null){
                            if (info != null&&info instanceof NetInfo){
                                NetInfo innerinfo = (NetInfo) info;
                                if (innerinfo.getCode() == ERROR_TIMEOUT_EXCEPTION){
                                    requestcache rc = getInstance().getRequestCache();
                                    if (rc != null){
                                        requestcache.cacheitem item = new requestcache.cacheitem();
                                        item.setUrl(url);
                                        item.setContent(content);
                                        rc.getCacheitems().add(item);
                                        getInstance().saveRequestCache(rc);
                                    }

                                }
                            }
                            callback.callbackDealwith(info);
                        }
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void cacheRequest(){
        try {
            requestcache arc = getRequestCache();
            if (arc != null &&arc.getCacheitems() != null){
                if (arc.getCacheitems().size() > 0){
                    for (requestcache.cacheitem item:arc.getCacheitems()) {
                        if (!TextUtils.isEmpty(item.getUrl())&&!TextUtils.isEmpty(item.getContent())){
                            String url = item.getUrl();
                            String content = item.getContent();
                            HttpClientConnector.HttpConnectCommonAsync(6, url, content, new NetCallback() {
                                @Override
                                public void callbackDealwith(int currentType, Object info) {
                                        if (info != null&&info instanceof NetInfo){
                                            NetInfo innerinfo = (NetInfo) info;
                                            if (innerinfo.getCode() == 0){
                                                requestcache rc = getInstance().getRequestCache();
                                                if (rc != null&&rc.getCacheitems()!=null&&rc.getCacheitems().size()> 0){
                                                    Iterator<requestcache.cacheitem> it =  rc.getCacheitems().iterator();
                                                    while(it.hasNext()){
                                                        requestcache.cacheitem item = it.next();
                                                        if (!TextUtils.isEmpty(item.getUrl())
                                                                &&item.getUrl().equals(url)
                                                                &&!TextUtils.isEmpty(item.getContent())
                                                                &&item.getContent().equals(content)){
                                                            it.remove();
                                                        }
                                                    }
                                                    getInstance().saveRequestCache(rc);
                                                }

                                            }
                                        }
                                }
                            });
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private requestcache getRequestCache(){
        requestcache oRet = null;
        try {
            if (!TextUtils.isEmpty(PreferUtil.getInstance().getString("urlcache", ""))){
                requestcache realObj = JSON.parseObject(PreferUtil.getInstance().getString("urlcache", ""), requestcache.class);
                oRet = realObj;
            }else{
                requestcache realObj = new requestcache();
                realObj.setCacheitems(new ArrayList<requestcache.cacheitem>());
                String sresutl = JSON.toJSONString(realObj);
                PreferUtil.getInstance().putString("urlcache", sresutl);
                oRet = realObj;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return oRet;
    }
    private void saveRequestCache(requestcache rc){
        try{
            if (rc != null){
                String sresutl = JSON.toJSONString(rc);
                PreferUtil.getInstance().putString("urlcache", sresutl);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * @方法名称: EventPost
     * @描述:  自定义事件的上报方法
     * @参数: [event_id, event_name, currency, value, content_type, content_ids, callback]
     * @返回值 void
     * @创建时间：2022/12/1 12:04
     * @作者 zhou1
     */
    public static void EventPost(String event_id, String event_name, String currency, String value, String content_type, List<String> content_ids, OutNetCallback callback){
        try{
            if(callback != null){
                String url = domainName + UrlConfig.EVENTPOST;
                eventpostinfo info = new eventpostinfo();
                String w2aStr = PreferUtil.getInstance().getString("w2a","");
                if (TextUtils.isEmpty(w2aStr)){
                    return;
                }
                info.setW2a_data_encrypt(w2aStr);
                eventpostinfo.CustomrData customrData = new eventpostinfo.CustomrData();
                customrData.setEvent_id("");
                customrData.setEvent_name("");
                if (!TextUtils.isEmpty(event_id)){
                    info.setEvent_id(event_id);
                }else{
                    info.setEvent_id("");
                }
                if (!TextUtils.isEmpty(event_name)){
                    info.setEvent_name(event_name);
                }else{
                    info.setEvent_name("");
                }
                if (!TextUtils.isEmpty(currency)){
                    customrData.setCurrency(currency);
                }else{
                    customrData.setCurrency("");
                }
                if (!TextUtils.isEmpty(value)){
                    customrData.setValue(value);
                }else{
                    customrData.setValue("0.0");
                }
                if (!TextUtils.isEmpty(content_type)){
                    customrData.setContent_type(content_type);
                }else{
                    customrData.setContent_type("product");
                }
                if (content_ids != null){
                    customrData.setContent_ids(content_ids);
                }else{
                    customrData.setContent_ids(null);
                }
                info.setCustom_data(customrData);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(4, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (callback != null){
                            if (info != null&&info instanceof NetInfo){
                                NetInfo innerinfo = (NetInfo) info;
                                if (innerinfo.getCode() == ERROR_TIMEOUT_EXCEPTION){
                                    requestcache rc = getInstance().getRequestCache();
                                    if (rc != null){
                                        requestcache.cacheitem item = new requestcache.cacheitem();
                                        item.setUrl(url);
                                        item.setContent(content);
                                        rc.getCacheitems().add(item);
                                        getInstance().saveRequestCache(rc);
                                    }
                                }
                            }
                            callback.callbackDealwith(info);
                        }
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * @方法名称: UserDataUpdate
     * @描述: 用户数据更新，注意会生成新的id加密数据
     * @参数: [em：邮箱, fb_login_id：facebook 登录id-不传，会默认为空, phone：手机号, zipcode: 邮编,city:城市,  state:州省市, gender:性别, fn:名字, ln:姓氏, datebirth:生日, country:国家, callback：更新回调]
     * @返回值 void
     * @创建时间：2022/12/1 12:05
     * @作者 zhou1
     */
    public static void UserDataUpdate(String em, String fb_login_id, String phone, String zipcode, String city, String state, String gender,
                                      String fn, String ln, String datebirth, String country, OutNetCallback callback){

        try{
            if(callback != null){
                String url = domainName + UrlConfig.USERDATAUPDATE;
                updatedatainfo info = new updatedatainfo();
                updatedatainfo.DeviceId deviceId = new updatedatainfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setDevice_id(deviceId);
                if (!TextUtils.isEmpty(em)){
                    info.setEm(em);
                }else{
                    info.setEm("");
                }
                if (!TextUtils.isEmpty(fb_login_id)){
                    info.setFb_login_id(fb_login_id);
                }else{
                    info.setFb_login_id("");
                }
                if (!TextUtils.isEmpty(phone)){
                    info.setPh(phone);
                }else{
                    info.setPh("");
                }
                if (!TextUtils.isEmpty(zipcode)){
                    info.setZp(zipcode);
                }else{
                    info.setZp("");
                }
                if (!TextUtils.isEmpty(city)){
                    info.setCt(city);
                }else{
                    info.setCt("");
                }
                if (!TextUtils.isEmpty(country)){
                    info.setCountry(country);
                }else{
                    info.setCountry("");
                }
                if (!TextUtils.isEmpty(datebirth)){
                    info.setDb(datebirth);
                }else{
                    info.setDb("");
                }
                if (!TextUtils.isEmpty(fn)){
                    info.setFn(fn);
                }else{
                    info.setFn("");
                }
                if (!TextUtils.isEmpty(ln)){
                    info.setLn(ln);
                }else{
                    info.setLn("");
                }
                if (!TextUtils.isEmpty(gender)){
                    info.setGe(gender);
                }else{
                    info.setGe("");
                }
                if (!TextUtils.isEmpty(state)){
                    info.setSt(state);
                }else{
                    info.setSt("");
                }


                String w2aStr = PreferUtil.getInstance().getString("w2a", "");
                if (TextUtils.isEmpty(w2aStr)){
                    return;
                }
                info.setW2a_data_encrypt(w2aStr);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (info != null&&info instanceof NetInfo){
                            NetInfo realinfo = (NetInfo) info;
                            if (realinfo.getCode() == 0){
                                if (!TextUtils.isEmpty(realinfo.getData())){
                                    landingreadresponseinfo realObj = JSON.parseObject(realinfo.getData(), landingreadresponseinfo.class);
                                    if (realObj != null){
                                        String w2aStr = realObj.getW2a_data_encrypt();
                                        PreferUtil.getInstance().putString("w2a", w2aStr);
                                    }
                                }

                            }

                        }
                        if (callback != null){
                            callback.callbackDealwith(info);
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * @方法名称: UserDataUpdate
     * @描述: 用户数据更新，注意会生成新的id加密数据
     * @参数: [em：邮箱, fb_login_id：facebook 登录id-不传，会默认为空, phone：手机号, callback：更新回调]
     * @返回值 void
     * @创建时间：2022/12/1 12:05
     * @作者 zhou1
     */
    public static void UserDataUpdate(String em, String fb_login_id, String phone, OutNetCallback callback){
        try{
            if(callback != null){
                String url = domainName + UrlConfig.USERDATAUPDATE;
                updatedatainfo info = new updatedatainfo();
                updatedatainfo.DeviceId deviceId = new updatedatainfo.DeviceId();
                deviceId.setAndroid_ID(NetworkUtil.getAndroidId());
                deviceId.setImei(NetworkUtil.getImei());
                deviceId.setIdfa("");
                deviceId.setIdfv("");
                deviceId.setAdvertiser_ID("");
                info.setDevice_id(deviceId);
                if (!TextUtils.isEmpty(em)){
                    info.setEm(em);
                }else{
                    info.setEm("");
                }
                if (!TextUtils.isEmpty(fb_login_id)){
                    info.setFb_login_id(fb_login_id);
                }else{
                    info.setFb_login_id("");
                }
                if (!TextUtils.isEmpty(phone)){
                    info.setPh(phone);
                }else{
                    info.setPh("");
                }

                info.setZp("");
                info.setCt("");
                info.setCountry("");
                info.setDb("");
                info.setFn("");
                info.setLn("");
                info.setGe("");
                info.setSt("");

                String w2aStr = PreferUtil.getInstance().getString("w2a", "");
                if (TextUtils.isEmpty(w2aStr)){
                    return;
                }
                info.setW2a_data_encrypt(w2aStr);
                String content = JSON.toJSONString(info);
                HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
                    @Override
                    public void callbackDealwith(int currentType, Object info) {
                        if (info != null&&info instanceof NetInfo){
                            NetInfo realinfo = (NetInfo) info;
                            if (realinfo.getCode() == 0){
                                if (!TextUtils.isEmpty(realinfo.getData())){
                                    landingreadresponseinfo realObj = JSON.parseObject(realinfo.getData(), landingreadresponseinfo.class);
                                    if (realObj != null){
                                        String w2aStr = realObj.getW2a_data_encrypt();
                                        PreferUtil.getInstance().putString("w2a", w2aStr);
                                    }
                                }

                            }

                        }
                        if (callback != null){
                            callback.callbackDealwith(info);
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * @方法名称: AdvDataRead
     * @描述: 落地页数据获取
     * @参数: [callback]
     * @返回值 void
     * @创建时间：2022/12/1 12:06
     * @作者 zhou1
     */
    public static String[] AdvDataRead(){
//        try{
//            if(callback != null){
//                String url = domainName + UrlConfig.LANDINGPAGEREAD;
//                String content = "";
//                HttpClientConnector.HttpConnectCommonAsync(2, url, content, new NetCallback() {
//                    @Override
//                    public void callbackDealwith(int currentType, Object info) {
//                        if (callback != null){
//                            callback.callbackDealwith(info);
//                        }
//                    }
//                });
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        String[] sRet = {};
        try{
            String advStr = PreferUtil.getInstance().getString("adv", "");
            if (!TextUtils.isEmpty(advStr)){
                advinfo a = JSON.parseObject(advStr, advinfo.class);
                if (a != null&& a.getAdv_data()!=null&&a.getAdv_data().size()>0){
                    sRet = a.getAdv_data().toArray(new String[a.getAdv_data().size()]);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sRet;


    }

    public static String[] AdvDataRead(String advstr){

        String[] sRet = {};
        try{
            String advStr = advstr;
            if (!TextUtils.isEmpty(advStr)){
                advinfo a = JSON.parseObject(advStr, advinfo.class);
                if (a != null&& a.getAdv_data()!=null&&a.getAdv_data().size()>0){
                    sRet = a.getAdv_data().toArray(new String[a.getAdv_data().size()]);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sRet;


    }
    private String getclipboard(){
        String sRet = "";

        try {
            ClipboardManager clipboard = (ClipboardManager) hm.getInstance().mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) {
                return "";
            }
            //如果是文本信息
            if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData cdText = clipboard.getPrimaryClip();
                ClipData.Item item = cdText.getItemAt(0);
                //此处是TEXT文本信息
                if (item.getText() != null) {
                    //item为剪贴板的内容，你可以取到这个字符串，然后再根据规则去进行剪贴拼接
                    String content = item.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
//                        System.out.println("粘贴板内容" + content);
                        //进行数据处理后需要清空粘贴板
                        sRet = content;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sRet;
    }

    public static void useFingerPrinting(boolean enable){
        bWebviewEnable = enable;
    }
    public static String GetW2AEncrypt(){
        return PreferUtil.getInstance().getString("w2a","");
    }
    public static void SetW2AEncrypt(String w2a_data){
        PreferUtil.getInstance().putString("w2a", w2a_data);
    }
    public static void testprint(){
        Intent intent = new Intent(hm.getInstance().mApplication, com.huntmobi.web2app.app.WebActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        hm.getInstance().mApplication.startActivity(intent);
    }
}
