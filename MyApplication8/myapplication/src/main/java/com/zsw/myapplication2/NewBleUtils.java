package com.zsw.myapplication2;

import static android.content.pm.PackageManager.GET_PERMISSIONS;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
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

public class NewBleUtils extends BleManager<BleManagerCallbacks> {

    public static final int BLE_HANDLER_TO_CONNECT_BLE_TWO = 20;
    /**
     * 发送唤醒数据，再发送指令数据
     */
    public static final int BLE_HANDLER_BLE_SEND = 8;
    /**
     * 发送指令数据
     */
    public static final int BLE_HANDLER_BLE_SEND_DATA = 9;

    /**
     * The manager constructor.
     * <p>
     * After constructing the manager, the callbacks object must be set with
     * {@link #setGattCallbacks(BleManagerCallbacks)}.
     * <p>
     * To connect a device, call {@link #connect(BluetoothDevice)}.
     *
     * @param context the context.
     */
    public NewBleUtils(@NonNull Context context) {
        super(context);
        init(context);
        initHandler();
        mContext = context;
        setGattCallbacks(blinkyManagerCallbacks);
    }

    public Context mContext;
    private boolean isConnecting = false;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private final List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();
    private String connectingAddress;
    private Handler handler;
    private BluetoothGattCharacteristic dataCharacteristic_Write;
    private BluetoothGattCharacteristic dataCharacteristic_Notify;
    private static NewBleUtils mBleNrfUtils = null;
    public String deviceMac = "";
    @NonNull
    @Override
    protected BleManager<BleManagerCallbacks>.BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    private DataSentCallback dataSendCallback = new DataSentCallback() {
        @Override
        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk", "dataSendCallback:发出数据： " + data.toString() + " ,地址： " + device.getAddress());
        }
    };

    public static NewBleUtils getBleNrfUtil(Context context) {
        if (mBleNrfUtils == null) {
            mBleNrfUtils = new NewBleUtils(context);
        }
        return mBleNrfUtils;
    }

    public void sendData(int type, byte[] data) {

        writeCharacteristic(dataCharacteristic_Write, data).with(dataSendCallback).enqueue();
    }

    private BleManagerCallbacks blinkyManagerCallbacks = new BleManagerCallbacks() {

        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.e("akk", "11111----- 连接中...... ----------------");
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.e("akk", "111111-------- 连接成功...... ----------------" + device.getAddress());
            isConnecting = false;
//            SettingUtil.sendMsg(MainActivity.handler,
//                    MainActivity.HDK_CONNECT_OK, 0);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.e("akk", "11111----- 断开中...... ----------------");

        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
            Log.e("akk", "111111-------- 断开...... ----------------" + device.getAddress());
            isConnecting = false;
            if (connectingAddress.equals(device.getAddress())) { // 如果断开的设备时需要连接的设备时通知UI界面
//                SettingUtil.sendMsg(MainActivity.handler,
//                        MainActivity.DEVICE_HANDLER_BLE_DISCONNECT, 0);

            }
        }

        @Override
        public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
            Log.e("akk", "111111-------- 丢失...... ----------------" + device.getAddress());
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
            Log.e("akk", "111111-------- 失败...... ----------------" + device.getAddress());
        }

        @Override
        public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
            Log.e("akk", "111111-------- 错误...... ----------------" + message);
        }

        @Override
        public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

        }
    };

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

    private DataReceivedCallback dataCallback = new DataReceivedCallback() {
        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e("akk", "22222222dataCallback:收到数据： " + data.toString() + " ,地址： " + device.getAddress());
            byte[] value = data.getValue();

        }
    };
    private BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {


        @Override
        protected void initialize() {
            super.initialize();
            Log.e("akk", "----------initialize()");
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            Log.e("akk", "------isRequiredServiceSupported------读取服务成功-----" + getBluetoothDevice().getAddress());
            isConnecting = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.e("akk", "获取到发送数据服务");
                    BluetoothGattService service2 = gatt.getService(UUID.fromString(DataUtil.UUID_SEND));
                    dataCharacteristic_Write = service2.getCharacteristic(UUID.fromString(DataUtil.UUID_SEND_WRITE));
                    BluetoothGattService service3 = gatt.getService(UUID.fromString(DataUtil.UUID_GET));
                    if (null != service3) {
                        Log.e("akk", "获取到接受数据服务");
                        dataCharacteristic_Notify = service3.getCharacteristic(UUID.fromString(DataUtil.UUID_GET_READ));

//                        setNotificationCallback(passwordCharacteristic_Notify).with(passwordCallback);
//                        enableNotifications(passwordCharacteristic_Notify).enqueue();
                        setNotificationCallback(dataCharacteristic_Notify).with(dataCallback);
                        enableNotifications(dataCharacteristic_Notify).enqueue();

//                        handler.sendEmptyMessageDelayed(BLE_HANDLER_BLE_VERIFY_PASSWORD, 200);
                        return;
                    }
                    Log.e("akk", "不是我司的设备");
//                    SettingUtil.sendMsg(MainActivity.handler, MainActivity.DEVICE_HANDLER_BLE_CONNECT_ERROR, 0);
                }
            }, 300);
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            Log.e("akk", "------onDeviceDisconnected");
        }


    };

    /**
     * 打开、关闭蓝牙
     *
     * @param enable true:打开蓝牙、false:关闭蓝牙
     * @return 操作结果
     */
    public boolean enableBluetooth(boolean enable) {
        if (enable) {

            if (!mBluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
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
    public void startScan() {
        if (isScanning) {
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
        scanner.startScan(null, settings, scanCallback);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        Log.e("akk", "BleUtil: stopScanNrf");
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
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
            }
            if (result.getDevice().getName() != null && !result.getDevice().getName().isEmpty()) {
                Log.e("akk", "BleNrfUtil: onScanResult: " + result.getDevice().getName());

                deviceDiscovered(result);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT};
//                    ActivityCompat.requestPermissions(MyApplication.mAct,permissions, GET_PERMISSIONS);
//                    return;
                }
                if (result.getDevice().getName() != null && !result.getDevice().getName().isEmpty()) {
                    Log.e("akk", "BleNrfUtil: onBatchScanResults: " + result.getDevice().getName());
                    Log.e("akk", "BleNrfUtil: onBatchScanResults2: " + result.getDevice().getAddress());
                    if (!TextUtils.isEmpty(result.getDevice().getName())){
                        if (result.getDevice().getName().equals("I7")){
                            deviceMac = result.getDevice().getAddress();
                        }

                    }
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
}
