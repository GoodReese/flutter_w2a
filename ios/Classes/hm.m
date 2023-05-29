//
//  HM.m
//  HM
//
//  Created by CCC on 2022/12/2.
//

#import "hm.h"
#import "HM_NetWork.h"
#import "HM_Config.h"
#import "GetWebViewInfo.h"

@implementation hm

+ (void)init:(NSString *)Gateway InstallEventName:(NSString *)InstallEventName {
    if (Gateway.length < 1) {
        return;
    }
    [[HM_Config sharedManager] saveDeviceID];
    [[HM_Config sharedManager] saveBaseInfo];

    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
//    [userDefaults setObject:Gateway.length > 0 ? Gateway : @"https://capi.bi4sight.com" forKey:@"HM_Gateway"];// 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
    [userDefaults setObject:Gateway forKey:@"HM_Gateway"];// 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
    [userDefaults setObject:InstallEventName.length > 0 ? InstallEventName : @"CompleteRegistration" forKey:@"HM_InstallEventName"];// 完成注册的事件名称，如果不传默认为：CompleteRegistration
    NSString *isFirst = [userDefaults objectForKey:@"HM_isFirstInsert"];
    if ([isFirst isEqual: @"0"]) { //  不是第一次安装
        NSString *HM_W2a_Data = [userDefaults objectForKey:@"HM_W2a_Data"];
        if (HM_W2a_Data.length > 0) { // 是web2App用户
            [hm updataInfo];
            [hm requestErrorPurchaseEvent];
            [hm requestErrorEventPost];
        } // 不是则无操作
    } else { //  第一次安装
        [userDefaults setObject:@"0" forKey:@"HM_isFirstInsert"];
        NSString *copyString = [[UIPasteboard generalPasteboard] string];
        if (copyString.length > 0) { //
            NSString *preStr = @"w2a_data:";
            BOOL result = [copyString hasPrefix:preStr];
            if (result) {// 剪切板有包含w2a_data:开头的数据
                [userDefaults setObject:copyString forKey:@"HM_W2a_Data"];
                // 调用网关【新装API】，并将获取到的adv_data[]写入本地
                [hm requestNewUser];
            } else { // 无符合条件数据
//                [hm reuqestRegisterInfo];
                [hm getWebViewInfo];
            }
        } else {
            [hm getWebViewInfo];
//            [hm reuqestRegisterInfo];
        }
    }
    [userDefaults synchronize];
    
}

+ (void)getWebViewInfo {
    [[GetWebViewInfo shared] creatWebView:^(NSString * _Nonnull string) {
//        NSLog(@"callBack: %@", string);
        [hm reuqestRegisterInfo];
    }];
}

//MARK: 调用网关 【落地页信息读取API】，判断是否是落地页用户
+ (void)reuqestRegisterInfo {
    /*  调用网关 【落地页信息读取API】，判断是否是落地页用户
        读取：存本地，以后调别的API都要用到该信息
        读取：调用网关【新装API】，并将获取到的adv_data[]写入本地
        读不到：标记非新安装（写本地信息）
    */
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/landingpageread", Gateway];
    NSMutableDictionary * dic = [NSMutableDictionary dictionaryWithDictionary:@{}];
    NSString *jsonString = [userDefaults objectForKey:@"HM_WebView_Fingerprint"];
    if (jsonString.length > 0) {
        NSDictionary *d = [[HM_Config sharedManager] dictionaryWithJsonString:jsonString];
        [dic setObject:[d objectForKey:@"ca"] forKey:@"ca"];
        [dic setObject:[d objectForKey:@"wg"] forKey:@"wg"];
        [dic setObject:[d objectForKey:@"pi"] forKey:@"pi"];
        [dic setObject:[d objectForKey:@"ao"] forKey:@"ao"];
        [dic setObject:[d objectForKey:@"se"] forKey:@"se"];
        [dic setObject:[d objectForKey:@"ft"] forKey:@"ft"];
    }
    
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSString *w2a_data_encrypt = data[@"w2a_data_encrypt"];
            [userDefaults setObject:w2a_data_encrypt forKey:@"HM_W2a_Data"];
            [userDefaults synchronize];
            [hm requestNewUser];
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];
}

