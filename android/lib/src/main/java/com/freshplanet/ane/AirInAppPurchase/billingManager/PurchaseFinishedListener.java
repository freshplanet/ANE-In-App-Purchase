package com.freshplanet.ane.AirInAppPurchase.billingManager;

public interface PurchaseFinishedListener {

    void onPurchasesFinished(Boolean success, String data);
}
