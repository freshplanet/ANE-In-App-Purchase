//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
#import "FlashRuntimeExtensions.h"
#import "JSONKit.h"

@interface AirInAppPurchase : NSObject <SKPaymentTransactionObserver, SKProductsRequestDelegate>

- (void) sendRequest:(SKRequest*)request AndContext:(FREContext*)ctx;
- (void) completeTransaction:(SKPaymentTransaction*)transaction;
- (void) failedTransaction:(SKPaymentTransaction*)transaction;
- (void) purchasingTransaction:(SKPaymentTransaction*)transaction;
- (void) restoreTransaction:(SKPaymentTransaction*)transaction;
@end

FREObject AirInAppPurchaseInit(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);
FREObject makePurchase(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);
FREObject userCanMakeAPurchase(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);
FREObject getProductsInfo(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);
FREObject removePurchaseFromQueue(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);
FREObject restoreTransaction(FREContext context, void* functionData, uint32_t argc, FREObject argv[]);