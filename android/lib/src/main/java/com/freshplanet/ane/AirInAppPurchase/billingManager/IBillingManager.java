package com.freshplanet.ane.AirInAppPurchase.billingManager;

import android.app.Activity;

import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;

import org.json.JSONObject;

import java.util.List;

public interface IBillingManager {

    void enableDebugLogging(boolean enable, String tag);
    void enableDebugLogging(boolean enable);
    void dispose();
    void queryInventory(final List<String> skuList, final List<String> skuSubsList, final QueryInventoryFinishedListener listener);
    void purchaseProduct(final Activity activity, final String skuID, final String oldSkuID, final int replaceSkusProrationMode, final String productType, final PurchaseFinishedListener listener, final int offerIndex, final String userId);
    void queryPurchaseHistory(final QueryPurchasesFinishedListener listener);
    void queryPurchases(final QueryPurchasesFinishedListener listener, final boolean includeAcknowledged);
    void consumePurchase(final String purchaseToken, final ConsumeResponseListener listener);
    JSONObject purchaseToJSON(Purchase purchase);
}