//MARK: 调用网关【新装API】，并将获取到的adv_data[]写入本地
+ (void)requestNewUser {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/oninstall", Gateway];
    NSDictionary *device_info = [userDefaults objectForKey:@"HM_Device_Info"];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSString *event_name = [userDefaults objectForKey:@"HM_InstallEventName"];
    NSDictionary *dic = @{@"device_info" : device_info,
                          @"device_id" : device_id,
                          @"event_name" : event_name,
                          @"w2a_data_encrypt" : w2a_data_encrypt};
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSArray *adv_data = data[@"adv_data"];
            [userDefaults setObject:adv_data forKey:@"HM_Adv_Data"];
            [userDefaults synchronize];
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];

}

//MARK: 调用网关【会话API】上报信息
+ (void)updataInfo {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/onsession", Gateway];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    [[HM_NetWork shareInstance] requestJsonPost:url params:@{@"device_id" : device_id, @"w2a_data_encrypt" : w2a_data_encrypt} successBlock:^(NSDictionary * _Nonnull responseObject) {
        
    } failBlock:^(NSError * _Nonnull error) {
        
    }];
    
}

//MARK: 调用【网关购物API】，上报&由网关转发购物事件
+ (void)Purchase:(NSString *) nameStr Currency : (NSString *) usdStr Value : (NSString *) valueStr ContentType : (NSString *) typeStr ContentIds : (NSString *) idsStr{
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/onpurchase", Gateway];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSMutableDictionary *custom_data = [NSMutableDictionary dictionaryWithDictionary:@{
        @"event_name" : nameStr.length > 0 ? nameStr : @"Purchase",
        @"currency" : usdStr.length > 0 ? usdStr : @"USD",
        @"value" : valueStr.length > 0 ? valueStr : @"0.00"
    }];
    if (typeStr.length > 0) {
        [custom_data setObject:typeStr forKey:@"content_type"];
        if (idsStr.length > 0) {
            NSArray *idsArray = [idsStr componentsSeparatedByString:@","];
            if (idsArray.count > 0) {
                [custom_data setObject:idsArray forKey:@"content_ids"];
            }
        }
    }
    [[HM_NetWork shareInstance] requestJsonPost:url params:@{@"device_id" : device_id, @"w2a_data_encrypt" : w2a_data_encrypt, @"custom_data" : [NSDictionary dictionaryWithDictionary:custom_data]} successBlock:^(NSDictionary * _Nonnull responseObject) {
        
    } failBlock:^(NSError * _Nonnull error) {
        if (error.code == -1001) {
            [hm saveErrorPurchaseEvent:custom_data];
        }
    }];
}

//MARK: 调用网关【EventPost（转发API）】，上报&由网关转发自定义事件
+ (void)EventPost:(NSString *) nameStr Currency : (NSString *) usdStr Value : (NSString *) valueStr ContentType : (NSString *) typeStr ContentIds : (NSString *) idsStr{
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/eventpost", Gateway];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSMutableDictionary *custom_data = [NSMutableDictionary dictionaryWithDictionary:@{
        @"event_name" : nameStr.length > 0 ? nameStr : @"Purchase",
        @"currency" : usdStr.length > 0 ? usdStr : @"USD",
        @"value" : valueStr.length > 0 ? valueStr : @"0.00"
    }];
    if (typeStr.length > 0) {
        [custom_data setObject:typeStr forKey:@"content_type"];
    }
    if (idsStr.length > 0) {
        NSArray *idsArray = [idsStr componentsSeparatedByString:@","];
        if (idsArray.count > 0) {
            [custom_data setObject:idsArray forKey:@"content_ids"];
        }
    }
    [[HM_NetWork shareInstance] requestJsonPost:url params:@{@"device_id" : device_id, @"w2a_data_encrypt" : w2a_data_encrypt, @"custom_data" : [NSDictionary dictionaryWithDictionary:custom_data], @"event_name" : nameStr.length > 0 ? nameStr : @"Purchase"} successBlock:^(NSDictionary * _Nonnull responseObject) {
        
    } failBlock:^(NSError * _Nonnull error) {
        if (error.code == -1001) {
            [hm saveErrorEventPost:custom_data];
        }
    }];
}

