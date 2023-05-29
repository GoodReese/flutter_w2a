//
//  HM_NetWork.m
//  HT_Test
//
//  Created by CCC on 2022/11/22.
//

#import "HM_NetWork.h"

@implementation HM_NetWork

+ (instancetype)shareInstance
{
    static HM_NetWork *_sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[HM_NetWork alloc] init];
        [_sharedInstance configure];
    });
    return _sharedInstance;
}

- (void)configure
{
    self.isEnableLog = false;
    self.requestURL = @"";
}

-(void) setLogEnabled:(BOOL) isEnable {
    self.isEnableLog = isEnable;
}

- (void)requestJsonPost:(NSString *)relativePath params:(NSDictionary *)params successBlock:(HSResponseSuccessBlock)successBlock failBlock:(HSResponseFailBlock)failBlock
{
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:nil];
    NSString *strJson = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    NSString *urlString = [NSString stringWithFormat:@"%@%@", self.requestURL, relativePath];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:urlString]];
    request.HTTPMethod = @"POST";
    request.HTTPBody = [NSJSONSerialization dataWithJSONObject:params options:NSJSONWritingPrettyPrinted error:nil];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    request.timeoutInterval = 30;
    NSURLSessionDataTask *dataTask = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            if (successBlock) {
                NSDictionary *responseObject = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
                if (self.isEnableLog) {
                    NSString *jsonStr = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                    if (jsonData.length > 0) {
                        NSLog(@"**************\n hm_event log \n\nurl:%@\n\nrequestBody:\n%@\n\nresponse:\n%@\n**************\n", relativePath, strJson, jsonStr);
                    }
                }
                if ([responseObject isKindOfClass:[NSDictionary class]]) {
                    successBlock(responseObject);
                }
            }
        } else {
            if (failBlock) {
                if (error) {
                    
                    NSLog(@"**************\n hm_event log\n\nurl:%@\n\nrequestBody:\n%@\n\nerror:\n%@\n \n**************\n", relativePath, strJson, error);
                    failBlock(error);
                }
            }
        }
    }];
    [dataTask resume];
}

@end
