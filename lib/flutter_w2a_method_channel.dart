import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_w2a_platform_interface.dart';

/// An implementation of [FlutterW2aPlatform] that uses method channels.
class MethodChannelFlutterW2a extends FlutterW2aPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_w2a');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  void useFingerPrinting(bool isEnabled) {
    methodChannel.invokeMethod("useFingerPrinting", {"isEnabled": isEnabled});
  }

  @override
  Future<List> init(String gateWay, String installEventName) async {
    return await methodChannel.invokeMethod(
        "init", {"gateWay": gateWay, "installEventName": installEventName});
  }

  @override
  void purchase(String name, String currency, String value, String contentType,
      String contentIds) {
    methodChannel.invokeMethod("purchase", {
      "name": name,
      "currency": currency,
      "value": value,
      "contentType": contentType,
      "contentIds": contentIds,
    });
  }

  @override
  void eventPost(String name, String currency, String value, String contentType,
      String contentIds) {
    methodChannel.invokeMethod("eventPost", {
      "name": name,
      "currency": currency,
      "value": value,
      "contentType": contentType,
      "contentIds": contentIds,
    });
  }

  @override
  void userDataUpdateEvent(
      String email, String fbLoginId, String userId, String phone) {
    methodChannel.invokeMethod("userDataUpdateEvent", {
      "email": email,
      "fbLoginId": fbLoginId,
      "userId": userId,
      "phone": phone,
    });
  }
}