//MARK: 调用网关【UserDataUpdate（用户信息更新API）】
+ (void)UserDataUpdateEvent:(NSString *) emStr Fb_login_id : (NSString *) fbStr UserId : (NSString *) idStr Phone : (NSString *) phStr{
    NSString *em = emStr;
    NSString *fb_login_id = fbStr;
    NSString *external_id = idStr;
    NSString *ph = phStr;
    
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/userdataupdate", Gateway];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSDictionary *dic = @{@"device_id" : device_id,
                          @"w2a_data_encrypt" : w2a_data_encrypt,
                          @"em" : em,
                          @"fb_login_id" : fb_login_id,
                          @"external_id" : external_id,
                          @"ph" : ph
    };
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSString *w2a_data_encrypt = data[@"w2a_data_encrypt"];
            [userDefaults setObject:w2a_data_encrypt forKey:@"HM_W2a_Data"];
            [userDefaults synchronize];
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];
}

 
+(NSArray *)AdvDataRead {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return @[];
    }
    NSArray *adv_Data = [userDefaults objectForKey:@"HM_Adv_Data"];
    return adv_Data.count > 0 ? adv_Data : @[];
}

+(void)setLogEnabled:(BOOL)isEnable {
    [[HM_NetWork shareInstance] setLogEnabled:isEnable];
}

+(void) requestErrorPurchaseEvent {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSArray *array = [userDefaults objectForKey:@"HM_Erroe_Purchase"];
    if (array.count > 0) {
        NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
        if (w2a_data_encrypt.length < 1){
            return;
        }
        NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
        if (Gateway.length < 1) {
            return;
        }
        NSString *url = [NSString stringWithFormat:@"%@/onpurchase", Gateway];
        NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
        NSDictionary *dic = array[0];
        NSMutableArray *mArray = [NSMutableArray arrayWithArray:array];
        [mArray removeObjectAtIndex:0];
        [userDefaults setObject:mArray forKey:@"HM_Erroe_Purchase"];
        [userDefaults synchronize];
        [[HM_NetWork shareInstance] requestJsonPost:url params:@{@"device_id" : device_id, @"w2a_data_encrypt" : w2a_data_encrypt, @"custom_data" : [NSDictionary dictionaryWithDictionary:dic]} successBlock:^(NSDictionary * _Nonnull responseObject) {
            [hm requestErrorPurchaseEvent];
        } failBlock:^(NSError * _Nonnull error) {
            if (error.code == -1001) {
                [hm saveErrorPurchaseEvent:dic];
            }
        }];
    }
    
}

+(void) saveErrorPurchaseEvent:(NSDictionary *) custom_data {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSMutableArray * mArray = [NSMutableArray arrayWithArray: [userDefaults objectForKey:@"HM_Erroe_Purchase"]];
    [mArray addObject:custom_data];
    [userDefaults setObject:mArray forKey:@"HM_Erroe_Purchase"];
    [userDefaults synchronize];
}

+(void) requestErrorEventPost {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSArray *array = [userDefaults objectForKey:@"HM_Erroe_EventPost"];
    if (array.count > 0) {
        NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
        if (w2a_data_encrypt.length < 1){
            return;
        }
        NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
        if (Gateway.length < 1) {
            return;
        }
        NSString *url = [NSString stringWithFormat:@"%@/eventpost", Gateway];
        NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
        NSDictionary *dic = array[0];
        NSString *nameStr = dic[@"event_name"];

        NSMutableArray *mArray = [NSMutableArray arrayWithArray:array];
        [mArray removeObjectAtIndex:0];
        [userDefaults setObject:mArray forKey:@"HM_Erroe_EventPost"];
        [userDefaults synchronize];

        [[HM_NetWork shareInstance] requestJsonPost:url params:@{@"device_id" : device_id, @"w2a_data_encrypt" : w2a_data_encrypt, @"custom_data" : [NSDictionary dictionaryWithDictionary:dic], @"event_name" : nameStr.length > 0 ? nameStr : @"Purchase"} successBlock:^(NSDictionary * _Nonnull responseObject) {
            [hm requestErrorEventPost];
        } failBlock:^(NSError * _Nonnull error) {
            if (error.code == -1001) {
                [hm saveErrorEventPost:dic];
            }
        }];
    }

}

