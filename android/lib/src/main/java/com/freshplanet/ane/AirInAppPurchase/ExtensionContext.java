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
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtensionContext extends FREContext {

    private static final String TAG = "AirInAppPurchase";

    private BillingManager _billingManager;

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

    private BillingManager.SetupFinishedListener _initLibListener = new BillingManager.SetupFinishedListener() {
        @Override
        public void SetupFinished(Boolean success) {

            if(success)
                _dispatchEvent(INIT_SUCCESSFUL, "");
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

    private BillingManager.PurchaseFinishedListener _purchaseFinishedListener = new BillingManager.PurchaseFinishedListener() {
        @Override
        public void onPurchasesFinished(Boolean success, String data) {

            if(success)
                _dispatchEvent(PURCHASE_SUCCESSFUL, data);
            else
                _dispatchEvent(PURCHASE_ERROR, data != null ? data : "");
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



    private BillingManager.QueryInventoryFinishedListener _getProductsInfoListener = new BillingManager.QueryInventoryFinishedListener() {
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

    private BillingManager.QueryPurchasesFinishedListener _getPurchasesListener = new BillingManager.QueryPurchasesFinishedListener() {
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
        public FREObject call(FREContext ctx, FREObject[] args) {

            Boolean debug = getBooleanFromFREObject(args[1]);

            if(_billingManager != null) {
                _billingManager.dispose();
            }

            _billingManager = new BillingManager(ctx.getActivity());
            _billingManager.enableDebugLogging(debug, TAG);
            _billingManager.initialize(_initLibListener, _purchaseUpdatedListener);


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
                        _billingManager.purchaseProduct(ctx.getActivity(), purchaseId, null, -1, BillingClient.SkuType.INAPP, _purchaseFinishedListener);
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


            if (purchaseId == null)
                _dispatchEvent(PURCHASE_ERROR, "null purchaseId");
            else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _billingManager.purchaseProduct(ctx.getActivity(), purchaseId, oldPurchaseId, prorationMode, BillingClient.SkuType.SUBS, _purchaseFinishedListener);
                    }
                });

            }

            return null;
        }
    };

    private FREFunction restoreTransaction = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            _billingManager.queryPurchases(_getPurchasesListener);

            return null;
        }
    };

    private FREFunction removePurchaseFromQueue = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            String receipt = getStringFromFREObject(args[1]);
            String developerPayload = getStringFromFREObject(args[2]);
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


                _billingManager.consumePurchase(purchaseToken, developerPayload, _consumeResponseListener);

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