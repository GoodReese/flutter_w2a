#import "FlutterW2aPlugin.h"
#import "hm.h"

@implementation FlutterW2aPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_w2a"
            binaryMessenger:[registrar messenger]];
  FlutterW2aPlugin* instance = [[FlutterW2aPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"useFingerPrinting" isEqualToString:call.method]) {
    BOOL isEnabled = [call.arguments[@"isEnabled"] boolValue];
    [hm useFingerPrinting: isEnabled];
    result(@"");
  } else if ([@"init" isEqualToString:call.method]) {
    NSString *gateway = call.arguments[@"gateway"];
    NSString *installEventName = call.arguments[@"installEventName"];
    NSLog(@"调用了初始化");
    [hm init: gateway InstallEventName: installEventName success:^(NSArray * _Nonnull array) {
      NSLog(@"初始化完毕");
      result(array);
    }];
  } else if ([@"purchase" isEqualToString:call.method]) {
    NSString *name = call.arguments[@"name"];
    NSString *currency = call.arguments[@"currency"];
    NSString *value = call.arguments[@"value"];
    NSString *contentType = call.arguments[@"contentType"];
    NSString *contentIds = call.arguments[@"contentIds"];
    [hm Purchase:name Currency:currency Value:value ContentType:contentType ContentIds:contentIds];
    result(@"");
  } else if ([@"eventPost" isEqualToString:call.method]) {
    NSString *name = call.arguments[@"name"];
    NSString *currency = call.arguments[@"currency"];
    NSString *value = call.arguments[@"value"];
    NSString *contentType = call.arguments[@"contentType"];
    NSString *contentIds = call.arguments[@"contentIds"];
    [hm EventPost:name Currency:currency Value:value ContentType:contentType ContentIds:contentIds];
    result(@"");
  } else if ([@"userDataUpdateEvent" isEqualToString:call.method]) {
    NSString *email = call.arguments[@"email"];
    NSString *fbLoginId = call.arguments[@"fbLoginId"];
    NSString *userId = call.arguments[@"userId"];
    NSString *phone = call.arguments[@"phone"];
    [hm UserDataUpdateEvent:email Fb_login_id:fbLoginId UserId:userId Phone:phone];
    result(@"");
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
