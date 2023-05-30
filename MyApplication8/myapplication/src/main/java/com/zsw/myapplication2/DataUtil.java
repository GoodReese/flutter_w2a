package com.zsw.myapplication2;

public class DataUtil {
	public class SharePreferencesData {
		public final String sharedPreferencesName = "akk";
		public final String account = "account";
		public final String password = "password";
		public final String isLogin = "isLogin";
		public final String userId = "userId";
		public final String token = "token";
	}

	/** 操作蓝牙密码的服务UUID */
	public static final String UUID_PASSWORD = "0000ffc0-0000-1000-8000-00805f9b34fb";
	/** 验证、修改蓝牙密码的特征值UUID */
	public static final String UUID_PASSWORD_VERIFY = "0000ffc1-0000-1000-8000-00805f9b34fb";
	/** 操作蓝牙密码回复的特征值UUID */
	public static final String UUID_PASSWORD_BACK = "0000ffc2-0000-1000-8000-00805f9b34fb";

	/** 发送数据给蓝牙的服务UUID */
	public static final String UUID_SEND = "8f400001-cfb4-14a3-f1ba-f61f35cddbaf";
	/** 发送数据给蓝牙的特征值UUID */
	public static final String UUID_SEND_WRITE = "8f400002-cfb4-14a3-f1ba-f61f35cddbaf";

	/** 接收蓝牙通知的服务UUID */
	public static final String UUID_GET = "02f00000-0000-0000-0000-00000000fe00";
	/** 接收蓝牙通知的特征值UUID */
	public static final String UUID_GET_READ = "02f00000-0000-0000-0000-00000000ff02";

	/** 修改蓝牙参数的服务UUID */
	public static final String UUID_BLE = "0000ff90-0000-1000-8000-00805f9b34fb";
	/** 修改、读取蓝牙名字的特征值UUID */
	public static final String UUID_BLE_WRITE_READ = "0000ff91-0000-1000-8000-00805f9b34fb";

	public static final String sharedPreferencesName = "akkble";
	public static final String BLUE_AKK = "st_blue";
	public static final String IS_FIRST = "isFirst";
	public static final String IS_CLEAN = "isClean";
	public static final String IS_DELETE = "isDelete";
	public static final String IS_UPDATE = "isUpdate";
	public static final String IS_EAR = "isEar";
	public static final String IS_FIRST_ID = "isFirst_id";
	public static final String IS_CLEAN_FIRST = "isClean_first";
	public static final String IS_DELETE_FIRST = "isDelete_first";
	public static final String IS_UPDATE_FIRST = "isUpdate_first";
	public static final String IS_EAR_FIRST = "isEar_first";
	public static final String IS_UNBINDEAR = "isUnbindEar";
	
	public static final String GAME_UPDATE ="game_update";
	public static final String GAME_DELETE ="game_delete";
	public static final String DATA_ERA_UNBIND ="data_ear_unbind";
	public static final String DATA_ERA_MODE ="data_ear_mode";
	public static final String YAOKONG_START ="yao_start";
	public static final String ENABLE_BLUETOOTH = "data_enable_bluetooth";
	public static final String FRONT_END_CLEAN = "front_end_clean";
	public static final String CONNECTNET_COUNT = "CONNECTNET_COUNT";
	
	public static final String GET_URL="http://work.huanbaock.cn/api/DeviceStatic/getCurrentTime";
	public static final String DEVICEID="deviceid";
	public static final String DEVICEID_VALUES="1212121";
	public static final String HEADER="Content-Type";
	public static final String HEADER_VALUES="application/json";
	public static final String GAME_DENY_TIME = "game_deny_time";
	public static final String GET_DEVICEINFO_URL="http://work.huanbaock.cn/api/DeviceStatic/getDeviceInfo";
	public static final String GET_DEVICEHEAD_TIME="http://work.huanbaock.cn/api/DeviceStatic/getHeadsetValidetime";
	public static final String GET_HEADSETINFO_URL="http://work.huanbaock.cn/api/DeviceStatic/getHeadsetSepcialInfo";
	public static final String HEADINFO="deviceheadsetId";
}
