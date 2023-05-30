/**********************************************************
 * Copyright © 2013-1014 深圳市慧星辰科技有限公司版权所有
 * 创 建 人：zsw
 * 创 建 日 期：2020-9-23 下午2:36:13
 * 版 本 号：
 * 修 改 人：
 * 描 述：
 * <p>
 * <p/>
 * </p>
 **********************************************************/
package com.huntmobi.web2app.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.huntmobi.web2app.hm;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * <p>
 * 网络操作封装，使用okhttp 作为网络请求的基础工具类，不再使用httpclient
 * android官方在api23后不再提供它的内置支持。
 * </p>
 *
 * @author zsw
 * @date 2015-3-1
 * @version
 * @since
 */
public class HttpClientConnector {

    /**post请求里,如果参数带有集合,则以下面的特殊字符串作为key,JsonObject作为value.此JsonObject键为接口协议字段,值为实际参数**/
    public static final String ARGUMENT_CONTAINS_JSON_ARRAY = "1111";   //异步
    public static final String ARGUMENT_CONTAINS_JSON_ARRAY_SYNPRO = "11111";   //同步


    public  enum REQUSET_TYPE {
        REQUSET_TYPE_POST,//post请求
        REQUSET_TYPE_GET,//get请求
    }

    public enum EXTEND_METHOD {
        METHOD_1,//原始方法
        METHOD_2,//扩展方法
        METHOD_3,//新接口方法
        METHOD_4//新接口方法
    }
    //请求错误码
    public static final String ERRO_PARM_NULL = "paramnull";//必须的参数不能为空
    public static final String ERRO_PARM_NONETWORK = "nonetwork";//必须的参数不能为空
    public static final String ERRO_PROCESSING_EXCEPTION = "processingfail";
    public static final int ERRO_FASTJSON_EXCEPTION = -100;
    public static final int ERRO_OTHER_EXCEPTION = -200;
	public static final int ERROR_TIMEOUT_EXCEPTION = -300;
    private final static int RETRY_TIME = 3;
    private final static int RETRY_TIME_EXT = 1;//优化路由通讯时间
    private final static String TAG = "HttpClientConnector";
    /**
     * 获取okhttp单例，防止重复创建
     */
    private static OkHttpClient getOkHttpClient(){
    	return OkHttpClientInstance.getInstance();
    }
    /**
     * 执行okhttp的post请求
     * @param url：请求的url
     * @param content json格式的内容字符串
     */
    private static void OkHttpsPost(final int CurrentType, String url, String content, final Handler handler, final NetCallback callback){
    	Request req = null;
    	Call call = null;
    	if (!NetworkUtil.isNetworkerConnect())
        {
            return;
        }
    	RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), content);


		Log.e("hm", url+ " "+Calendar.getInstance().getTime().toString());
		hm.getInstance().debugStr += url+ " "+Calendar.getInstance().getTime().toString() +"\n";
		Log.e("hm", content);
		hm.getInstance().debugStr += content +"\n";
    	try {
    		 req = new Request.Builder().url(url)  
        	        .post(requestBody)  
        	        .build();  
    		 call = getOkHttpClient().newCall(req);
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			e.printStackTrace();
			return;
		}
    	
    	call.enqueue(new Callback(){
    		@Override
            public void onFailure(Call call, IOException e) {
				hm.getInstance().debugStr += e.getMessage() +"\n";
    			if (handler != null){
			       Message sMessage = new Message();
	               sMessage.what = CurrentType;
	               NetInfo info = new NetInfo();
	               info.setCode(ERRO_OTHER_EXCEPTION);
	               info.setData(e.toString());
	               sMessage.obj = info;
	               handler.sendMessage(sMessage);
    			}
               if (callback != null){
				   NetInfo info = null;

				   info = new NetInfo();
				   if (e instanceof  SocketTimeoutException){
					   info.setCode(ERROR_TIMEOUT_EXCEPTION);
					   info.setMessage(e.toString());
				   }else{
					   info.setCode(ERRO_OTHER_EXCEPTION);
					   info.setMessage(e.toString());
				   }

            	   callback.callbackDealwith(CurrentType, info);
               }

            }

            
			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				// TODO Auto-generated method stub
				if(arg1.isSuccessful()){
					final String result = arg1.body().string();
					Log.e("hm", result);
					hm.getInstance().debugStr += result +"\n";
					if (!TextUtils.isEmpty(result)) {
						try {
							NetInfo realInfo = JSON.parseObject(result, NetInfo.class);
							if (realInfo.getCode() == 0){
								if(handler != null){
									Message sMessage = new Message();
									sMessage.what = CurrentType;
									sMessage.obj = realInfo;
									handler.sendMessage(sMessage);
								}
								if (callback != null){
									callback.callbackDealwith(CurrentType, realInfo);
								}

							}else if (realInfo.getCode() == 2000){
								//预留，如果要做校验失败踢下线功能，在这个地方操作
//		                	HsotaApplication.getInstance().getmCurrentActivity()
							}else{

								if (handler != null){
									Message sMessage = new Message();
									sMessage.what = CurrentType;
									sMessage.obj = realInfo;
									handler.sendMessage(sMessage);
								}
								if (callback != null){
									callback.callbackDealwith(CurrentType, realInfo);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
							if(handler != null){
								Message sMessage = new Message();
								NetInfo info = new NetInfo();
								info.setCode(ERRO_FASTJSON_EXCEPTION);
								info.setMessage(e.toString());
								sMessage.what = CurrentType;
								sMessage.obj = info;
								handler.sendMessage(sMessage);
							}
							if (callback != null){
								NetInfo info = new NetInfo();
								info.setCode(ERRO_FASTJSON_EXCEPTION);
								info.setMessage(e.toString());
								callback.callbackDealwith(CurrentType, info);
							}
						}
					}
				}else{
					if (handler != null){
						Message sMessage = new Message();
						sMessage.what = CurrentType;
						NetInfo info = new NetInfo();
						info.setCode(ERRO_OTHER_EXCEPTION);
						info.setData(arg1.message());
						sMessage.obj = info;
						handler.sendMessage(sMessage);
					}
					if (callback != null){
						NetInfo info = new NetInfo();
						info.setCode(ERRO_OTHER_EXCEPTION);
						info.setData(arg1.message());
						callback.callbackDealwith(CurrentType, info);
					}
				}

			}
    	});
    }
    private static void OkHttpsPostSync(String url, String content, final Handler handler, final NetCallback callback){
    	Request req = null;
    	Call call = null;
    	if (!NetworkUtil.isNetworkerConnect())
        {
            return;
        }
    	RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);  
    	try {
    		 req = new Request.Builder().url(url)  
        	        .post(requestBody)  
        	        .build();  
    		 call = getOkHttpClient().newCall(req);
    		 Response response = call.execute();
    		 NetInfo info = null;
    		 if (response.code() == 200){
    			 final String result = response.body().string();
    			 if (!TextUtils.isEmpty(result)){
    				 try {
    					 info = JSON.parseObject(result, NetInfo.class);
					} catch (JSONException e) {
						// TODO: handle exception
						info = new NetInfo();
	    				 info.setCode(ERRO_FASTJSON_EXCEPTION);
	                     info.setMessage(response.message());
					}
    				 
    			 }else{
    				 info = new NetInfo();
    				 info.setCode(ERRO_OTHER_EXCEPTION);
                     info.setMessage(response.message());
    			 }
                 
    		 }else{
    			 info = new NetInfo();
                 info.setCode(ERRO_OTHER_EXCEPTION);
                 info.setMessage(response.message());
    		 }
    		 
    		 callback.callbackDealwith(0, info);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return;
		}
    	
    }
    /**
     * 同步方式的网络请求，主要用在不需要和ui线程交互的场景中，请求和回调写在一起，方便阅读代码
     */
    public static void HttpConnectCommonSync(final int iType, final String url, final String content, final NetCallback callback){
    	ThreadPoolManager.executeHttpTask(new Runnable() {
			public void run() {
				OkHttpsPost(iType, url, content, null, callback);
			}
		});
    }
	/**
	 * 异步方式的网络请求，主要用在需要和ui线程交互的场景中，回调写在单独的接口中。
	 */
	public static void HttpConnectCommonAsync(final int iType, final String url, final String content, final NetCallback callback){
		//线程是异步的，利于堆栈保存调用url标识，用于在回调时识别是哪个接口的回调 

		ThreadPoolManager.executeHttpTask(new Runnable() {
			public void run() {
				OkHttpsPost(iType, url, content, null, callback);
			}
		});
	}


    public static String MD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }


        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }



    /**
     * @descrition         使用Post请求上传文字和图片
     * @param serverUrl     服务器端url
     * @param data          文字内容
     * @param images        图片列表
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @author hushicheng
     * @createDate  2015.12.3
     */
