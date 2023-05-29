//
//  HM_Config.h
//  HT_Test
//
//  Created by CCC on 2022/12/2.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface HM_Config : NSObject

+(instancetype) sharedManager;

-(void) saveDeviceID;

-(void) saveBaseInfo;

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString;

@end

NS_ASSUME_NONNULL_END