+(void) saveErrorEventPost:(NSDictionary *) custom_data {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSMutableArray * mArray = [NSMutableArray arrayWithArray: [userDefaults objectForKey:@"HM_Erroe_EventPost"]];
    [mArray addObject:custom_data];
    [userDefaults setObject:mArray forKey:@"HM_Erroe_EventPost"];
    [userDefaults synchronize];
}


//MARK: 调用网关【UserDataUpdate（用户信息更新API）】--扩展
+ (void)UserDataUpdateEvent:(NSString *) emStr Fb_login_id : (NSString *) fbStr UserId : (NSString *) idStr Phone : (NSString *) phStr Zipcode : (NSString *) zipcodeStr City : (NSString *) cityStr State : (NSString *) stateStr Gender : (NSString *) genderStr Fn : (NSString *) fnStr Ln : (NSString *) lnStr DateBirth : (NSString *) dateBirthStr Country : (NSString *) countryStr {
    
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/userdataupdate", Gateway];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSDictionary *dic = @{@"device_id" : device_id,
                          @"w2a_data_encrypt" : w2a_data_encrypt,
                          @"em" : emStr,
                          @"fb_login_id" : fbStr,
                          @"external_id" : idStr,
                          @"ph" : phStr,
                          @"zp" : zipcodeStr,
                          @"ct" : cityStr,
                          @"st" : stateStr,
                          @"ge" : genderStr,
                          @"fn" : fnStr,
                          @"ln" : lnStr,
                          @"db" : dateBirthStr,
                          @"country" : countryStr,
    };
    
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSString *w2a_data_encrypt = data[@"w2a_data_encrypt"];
            [userDefaults setObject:w2a_data_encrypt forKey:@"HM_W2a_Data"];
            [userDefaults synchronize];
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];
}


+ (void)init:(NSString *)Gateway InstallEventName:(NSString *)InstallEventName success : (void(^)(NSArray * array))successBlock {
    if (Gateway.length < 1) {
        return;
    }
    [[HM_Config sharedManager] saveDeviceID];
    [[HM_Config sharedManager] saveBaseInfo];

    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
//    [userDefaults setObject:Gateway.length > 0 ? Gateway : @"https://capi.bi4sight.com" forKey:@"HM_Gateway"];// 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
    [userDefaults setObject:Gateway forKey:@"HM_Gateway"];// 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
    [userDefaults setObject:InstallEventName.length > 0 ? InstallEventName : @"CompleteRegistration" forKey:@"HM_InstallEventName"];// 完成注册的事件名称，如果不传默认为：CompleteRegistration
    NSString *isFirst = [userDefaults objectForKey:@"HM_isFirstInsert"];
    if ([isFirst isEqual: @"0"]) { //  不是第一次安装
        NSString *HM_W2a_Data = [userDefaults objectForKey:@"HM_W2a_Data"];
        if (HM_W2a_Data.length > 0) { // 是web2App用户
            [hm updataInfo];
            [hm requestErrorPurchaseEvent];
            [hm requestErrorEventPost];
            successBlock([hm AdvDataRead]);
        } // 不是则无操作
    } else { //  第一次安装
        [userDefaults setObject:@"0" forKey:@"HM_isFirstInsert"];
        BOOL isNeedRequest = true;
        NSString *copyString = [[UIPasteboard generalPasteboard] string];
        if (copyString.length > 0) { //
            NSString *preStr = @"w2a_data:";
            BOOL result = [copyString hasPrefix:preStr];
            if (result) {// 剪切板有包含w2a_data:开头的数据
                [userDefaults setObject:copyString forKey:@"HM_W2a_Data"];
                // 调用网关【新装API】，并将获取到的adv_data[]写入本地
                isNeedRequest = false;
                [hm requestNewUser:^(NSArray *array) {
                    successBlock(array);
                }];
            } else { // 无符合条件数据
                isNeedRequest = false;
                [hm getWebViewInfo:^(NSArray *array) {
                    successBlock(array);
                }];
            }
        } else {
            isNeedRequest = false;
            [hm getWebViewInfo:^(NSArray *array) {
                successBlock(array);
            }];
        }
        if (isNeedRequest) {
            [hm getWebViewInfo:^(NSArray *array) {
                successBlock(array);
            }];
        }
    }
    [userDefaults synchronize];
}

