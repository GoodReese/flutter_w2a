package com.zsw.myapplication2;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

public class SettingUtil {

    public static boolean isOpenGPS(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    public static void sendMsg(Handler handler,int what,int time){
        if(null != handler){
            if(time == 0){
                handler.sendEmptyMessage(what);
            }else{
                handler.sendEmptyMessageDelayed(what,time);
            }
        }
    }

    public static void sendMsg(Handler handler, int what, Object value, int time) {
        if (null != handler) {
            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.obj = value;
            if (time == 0) {
                handler.sendMessage(msg);
            } else {
                handler.sendMessageDelayed(msg, time);
            }
        }
    }

}
