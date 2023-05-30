package com.example.flutter_w2a;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import com.huntmobi.web2app.hm;
import com.huntmobi.web2app.utils.DataCallback;
import com.huntmobi.web2app.utils.NetCallback;
import com.huntmobi.web2app.utils.NetInfo;
import com.huntmobi.web2app.utils.OutNetCallback;
import android.app.Application;
import android.content.Context;
import java.util.Arrays;

/** FlutterW2aPlugin */
public class FlutterW2aPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context mContext;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_w2a");
    channel.setMethodCallHandler(this);
    mContext = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("useFingerPrinting")) {
      boolean isEnabled = call.argument("isEnabled");
      hm.useFingerPrinting(isEnabled);
      result.success("");
    } else if (call.method.equals("init")) {
      String gateway = call.argument("gateway");
      String installEventName = call.argument("installEventName");
      Application application = (Application) mContext.getApplicationContext();
      hm.Init(application, gateway, installEventName, new DataCallback() {
          @Override
          public void CallbackDealwith(String[] strings) {
              if (strings != null){
                result.success(strings);
              }
          }
      });
    } else if (call.method.equals("purchase")) {
      String name = call.argument("name");
      String currency = call.argument("currency");
      String value = call.argument("value");
      String contentType = call.argument("contentType");
      String contentIds = call.argument("contentIds");
      hm.Purchase(name, currency, value, contentType, Arrays.asList(contentIds.split(",")), new OutNetCallback() {
          @Override
          public void callbackDealwith(Object info) {

          }
      });
      result.success("");
    } else if (call.method.equals("eventPost")) {
      String name = call.argument("name");
      String currency = call.argument("currency");
      String value = call.argument("value");
      String contentType = call.argument("contentType");
      String contentIds = call.argument("contentIds");
      hm.EventPost("", name, currency, value, contentType, Arrays.asList(contentIds.split(",")), new OutNetCallback() {
          @Override
          public void callbackDealwith(Object info) {

          }
      });
      result.success("");
    } else if (call.method.equals("userDataUpdateEvent")) {
      String email = call.argument("email");
      String fbLoginId = call.argument("fbLoginId");
      String userId = call.argument("userId");
      String phone = call.argument("phone");
      hm.UserDataUpdate(email, fbLoginId, phone, new OutNetCallback() {
          @Override
          public void callbackDealwith(Object info) {

          }
      });
      result.success("");
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
