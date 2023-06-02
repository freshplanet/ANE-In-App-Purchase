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
package com.freshplanet.ane.AirInAppPurchase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.adobe.fre.FREArray;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.freshplanet.ane.AirInAppPurchase.billingManager.BillingManagerV4;
import com.freshplanet.ane.AirInAppPurchase.billingManager.BillingManagerV5;
import com.freshplanet.ane.AirInAppPurchase.billingManager.IBillingManager;
import com.freshplanet.ane.AirInAppPurchase.billingManager.PurchaseFinishedListener;
import com.freshplanet.ane.AirInAppPurchase.billingManager.QueryInventoryFinishedListener;
import com.freshplanet.ane.AirInAppPurchase.billingManager.QueryPurchasesFinishedListener;
import com.freshplanet.ane.AirInAppPurchase.billingManager.SetupFinishedListener;

import org.json.JSONException;
import org.json.JSONObject;

public class ExtensionContext extends FREContext {

    private static final String TAG = "AirInAppPurchase";

    private IBillingManager _billingManager;
    private boolean _disposed;

    ExtensionContext() {
    }

    /**
     *
     * HELPERS
     *
     */

    private void _dispatchEvent(String type, String data) {

        try {
            dispatchStatusEventAsync(type, "" + data);
        }
        catch (Exception exception) {
            Log.e(TAG, "dispatchStatusEventAsync", exception);
        }
    }


    /**
     *
     * EVENTS / LISTENERS
     *
     */

    private static final String INIT_SUCCESSFUL = "INIT_SUCCESSFUL";
    private static final String INIT_ERROR = "INIT_ERROR";

    private static final String PURCHASE_SUCCESSFUL = "PURCHASE_SUCCESSFUL";
    private static final String PURCHASE_ERROR = "PURCHASE_ERROR";

    private static final String CONSUME_SUCCESSFUL = "CONSUME_SUCCESSFUL";
    private static final String CONSUME_ERROR = "CONSUME_ERROR";

    private static final String PRODUCT_INFO_RECEIVED = "PRODUCT_INFO_RECEIVED";
    private static final String PRODUCT_INFO_ERROR = "PRODUCT_INFO_ERROR";

    private static final String RESTORE_INFO_RECEIVED = "RESTORE_INFO_RECEIVED";
    private static final String RESTORE_INFO_ERROR = "RESTORE_INFO_ERROR";

    private SetupFinishedListener _initLibListener = new SetupFinishedListener() {
        @Override
        public void SetupFinished(Boolean success, String billingVersion) {

            if(success)
                _dispatchEvent(INIT_SUCCESSFUL, billingVersion);
            else
                _dispatchEvent(INIT_ERROR, "");
        }
    };

