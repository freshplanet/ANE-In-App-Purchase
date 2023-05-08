package com.freshplanet.ane.AirInAppPurchase.billingManager;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BillingManagerV4 implements IBillingManager {

    private boolean _debugLog = false;
    private boolean _disposed = false;
    private String _debugTag = "BillingManagerV4";
    private BillingClient _billingClient;

    private interface QueryPurchasesInternalListener {

        void onQueryPurchasesFinished(Boolean success, String error);
    }

    private interface GetProductInfoFinishedListener {

        void onGetProductInfoFinishedListener(List<SkuDetails> skuDetailsList);
    }

    public BillingManagerV4(BillingClient billingClient) {

        _billingClient = billingClient;

    }

    public void dispose() {

        if(_billingClient != null)
            _billingClient.endConnection();
        _disposed = true;
    }


    private void startServiceConnectionIfNeeded(final Runnable executeOnSuccess, final Runnable executeOnError) {

        if(_disposed || _billingClient == null)
            return;


        if (_billingClient.isReady()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.run();
            }
        } else {
            _billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {

                        if (executeOnSuccess != null) {
                            executeOnSuccess.run();
                        }
                    } else {
                        if (executeOnError != null) {
                            executeOnError.run();
                        }
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                    if (executeOnError != null) {
                        executeOnError.run();
                    }
                }
            });
        }
    }

    public void queryInventory(final List<String> skuList, final List<String> skuSubsList, final QueryInventoryFinishedListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();
                    final List<SkuDetails> result = new ArrayList<SkuDetails>();


                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

                    getProductInfo(params.build(), new GetProductInfoFinishedListener() {
                        @Override
                        public void onGetProductInfoFinishedListener(List<SkuDetails> skuDetailsList) {

                            if(skuDetailsList != null) {
                                result.addAll(skuDetailsList);
                            }

                            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                            params.setSkusList(skuSubsList).setType(BillingClient.SkuType.SUBS);

                            getProductInfo(params.build(), new GetProductInfoFinishedListener() {
                                @Override
                                public void onGetProductInfoFinishedListener(List<SkuDetails> skuDetailsList) {

                                    if(skuDetailsList != null) {
                                        result.addAll(skuDetailsList);
                                    }


                                    JSONObject detailsObject = new JSONObject();

                                    for (SkuDetails skuDetails : result) {
                                        if (skuDetails != null) {
                                            try {
                                                detailsObject.put(skuDetails.getSku(), new JSONObject(skuDetails.getOriginalJson()));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    JSONObject resultObject = new JSONObject();
                                    try {
                                        resultObject.put("details", detailsObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    listener.onQueryInventoryFinished(true, resultObject.toString());

                                }
                            });


                        }
                    });

                }
                catch (Exception e) {
                    listener.onQueryInventoryFinished(false, e.toString());
                }
            }
        };

        Runnable executeOnDisconnectedService = new Runnable() {
            @Override
            public void run() {
                listener.onQueryInventoryFinished(false, "Service disconnected.");
            }
        };

        startServiceConnectionIfNeeded(executeOnConnectedService, executeOnDisconnectedService);



    }

    private void getProductInfo(final SkuDetailsParams params, final GetProductInfoFinishedListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();
                    _billingClient.querySkuDetailsAsync(params,
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        listener.onGetProductInfoFinishedListener(skuDetailsList);
                                    }
                                    else {
                                        listener.onGetProductInfoFinishedListener(null);
                                    }
                                }
                            });
                }
                catch (Exception e) {
                    listener.onGetProductInfoFinishedListener(null);
                }
            }
        };

        Runnable executeOnDisconnectedService = new Runnable() {
            @Override
            public void run() {
                listener.onGetProductInfoFinishedListener(null);
            }
        };

        startServiceConnectionIfNeeded(executeOnConnectedService, executeOnDisconnectedService);


    }

    public void queryPurchases(final QueryPurchasesFinishedListener listener, final boolean includeAcknowledged) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();

                    final List<Purchase> purchases = new ArrayList<Purchase>();

                    // fetch inapp
                    queryPurchasesInternal(BillingClient.SkuType.INAPP, purchases, false, new QueryPurchasesInternalListener() {
                        @Override
                        public void onQueryPurchasesFinished(Boolean success, String error) {
                            if(!success || error != null) {
                                listener.onQueryPurchasesFinished(false, error);
                                return;
                            }
                            // now fetch subs
                            queryPurchasesInternal(BillingClient.SkuType.SUBS, purchases, includeAcknowledged, new QueryPurchasesInternalListener() {
                                @Override
                                public void onQueryPurchasesFinished(Boolean success, String error) {
                                    if(!success || error != null) {
                                        listener.onQueryPurchasesFinished(false, error);
                                        return;
                                    }

                                    final JSONObject resultObject = new JSONObject();
                                    final JSONArray purchasesArray = new JSONArray();

                                    for (Purchase p : purchases) {

                                        JSONObject purchaseJSON = purchaseToJSON(p);
                                        if(purchaseJSON != null) {
                                            purchasesArray.put(purchaseToJSON(p));
                                        }

                                    }

                                    try {
                                        resultObject.put("purchases", purchasesArray);
                                    }
                                    catch (JSONException e) {
                                        listener.onQueryPurchasesFinished(false, e.getMessage());
                                        return;
                                    }

                                    listener.onQueryPurchasesFinished(true, resultObject.toString());

                                }
                            });

                        }
                    });
                }
                catch (Exception e) {
                    listener.onQueryPurchasesFinished(false,e.toString());
                }

            }
        };

        Runnable executeOnDisconnectedService = new Runnable() {
            @Override
            public void run() {
                listener.onQueryPurchasesFinished(false,"Service disconnected");
            }
        };

        startServiceConnectionIfNeeded(executeOnConnectedService, executeOnDisconnectedService);

    }

    void queryPurchasesInternal(final String purchaseType, final List<Purchase> purchases, final Boolean includeAcknowledged, final QueryPurchasesInternalListener listener) {
        _billingClient.queryPurchasesAsync(purchaseType, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> list) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase p : list) {

                        if(p.isAcknowledged()) {

                            if(includeAcknowledged) {
                                purchases.add(p);
                            }
                            // just consume
                            if(purchaseType.equals(BillingClient.SkuType.INAPP)) {
                                consumePurchase(p.getPurchaseToken(), new ConsumeResponseListener() {
                                    @Override
                                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                        // do nothing, if consume didnt work, it should work on next restore
                                    }
                                });
                            }
                        }
                        else {
                            purchases.add(p);
                        }
                    }

                    listener.onQueryPurchasesFinished(true, null);
                }
                else {
                    // report errors
                    listener.onQueryPurchasesFinished(false, billingResult.getDebugMessage());
                }
            }
        });
    }

    public void purchaseProduct(final Activity activity, final String skuID, final String oldSkuID, final int replaceSkusProrationMode, final String productType, final PurchaseFinishedListener listener, final int offerIndex, final String userId) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    if(!productType.equals(BillingClient.SkuType.INAPP) && !productType.equals(BillingClient.SkuType.SUBS)) {
                        listener.onPurchasesFinished(false, "Unknown product type");
                        return;
                    }

                    if(productType.equals(BillingClient.SkuType.SUBS) && _billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        listener.onPurchasesFinished(false, "Subscriptions are not available.");
                        return;
                    }


                    final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(Arrays.asList(skuID)).setType(productType);
                    getProductInfo(params.build(), new GetProductInfoFinishedListener() {
                        @Override
                        public void onGetProductInfoFinishedListener(List<SkuDetails> skuDetailsList) {
                            if(skuDetailsList != null && skuDetailsList.size() > 0) {

                                SkuDetails details = skuDetailsList.get(0);

                                final BillingFlowParams.Builder flowParamsBuilder = BillingFlowParams.newBuilder()
                                        .setSkuDetails(details);

                                if(oldSkuID != null && !oldSkuID.equals("") && replaceSkusProrationMode >= 0) {

                                    final List<Purchase> subPurchases = new ArrayList<Purchase>();

                                    // we have to find old subscription purchase first
                                    queryPurchasesInternal(BillingClient.SkuType.SUBS, subPurchases, true, new QueryPurchasesInternalListener() {
                                        @Override
                                        public void onQueryPurchasesFinished(Boolean success, String error) {

                                            if(!success || error != null) {
                                                listener.onPurchasesFinished(false, "Unable to get old subscription purchase " + skuID);
                                                return;
                                            }

                                            BillingFlowParams.SubscriptionUpdateParams.Builder subUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder();
                                            subUpdateParams.setReplaceSkusProrationMode(replaceSkusProrationMode);

                                            boolean didFindOldProduct = false;
                                            for (Purchase subPurchase : subPurchases) {
                                                if(subPurchase.getSkus().contains(oldSkuID)) {
                                                    didFindOldProduct = true;
                                                    subUpdateParams.setOldSkuPurchaseToken(subPurchase.getPurchaseToken());
                                                    break;
                                                }
                                            }

                                            if(!didFindOldProduct) {
                                                listener.onPurchasesFinished(false, "Unable to get old subscription purchase " + skuID);
                                                return;
                                            }

                                            flowParamsBuilder.setSubscriptionUpdateParams(subUpdateParams.build());
                                            _billingClient.launchBillingFlow(activity, flowParamsBuilder.build());

                                        }
                                    });

                                }
                                else {
                                    _billingClient.launchBillingFlow(activity, flowParamsBuilder.build());
                                }

                            }
                            else {
                                listener.onPurchasesFinished(false, "Unable to get productInfo for purchasing skuID " + skuID);
                            }
                        }
                    });
                }
                catch (Exception e) {
                    listener.onPurchasesFinished(false,e.toString());
                }

            }
        };

        Runnable executeOnDisconnectedService = new Runnable() {
            @Override
            public void run() {
                listener.onPurchasesFinished(false, "Service disconnected");
            }
        };

        startServiceConnectionIfNeeded(executeOnConnectedService, executeOnDisconnectedService);

    }


    public void consumePurchase(final String purchaseToken, final ConsumeResponseListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();
                    ConsumeParams.Builder paramsBuilder = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchaseToken);

                    _billingClient.consumeAsync(paramsBuilder.build(),listener);
                }
                catch (Exception e) {
                    BillingResult result = BillingResult.newBuilder().setDebugMessage(e.toString()).setResponseCode(BillingClient.BillingResponseCode.ERROR).build();
                    listener.onConsumeResponse(result,result.getDebugMessage());
                }
            }
        };

        Runnable executeOnDisconnectedService = new Runnable() {
            @Override
            public void run() {
                BillingResult result = BillingResult.newBuilder().setDebugMessage("Service disconnected").setResponseCode(BillingClient.BillingResponseCode.ERROR).build();
                listener.onConsumeResponse(result,result.getDebugMessage());
            }
        };

        startServiceConnectionIfNeeded(executeOnConnectedService, executeOnDisconnectedService);
    }

    public JSONObject purchaseToJSON(Purchase purchase) {

        JSONObject resultObject = null;

        try {

            JSONObject receiptObject = new JSONObject();
            receiptObject.put("signedData", purchase.getOriginalJson());
            receiptObject.put("signature", purchase.getSignature());

            resultObject = new JSONObject();
            resultObject.put("productId", purchase.getSkus().get(0));
            resultObject.put("receiptType", "GooglePlay");
            resultObject.put("receipt", receiptObject);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return resultObject;
    }


    /**
     * Enables or disable debug logging through LogCat.
     */
    public void enableDebugLogging(boolean enable, String tag) {
        checkNotDisposed();
        _debugLog = enable;
        _debugTag = tag;
    }

    public void enableDebugLogging(boolean enable) {
        checkNotDisposed();
        _debugLog = enable;
    }

    private void checkNotDisposed() {
        if (_disposed) throw new IllegalStateException("BillingManager was disposed of, so it cannot be used.");
    }


    void logDebug(String msg) {
        if (_debugLog) Log.d(_debugTag, msg);
    }
}