+ (void)getWebViewInfo : (void(^)(NSArray * array))block  {
    [[GetWebViewInfo shared] creatWebView:^(NSString * _Nonnull string) {
        [hm reuqestRegisterInfo:^(NSArray *array) {
            block(array);
        }];
    }];
}


//MARK: 调用网关 【落地页信息读取API】，判断是否是落地页用户 -- 带block
+ (void)reuqestRegisterInfo : (void(^)(NSArray * array))block {
    /*  调用网关 【落地页信息读取API】，判断是否是落地页用户
        读取：存本地，以后调别的API都要用到该信息
        读取：调用网关【新装API】，并将获取到的adv_data[]写入本地
        读不到：标记非新安装（写本地信息）
    */
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/landingpageread", Gateway];
    NSMutableDictionary * dic = [NSMutableDictionary dictionaryWithDictionary:@{}];
    NSString *jsonString = [userDefaults objectForKey:@"HM_WebView_Fingerprint"];
    if (jsonString.length > 0) {
        NSDictionary *d = [[HM_Config sharedManager] dictionaryWithJsonString:jsonString];
        [dic setObject:[d objectForKey:@"ca"] forKey:@"ca"];
        [dic setObject:[d objectForKey:@"wg"] forKey:@"wg"];
        [dic setObject:[d objectForKey:@"pi"] forKey:@"pi"];
        [dic setObject:[d objectForKey:@"ao"] forKey:@"ao"];
        [dic setObject:[d objectForKey:@"se"] forKey:@"se"];
        [dic setObject:[d objectForKey:@"ft"] forKey:@"ft"];
    }
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSString *w2a_data_encrypt = data[@"w2a_data_encrypt"];
            [userDefaults setObject:w2a_data_encrypt forKey:@"HM_W2a_Data"];
            [userDefaults synchronize];
            [hm requestNewUser:^(NSArray *array) {
                block(array);
            }];
        } else {
            block(@[]);
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];
}

//MARK: 调用网关【新装API】，并将获取到的adv_data[]写入本地 -- 带block
+ (void)requestNewUser : (void(^)(NSArray * array))block {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *w2a_data_encrypt = [userDefaults objectForKey:@"HM_W2a_Data"];
    if (w2a_data_encrypt.length < 1){
        return;
    }
    NSString *Gateway = [userDefaults objectForKey:@"HM_Gateway"];
    if (Gateway.length < 1) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/oninstall", Gateway];
    NSDictionary *device_info = [userDefaults objectForKey:@"HM_Device_Info"];
    NSDictionary *device_id = [userDefaults objectForKey:@"HM_Device_Id"];
    NSString *event_name = [userDefaults objectForKey:@"HM_InstallEventName"];
    NSDictionary *dic = @{@"device_info" : device_info,
                          @"device_id" : device_id,
                          @"event_name" : event_name,
                          @"w2a_data_encrypt" : w2a_data_encrypt};
    [[HM_NetWork shareInstance] requestJsonPost:url params:dic successBlock:^(NSDictionary * _Nonnull responseObject) {
        NSString *code = [responseObject[@"code"] stringValue];
        if ([code isEqual: @"0"]) {
            NSDictionary *data = responseObject[@"data"];
            NSArray *adv_data = data[@"adv_data"];
            [userDefaults setObject:adv_data forKey:@"HM_Adv_Data"];
            [userDefaults synchronize];
            block(adv_data);
        } else {
            block(@[]);
        }
    } failBlock:^(NSError * _Nonnull error) {
        
    }];

}


+(void)useFingerPrinting:(BOOL)isEnable {
    [[GetWebViewInfo shared] useFingerPrinting:isEnable];
}


+(NSString *) GetW2AEncrypt {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *HM_W2a_Data = [userDefaults objectForKey:@"HM_W2a_Data"];
    return  HM_W2a_Data;
}

+(void) SetW2AEncrypt : (NSString *) w2a_data {
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    [userDefaults setObject:w2a_data forKey:@"HM_W2a_Data"];
    [userDefaults synchronize];
}

@end
