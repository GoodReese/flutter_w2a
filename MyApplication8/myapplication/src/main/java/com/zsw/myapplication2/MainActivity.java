package com.zsw.myapplication2;

import static android.content.pm.PackageManager.GET_PERMISSIONS;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huntmobi.web2app.bean.landingreadresponseinfo;
import com.huntmobi.web2app.hm;
import com.huntmobi.web2app.utils.DataCallback;
import com.huntmobi.web2app.utils.NetCallback;
import com.huntmobi.web2app.utils.NetInfo;
import com.huntmobi.web2app.utils.OutNetCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Button mTestBut = null;//测试初始化
    private Button mConnectBut = null;//测试事件转发
    private Button mSendBut = null;//测试更新数据
    private Button mPurchaseBut = null;//测试购买
    private Button mReadBut = null;//测试读取落地页数据
    private Button mNewUpdateBut = null; //新的测试更新数据接口
    private Button mRefreshBut = null;
    private EditText mEmailEt = null;
    private EditText mPhoneEt = null;
    private EditText mCurrencyEt = null;
    private EditText mValueEt = null;
    private EditText mEventNameEt = null;
    private EditText mIdsEt = null;
    private TextView mDebugTv = null;

    private EditText mZpEt = null;
    private EditText mCtEt = null;
    private EditText mStEt = null;
    private EditText mFnEt = null;
    private EditText mLnEt = null;
    private EditText mDbEt =null;
    private EditText mCountryEt = null;
    private EditText mGeEt = null;


    private final static int GPS_REQUEST_CODE = 5;
    private String[] permissions={Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    NewBleUtils newBleUtils = null;
    /**
     * 打开APP信息界面的标识
     */
    private final static int INFO_REQUEST_CODE = 6;
    Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newBleUtils = NewBleUtils.getBleNrfUtil(getApplicationContext());
        initView();
        verifyIfRequestPermission();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hm.getInstance() != null){
                    mDebugTv.setText(hm.getInstance().debugStr);
                }
            }
        }, 10, 20*1000);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    private void initView(){
        mDebugTv = findViewById(R.id.show_zone);
        mDebugTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mEmailEt = findViewById(R.id.em_id);
        mPhoneEt = findViewById(R.id.phone_id);
        mCurrencyEt = findViewById(R.id.currency_id);
        mRefreshBut = findViewById(R.id.test_but_id7);
        mValueEt = findViewById(R.id.cost_id);
        mIdsEt = findViewById(R.id.ids_id);
        mEventNameEt = findViewById(R.id.eventname_id);
        mPurchaseBut = findViewById(R.id.test_but_id4);
        mZpEt = findViewById(R.id.zp_id);
        mCtEt = findViewById(R.id.city_id);
        mCountryEt = findViewById(R.id.country_id);
        mDbEt = findViewById(R.id.db_id);
        mFnEt = findViewById(R.id.fn_id);
        mLnEt = findViewById(R.id.ln_id);
        mStEt = findViewById(R.id.state_id);
        mGeEt = findViewById(R.id.ge_id);
        mRefreshBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hm.getInstance() != null){
                    mDebugTv.setText(hm.getInstance().debugStr);
                }
            }
        });
        mPurchaseBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> list = new ArrayList<String>();
                String eventName = mEventNameEt.getText().toString();
                String currency = mCurrencyEt.getText().toString();
                String value = mValueEt.getText().toString();
                String idsStr = mIdsEt.getText().toString();
                list = Arrays.asList(idsStr.split(","));
                hm.Purchase("Purchase", currency, value, "product", list,new OutNetCallback() {
                    @Override
                    public void callbackDealwith(Object info) {

                    }
                });
            }
        });
        mReadBut = findViewById(R.id.test_but_id5);
        mReadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] advStr = hm.AdvDataRead();
//                  hm.testprint();
            }
        });
        mTestBut = findViewById(R.id.test_but_id);
        mTestBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hm.Init(getApplication(), null, null);
                //获取指纹，默认打开，用下面的函数可以关闭
//                  hm.useFingerPrinting(true);
                  hm.Init(getApplication(), null, null, new DataCallback() {
                      @Override
                      public void CallbackDealwith(String[] strings) {
                          if (strings != null){

                          }
                      }


                  });
