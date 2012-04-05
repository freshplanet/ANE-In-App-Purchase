//
//  AirInAppPurchase.m
//  AirInAppPurchase
//
//  Created by Thibaut Crenn on 3/31/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "AirInAppPurchase.h"

FREContext AirInAppCtx = nil;

void *AirInAppRefToSelf;



@implementation AirInAppPurchase

- (id) init
{
    self = [super init];
    if (self)
    {
        [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    }
    AirInAppRefToSelf = self;
    return self;
}

- (BOOL) canMakePayment
{
    return [SKPaymentQueue canMakePayments];
}

- (void) sendRequest:(SKRequest*)request AndContext:(FREContext*)ctx
{
    request.delegate = self;
    [request start];   
}


- (void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray *)transactions
{
    
    FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"UPDATED_TRANSACTIONS", (uint8_t*) [@"ok" UTF8String]  ); 
    
    NSMutableDictionary *data;
    
    for ( SKPaymentTransaction* transaction in transactions)
    {
        if ([transaction transactionState] == SKPaymentTransactionStatePurchased)
        {
            // purchase done
            // dispatch event
            data = [[NSMutableDictionary alloc] init];
            [data setValue:[[transaction payment] productIdentifier] forKey:@"productId"];
                        
            NSString* receiptString = [[[NSString alloc] initWithData:transaction.transactionReceipt encoding:NSUTF8StringEncoding] autorelease];
            [data setValue:receiptString forKey:@"receipt"];
            [data setValue:@"AppStore" forKey:@"receiptType"];
            
            FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"PURCHASE_SUCCESSFUL", (uint8_t*)[[data JSONString] UTF8String]); 


        } else if ([transaction transactionState] == SKPaymentTransactionStateFailed) {
            // purchase failed
            [[transaction payment] productIdentifier];
            [[transaction error] code];
            
            data = [[NSMutableDictionary alloc] init];
            [data setValue:[NSNumber numberWithInteger:[[transaction error] code]]  forKey:@"code"];
            [data setValue:[[transaction error] localizedFailureReason] forKey:@"FailureReason"];
            [data setValue:[[transaction error] localizedDescription] forKey:@"FailureDescription"];
            [data setValue:[[transaction error] localizedRecoverySuggestion] forKey:@"RecoverySuggestion"];

            // dispatch event
            FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"PURCHASE_ERROR", (uint8_t*)             
               [[data JSONString] UTF8String]
            ); 
            
            // conclude the transaction
            [queue finishTransaction:transaction];
        } 
        
        else if ([transaction transactionState] == SKPaymentTransactionStatePurchasing) {
            // purchase failed
            [[transaction payment] productIdentifier];
            [transaction error];
            // dispatch event
            FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"PURCHASING", (uint8_t*)             
                                        [[[transaction payment] productIdentifier] UTF8String]
                                        ); 
                        
        } 
        else if ([transaction transactionState] == SKPaymentTransactionStateRestored) {
            // purchase failed
            [[transaction payment] productIdentifier];
            [transaction error];
            // dispatch event
            FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"TRANSACTION_RESTORED", (uint8_t*)             
                                        [[[transaction error] localizedDescription] UTF8String]
                                        ); 
            
            
            // conclude the transaction
            [queue finishTransaction:transaction];
        }         
        else {
            // unknown state
            FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"PURCHASE_UNKNOWN", (uint8_t*)             
                                        [@"Unknown Reason" UTF8String]
            ); 
        }
    }
}


- (void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue *)queue
{
    FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"DEBUG", (uint8_t*)             [@"restoreCompletedTransactions" UTF8String] ); 
}

- (void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error
{
    FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"DEBUG", (uint8_t*)             [@"restoreFailed" UTF8String] ); 
}

- (void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray *)transactions
{
    FREDispatchStatusEventAsync(AirInAppCtx, (uint8_t*)"DEBUG", (uint8_t*)             [@"removeTransaction" UTF8String] ); 
}


