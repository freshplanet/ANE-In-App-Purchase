//
//  SKStoreProductViewController+removeSceneDisconnected.h
//  AirInAppPurchase
//
//  Created by Mateo Kozomara on 23.09.2022..
//  Copyright © 2022 Freshplanet, Inc. All rights reserved.
//

#import <StoreKit/StoreKit.h>


API_AVAILABLE(ios(15.7))
@interface SKStoreProductViewController (removeSceneDisconnected)
- (void) sceneDisconnected: (id)arg;
- (void) appWillTerminate;
@end


