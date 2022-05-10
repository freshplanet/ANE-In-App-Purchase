/**
 * Copyright 2017 FreshPlanet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "AirInAppPurchase.h"

#define DEFINE_ANE_FUNCTION(fn) FREObject fn(FREContext context, void* functionData, uint32_t argc, FREObject argv[])
#define MAP_FUNCTION(fn, data) { (const uint8_t*)(#fn), (data), &(fn) }

@implementation AirInAppPurchase

static NSString * _purchasingProductId = nil;
static SKPayment * _promotionPayment = nil;


- (id) initWithContext:(FREContext)extensionContext {
    
    if (self = [super init])
        _context = extensionContext;
    
    return self;
}

- (void) sendLog:(NSString*)log {
    [self sendEvent:@"log" level:log];
}

- (void) sendEvent:(NSString*)code {
    [self sendEvent:code level:@""];
}

- (void) sendEvent:(NSString*)code level:(NSString*)level {
    FREDispatchStatusEventAsync(_context, (const uint8_t*)[code UTF8String], (const uint8_t*)[level UTF8String]);
}

- (NSString*) jsonStringFromData:(id)data {
    
    NSError* error;
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:&error];
    NSString* jsonString = nil;
    
    if (jsonData != nil)
        jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    return jsonString;
}

- (void) registerObserver {
    
    [self sendEvent:@"DEBUG" level:@"registerObserver"];
    [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    
    // We check if there is any purchase not completed here.
    // seems to be ios9 bug
    NSUInteger nbTransaction = [[SKPaymentQueue defaultQueue].transactions count];
    
    if (nbTransaction > 0) {
        
        NSArray* transactions = [SKPaymentQueue defaultQueue].transactions;
        for (SKPaymentTransaction* transaction in transactions) {
            
            switch (transaction.transactionState) {
                case SKPaymentTransactionStatePurchased:
                    [self completeTransaction:transaction];
                    break;
                default:
                    [self sendEvent:@"PURCHASE_UNKNOWN" level:@"Unknown Reason"];
                    break;
            }
        }
    }
}

// get products info
- (void) sendRequest:(SKRequest*)request andContext:(FREContext*)ctx {
    
    request.delegate = self;
    [request start];
}

// on product info received
- (void) productsRequest:(SKProductsRequest*)request didReceiveResponse:(SKProductsResponse*)response {
 
    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *productElement = [[NSMutableDictionary alloc] init];
    
    _iapProducts = [[NSMutableDictionary alloc] init];
    
    for (SKProduct* product in [response products]) {
        
        [_iapProducts setValue:product forKey:product.productIdentifier];
        
        NSMutableDictionary *details = [[NSMutableDictionary alloc] init];
        [numberFormatter setLocale:product.priceLocale];
        [details setValue: [numberFormatter stringFromNumber:product.price] forKey:@"price"];
        [details setValue: product.localizedTitle forKey:@"title"];
        [details setValue: product.localizedDescription forKey:@"description"];
        [details setValue: product.productIdentifier forKey:@"productId"];
        [details setValue: [numberFormatter currencyCode] forKey:@"price_currency_code"];
        [details setValue: [numberFormatter currencySymbol] forKey:@"price_currency_symbol"];
        [details setValue: product.price forKey:@"value"];
        
        if (@available(macOS 10.14.4, ios 12.2, *))
        {
            if (product.discounts != nil && product.discounts.count > 0) {
                NSMutableArray *discounts = [[NSMutableArray alloc] init];
                for (SKProductDiscount* discount in product.discounts) {
                    NSMutableDictionary *discountElement = [[NSMutableDictionary alloc] init];
                    [discountElement setValue: discount.identifier forKey:@"productId"];
                    [discountElement setValue: [numberFormatter stringFromNumber:discount.price] forKey:@"price"];
                    [discounts addObject:discountElement];
                }
                [details setValue:discounts forKey:@"discounts"];
            }
        }
        
        [productElement setObject:details forKey:product.productIdentifier];
        
    }
    
    [dictionary setObject:productElement forKey:@"details"];
    
    if ([response invalidProductIdentifiers] != nil && [[response invalidProductIdentifiers] count] > 0) {
        
        NSString* jsonArray = [self jsonStringFromData:[response invalidProductIdentifiers]];
        [self sendEvent:@"PRODUCT_INFO_ERROR" level:jsonArray];
    }
    else if ([NSJSONSerialization isValidJSONObject:dictionary]) {
        
        NSData *json;
        NSError *error = nil;
        
        // Serialize the dictionary
        json = [NSJSONSerialization dataWithJSONObject:dictionary options:NSJSONWritingPrettyPrinted error:&error];
        
        // If no errors, let's return the JSON
        if (json != nil && error == nil) {
            
            NSString *jsonDictionary = [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding];
            [self sendEvent:@"PRODUCT_INFO_RECEIVED" level:jsonDictionary];
        }
    }
}

// on product info finish
- (void) requestDidFinish:(SKRequest*)request {
    [self sendEvent:@"DEBUG" level:@"requestDidFinish"];
}

// on product info error
- (void)request:(SKRequest*)request didFailWithError:(NSError*)error {
    
    [self sendEvent:@"DEBUG" level:@"requestDidFailWithError"];
    [self sendEvent:@"PRODUCT_INFO_ERROR" level:[error debugDescription]];
}

// complete a transaction (item has been purchased, need to check the receipt)
- (void) completeTransaction:(SKPaymentTransaction*)transaction {
    NSString* jsonString = [self getJsonForTransaction:transaction];
    [self sendEvent:@"PURCHASE_SUCCESSFUL" level:jsonString];
}

// transaction failed, remove the transaction from the queue.
- (void) failedTransaction:(SKPaymentTransaction*)transaction {
    
    [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"failedTransaction"]];
    // purchase failed
    NSMutableDictionary* data;

    [[transaction payment] productIdentifier];
    [[transaction error] code];
    
    data = [[NSMutableDictionary alloc] init];
    [data setValue:[NSNumber numberWithInteger:[[transaction error] code]]  forKey:@"code"];
    [data setValue:[[transaction error] localizedFailureReason] forKey:@"FailureReason"];
    [data setValue:[[transaction error] localizedDescription] forKey:@"FailureDescription"];
    [data setValue:[[transaction error] localizedRecoverySuggestion] forKey:@"RecoverySuggestion"];
    
    NSString* jsonString = [self jsonStringFromData:data];
    NSString* error = transaction.error.code == SKErrorPaymentCancelled ? @"RESULT_USER_CANCELED" : jsonString;
    
    // conclude the transaction
     @try {
        if ([transaction transactionState] != SKPaymentTransactionStatePurchasing) {
               [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        }
    }
     @catch (NSException *e) {
         [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"Error in failedTransaction: %@", e]];
     }
    
    // dispatch event
    [self sendEvent:@"PURCHASE_ERROR" level:error];
}

- (NSMutableDictionary *) getDataForTransaction: (SKPaymentTransaction*) transaction {
    NSMutableDictionary *data;
    data = [[NSMutableDictionary alloc] init];
    [data setValue:[[transaction payment] productIdentifier] forKey:@"productId"];
    [data setValue:[transaction transactionIdentifier] forKey:@"transactionId"];
    
    NSString* receiptString = nil;
    #if TARGET_OS_IPHONE
        receiptString = [[NSString alloc] initWithData:transaction.transactionReceipt encoding:NSUTF8StringEncoding];
    #elif TARGET_OS_OSX
        NSData *receiptData = [NSData dataWithContentsOfURL:[[NSBundle mainBundle] appStoreReceiptURL]];
        receiptString = [receiptData base64EncodedStringWithOptions:0];
    #endif
    
    [data setValue:receiptString forKey:@"receipt"];
    [data setValue:@"AppStore"   forKey:@"receiptType"];
    [data setValue:[NSString stringWithFormat: @"%f", [transaction.transactionDate timeIntervalSince1970]] forKey:@"timestamp"];
    
    return data;
}

- (NSString *) getJsonForTransaction: (SKPaymentTransaction*) transaction {
    NSMutableDictionary *data = [self getDataForTransaction:transaction];
    NSString* jsonString = [self jsonStringFromData:data];
    return jsonString;
}

// transaction is being purchasing, logging the info.
- (void) purchasingTransaction:(SKPaymentTransaction*)transaction {
    
    // purchasing transaction
    // dispatch event
    [self sendEvent:@"PURCHASING" level:[[transaction payment] productIdentifier]];
}

// transaction restored, remove the transaction from the queue.
- (void) restoreTransaction:(SKPaymentTransaction*)transaction {
    
    // transaction restored
    // dispatch event
    NSString* jsonString = [self getJsonForTransaction:transaction];
    [self sendEvent:@"TRANSACTION_RESTORED" level:jsonString];
    
    // conclude the transaction
    @try {
        if ([transaction transactionState] != SKPaymentTransactionStatePurchasing) {
               [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        }
    }
     @catch (NSException *e) {
         [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"Error in restoreTransaction: %@", e]];
     }
}


// list of transactions has been updated.
- (void) paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray*)transactions {
    BOOL isMac = false;
    #if TARGET_OS_OSX
        isMac = true;
    #endif
    
    [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated"]];
    NSUInteger nbTransaction = [transactions count];
    NSString* pendingTransactionInformation = [NSString stringWithFormat:@"pending transaction - %@", [NSNumber numberWithUnsignedInteger:nbTransaction]];
    [self sendEvent:@"UPDATED_TRANSACTIONS" level:pendingTransactionInformation];

    for (SKPaymentTransaction* transaction in transactions) {
        switch (transaction.transactionState) {
            case SKPaymentTransactionStatePurchased:
                [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated PURCHASED"]];
                if(!isMac || (isMac && _purchasingProductId && [transaction.payment.productIdentifier isEqualToString:_purchasingProductId] )) {
                    _purchasingProductId = nil;
                    [self completeTransaction:transaction];
                }
                break;
            case SKPaymentTransactionStateFailed:
                [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated FAILED"]];
                if(!isMac || (isMac && _purchasingProductId && [transaction.payment.productIdentifier isEqualToString:_purchasingProductId] )) {
                    _purchasingProductId = nil;
                    [self failedTransaction:transaction];
                }
                break;
            case SKPaymentTransactionStatePurchasing:
                [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated PURCHASING"]];
                if(!isMac || (isMac && _purchasingProductId && [transaction.payment.productIdentifier isEqualToString:_purchasingProductId] )) {
                    [self purchasingTransaction:transaction];
                }
                
                break;
            case SKPaymentTransactionStateRestored:
                [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated RESTORED"]];
                if(!isMac || (isMac && _purchasingProductId && [transaction.payment.productIdentifier isEqualToString:_purchasingProductId] )) {
                    _purchasingProductId = nil;
                    [self restoreTransaction:transaction];
                }
                break;
            default:
                [self sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"transactions updated UNKNOWN"]];
                [self sendEvent:@"PURCHASE_UNKNOWN" level:@"Unknown Reason"];
                break;
        }
    }
}

- (void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue*)queue {
    NSMutableArray * purchases = [[NSMutableArray alloc] init];
    for (SKPaymentTransaction* transaction in [queue transactions]) {
        [purchases addObject:[self getDataForTransaction:transaction]];
    }
    NSMutableDictionary * toReturn = [[NSMutableDictionary alloc] init];
    [toReturn setValue:purchases forKey:@"purchases"];
    [self sendEvent:@"RESTORE_INFO_RECEIVED" level:[self jsonStringFromData:toReturn]];
}

- (void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error {
    [self sendEvent:@"DEBUG" level:@"restoreFailed"];
    [self sendEvent:@"RESTORE_INFO_ERROR" level:[error description]];
}

- (void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray*)transactions {
    [self sendEvent:@"DEBUG" level:@"removeTransaction"];
}

- (BOOL)paymentQueue:(SKPaymentQueue *)queue shouldAddStorePayment:(SKPayment *)payment forProduct:(SKProduct *)product {
    _promotionPayment = payment;
    return false;
}
@end

AirInAppPurchase* getAirInAppPurchaseContextNativeData(FREContext context) {
    
    CFTypeRef controller;
    FREGetContextNativeData(context, (void**)&controller);
    return (__bridge AirInAppPurchase*)controller;
}

DEFINE_ANE_FUNCTION(AirInAppPurchaseInit) {
    
    AirInAppPurchase* controller = [[AirInAppPurchase alloc] initWithContext:context];
    FRESetContextNativeData(context, (void*)CFBridgingRetain(controller));
    
    [controller registerObserver];
    
    return nil;
}

DEFINE_ANE_FUNCTION(makeSubscription) {
    
    AirInAppPurchase* controller = getAirInAppPurchaseContextNativeData(context);
    
    if (!controller)
        return nil;
    
    uint32_t stringLength;
    const uint8_t* string1;
    
    if (FREGetObjectAsUTF8(argv[0], &stringLength, &string1) != FRE_OK)
        return nil;
   
    
    
    
    _purchasingProductId = [NSString stringWithUTF8String:(char*)string1];

    SKProduct *product = [controller.iapProducts valueForKey:_purchasingProductId];
    if(!product) {
        [controller sendEvent:@"PURCHASE_ERROR" level:@"Unknow product id"];
        return nil;
    }

    SKMutablePayment* payment = [SKMutablePayment paymentWithProduct:product];
    
    if (@available(macOS 10.14.4, ios 12.2, *)) {
        if(argc >= 4) {
            const uint8_t* discountString;
            
            if (FREGetObjectAsUTF8(argv[4], &stringLength, &discountString) == FRE_OK) {
                NSString *discountData = [NSString stringWithUTF8String:(char*)discountString];
                if(![discountData isEqualToString:@""]) {
                    NSData *jsonData = [discountData dataUsingEncoding:NSUTF8StringEncoding];
                    NSError *error;
                    NSDictionary *discountJSON = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];

                    @try {
                        NSString *discountId = [discountJSON valueForKey:@"productId"];
                        NSString *keyId = [discountJSON valueForKey:@"keyId"];
                        NSUUID *nonce = [[NSUUID alloc] initWithUUIDString:[discountJSON valueForKey:@"nonce"]];
                        NSString *signature = [discountJSON valueForKey:@"signature"];
                        NSNumber *timestamp = [discountJSON valueForKey:@"timestamp"];
                        NSString *userId = [discountJSON valueForKey:@"userId"];

                        SKPaymentDiscount *discount = [[SKPaymentDiscount alloc] initWithIdentifier:discountId keyIdentifier:keyId nonce:nonce signature:signature timestamp:timestamp];
                                                
                        payment.applicationUsername = userId;
                        payment.paymentDiscount = discount;
                        
                    } @catch (NSException *exception) {
                        NSLog(@"%@", [@"AirInAppPurchae exception setting discount: " stringByAppendingString:exception.reason]);
                    }
                }
            }
            
        }
    }
    

    FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [_purchasingProductId UTF8String]);
    FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [[payment productIdentifier] UTF8String]);

    [[SKPaymentQueue defaultQueue] addPayment:payment];
    
    return nil;
}

DEFINE_ANE_FUNCTION(makePurchase) {
    
    uint32_t stringLength;
    const uint8_t* string1;
    
    if (FREGetObjectAsUTF8(argv[0], &stringLength, &string1) != FRE_OK)
        return nil;
    
    AirInAppPurchase* controller = getAirInAppPurchaseContextNativeData(context);
    
    if (!controller)
        return nil; // todo - error

    _purchasingProductId = [NSString stringWithUTF8String:(char*)string1];
    
    SKProduct *product = [controller.iapProducts valueForKey:_purchasingProductId];
    if(!product) {
        [controller sendEvent:@"PURCHASE_ERROR" level:@"Unknow product id"];
        return nil;
    }
    
//    SKPayment* payment = [SKPayment paymentWithProductIdentifier:productIdentifier];
    SKPayment* payment = [SKPayment paymentWithProduct:product];
  
    FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [_purchasingProductId UTF8String]);
    FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [[payment productIdentifier] UTF8String]);
    
    [[SKPaymentQueue defaultQueue] addPayment:payment];
    
    return nil;
}

DEFINE_ANE_FUNCTION(userCanMakeAPurchase) {
    
    BOOL canMakePayment = [SKPaymentQueue canMakePayments];
    
    if (canMakePayment)
        FREDispatchStatusEventAsync(context, (uint8_t*) "PURCHASE_ENABLED", (uint8_t*) [@"Yes" UTF8String]);
    else
        FREDispatchStatusEventAsync(context, (uint8_t*) "PURCHASE_DISABLED", (uint8_t*) [@"No" UTF8String]);

    return nil;
}

DEFINE_ANE_FUNCTION(getProductsInfo) {
    
    AirInAppPurchase* controller = getAirInAppPurchaseContextNativeData(context);
    
    if (!controller)
        return nil; // todo - error
    
    FREObject arr = argv[0]; // array
    uint32_t arr_len; // array length
    
    FREGetArrayLength(arr, &arr_len);

    NSMutableSet* productsIdentifiers = [[NSMutableSet alloc] init];
     
    for (int32_t i = arr_len - 1; i >= 0; i--) {
        
        FREObject element;
        FREGetArrayElementAt(arr, i, &element);
        
        uint32_t stringLength;
        const uint8_t *string;
        FREGetObjectAsUTF8(element, &stringLength, &string);
        NSString *productIdentifier = [NSString stringWithUTF8String:(char*)string];
        
        [productsIdentifiers addObject:productIdentifier];
    }
    
    SKProductsRequest* request = [[SKProductsRequest alloc] initWithProductIdentifiers:productsIdentifiers];
    [controller sendRequest:request andContext:context];
    
    return nil;
}

// remove all transactions from the queue before purchasing
DEFINE_ANE_FUNCTION(clearTransactions) {
    AirInAppPurchase* controller = getAirInAppPurchaseContextNativeData(context);
    
    if (!controller)
        return nil;
    
    NSArray* transactions = [[SKPaymentQueue defaultQueue] transactions];
    for (SKPaymentTransaction* transaction in transactions) {
        @try {
            if ([transaction transactionState] != SKPaymentTransactionStatePurchasing) {
                [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
            }
        }
        @catch (NSException *e) {
            [controller sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"Error in clearTransactions: %@", e]];
        }
    }
    return nil;
}

DEFINE_ANE_FUNCTION(removePurchaseFromQueue) {
    
    uint32_t stringLength;
    const uint8_t* string1;
    
    if (FREGetObjectAsUTF8(argv[0], &stringLength, &string1) != FRE_OK)
        return nil;
    
    AirInAppPurchase* controller = getAirInAppPurchaseContextNativeData(context);
    
    if (!controller)
        return nil;
    
    NSString* productIdentifier = [NSString stringWithUTF8String:(char*)string1];
    NSArray* transactions = [[SKPaymentQueue defaultQueue] transactions];

    FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [[NSString stringWithFormat:@"removing purchase from queue %@", productIdentifier] UTF8String]);

    for (SKPaymentTransaction* transaction in transactions) {

        FREDispatchStatusEventAsync(context, (uint8_t*) "DEBUG", (uint8_t*) [[[transaction payment] productIdentifier] UTF8String]);

        switch ([transaction transactionState]) {
            case SKPaymentTransactionStatePurchased:
                FREDispatchStatusEventAsync(context, (uint8_t*)"DEBUG", (uint8_t*) [@"SKPaymentTransactionStatePurchased" UTF8String]);
                break;
            case SKPaymentTransactionStateFailed:
                FREDispatchStatusEventAsync(context, (uint8_t*)"DEBUG", (uint8_t*) [@"SKPaymentTransactionStateFailed" UTF8String]);
                break;
            case SKPaymentTransactionStatePurchasing:
                FREDispatchStatusEventAsync(context, (uint8_t*)"DEBUG", (uint8_t*) [@"SKPaymentTransactionStatePurchasing" UTF8String]);
            case SKPaymentTransactionStateRestored:
                FREDispatchStatusEventAsync(context, (uint8_t*)"DEBUG", (uint8_t*) [@"SKPaymentTransactionStateRestored" UTF8String]);
            default:
                FREDispatchStatusEventAsync(context, (uint8_t*)"DEBUG", (uint8_t*) [@"Unknown Reason" UTF8String]);
                break;
        }

        if ([transaction transactionState] == SKPaymentTransactionStatePurchased && [[[transaction payment] productIdentifier] isEqualToString:productIdentifier]) {
            
            @try {
                   if ([transaction transactionState] != SKPaymentTransactionStatePurchasing) {
                          [controller sendEvent:@"DEBUG" level:@"finishingTransaction"];
                          [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
                   }
               }
                @catch (NSException *e) {
                    [controller sendEvent:@"DEBUG" level:[NSString stringWithFormat:@"Error in removePurchaseFromQueue: %@", e]];
                }
            break;
        }
    }
    
    return nil;
}

DEFINE_ANE_FUNCTION(restoreTransaction) {
    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
    return nil;
}

DEFINE_ANE_FUNCTION(getPendingAppStorePurchase) {
    if(_promotionPayment != nil) {
      
        FREObject result;
        FRENewObjectFromUTF8((int)_promotionPayment.productIdentifier.length, (const uint8_t *)[_promotionPayment.productIdentifier UTF8String], &result);
        _promotionPayment = nil;
        return result;
    }
    
    return nil;
}

void AirInAppPurchaseContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet) {
    
    static FRENamedFunction functions[] = {
        { (const uint8_t*)"initLib", NULL, &AirInAppPurchaseInit },
        MAP_FUNCTION(makePurchase, NULL),
        MAP_FUNCTION(userCanMakeAPurchase, NULL),
        MAP_FUNCTION(getProductsInfo, NULL),
        MAP_FUNCTION(removePurchaseFromQueue, NULL),
        MAP_FUNCTION(makeSubscription, NULL),
        MAP_FUNCTION(restoreTransaction, NULL),
        MAP_FUNCTION(clearTransactions, NULL),
        MAP_FUNCTION(getPendingAppStorePurchase, NULL)
    };
    
    *numFunctionsToTest = sizeof(functions) / sizeof(FRENamedFunction);
    *functionsToSet = functions;
}

void AirInAppPurchaseContextFinalizer(FREContext ctx) {
    
    CFTypeRef controller;
    FREGetContextNativeData(ctx, (void**)&controller);
    
    [[SKPaymentQueue defaultQueue] removeTransactionObserver:(__bridge AirInAppPurchase*)controller];
    
    CFBridgingRelease(controller);
}

void AirInAppPurchaseInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet) {
    
	*extDataToSet = NULL;
	*ctxInitializerToSet = &AirInAppPurchaseContextInitializer;
	*ctxFinalizerToSet = &AirInAppPurchaseContextFinalizer;
}

void AirInAppPurchaseFinalizer(void *extData) {
    
}

