package com.zsw.myapplication2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;


public class BleNrfUtils extends BleManager<BleManagerCallbacks> {

    private static BleNrfUtils mBleNrfUtils;

    public static final int BLE_HANDLER_TO_CONNECT_BLE_TWO = 20;
    /**
     * 进行密码验证
     */
    public static final int BLE_HANDLER_BLE_VERIFY_PASSWORD = 5;

    /**
     * 修改密码
     */
    public static final int BLE_HANDLER_BLE_CHANGE_PASSWORD = 6;
    /**
     * 发送唤醒数据，再发送指令数据
     */
    public static final int BLE_HANDLER_BLE_SEND = 8;
    /**
     * 发送指令数据
     */
    public static final int BLE_HANDLER_BLE_SEND_DATA = 9;
    public static final byte AKK2BLE_PURCHASE = 0x22;
    public static final byte AKK2BLE_PURCHASE_OK = (byte) 0xA2;
    public static final byte AKK2BLE_PURCHASE_ER = 0x62;

    /**
     * 发送消息超时判断
     */
    public static final int BLE_SEND_MSG_OUT_TIME = 10;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;

    /**
     *扫描到的蓝牙设备集合
     */

    private final List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();

    private boolean isConnecting = false;
    private String connectingAddress;//55-04-28-64-22-72-61-DA

    //3516 同步参数
    public static final byte AKK2BLE_SYNC_PARAM = 0x28;
    public static final byte AKK2BLE_SYNC_PARAM_OK = (byte) 0xa8;
    public static final byte AKK2BLE_SYNC_PARAM_ER = 0x68;

    byte[] valuePass = {54, 53, 52, 51, 50, 49};

    private String frontEndVersion;//版本号
    private int frontEndTestRunTime;//试机次数
    private int frontEndHeartbeat;//心跳
    private int frontEndPurchasedArea;//购买区域
    private int frontEndIsBuy;//是否已经购买
    private int frontEndAwitchMode;//切换模式
    private int frontEndFrequencyPoint;//频点显示值
    private int frontEndValid;//有效时长
    private String frontEndId;//前端ID

    public BleNrfUtils(@NonNull Context context) {
        super(context);
        init(context);
        initHandler();
        setGattCallbacks(blinkyManagerCallbacks);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    public static BleNrfUtils getBleNrfUtil(Context context) {
        if (mBleNrfUtils == null) {
            mBleNrfUtils = new BleNrfUtils(context);
        }
        return mBleNrfUtils;
    }

    /**
     * 初始化对本地蓝牙适配器的引用
     */
    private void init(Context context) {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e("akk", "BleUtil: Unable to initialize BluetoothManager.");
                return;
            }
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e("akk", "BleUtil: Unable to initialize BluetoothAdapter.");
            }
        }
    }

    private void initHandler() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BLE_HANDLER_TO_CONNECT_BLE_TWO:// 连接设备
                        toConnect();
                        break;
                    case BLE_HANDLER_BLE_VERIFY_PASSWORD: // 进行密码验证
//                        handler.sendEmptyMessageDelayed(BLE_SEND_MSG_OUT_TIME,30*1000);
//                        SettingUtil.sendMsg(MainActivity.handler,MainActivity.BLE_SEND_MSG_OUT_TIME_MAIN,30*1000);
//                        SettingUtil.sendMsg(MainActivity.handler,MainActivity.HDK_PASS_VERIFY,0);
                        Log.e("akk", "进行密码验证");
                        sendData(1, "897654".getBytes());
                        break;
                    case BLE_HANDLER_BLE_CHANGE_PASSWORD: // 修改密码
                        Log.e("akk", "修改密码");
                        sendData(1, "000000123456".getBytes());
                        break;
                    case BLE_HANDLER_BLE_SEND: // 发送唤醒数据，再发送指令数据
                        Log.e("akk", "发送唤醒数据，再发送指令数据");
                        sendData(2, "000000".getBytes());
                        SettingUtil.sendMsg(this, BLE_HANDLER_BLE_SEND_DATA, msg.obj, 300);
                        break;
                    case BLE_HANDLER_BLE_SEND_DATA: // 发送指令数据
                        Log.e("akk", "发送的指令---" + ((byte[]) msg.obj)[1]);
//                        handler.sendEmptyMessageDelayed(BLE_SEND_MSG_OUT_TIME,30*1000);
//                        SettingUtil.sendMsg(MainActivity.handler,MainActivity.BLE_SEND_MSG_OUT_TIME_MAIN,30*1000);

                        sendData(2, (byte[]) msg.obj);
                        break;
