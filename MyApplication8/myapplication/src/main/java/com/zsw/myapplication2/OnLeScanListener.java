package com.zsw.myapplication2;

import android.bluetooth.BluetoothDevice;

public interface OnLeScanListener {
    /**
     * 搜索到蓝牙设备的回调
     * @param device 蓝牙设备数据
     * @param rssi 信号强度
     * @param scanRecord
     */
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

    /**
     * 停止搜索蓝牙设备
     */
    public void onStopScan();
}
