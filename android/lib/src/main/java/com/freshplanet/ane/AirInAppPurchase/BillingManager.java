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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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

    private interface QueryPurchasesInternalListener {

        void onQueryPurchasesFinished(Boolean success, String error);
    }

    public interface PurchaseFinishedListener{

        void onPurchasesFinished(Boolean success, String data);
    }

    private interface GetProductInfoFinishedListener {

        void onGetProductInfoFinishedListener(List<ProductDetails> productDetailsList);
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
                    final List<ProductDetails> result = new ArrayList<ProductDetails>();

                    List<QueryProductDetailsParams.Product> iapList = new ArrayList<>();

                    for (String productId : skuList) {
                        iapList.add(QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build());

                    }

                    QueryProductDetailsParams iapParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(iapList)
                            .build();

                    getProductInfo(iapParams, new GetProductInfoFinishedListener() {
                        @Override
                        public void onGetProductInfoFinishedListener(List<ProductDetails> productDetailsList) {

                            if(productDetailsList != null) {
                                result.addAll(productDetailsList);
                            }

                            List<QueryProductDetailsParams.Product> subList = new ArrayList<>();

                            for (String productId : skuSubsList) {
                                subList.add(QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build());

                            }

                            QueryProductDetailsParams subParams = QueryProductDetailsParams.newBuilder()
                                    .setProductList(subList)
                                    .build();

                            getProductInfo(subParams, new GetProductInfoFinishedListener() {
                                @Override
                                public void onGetProductInfoFinishedListener(List<ProductDetails> productDetailsList) {

                                    if(productDetailsList != null) {
                                        result.addAll(productDetailsList);
                                    }


                                    JSONObject detailsObject = new JSONObject();

                                    for (ProductDetails productDetails : result) {
                                        if (productDetails != null) {
                                            try {
                                                detailsObject.put(productDetails.getProductId(), productDetailsToJson(productDetails));
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

    private void getProductInfo(final QueryProductDetailsParams params, final GetProductInfoFinishedListener listener) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();

                    _billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                        @Override
                        public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                listener.onGetProductInfoFinishedListener(list);
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

    void queryPurchases(final QueryPurchasesFinishedListener listener, final boolean includeAcknowledged) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    checkNotDisposed();

                    final List<Purchase> purchases = new ArrayList<Purchase>();

                    // fetch inapp
                    queryPurchasesInternal(BillingClient.ProductType.INAPP, purchases, false, new QueryPurchasesInternalListener() {
                        @Override
                        public void onQueryPurchasesFinished(Boolean success, String error) {
                            if(!success || error != null) {
                                listener.onQueryPurchasesFinished(false, error);
                                return;
                            }
                            // now fetch subs
                            queryPurchasesInternal(BillingClient.ProductType.SUBS, purchases, includeAcknowledged, new QueryPurchasesInternalListener() {
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

    void queryPurchasesInternal(final String productType, final List<Purchase> purchases, final Boolean includeAcknowledged, final QueryPurchasesInternalListener listener) {

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder().setProductType(productType).build();
        _billingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> list) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase p : list) {

                        if(p.isAcknowledged()) {

                            if(includeAcknowledged) {
                                purchases.add(p);
                            }
                            // just consume
                            if(productType.equals(BillingClient.ProductType.INAPP)) {
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

    void purchaseProduct(final Activity activity, final String skuID, final String oldSkuID, final int replaceSkusProrationMode, final String productType, final PurchaseFinishedListener listener, final int offerIndex, final String userId) {

        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                try {
                    if(!productType.equals(BillingClient.ProductType.INAPP) && !productType.equals(BillingClient.ProductType.SUBS)) {
                        listener.onPurchasesFinished(false, "Unknown product type");
                        return;
                    }

                    if(productType.equals(BillingClient.ProductType.SUBS) && _billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        listener.onPurchasesFinished(false, "Subscriptions are not available.");
                        return;
                    }


                    List<QueryProductDetailsParams.Product> productList = Collections.singletonList(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuID)
                            .setProductType(productType)
                            .build());

                    QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                            .setProductList(productList)
                            .build();

                    getProductInfo(params, new GetProductInfoFinishedListener() {
                        @Override
                        public void onGetProductInfoFinishedListener(List<ProductDetails> productDetailsList) {

                            if(productDetailsList != null && productDetailsList.size() > 0) {

                                ProductDetails details = productDetailsList.get(0);

                                BillingFlowParams.ProductDetailsParams.Builder productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(details);


                                if(productType.equals(BillingClient.ProductType.SUBS)) {
                                    String offerToken = details
                                            .getSubscriptionOfferDetails()
                                            .get(offerIndex)
                                            .getOfferToken();
                                    productDetailsParams.setOfferToken(offerToken);
                                }


                                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = Collections.singletonList(productDetailsParams.build());

                                final BillingFlowParams.Builder billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList);

                                billingFlowParams.setObfuscatedAccountId(userId);

                                if(oldSkuID != null && !oldSkuID.equals("") && replaceSkusProrationMode >= 0) {

                                    final List<Purchase> subPurchases = new ArrayList<>();

                                    // we have to find old subscription purchase first
                                    queryPurchasesInternal(BillingClient.ProductType.SUBS, subPurchases, true, new QueryPurchasesInternalListener() {
                                        @Override
                                        public void onQueryPurchasesFinished(Boolean success, String error) {

                                            if(!success || error != null) {
                                                listener.onPurchasesFinished(false, "Unable to get old subscription purchase " + skuID);
                                                return;
                                            }

                                            BillingFlowParams.SubscriptionUpdateParams.Builder subUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder();
                                            subUpdateParams.setReplaceProrationMode(replaceSkusProrationMode);

                                            boolean didFindOldProduct = false;
                                            for (Purchase subPurchase : subPurchases) {
                                                if(subPurchase.getProducts().contains(oldSkuID)) {
                                                    didFindOldProduct = true;
                                                    subUpdateParams.setOldPurchaseToken(subPurchase.getPurchaseToken());
                                                    break;
                                                }
                                            }

                                            if(!didFindOldProduct) {
                                                listener.onPurchasesFinished(false, "Unable to get old subscription purchase " + skuID);
                                                return;
                                            }

                                            billingFlowParams.setSubscriptionUpdateParams(subUpdateParams.build());
                                            _billingClient.launchBillingFlow(activity, billingFlowParams.build());

                                        }
                                    });

                                }
                                else {
                                    _billingClient.launchBillingFlow(activity, billingFlowParams.build());
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


    void consumePurchase(final String purchaseToken, final ConsumeResponseListener listener) {

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

    public JSONObject productDetailsToJson(ProductDetails productDetails) {

        JSONObject resultObject = null;

        try {

            resultObject = new JSONObject();
            resultObject.put("productId", productDetails.getProductId());
            resultObject.put("type", productDetails.getProductType());
            resultObject.put("title", productDetails.getTitle());
            resultObject.put("name", productDetails.getName());
            resultObject.put("description", productDetails.getDescription());
            ProductDetails.OneTimePurchaseOfferDetails purchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
            if(purchaseOfferDetails != null) {
                resultObject.put("price", purchaseOfferDetails.getFormattedPrice());
                resultObject.put("price_amount_micros", purchaseOfferDetails.getPriceAmountMicros());
                resultObject.put("price_currency_code", purchaseOfferDetails.getPriceCurrencyCode());
            }

            List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList = productDetails.getSubscriptionOfferDetails();
            if(subscriptionOfferDetailsList != null) {
                JSONArray offersArray = new JSONArray();

                for (ProductDetails.SubscriptionOfferDetails offerDetails : subscriptionOfferDetailsList) {
                    JSONObject offerJson = new JSONObject();
                    if(offerDetails.getOfferId() != null)
                        offerJson.put("offerId", offerDetails.getOfferId());
                    offerJson.put("basePlanId", offerDetails.getBasePlanId());
                    offerJson.put("offerTags", new JSONArray(offerDetails.getOfferTags()));
                    offerJson.put("offerToken", offerDetails.getOfferToken());

                    JSONArray pricingPhases = new JSONArray();

                    for (ProductDetails.PricingPhase pricingPhase : offerDetails.getPricingPhases().getPricingPhaseList()) {
                        JSONObject phaseJson = new JSONObject();
                        phaseJson.put("billingCycleCount", pricingPhase.getBillingCycleCount());
                        phaseJson.put("billingPeriod", pricingPhase.getBillingPeriod());
                        phaseJson.put("formattedPrice", pricingPhase.getFormattedPrice());
                        phaseJson.put("priceAmountMicros", pricingPhase.getPriceAmountMicros());
                        phaseJson.put("priceCurrencyCode", pricingPhase.getPriceCurrencyCode());
                        phaseJson.put("recurrenceMode", pricingPhase.getRecurrenceMode());
                        pricingPhases.put(phaseJson);
                    }
                    offerJson.put("pricingPhases", pricingPhases);


                    offersArray.put(offerJson);

                }

                resultObject.put("subscriptionOffers", offersArray);

            }
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