//                    case BLE_SEND_MSG_OUT_TIME://发送消息超时判断
//                        SettingUtil.sendMsg(MainActivity.handler,MainActivity.BLE_SEND_MSG_OUT_TIME_MAIN,0);
//                        break;
                }
            }
        };
    }

    /**
     * 打开、关闭蓝牙
     *
     * @param enable true:打开蓝牙、false:关闭蓝牙
     * @return 操作结果
     */
    public boolean enableBluetooth(boolean enable) {
        if (enable) {

            if (!mBluetoothAdapter.isEnabled()) {
                return mBluetoothAdapter.enable();
            }
            return true;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                return mBluetoothAdapter.disable();
            }
            return false;
        }
    }

    /**
     * 获取蓝牙开关状态
     *
     * @return true:蓝牙已打开、false:蓝牙已关闭
     */
    public boolean isEnableBluetooth() {
        return mBluetoothAdapter.isEnabled();
    }


    private boolean isScanning = false;
    /**
     * 扫描设备的回调
     */
    private OnLeScanListener onLeScanListener;

    /**
     * 开始扫描
     */
    public void startScan(){
        if(isScanning){
            return;
        }
        isScanning = true;
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .setUseHardwareFilteringIfSupported(false)
                .build();
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(null,settings,scanCallback);
    }

    /**
     * 停止扫描
     */
    public void stopScan(){
        Log.e("akk","BleUtil: stopScanNrf");
        isScanning = false;
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback);
    }

    /**
     * 搜索设备回调
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null && !result.getDevice().getName().isEmpty()){
                Log.e("akk","BleNrfUtil: onScanResult: "+ result.getDevice().getName());
                deviceDiscovered(result);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results){
                if(result.getDevice().getName() != null && !result.getDevice().getName().isEmpty()){
                    Log.e("akk","BleNrfUtil: onBatchScanResults: "+ result.getDevice().getName());
                    deviceDiscovered(result);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("akk","BleNrfUtil: onScanFailed: " + errorCode);
            stopScan();
        }
    };

    /**
     * 添加或刷新设备数据，并通知回调
     * @param result 添加或刷新的设备数据
     */
    private void deviceDiscovered(ScanResult result){
        Log.e("akk","列表中的数量： " + mDevices.size());
        DiscoveredBluetoothDevice device;
        // check if it"s a new device.
        final int index = indexOf(result);
        if(index == -1){
            device = new DiscoveredBluetoothDevice(result);
            mDevices.add(device);
        } else {
            device = mDevices.get(index);
        }

        // Update RSSU and name
        device.update(result);

        if(onLeScanListener != null){
            onLeScanListener.onLeScan(result.getDevice(),result.getRssi(),result.getScanRecord().getBytes());
        }
    }

    /**
     * @param result 蓝牙设备数据
     * @return 集合中该设备的下角标
     */
    private int indexOf(ScanResult result){
        int i = 0;
        for(DiscoveredBluetoothDevice device : mDevices){
            if(device.matches(result))
                return i;
            i++;
        }
        return -1;
    }

    public void toConnect(String address){
        if(isConnected()){
            Log.e("akk","toConnect: 已连接设备");
            if(getBluetoothDevice() == null || !getBluetoothDevice().getAddress().equals(address)){
                Log.e("akk","toConnect: 已连接设备，不是需要连接的");
                disconnect().enqueue();
                isConnecting = true;
                connectingAddress = address;
                handler.sendEmptyMessageDelayed(BLE_HANDLER_TO_CONNECT_BLE_TWO,800);
            }
        } else {
            if(isConnecting){
                Log.e("akk","toConnect: 连接中");
                if(!connectingAddress.equals(address)){
                    Log.e("akk","toConnect: 连接中，不是需要连接的");
                    isConnecting = true;
                    connectingAddress = address;
                    handler.sendEmptyMessageDelayed(BLE_HANDLER_TO_CONNECT_BLE_TWO, 800);
                }
            }else{
                Log.e("akk","toConnect: 未连接，也没在连接中");
                isConnecting = true;
                connectingAddress = address;
                toConnect();
            }
        }
    }

    /**
     * 连接设备
     */
    private void toConnect(){
        isConnecting = true;
        BluetoothDevice bluetoothDevice = null;
        if(Build.BRAND.equalsIgnoreCase("samsung")){//如果不是三星的手机就通知搜索
            for(DiscoveredBluetoothDevice dbd : mDevices){
                if(connectingAddress.equals(dbd.getAddress())){
                    bluetoothDevice = dbd.getDevice();
                    break;
                }
            }
        } else {
            if(isScanning){
                stopScan();
            }
        }

        if(bluetoothDevice == null){
            Log.e("akk","toConnect:没有搜索到连接的设备");
            bluetoothDevice = mBluetoothAdapter.getRemoteDevice(connectingAddress);
        }
        if(bluetoothDevice == null){
            Log.e("akk","toConnect: 无法连接到设备！");
            isConnecting = false;
            connectingAddress = null;
            return;
        }
        connect(bluetoothDevice)
                .retry(3,100)
                .useAutoConnect(false)
                .enqueue();
    }

    /**
     * 断开连接
     */
    public void toDisconnect(){
        if(isConnected()){
            isConnecting = false;
            disconnect().enqueue();;
            Log.e("akk","断开连接!");
        }
    }

    private BluetoothGattCharacteristic passwordCharacteristic_write;
    private BluetoothGattCharacteristic passwordCharacteristic_Notify;
    private BluetoothGattCharacteristic dataCharacteristic_Write;
    private BluetoothGattCharacteristic dataCharacteristic_Notify;

    /**
     * BluetoothGatt callbacks object.
     */
    private BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {


        @Override
        protected void initialize() {
            super.initialize();
            Log.e("akk","----------initialize()");
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            Log.e("akk","------isRequiredServiceSupported------读取服务成功-----"+getBluetoothDevice().getAddress());
            isConnecting = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BluetoothGattService service1 = gatt.getService(UUID.fromString(DataUtil.UUID_PASSWORD));
                    if(null != service1){
                        Log.e("akk","获取到验证密码服务");
                        passwordCharacteristic_write = service1.getCharacteristic(UUID.fromString(DataUtil.UUID_PASSWORD_VERIFY));
                        passwordCharacteristic_Notify = service1.getCharacteristic(UUID.fromString(DataUtil.UUID_PASSWORD_BACK));
                        BluetoothGattService service2 = gatt.getService(UUID.fromString(DataUtil.UUID_SEND));
                        if(null != service2){
                            Log.e("akk","获取到发送数据服务");
                            dataCharacteristic_Write = service2.getCharacteristic(UUID.fromString(DataUtil.UUID_SEND_WRITE));
                            BluetoothGattService service3 = gatt.getService(UUID.fromString(DataUtil.UUID_GET));
                            if(null != service3){
                                Log.e("akk","获取到接受数据服务");
                                dataCharacteristic_Notify = service3.getCharacteristic(UUID.fromString(DataUtil.UUID_GET_READ));

                                setNotificationCallback(passwordCharacteristic_Notify).with(passwordCallback);
                                enableNotifications(passwordCharacteristic_Notify).enqueue();
                                setNotificationCallback(dataCharacteristic_Notify).with(dataCallback);
                                enableNotifications(dataCharacteristic_Notify).enqueue();

                                handler.sendEmptyMessageDelayed(BLE_HANDLER_BLE_VERIFY_PASSWORD, 200);
                                return;
                            }
                        }
                    }
                    Log.e("akk","不是我司的设备");
//                    SettingUtil.sendMsg(MainActivity.handler, MainActivity.DEVICE_HANDLER_BLE_CONNECT_ERROR, 0);
                }
            },300);
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            Log.e("akk","------onDeviceDisconnected");
        }


    };

    public void sendData(int type, byte[] data) {
        if (type == 1) { // 发送密码
            writeCharacteristic(passwordCharacteristic_write, data).with(passwordSendCallback).enqueue();
        } else { // 发送数据
            writeCharacteristic(dataCharacteristic_Write, data).with(dataSendCallback).enqueue();
        }
    }

    private DataSentCallback passwordSendCallback = new DataSentCallback() {
        @Override
        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk","passwordSendCallback:发出数据： " + data.toString() + " ,地址： " + device.getAddress());
        }

    };

    private DataReceivedCallback passwordCallback = new DataReceivedCallback() {
        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk","2222222passwordCallback:收到数据： " + data.toString() + " ,地址： " + device.getAddress());
            byte[] value = data.getValue();
            if(value.length == 6){//长度正确
                if(value[0] == 54 && value[1] == 53 && value[2] == 52 && value[3] == 51 && value[4] == 50 && value[5] == 49){
                    Log.e("akk","验证密码成功");
                    isConnecting = false;
//                    handler.removeMessages(BLE_SEND_MSG_OUT_TIME);
//                    SettingUtil.sendMsg(MainActivity.handler,MainActivity.DEVICE_HANDLER_BLE_CONNECT,0);
                    // 发送消息给界面
//                    SettingUtil.sendMsg(MainActivity.handler, MainActivity.DEVICE_HANDLER_BLE_CONNECT, 0);
//                    Send2HDK(StringUtil.sendFrontEndData1((int)(System.currentTimeMillis()/1000), AKK2BLE_SYNC_PARAM, (byte) 4), 100);
//                    SettingUtil.sendMsg(MainActivity.handler,MainActivity.SYNCHRONOUS_DATA,0);
                }else{
                    Log.e("akk","验证密码失败");
//                    SettingUtil.sendMsg(MainActivity.handler, MainActivity.HDK_PASS_FAILE, 0);
                }
            }else{
                Log.e("akk","验证密码长度不正确");
//                SettingUtil.sendMsg(MainActivity.handler, MainActivity.HDK_PASS_FAILE, 0);
            }
        }
    };

    private DataSentCallback dataSendCallback = new DataSentCallback() {
        @Override
        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk","dataSendCallback:发出数据： " + data.toString() + " ,地址： " + device.getAddress());
        }
    };

    /**
     * 设备信息数据
     */
    private byte[] dataValue;
    String frontEndIdstr = "";

    /**
     * 数据操作的通知回调
     */
    private DataReceivedCallback dataCallback = new DataReceivedCallback() {
        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk","22222222dataCallback:收到数据： " + data.toString() + " ,地址： " + device.getAddress());
            byte[] value = data.getValue();
            if(value[2] == (byte) 0xA8){//同步数据成功
                if(value[4] == (byte) 0x00){
//                    setFrontEndTestRunTime(StringUtil.bytesToInt2(value, 5));//试机次数
//                    setFrontEndVersion("V"+StringUtil.getVersion(value));
//                    setFrontEndPurchasedArea(StringUtil.bytesToInt2(value, 11));//购买区域
//                    setFrontEndFrequencyPoint(StringUtil.bytesToInt(value, 13));//频点获取
//                    setFrontEndAwitchMode(value[17]);//模式切换
                    Log.e("akk","同步数据第一包成功="+getFrontEndPurchasedArea()+";频点设置="+getFrontEndFrequencyPoint());
                } else if(value[4] == (byte) 0x01){
                    setFrontEndIsBuy(value[5]);//购买标志
//                    setFrontEndValid(StringUtil.bytesToInt2(value, 6));//有效时长
                    frontEndIdstr = "";
                    for(int i = 8;i<16;i++) {
                        frontEndIdstr += String.format("%02X", value[i]);
                    }
                    Log.e("akk","----同步数据第二包成功=购买标志="+getFrontEndIsBuy()+";有效时长="+getFrontEndValid());
                } else if(value[4] == (byte) 0x02){
                    Log.e("akk","同步数据第三包成功=");
                    for(int i = 5;i<17;i++){
                        frontEndIdstr += String.format("%02X", value[i]);
                    }
                } else if(value[4] == (byte) 0x03){
                    Log.e("akk","同步数据第四包成功=");
                    for(int i = 5;i<9;i++){
                        frontEndIdstr += String.format("%02X", value[i]);
                    }
                    Log.e("akk","同步数据第四包成功前端ID字符串frontEndIdstr="+frontEndIdstr);
                    setFrontEndId(frontEndIdstr);//前端ID

//                    SettingUtil.sendMsg(MainActivity.handler,MainActivity.SYNCHRONOUS_DATA_OK,0);
                }
            } else if(value[2] == (byte) 0x68){//同步数据失败
//                SettingUtil.sendMsg(MainActivity.handler, MainActivity.SYNCHRONOUS_DATA_FAILE, 0);

            } else if (value[2] == (byte) 0xA2){//购买设置成功
//                SettingUtil.sendMsg(MainActivity.handler, MainActivity.HDK_BUY_SET_OK, 0);
            } else if (value[2] == (byte) 0x62){//购买设置失败
//                SettingUtil.sendMsg(MainActivity.handler, MainActivity.HDK_BUY_SET_ERROR, 0);
            }
        }
    };

    public Handler getHandler(){
        return handler;
    }

    public void setOnLeScanListener(OnLeScanListener onLeScanListener) {
        this.onLeScanListener = onLeScanListener;
    }

    public List<DiscoveredBluetoothDevice> getmDevices() {
        return mDevices;
    }

    public byte[] getDataValue() {
        return dataValue;
    }

    public void setDataValue(byte[] dataValue) {
        this.dataValue = dataValue;
    }

    private BleManagerCallbacks blinkyManagerCallbacks = new BleManagerCallbacks() {

        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.e("akk","11111----- 连接中...... ----------------");
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.e("akk","111111-------- 连接成功...... ----------------" + device.getAddress());
            isConnecting = false;
//            SettingUtil.sendMsg(MainActivity.handler,
//                    MainActivity.HDK_CONNECT_OK, 0);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.e("akk","11111----- 断开中...... ----------------");

        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
            Log.e("akk","111111-------- 断开...... ----------------" + device.getAddress());
            isConnecting = false;
            if (connectingAddress.equals(device.getAddress())) { // 如果断开的设备时需要连接的设备时通知UI界面
//                SettingUtil.sendMsg(MainActivity.handler,
//                        MainActivity.DEVICE_HANDLER_BLE_DISCONNECT, 0);

            }
        }

        @Override
        public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {

        }

        @Override
        public void onBondingRequired(@NonNull BluetoothDevice device) {

        }

        @Override
        public void onBonded(@NonNull BluetoothDevice device) {

        }

        @Override
        public void onBondingFailed(@NonNull BluetoothDevice device) {

        }

        @Override
        public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

        }

        @Override
        public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

        }
    };

    public String getFrontEndVersion() {
        return frontEndVersion;
    }
    public void setFrontEndVersion(String frontEndVersion) {
        this.frontEndVersion = frontEndVersion;
    }
    public int getFrontEndTestRunTime() {
        return frontEndTestRunTime;
    }
    public void setFrontEndTestRunTime(int frontEndTestRunTime) {
        this.frontEndTestRunTime = frontEndTestRunTime;
    }
    public int getFrontEndHeartbeat() {
        return frontEndHeartbeat;
    }
    public void setFrontEndHeartbeat(int frontEndHeartbeat) {
        this.frontEndHeartbeat = frontEndHeartbeat;
    }
    public int getFrontEndPurchasedArea() {
        return frontEndPurchasedArea;
    }
    public void setFrontEndPurchasedArea(int frontEndPurchasedArea) {
        this.frontEndPurchasedArea = frontEndPurchasedArea;
    }
    public int getFrontEndIsBuy() {
        return frontEndIsBuy;
    }
    public void setFrontEndIsBuy(int frontEndIsBuy) {
        this.frontEndIsBuy = frontEndIsBuy;
    }
    public int getFrontEndAwitchMode() {
        return frontEndAwitchMode;
    }
    public void setFrontEndAwitchMode(int frontEndAwitchMode) {
        this.frontEndAwitchMode = frontEndAwitchMode;
    }
    public int getFrontEndFrequencyPoint() {
        return frontEndFrequencyPoint;
    }
    public void setFrontEndFrequencyPoint(int frontEndFrequencyPoint) {
        this.frontEndFrequencyPoint = frontEndFrequencyPoint;
    }

    public int getFrontEndValid() {
        return frontEndValid;
    }

    public void setFrontEndValid(int frontEndValid) {
        this.frontEndValid = frontEndValid;
    }

    public String getFrontEndId() {
        return frontEndId;
    }

    public void setFrontEndId(String frontEndId) {
        this.frontEndId = frontEndId;
    }
    public String getConnectingAddress(){
        return connectingAddress;
    }
    public void Send2HDK(Object value, int time) {
        SettingUtil.sendMsg(getHandler(), BLE_HANDLER_BLE_SEND, value, time);
    }

    public static byte[] sendFrontEndData3(byte order,byte[] citycode,byte[] buyTime,byte[] vail){
        byte[] data = new byte[12];
        data[0] = 0x55;
        data[1] = (byte) 8;
        data[2] = order;
        data[3] = citycode[0];
        data[4] = citycode[1];
        data[5] = buyTime[0];
        data[6] = buyTime[1];
        data[7] = buyTime[2];
        data[8] = buyTime[3];
        data[9] = vail[0];
        data[10] = vail[1];
        data[11] = (byte) (data[0]+data[1]+data[2]+data[3]+data[4]+data[5]+data[6]+data[7]+data[8]+data[9]+data[10]);
        Log.e("wqwq", "----front=="+ Arrays.toString(data));
        return data;
    }
}
