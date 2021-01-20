package com.freshplanet.ane.AirInAppPurchase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BillingManager {

    private boolean _debugLog = false;
    private boolean _disposed = false;
    private String _debugTag = "BillingManager";
    private boolean _setupDone = false;
    private Context _context;
    private BillingClient _billingClient;



    public interface SetupFinishedListener {

        void SetupFinished(Boolean success);
    }

    public interface QueryInventoryFinishedListener {

        void onQueryInventoryFinished(Boolean success, String data);
    }

    public interface QueryPurchasesFinishedListener {

        void onQueryPurchasesFinished(Boolean success, String data);
    }

    public interface PurchaseFinishedListener{

        void onPurchasesFinished(Boolean success, String data);
    }

    private interface GetProductInfoFinishedListener {

        void onGetProductInfoFinishedListener(List<SkuDetails> skuDetailsList);
    }

    BillingManager(Context ctx) {

        _context = ctx;

    }

    void dispose() {

        if(_billingClient != null)
            _billingClient.endConnection();
        _disposed = true;
    }



    void initialize(final SetupFinishedListener setupFinishedListener, final PurchasesUpdatedListener purchasesUpdatedListener) {

        try {

            checkNotDisposed();
            if (_setupDone) throw new IllegalStateException("BillingManager is already set up.");

            _billingClient = BillingClient.newBuilder(_context)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build();
            _billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {

                    if (_disposed) return;


                    if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        logDebug("BillingManager connected");
                        _setupDone = true;
                        setupFinishedListener.SetupFinished(true);

                    }
                    else {
                        setupFinishedListener.SetupFinished(false);
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    logDebug("BillingManager disconnected");
                    if (_disposed) return;

                    setupFinishedListener.SetupFinished(false);


                }
            });
        }
        catch (Exception e) {
            logDebug("Error initializing BillingManager " + e.toString());
            setupFinishedListener.SetupFinished(false);
        }

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

    void queryInventory(final List<String> skuList, final List<String> skuSubsList, final QueryInventoryFinishedListener listener) {

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
                                        try {
                                            detailsObject.put(skuDetails.getSku(), new JSONObject(skuDetails.getOriginalJson()));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
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
                                    listener.onGetProductInfoFinishedListener(skuDetailsList);
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

    void queryPurchases(final QueryPurchasesFinishedListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();

                    List<Purchase> purchases = new ArrayList<Purchase>();

                    Purchase.PurchasesResult purchasesResult = _billingClient.queryPurchases(BillingClient.SkuType.INAPP);

                    if(purchasesResult.getBillingResult().getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase p : purchasesResult.getPurchasesList()) {
                            if(p.isAcknowledged()) {
                                // just consume
                                consumePurchase(p.getPurchaseToken(), null, new ConsumeResponseListener() {
                                    @Override
                                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                        // do nothing, if consume didnt work, it should work on next restore
                                    }
                                });
                            }
                            else {
                                purchases.add(p);
                            }
                        }

                    }
                    else {
                        // report errors
                        listener.onQueryPurchasesFinished(false, purchasesResult.getBillingResult().getDebugMessage());
                        return;

                    }

                    Purchase.PurchasesResult subscriptionsResult = _billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    if(subscriptionsResult.getBillingResult().getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        for (Purchase p : subscriptionsResult.getPurchasesList()) {
                            if(!p.isAcknowledged()) {
                                purchases.add(p);
                            }
                        }
                    }
                    else {
                        // report errors
                        listener.onQueryPurchasesFinished(false, subscriptionsResult.getBillingResult().getDebugMessage());
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

                    resultObject.put("purchases", purchasesArray);
                    listener.onQueryPurchasesFinished(true, resultObject.toString());

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

    void purchaseProduct(final Activity activity, final String skuID, final String oldSkuID, final int replaceSkusProrationMode, final String productType, final PurchaseFinishedListener listener) {

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
                                BillingFlowParams.Builder flowParamsBuilder = BillingFlowParams.newBuilder()
                                        .setSkuDetails(details);

                                if(oldSkuID != null && !oldSkuID.equals("")) {
                                    flowParamsBuilder.setOldSku(oldSkuID);
                                }

                                if(replaceSkusProrationMode >= 0) {
                                    flowParamsBuilder.setReplaceSkusProrationMode(replaceSkusProrationMode);
                                }

                                _billingClient.launchBillingFlow(activity, flowParamsBuilder.build());

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


    void consumePurchase(final String purchaseToken, final String developerPayload, final ConsumeResponseListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();
                    ConsumeParams.Builder paramsBuilder = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchaseToken);

                    if(developerPayload != null && !developerPayload.equals("")) {
                        paramsBuilder.setDeveloperPayload(developerPayload);
                    }


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
            resultObject.put("productId", purchase.getSku());
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
    void enableDebugLogging(boolean enable, String tag) {
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