//    public static String httpPostUploadTextAndImages(String serverUrl, String data, List<Bitmap> images) throws ClientProtocolException, IOException, Exception {
//        String result = null;
//        try{
//          HttpClient httpClient = new DefaultHttpClient();
//
//        BasicHttpContext httpContext = new BasicHttpContext();
//        HttpPost httpPost = new HttpPost(serverUrl);
//
//        long timestamp = System.currentTimeMillis() / 1000;
////        String sign = "id=" + Constants.NET_URL_ID + "&pwd="+ Constants.NET_URL_PWD_ENCRYPTION + "&timestamp=" + timestamp;
////        String md5 = MD5Util.getMD5String(sign);
//
//        String sign = "token=" + MagicianUserInfo.getInstance(AppLoader.getInstance()).getToken() + "&shopid=" + MagicianUserInfo.getInstance(AppLoader.getInstance()).getShopId() + "&key=" + MagicianUserInfo.getInstance(AppLoader.getInstance()).getKey();
//        String md5 = MD5Util.getMD5String(sign);
////        String str = "token=" + MagicianUserInfo.getInstance(AppLoader.getInstance()).getToken() + "&shopid=" + MagicianUserInfo.getInstance(AppLoader.getInstance()).getShopId() + "&sign=" + md5 + "&key="+ MagicianUserInfo.getInstance(AppLoader.getInstance()).getKey() +"&data=";
//
//        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
//        StringBody stringBody = new StringBody("", contentType);
//        entityBuilder.addPart("token", stringBody);
//
//        stringBody = new StringBody("" + timestamp,contentType);
//        entityBuilder.addPart("shopid", stringBody);
//
//        stringBody = new StringBody(md5, contentType);
//        entityBuilder.addPart("sign", stringBody);
//        stringBody = new StringBody("", contentType);
//        entityBuilder.addPart("key", stringBody);
//        stringBody = new StringBody(data, contentType);
//        entityBuilder.addPart("data", stringBody);
//
//        for (Bitmap bitmap : images) {
//            byte[] byteArray = BitmapUtil.compressBmpToBytes(bitmap, 200);
//            ByteArrayBody bab = new ByteArrayBody(byteArray, "kfc.jpg");
//
//            entityBuilder.addPart("image", bab);
//        }
//        httpPost.setEntity(entityBuilder.build());
//        System.setProperty("http.keepAlive", "false");
//        HttpResponse response = httpClient.execute(httpPost, httpContext);
//
//        int statusCode = response.getStatusLine().getStatusCode();
//        if (statusCode != HttpStatus.SC_OK) {
//            throw new IOException("Status code error:" + statusCode);
//        }
//
//          byte[] parseResponse = parseResponse(response);
//            result  = new String(parseResponse, HTTP.UTF_8);
//
//      }catch (Exception e){
//          e.printStackTrace();
//          throw new Exception();
//      }
//        return result;
//    }


}
