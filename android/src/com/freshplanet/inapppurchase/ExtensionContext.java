//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.inapppurchase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.adobe.air.AirInAppPurchaseActivityResultCallback;
import com.adobe.air.AndroidActivityWrapper;
import com.adobe.fre.FREArray;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtensionContext extends FREContext {

    private static final String TAG = "AirInAppPurchase";
    private static final int RC_REQUEST = 10001;

    private IabHelper _iabHelper = null;

    private AndroidActivityWrapper aaw = null;
    private Activity _freActivity = null;

    public ExtensionContext() {

        aaw = AndroidActivityWrapper.GetAndroidActivityWrapper();
        aaw.addActivityResultListener(_activityResultCallback);

        _freActivity = aaw.getActivity();
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

    private String _purchaseToResultString(Purchase purchase) {

        String resultString = null;

        try {

            JSONObject receiptObject = new JSONObject();
            receiptObject.put("signedData", purchase.getOriginalJson());
            receiptObject.put("signature", purchase.getSignature());

            JSONObject resultObject = new JSONObject();
            resultObject.put("productId", purchase.getSku());
            resultObject.put("receiptType", "GooglePlay");
            resultObject.put("receipt", receiptObject);

            resultString = resultObject.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return resultString;
    }

    /**
     *
     * ADOBE HACKERY
     *
     */

    private AirInAppPurchaseActivityResultCallback _activityResultCallback = new AirInAppPurchaseActivityResultCallback() {
        @Override
        public void onActivityResult(int i, int i1, Intent intent) {

            if (_iabHelper != null)
                _iabHelper.handleActivityResult(i, i1, intent);
        }
    };

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

    private IabHelper.OnIabSetupFinishedListener _initLibListener = new IabHelper.OnIabSetupFinishedListener() {
        @Override
        public void onIabSetupFinished(IabResult result) {

            if (result.isSuccess())
                _dispatchEvent(INIT_SUCCESSFUL, result.getMessage());
            else
                _dispatchEvent(INIT_ERROR, result.getMessage());
        }
    };

    private IabHelper.QueryInventoryFinishedListener _getProductsInfoListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure())
                _dispatchEvent(PRODUCT_INFO_ERROR, result.getMessage());
            else {

                String data = inv != null ? inv.toString() : "";
                _dispatchEvent(PRODUCT_INFO_RECEIVED, data);
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener _onIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {

            if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED)
                _dispatchEvent(PURCHASE_ERROR, "RESULT_USER_CANCELED");
            else if (result.isFailure())
                _dispatchEvent(PURCHASE_ERROR, result.getMessage());
            else {

                String resultString = _purchaseToResultString(info);
                _dispatchEvent(PURCHASE_SUCCESSFUL, resultString);
            }
        }
    };

    private IabHelper.QueryInventoryFinishedListener _restoreTransactionListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure())
                _dispatchEvent(RESTORE_INFO_ERROR, result.getMessage());
            else {

                String data = inv != null ? inv.toString() : "";
                _dispatchEvent(RESTORE_INFO_RECEIVED, data);
            }
        }
    };

    private IabHelper.OnConsumeFinishedListener _removePurchaseFromQueueListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            if (result.isFailure())
                _dispatchEvent(CONSUME_ERROR, result.getMessage());
            else {

                String resultString = _purchaseToResultString(purchase);
                _dispatchEvent(CONSUME_SUCCESSFUL, resultString);
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

            String key = getStringFromFREObject(args[0]);
            Boolean debug = getBooleanFromFREObject(args[1]);

            if (_iabHelper != null) {

                try {
                    _iabHelper.dispose();
                }
                catch (IabHelper.IabAsyncInProgressException exception) {
                    _dispatchEvent(INIT_ERROR, exception.getMessage());
                }
            }

            _iabHelper = new IabHelper(_freActivity, key);
            _iabHelper.enableDebugLogging(debug, TAG);
            _iabHelper.startSetup(_initLibListener);

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

            try {
                _iabHelper.queryInventoryAsync(true, skusName, skusSubsName, _getProductsInfoListener);
            }
            catch (IabHelper.IabAsyncInProgressException exception) {
                _dispatchEvent(PRODUCT_INFO_ERROR, exception.getMessage());
            }

            return null;
        }
    };

    private FREFunction makePurchase = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            String purchaseId = getStringFromFREObject(args[0]);

            if (purchaseId == null)
                _dispatchEvent(PURCHASE_ERROR, "null purchaseId");
            else {

                try {
                    _iabHelper.launchPurchaseFlow(_freActivity, purchaseId, RC_REQUEST, _onIabPurchaseFinishedListener);
                }
                catch (IabHelper.IabAsyncInProgressException exception) {
                    _dispatchEvent(PURCHASE_ERROR, exception.getMessage());
                }
            }

            return null;
        }
    };

    private FREFunction makeSubscription = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            String purchaseId = getStringFromFREObject(args[0]);

            if (purchaseId == null)
                _dispatchEvent(PURCHASE_ERROR, "null purchaseId");
            else {

                try {
                    _iabHelper.launchSubscriptionPurchaseFlow(_freActivity, purchaseId, RC_REQUEST, _onIabPurchaseFinishedListener);
                }
                catch (IabHelper.IabAsyncInProgressException exception) {
                    _dispatchEvent(PURCHASE_ERROR, exception.getMessage());
                }
            }

            return null;
        }
    };

    private FREFunction restoreTransaction = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            try {
                _iabHelper.queryInventoryAsync(_restoreTransactionListener);
            }
            catch (IabHelper.IabAsyncInProgressException exception) {
                _dispatchEvent(RESTORE_INFO_ERROR, exception.getMessage());
            }

            return null;
        }
    };

    private FREFunction removePurchaseFromQueue = new BaseFunction() {
        @Override
        public FREObject call(FREContext ctx, FREObject[] args) {

            String receipt = getStringFromFREObject(args[1]);
            JSONObject receiptJson = null;
            String signedData = null;
            Purchase purchase = null;

            try {

                receiptJson = new JSONObject(receipt);
                signedData = receiptJson.getString("signedData");

                if (signedData == null)
                    throw new JSONException("null signedData");

                purchase = new Purchase(IabHelper.ITEM_TYPE_INAPP, signedData, null); // TODO ITEM_TYPE_SUBS for subs?
            }
            catch (JSONException jsonException) {
                _dispatchEvent(CONSUME_ERROR, jsonException.getMessage());
            }

            try {
                _iabHelper.consumeAsync(purchase, _removePurchaseFromQueueListener);
            }
            catch (IabHelper.IabAsyncInProgressException exception) {
                _dispatchEvent(CONSUME_ERROR, exception.getMessage());
            }

            return null;
        }
    };

    /**
     *
     * FREContext SETUP
     *
     */

    public void dispose() {

        _freActivity = null;

        if (_iabHelper != null) {

            try {
                _iabHelper.dispose();
            }
            catch (IabHelper.IabAsyncInProgressException exception) {
                exception.printStackTrace();
            }

            _iabHelper = null;
        }

        if (aaw != null) {

            aaw.removeActivityResultListener(_activityResultCallback);
            aaw = null;
        }
    }

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