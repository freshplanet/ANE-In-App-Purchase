//
//  AirInAppPurchase.h
//  AirInAppPurchase
//
//  Created by Thibaut Crenn on 3/31/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
#import "FlashRuntimeExtensions.h"
#import "JSONKit.h"

@interface AirInAppPurchase : NSObject <SKPaymentTransactionObserver, SKProductsRequestDelegate>

- (void) sendRequest:(SKRequest*)request AndContext:(FREContext*)ctx;

@end

FREObject makePurchase(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject userCanMakeAPurchase(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject getProductsInfo(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);