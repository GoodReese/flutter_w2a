import 'flutter_w2a_platform_interface.dart';

class FlutterW2a {
  Future<String?> getPlatformVersion() {
    return FlutterW2aPlatform.instance.getPlatformVersion();
  }

  void useFingerPrinting(bool isEnabled) {
    FlutterW2aPlatform.instance.useFingerPrinting(isEnabled);
  }

  Future<List> init(String gateWay, String installEventName) async {
    return FlutterW2aPlatform.instance.init(gateWay, installEventName);
  }

  void purchase(String name, String currency, String value, String contentType,
      String contentIds) {
    FlutterW2aPlatform.instance
        .purchase(name, currency, value, contentType, contentIds);
  }

  void eventPost(String name, String currency, String value, String contentType,
      String contentIds) {
    FlutterW2aPlatform.instance
        .eventPost(name, currency, value, contentType, contentIds);
  }

  void userDataUpdateEvent(
    String email,
    String fbLoginId,
    String userId,
    String phone,
  ) {
    FlutterW2aPlatform.instance
        .userDataUpdateEvent(email, fbLoginId, userId, phone);
  }
}