//                openBle();
            }
        });
        mConnectBut = findViewById(R.id.test_but_id2);
        mConnectBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(newBleUtils.deviceMac)){
                    newBleUtils.toConnect(newBleUtils.deviceMac);
                }
                List<String> list = new ArrayList<String>();
                String eventName = mEventNameEt.getText().toString();
                String currency = mCurrencyEt.getText().toString();
                String value = mValueEt.getText().toString();
                String idsStr = mIdsEt.getText().toString();
                list = Arrays.asList(idsStr.split(","));
                  hm.EventPost("", eventName, currency, value, "product", list, new OutNetCallback() {
                      @Override
                      public void callbackDealwith(Object info) {

                      }
                  });
            }
        });
        mNewUpdateBut = findViewById(R.id.test_but_id6);
        mNewUpdateBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emStr = mEmailEt.getText().toString();
                String phoneStr = mPhoneEt.getText().toString();
                String zpStr = mZpEt.getText().toString();
                String ctStr = mCtEt.getText().toString();
                String stStr = mStEt.getText().toString();
                String fnStr = mFnEt.getText().toString();
                String lnStr = mLnEt.getText().toString();
                String countryStr = mCountryEt.getText().toString();
                String geStr = mGeEt.getText().toString();
                String dbStr = mDbEt.getText().toString();
                hm.UserDataUpdate(emStr, "", phoneStr, zpStr, ctStr, stStr, geStr, fnStr, lnStr, dbStr, countryStr, new OutNetCallback() {
                    @Override
                    public void callbackDealwith(Object info) {
                        if (info != null&&info instanceof NetInfo){
                            NetInfo realinfo = (NetInfo) info;
                            if (realinfo.getCode() == 0){

                            }

                        }
                    }
                });
            }
        });
        mSendBut = findViewById(R.id.test_but_id3);
        mSendBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                byte[] a = new byte[10];
//                a[0] = 0x05;
//                a[1] = 0x02;
//                a[2] = 0x01;
//                // 2017-8-12 20:12:52 05020107E60B1609
//
//                a[3] = (byte) (2022 >> 8 & 0xff);
//                a[4] = (byte) (2022 & 0xff);
//                a[5] = (byte) (8 & 0xff);
//                a[6] = (byte) (15 & 0xff);
//                a[7] = (byte) (22 & 0xff);
//                a[8] = (byte) (12 & 0xff);
//                a[9] = (byte) (52 & 0xff);
//                newBleUtils.sendData(1, a);
                String emStr = mEmailEt.getText().toString();
                String phoneStr = mPhoneEt.getText().toString();
                hm.UserDataUpdate(emStr, "", phoneStr,new OutNetCallback() {
                    @Override
                    public void callbackDealwith(Object info) {
                        if (info != null&&info instanceof NetInfo){
                            NetInfo realinfo = (NetInfo) info;
                            if (realinfo.getCode() == 0){

                            }

                        }
                    }
                });
            }
        });
    }
    private void connectDevice(){

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("akk","requestCode: " + requestCode + "  ==resultCode: " + resultCode);
        if(requestCode == GPS_REQUEST_CODE){//打开GPS的返回消息
            Log.e("akk","打开GPS的回复:"+resultCode);
        } else if(requestCode == INFO_REQUEST_CODE){
            Log.e("akk","打开App信息的回复:"+resultCode);
            verifyIfRequestPermission();
        }
    }
    private void goAppInfo() {
        new AlertDialog.Builder(this).setTitle(R.string.hint)
                .setMessage(R.string.hint_go_app_info)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                        intent.setData(uri);
                        startActivityForResult(intent,INFO_REQUEST_CODE);
                    }
                }).setNeutralButton(R.string.cancel,null).show();
    }
    private void verifyIfRequestPermission() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            Log.e("akk","6.0申请权限");
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)){//申请权限失败时，显示提示
                    //跳转到本应用的信息界面
                    goAppInfo();
                }else{
                    ActivityCompat.requestPermissions(this,permissions, GET_PERMISSIONS);
                    Log.e("akk","6.0以上没有权限时申请权限!");
                }
            }else{
                Log.e("akk","6.0已拥有该权限");
                //openBle();
            }
        } else {
            Log.e("akk","6.0以下的版本");
            //openBle();
        }
    }
    private void goGPS(){
        new AlertDialog.Builder(this).setTitle(R.string.hint)
                .setMessage(R.string.hint_go_gps)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,GPS_REQUEST_CODE);
                    }
                }).setNegativeButton(R.string.cancel,null).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {//第一次打开软件可以进来
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == GET_PERMISSIONS) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("----","cameraPermisssion");
//                checkReadPermission();//联动申请读

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    private void openBle() {

        if(newBleUtils.isConnected()){//如果已经连接上
//            Toast.makeText(MainActivity.this, getString(R.string.hdk_have_connect), Toast.LENGTH_SHORT).show();
            Log.e("ble", "hdk have connect");
            return;
        }
        if(SettingUtil.isOpenGPS(this)){//GPS已打开
            if(newBleUtils.isEnableBluetooth()){//蓝牙已打开
//                showAddNewDeviceDialog();
                newBleUtils.startScan();
            }else{//蓝牙未打开
//                showPgDialog(MainActivity.this,getString(R.string.ble_eanble));
                newBleUtils.enableBluetooth(true);
//                mHandler.sendEmptyMessageDelayed(BLE_EANBLEINGT,5*1000);
            }
        }else{//GPs未打开时，显示需要打开GPS的对话框
            goGPS();
        }
    }
}