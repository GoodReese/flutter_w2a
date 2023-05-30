package com.huntmobi.web2app.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * okhttp client 单例
 * @author zhou1
 *
 */
public class OkHttpClientInstance {

    public static OkHttpClient instance;

    private OkHttpClientInstance() {}

    public static OkHttpClient getInstance() {
        if (instance == null) {
            synchronized (OkHttpClientInstance.class) {
                if (instance == null) {
                    //配置了网络请求的超时时间
                    instance = new OkHttpClient().newBuilder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(new HttpHeaderInterceptor())
                            .build();

                }
            }
        }
        return instance;
    }
    
    /**
     * @author zhou1
     * 请求头拦截器，为 网络请求添加头数据
     */
    public static class HttpHeaderInterceptor implements Interceptor{

		
		@Override
		public Response intercept(Chain arg0) throws IOException {
			// TODO Auto-generated method stub
			Request request = arg0.request();
			 Request build = request.newBuilder()
		                .addHeader("token", "1")
		                .addHeader("sessionId", "2")
		                .build();
			return arg0.proceed(build);
		}
    	
    } 
}