- (void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    
    
    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    [numberFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    
    
    
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    NSString *formattedString;
    
    for (SKProduct* product in [response products])
    {        
        
        [numberFormatter setLocale:product.priceLocale];
        formattedString = [numberFormatter stringFromNumber:product.price];
        [dictionary setValue:formattedString forKey:[product productIdentifier]];
    }
    
    
    NSString* jsonDictionary = [dictionary JSONString];
    
    FREDispatchStatusEventAsync(AirInAppCtx ,(uint8_t*) "PRODUCT_INFO_SUCCESS", (uint8_t*) [jsonDictionary UTF8String] );
    
    if ([response invalidProductIdentifiers] != nil && [[response invalidProductIdentifiers] count] > 0)
    {
        NSString* jsonArray = [[response invalidProductIdentifiers] JSONString];
        
        FREDispatchStatusEventAsync(AirInAppCtx ,(uint8_t*) "PRODUCT_INFO_ERROR", (uint8_t*) [jsonArray UTF8String] );
        
    }
}


- (void)requestDidFinish:(SKRequest *)request
{
    FREDispatchStatusEventAsync(AirInAppCtx ,(uint8_t*) "DEBUG", (uint8_t*) [@"requestDidFinish" UTF8String] );
}

- (void)request:(SKRequest *)request didFailWithError:(NSError *)error
{
    FREDispatchStatusEventAsync(AirInAppCtx ,(uint8_t*) "DEBUG", (uint8_t*) [@"requestDidFailWithError" UTF8String] );
}


@end



FREObject makePurchase(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    uint32_t stringLength;
    const uint8_t *string1;
    FREGetObjectAsUTF8(argv[0], &stringLength, &string1);
    NSString *productIdentifier = [NSString stringWithUTF8String:(char*)string1];

    SKPayment* payment = [SKPayment paymentWithProductIdentifier:productIdentifier];
    
    [[SKPaymentQueue defaultQueue] addPayment:payment];
    
    return nil;
}



FREObject userCanMakeAPurchase(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    
    BOOL canMakePayment = [SKPaymentQueue canMakePayments];
    
    if (canMakePayment)
    {
        FREDispatchStatusEventAsync(ctx, (uint8_t*) "PURCHASE_ENABLED", (uint8_t*) [@"Yes" UTF8String]);
 
    } else
    {
        FREDispatchStatusEventAsync(ctx, (uint8_t*) "PURCHASE_DISABLED", (uint8_t*) [@"No" UTF8String]);
    }
    
    
    //FREObject returnedValue = nil;
    //FRENewObjectFromBool(canMakePayment, returnedValue);
    //return returnedValue;
    return nil;
}



// make a SKProductsRequest. wait for a SKProductsResponse
// arg : array of string (string = product identifier)
FREObject getProductsInfo(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{        
    FREObject arr = argv[0]; // array
    uint32_t arr_len; // array length
    
    FREGetArrayLength(arr, &arr_len);

    NSMutableSet* productsIdentifiers = [[NSMutableSet alloc] init];
     
    for(int32_t i=arr_len-1; i>=0;i--){
                
        // get an element at index
        FREObject element;
        FREGetArrayElementAt(arr, i, &element);
        
        // convert it to NSString
        uint32_t stringLength;
        const uint8_t *string;
        FREGetObjectAsUTF8(element, &stringLength, &string);
        NSString *productIdentifier = [NSString stringWithUTF8String:(char*)string];
        
        [productsIdentifiers addObject:productIdentifier];
    }
    
    SKProductsRequest* request = [[SKProductsRequest alloc] initWithProductIdentifiers:productsIdentifiers];

    if ((AirInAppPurchase*)AirInAppRefToSelf == nil)
    {
        [[AirInAppPurchase alloc] init];
    }
    
    
    [(AirInAppPurchase*)AirInAppRefToSelf sendRequest:request AndContext:ctx];
    
    
    return nil;
}


FREObject removePurchaseFromQueue(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[])
{
    uint32_t stringLength;
    const uint8_t *string1;
    FREGetObjectAsUTF8(argv[0], &stringLength, &string1);
    NSString *productIdentifier = [NSString stringWithUTF8String:(char*)string1];

    NSArray* transactions = [[SKPaymentQueue defaultQueue] transactions];

    for (SKPaymentTransaction* transaction in transactions)
    {
        if ([transaction transactionState] == SKPaymentTransactionStatePurchased && [[transaction payment] productIdentifier] == productIdentifier)
        {
            // conclude the transaction
            [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
            break;
        }
    }
    
    return nil;
}


// ContextInitializer()
//
// The context initializer is called when the runtime creates the extension context instance.
void AirInAppContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, 
                             uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet) 
{    
    // Register the links btwn AS3 and ObjC. (dont forget to modify the nbFuntionsToLink integer if you are adding/removing functions)
    NSInteger nbFuntionsToLink = 4;
    *numFunctionsToTest = nbFuntionsToLink;
    
    FRENamedFunction* func = (FRENamedFunction*) malloc(sizeof(FRENamedFunction) * nbFuntionsToLink);
    
    func[0].name = (const uint8_t*) "makePurchase";
    func[0].functionData = NULL;
    func[0].function = &makePurchase;
    
    func[1].name = (const uint8_t*) "userCanMakeAPurchase";
    func[1].functionData = NULL;
    func[1].function = &userCanMakeAPurchase;
    
    func[2].name = (const uint8_t*) "getProductsInfo";
    func[2].functionData = NULL;
    func[2].function = &getProductsInfo;

    
    func[3].name = (const uint8_t*) "removePurchaseFromQueue";
    func[3].functionData = NULL;
    func[3].function = &removePurchaseFromQueue;
                    
    
    *functionsToSet = func;
    
    AirInAppCtx = ctx;

    if ((AirInAppPurchase*)AirInAppRefToSelf == nil)
    {
        [[AirInAppPurchase alloc] init];
    }

}

// ContextFinalizer()
//
// Set when the context extension is created.

void AirInAppContextFinalizer(FREContext ctx) { 
    NSLog(@"Entering ContextFinalizer()");
    
    NSLog(@"Exiting ContextFinalizer()");	
}



// AirInAppInitializer()
//
// The extension initializer is called the first time the ActionScript side of the extension
// calls ExtensionContext.createExtensionContext() for any context.

void AirInAppInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet ) 
{
    
    NSLog(@"Entering ExtInitializer()");                    
    
	*extDataToSet = NULL;
	*ctxInitializerToSet = &AirInAppContextInitializer; 
	*ctxFinalizerToSet = &AirInAppContextFinalizer;
    
    NSLog(@"Exiting ExtInitializer()"); 
}


