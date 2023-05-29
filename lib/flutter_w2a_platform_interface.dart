import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_w2a_method_channel.dart';

abstract class FlutterW2aPlatform extends PlatformInterface {
  /// Constructs a FlutterW2aPlatform.
  FlutterW2aPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterW2aPlatform _instance = MethodChannelFlutterW2a();

  /// The default instance of [FlutterW2aPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterW2a].
  static FlutterW2aPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterW2aPlatform] when
  /// they register themselves.
  static set instance(FlutterW2aPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  void useFingerPrinting(bool isEnabled) {
    throw UnimplementedError('useFingerPrinting() has not been implemented.');
  }

  Future<List> init(String gateWay, String installEventName) {
    throw UnimplementedError('useFingerPrinting() has not been implemented.');
  }

  void purchase(String name, String currency, String value, String contentType,
      String contentIds) {
    throw UnimplementedError('useFingerPrinting() has not been implemented.');
  }

  void eventPost(String name, String currency, String value, String contentType,
      String contentIds) {
    throw UnimplementedError('useFingerPrinting() has not been implemented.');
  }

  void userDataUpdateEvent(
      String email, String fbLoginId, String userId, String phone) {
    throw UnimplementedError('useFingerPrinting() has not been implemented.');
  }
}
