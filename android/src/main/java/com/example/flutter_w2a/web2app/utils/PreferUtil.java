package com.huntmobi.web2app.utils;


import com.huntmobi.web2app.hm;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * com.hstar.ota.utils
 * 类的描述:
 * 责任人: hujie
 * 修改人: hujie
 * 创建/修改时间: 2016/1/26 10:28
 * Copyright : 2014-2015 深圳掌通宝科技有限公司-版权所有
 */
public class PreferUtil {
    public static PreferUtil INSTANCE;
    private static SharedPreferences mPrefer;	//SharedPreferences对象
    private static final String APP_NAME="hstar_ota";	//保存数据的文件名
    public static final String FIRST_USE="first_use";	//第一次使用APP
    public static final String STATEBARHEIGHT="stateBarHeight";	//通知栏的高度
    public static String IS_CLOSE_TIP = "close";

    public static PreferUtil getInstance(){
        if(INSTANCE==null){
            return new PreferUtil();
        }
        return INSTANCE;
    }

    public PreferUtil() {
        init();
    }

    /**初始化SharedPreferences*/
    public void init(){
        //实例化SharedPreferences
        mPrefer = hm.getInstance().mApplication.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 返回的是true，表示传入的字符串是否包含知道的char值序列，
     * @param key
     * @return
     */
    public boolean contains(String key){
        return mPrefer.contains(key);
    }

    /**
     * 保存字符串类型的数据
     * @param key
     * @param value
     */
    public void putString(String key, String value){
        SharedPreferences.Editor edit = mPrefer.edit();	//实例化SharedPreferences.Editor对象
        edit.putString(key, value==null?"":value);	//保存键值
        edit.commit();	//提交数据
    }

    /**
     * 取得字符类型的数据
     * @param key
     * @param value
     * @return
     */
    public String getString(String key,String value){
        return mPrefer.getString(key, value);
    }

    /**
     * 保存整型类型的数据
     * @param key
     * @param value
     */
    public void putInt(String key,int value){
        SharedPreferences.Editor edit = mPrefer.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    /**
     * 获取整型类型的数据
     * @param key
     * @param value
     * @return
     */
    public int getInt(String key,int value){
        return mPrefer.getInt(key, value);
    }

    /**
     * 保存布尔类型的数据
     * @param key
     * @param value
     */
    public void putBoolean(String key, boolean value) {
        mPrefer.edit().putBoolean(key, value).commit();
    }

    /**
     * @param key
     * @param /value
     * @return
     */
    public boolean getBoolean(String key, boolean defValue) {
        return mPrefer.getBoolean(key, defValue);
    }

    /**
     * 保存long型的数据
     * @param key
     * @param value
     */
    public void putLong(String key,long value){
        mPrefer.edit().putLong(key, value).commit();
    }

    /**
     * 取得long型的数据
     * @param key
     * @param value
     * @return
     */
    public long getLong(String key,long value){
        return mPrefer.getLong(key, value);
    }

    public void removeKey(String key) {
        mPrefer.edit().remove(key).commit();
    }
}
