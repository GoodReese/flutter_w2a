/**********************************************************
 * Copyright © 2013-1014 深圳市慧星辰科技有限公司版权所有
 * 创 建 人：zsw
 * 创 建 日 期：2014-7-22 下午1:55:19
 * 版 本 号：
 * 修 改 人：
 * 描 述：
 * <p>
 *
 * </p>
 **********************************************************/
package com.huntmobi.web2app.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;


import androidx.core.content.ContextCompat;

import com.huntmobi.web2app.hm;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;


/**
 * <p>
 * 网络环境判断相关工具接口
 * </p>
 *
 * @author zsw
 * @date 2020-9-23
 * @version
 * @since
 */
public class NetworkUtil {
	final static String TAG = "NetworkUtil";
	private final static String IP_ZTB = "10.10.10";
	public final static String WIFI_ZTB = "RT5350_AP";

	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}
	private static String getSystemPropertyByReflect(String key) {
		try {
			@SuppressLint("PrivateApi")
			Class<?> clz = Class.forName("android.os.SystemProperties");
			Method getMethod = clz.getMethod("get", String.class, String.class);
			return (String) getMethod.invoke(clz, key, "");
		} catch (Exception e) {/**/}
		return "";
	}
	public static String getDeviceIdFromSystemApi(Context context, int slotId) {
		String imei = "";
		try {
			TelephonyManager telephonyManager =
					(TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					imei = telephonyManager.getDeviceId(slotId);
				}
			}
		} catch (Throwable e) {
		}
		return imei;
	}

	public static String getDeviceIdFromSystemApi(Context context) {
		String imei = "";
		try {
			TelephonyManager telephonyManager =
					(TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null) {
				imei = telephonyManager.getDeviceId();
			}
		} catch (Throwable e) {
		}
		return imei;
	}
	/**
	 * 获取 IMEI/MEID
	 *
	 * @param context 上下文
	 * @return 获取到的值 或者 空串""
	 */
	public static String getDeviceId(Context context) {
		String imei = "";
		//Android 6.0 以后需要获取动态权限  检查权限
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			return imei;
		}

		// 1. 尝试通过系统api获取imei
		imei = getDeviceIdFromSystemApi(hm.getInstance().mApplication);
		if (TextUtils.isEmpty(imei)) {
			imei = getDeviceIdByReflect(hm.getInstance().mApplication, 0);
		}
		return imei;
	}
	public static String getDeviceId(Context context, int slotId) {
		String imei = "";
		// 1. 尝试通过系统api获取imei
		imei = getDeviceIdFromSystemApi(context, slotId);
		if (TextUtils.isEmpty(imei)) {
			imei = getDeviceIdByReflect(context, slotId);
		}
		return imei;
	}
	public static String getDeviceIdByReflect(Context context, int slotId) {
		try {
			TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			Method method = tm.getClass().getMethod("getDeviceId", int.class);
			return method.invoke(tm, slotId).toString();
		} catch (Throwable e) {
		}
		return "";
	}
	public static String getImei(){
		String imei = "";

		//Android 6.0 以后需要获取动态权限  检查权限
		if (ContextCompat.checkSelfPermission(hm.getInstance().mApplication, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			return imei;
		}

		try {
			TelephonyManager manager = (TelephonyManager) hm.getInstance().mApplication.getSystemService(Context.TELEPHONY_SERVICE);
			if (manager != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// android 8 即以后建议用getImei 方法获取 不会获取到MEID
					Method method = manager.getClass().getMethod("getImei", int.class);
					imei = (String) method.invoke(manager, 0);
				} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					//5.0的系统如果想获取MEID/IMEI1/IMEI2  ----framework层提供了两个属性值“ril.cdma.meid"和“ril.gsm.imei"获取
					imei = getSystemPropertyByReflect("ril.gsm.imei");
					//如果获取不到 就调用 getDeviceId 方法获取

				} else {//5.0以下获取imei/meid只能通过 getDeviceId  方法去取
				}
			}
		} catch (Exception e) {
		}

		if (TextUtils.isEmpty(imei)) {
			String imeiOrMeid = getDeviceId(hm.getInstance().mApplication, 0);
			//长度15 的是imei  14的是meid
			if (!TextUtils.isEmpty(imeiOrMeid) && imeiOrMeid.length() >= 15) {
				imei = imeiOrMeid;
			}
		}

		return imei;
	}
	/**
	 * 获取网络状态
	 * @return 1为wifi连接，2为2g网络，3为3g网络，-1为无网络连接
	 */
	public static int getNetworkerStatus() {
		ConnectivityManager conMan = (ConnectivityManager) hm.getInstance().mApplication.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conMan.getActiveNetworkInfo();
		if (null != info && info.isConnected()) {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				switch (info.getSubtype()) {
					case 1:
					case 2:
					case 4:
						// 2G网络
						return 2;
					default:
						// 3G及其以上网络
						return 3;
				}
			} else {
				// wifi网络
				return 1;
			}
		} else {
			// 无网络
			return -1;
		}
	}

	public static boolean hasNetWork() {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager) hm.getInstance().mApplication.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		cwjManager.getActiveNetworkInfo();
		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}

	/** 判断当前网络是否是wifi网络 */
	public static boolean isWifi() {
		return getNetworkerStatus() == 1;
	}

	public static boolean isWifiConnected() {
		try {
			ConnectivityManager connManager = (ConnectivityManager) hm.getInstance().mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return mWifi.isConnected();
		} catch (Exception e) {
		}
		return false;
	}

	private static WifiInfo getWifiInfo(Context context) {
//		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiManager wifi = (WifiManager) hm.getInstance().mApplication.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		return wifiInfo;
	}

	public static String getSSID() {
		String ssid = "";
		try {
			WifiInfo wifiInfo = NetworkUtil.getWifiInfo(hm.getInstance().mApplication);
			ssid = wifiInfo.getSSID();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return ssid;
	}


	//获取wifi下的ip地址
	public static String getIpAddr(){
		String ip = "";
		try{
			// TODO: 2017/9/29  Android 7.0 在获取 WifiManager的时候需要使用.getApplicationContext()，如果未使用会造成内存泄露。
//			WifiManager wifi = (WifiManager) AppLoader.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiManager wifi = (WifiManager)hm.getInstance().mApplication.getSystemService(Context.WIFI_SERVICE);
	         WifiInfo info = wifi.getConnectionInfo();
	        int ipnum = info.getIpAddress();
	     ip =   ( ipnum & 0xFF) + "." +
	       ((ipnum >> 8 ) & 0xFF) + "." + 
	       ((ipnum >> 16 ) & 0xFF) + "." +
	       ((ipnum >> 24 ) & 0xFF );
		}catch(Exception e){
			Log.e(TAG, e.toString());
		}
		return ip;
	}
	//GPRS连接下的ip
	@SuppressLint("LongLogTag")
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("WifiPreference IpAddress", ex.toString());
	    }
	    return "";
	}
	public static String getAndroidId(){
		try {
			return Settings.Secure.getString(hm.getInstance().mApplication.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
	public static String getOsVersion(){
		return android.os.Build.VERSION.RELEASE;
	}
	public static String getBrand(){
		return android.os.Build.BRAND;
	}
	public static String getModel(){
		return android.os.Build.MODEL;
	}
	public static String getLanguage(){
		return Locale.getDefault().getLanguage();
	}
	/**
	 * 在wifi网络下，判断是否为18wifi网络
	 */
	private static boolean isZTBWifiNet(Context context) {
		WifiInfo wifiInfo = NetworkUtil.getWifiInfo(context);
		int ipAddress = wifiInfo.getIpAddress();
		String ssid = wifiInfo.getSSID();
		String ipString = "";// 本机在WIFI状态下路由分配给的IP地址
		// 获得IP地址：
		if (ipAddress != 0) {
			ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
		}
		boolean isWifiIP = false;
		if (!TextUtils.isEmpty(ipString)) {
			ipString = ipString.substring(0, ipString.lastIndexOf("."));
			isWifiIP = IP_ZTB.equals(ipString);
		}

		boolean isWifiSSID = false;
		if (!TextUtils.isEmpty(ssid)) {
//			ssid = ssid.substring(1, 11);
//			 ssid=ssid.substring(1, 5);
//			if (WIFI_ZTB.equals(ssid)) {
			if(ssid.substring(1).startsWith(WIFI_ZTB)){
				isWifiSSID = true;
			}
		}
		boolean isZtbWifiNet = isWifiIP && isWifiSSID;
		Log.e(TAG, "ip " + ipString + ",ssid " + ssid + ",isWifiMode " + isZtbWifiNet);
		return isZtbWifiNet;
	}

	/**
	 * 判断网络连接状态
	 * 
	 * @return 返回true为连接
	 */
	public static boolean isNetworkerConnect() {
		return getNetworkerStatus() != -1;
	}

	/** 进入网络设置 */
	public static void netWorkSetting(Context context) {
		String sdkVersion = android.os.Build.VERSION.SDK;
		Intent intent = null;
		if (Integer.valueOf(sdkVersion) > 10) {
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		} else {
			intent = new Intent();
			ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");
			intent.setComponent(comp);
			intent.setAction("android.intent.action.VIEW");
		}
		context.startActivity(intent);
	}
	public static String getAndroidBuildVersion(){
		return android.os.Build.VERSION.SDK_INT + "";
	}
	/** 弹出无网络对话框 */
	public static void showNoNetWorkDialog(final Activity activity) {
//		Dialog dialog = null;
//		CustomDialog.Builder customBuilder = new CustomDialog.Builder(activity);
//		customBuilder.setTitle("网络提示").setMessage("您的手机暂未联网，会影响您的正常浏览")
//				.setPositiveButton("继续", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).setNegativeButton("设置", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						netWorkSetting(activity);
//						dialog.dismiss();
//					}
//				});
//		dialog = customBuilder.create();
//		dialog.setCancelable(false);
//		dialog.show();

	}
	
	/**
	 * 检查网络状况,没网就toast
	 * @return
	 */
	public  static boolean  checkNetworkWithToast(){
		
		boolean result = true;
		
		if(NetworkUtil.getNetworkerStatus()==-1){
			
			result =false;
			
			try {
				
//			ToastUtil.showCustomMessage(AppLoader.getInstance().getString(R.string.network_doesn_not_work));
			
			} catch (Exception e) {
				return result;
			}
		}
		
		return result;
		
	}
	/**
	 * 获取本地软件版本号
	 */
	public static int getLocalVersion(Context ctx) {
		int localVersion = 0;
		try {
			PackageInfo packageInfo = ctx.getApplicationContext()
					.getPackageManager()
					.getPackageInfo(ctx.getPackageName(), 0);
			localVersion = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return localVersion;
	}
	/**
	 * 获取本地软件版本号名称
	 */
	public static String getLocalVersionName(Context ctx) {
		String localVersion = "";
		try {
			PackageInfo packageInfo = ctx.getApplicationContext()
					.getPackageManager()
					.getPackageInfo(ctx.getPackageName(), 0);
			localVersion = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return localVersion;
	}
	public static String getScreenSize(){
		Display display = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
			WindowManager windowManager = (WindowManager) hm.getInstance().mApplication.getSystemService(Context.WINDOW_SERVICE);
			WindowMetrics wm = windowManager.getCurrentWindowMetrics();

			// 从Point对象中获取宽、高
			int x = wm.getBounds().width();
			int y = wm.getBounds().height();
			return ""+x+"X"+""+y;
		}else{
			WindowManager windowManager = (WindowManager) hm.getInstance().mApplication.getSystemService(Context.WINDOW_SERVICE);
			display = windowManager.getDefaultDisplay();
			Point outSize = new Point();
			// 通过Display对象获取屏幕宽、高数据并保存到Point对象中
			display.getSize(outSize);
			// 从Point对象中获取宽、高
			int x = outSize.x;
			int y = outSize.y;
			return ""+x+"X"+""+y;
		}

		// 方法一(推荐使用)使用Point来保存屏幕宽、高两个数据


	}

}