    private PurchasesUpdatedListener _purchaseUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<com.android.billingclient.api.Purchase> list) {

            if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK ) {
                if(list != null && list.size() > 0) {
                    _purchaseFinishedListener.onPurchasesFinished(true, _billingManager.purchaseToJSON(list.get(0)).toString());
                }
                else {
                    _purchaseFinishedListener.onPurchasesFinished(true, new JSONObject().toString());
                }

            }
            else if(billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.USER_CANCELED){
                _dispatchEvent(PURCHASE_ERROR, "RESULT_USER_CANCELED");
            }
            else {
                String errorLog = "Error: " + billingResult.getDebugMessage() + ", BillingResponseCode: " + billingResult.getResponseCode();
                _dispatchEvent(PURCHASE_ERROR, errorLog);
            }
        }
    };

    private PurchaseFinishedListener _purchaseFinishedListener = new PurchaseFinishedListener() {
        @Override
        public void onPurchasesFinished(Boolean success, String data) {
            if(success)
                _dispatchEvent(PURCHASE_SUCCESSFUL, data);
            else {

                String errorMessage;
                if(data != null && !data.isEmpty()) {
                    errorMessage = data;
                }
                else {
                    String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
                    errorMessage = "No data received for purchase error! Stack trace: " + stackTrace;
                }
                _dispatchEvent(PURCHASE_ERROR, errorMessage);

            }
        }
    };

    private ConsumeResponseListener _consumeResponseListener = new ConsumeResponseListener() {
        @Override
        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
            if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                _dispatchEvent(CONSUME_SUCCESSFUL, purchaseToken);
            }
            else {
                _dispatchEvent(CONSUME_ERROR, billingResult.getDebugMessage());
            }
        }
    };



    private QueryInventoryFinishedListener _getProductsInfoListener = new QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(Boolean success, String data) {

            if(success) {
                _dispatchEvent(PRODUCT_INFO_RECEIVED, data);
            }
            else {
                _dispatchEvent(PRODUCT_INFO_ERROR, data);
            }
        }
    };

    private QueryPurchasesFinishedListener _getPurchasesListener = new QueryPurchasesFinishedListener() {
        @Override
        public void onQueryPurchasesFinished(Boolean success, String data) {
            if(success) {
                _dispatchEvent(RESTORE_INFO_RECEIVED, data);
            }
            else {
                _dispatchEvent(RESTORE_INFO_ERROR, data);
            }
        }
    };


    /**
     *
     * INTERFACE
     *
     */

    private FREFunction initLib = new BaseFunction() {
        @Override
        public FREObject call(final FREContext ctx, FREObject[] args) {

            final Boolean debug = getBooleanFromFREObject(args[1]);

            if(_billingManager != null) {
                _billingManager.dispose();
            }

            try {

                final BillingClient billingClient = BillingClient.newBuilder(ctx.getActivity())
                        .setListener(_purchaseUpdatedListener)
                        .enablePendingPurchases()
                        .build();
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(BillingResult billingResult) {

                        if (_disposed) return;

                        if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                            // The BillingClient is ready. You can query purchases here.
                            Log.d(TAG, "BillingManager connected");

                            if(billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                Log.d(TAG, "BillingClient doesn't support PRODUCT_DETAILS, using BillingManagerV4");
                                _billingManager = new BillingManagerV4(billingClient);
                            }
                            else {
                                Log.d(TAG, "BillingClient supports PRODUCT_DETAILS, using BillingManagerV5");
                                _billingManager = new BillingManagerV5(billingClient);
                            }


                            _initLibListener.SetupFinished(true, _billingManager.getClass() == BillingManagerV5.class ? "billingV5" : "billingV4");

                        }
                        else {
                            _initLibListener.SetupFinished(false, "");
                        }
                    }
                    @Override
                    public void onBillingServiceDisconnected() {
                        // Try to restart the connection on the next request to
                        // Google Play by calling the startConnection() method.
                        Log.d(TAG, "BillingManager disconnected");
                        if (_disposed) return;

                        _initLibListener.SetupFinished(false, "");


                    }
                });
            }
            catch (Exception e) {
                Log.d(TAG, "Error initializing BillingManager " + e);
                _initLibListener.SetupFinished(false, "");
            }








            _billingManager.enableDebugLogging(debug, TAG);


            return null;
        }
    };

    private FREFunction getProductsInfo = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            List<String> skusName = null;
            if(args[0] != null) {
                skusName = getListOfStringFromFREArray((FREArray) args[0]);
            }

            List<String> skusSubsName = null;
            if(args[1] != null) {
                skusSubsName = getListOfStringFromFREArray((FREArray) args[1]);
            }

            _billingManager.queryInventory(skusName, skusSubsName, _getProductsInfoListener);


            return null;
        }
    };

    private FREFunction makePurchase = new BaseFunction() {
        @Override
        public FREObject call(final FREContext ctx, FREObject[] args) {

            final String purchaseId = getStringFromFREObject(args[0]);

            if (purchaseId == null)
                _dispatchEvent(PURCHASE_ERROR, "Null purchaseId");
            else {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _billingManager.purchaseProduct(ctx.getActivity(), purchaseId, null, -1, "inapp", _purchaseFinishedListener, -1, null);
                    }
                });

            }

            return null;
        }
    };

    private FREFunction makeSubscription = new BaseFunction() {
        @Override
        public FREObject call(final FREContext ctx, FREObject[] args) {

            final String purchaseId = getStringFromFREObject(args[0]);
            final String oldPurchaseId = getStringFromFREObject(args[1]);
            final int prorationMode = getIntFromFREObject(args[2]);
            final int subscriptionOfferIndex = getIntFromFREObject(args[4]);
            final String userId = getStringFromFREObject(args[5]);

            if (purchaseId == null)
                _dispatchEvent(PURCHASE_ERROR, "null purchaseId");
            else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _billingManager.purchaseProduct(ctx.getActivity(), purchaseId, oldPurchaseId, prorationMode, "subs", _purchaseFinishedListener, subscriptionOfferIndex, userId);
                    }
                });

            }

            return null;
        }
    };

    private FREFunction restoreTransaction = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            boolean queryHistory = getBooleanFromFREObject(args[0]);
            if(queryHistory)
                _billingManager.queryPurchaseHistory(_getPurchasesListener);
            else
                _billingManager.queryPurchases(_getPurchasesListener, false);


            return null;
        }
    };

    private FREFunction removePurchaseFromQueue = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            String receipt = getStringFromFREObject(args[1]);
            JSONObject receiptJson = null;
            String signedData = null;
            JSONObject signedDataJson = null;
            String purchaseToken = null;


            try {

                receiptJson = new JSONObject(receipt);
                signedData = receiptJson.getString("signedData");

                if (signedData == null)
                    throw new JSONException("null signedData");

                signedDataJson = new JSONObject(signedData);
                purchaseToken = signedDataJson.getString("purchaseToken");


                _billingManager.consumePurchase(purchaseToken, _consumeResponseListener);

            }
            catch (JSONException jsonException) {
                _dispatchEvent(CONSUME_ERROR, jsonException.getMessage());
            }

            return null;
        }
    };

    // --------------------------------------------------------------------------------------//
    //																						 //
    // 									 	FREContext SETUP								 //
    // 																						 //
    // --------------------------------------------------------------------------------------//

    /**
     *
     */
    public void dispose() {

        if(_billingManager != null)
            _billingManager.dispose();

        _billingManager = null;
        _disposed = true;

    }

    /**
     *
     * @return
     */
    public Map<String, FREFunction> getFunctions() {

        Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();

        functionMap.put("initLib", initLib);
        functionMap.put("getProductsInfo", getProductsInfo);
        functionMap.put("makePurchase", makePurchase);
        functionMap.put("makeSubscription", makeSubscription);
        functionMap.put("restoreTransaction", restoreTransaction);
        functionMap.put("removePurchaseFromQueue", removePurchaseFromQueue);

        return functionMap;
    }
}