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

package com.freshplanet.inapppurchase.functions;

import com.example.android.trivialdrivesample.util.IabResult;
import com.freshplanet.inapppurchase.activities.BillingActivity;
import org.json.JSONException;
import org.json.JSONObject;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.freshplanet.inapppurchase.Extension;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.Purchase;

public class RemovePurchaseFromQueuePurchase extends BaseFunction implements IabHelper.OnConsumeFinishedListener {

	@Override
	public FREObject call(FREContext context, FREObject[] args) {

		super.call(context, args);
		
		String receipt = getStringFromFREObject(args[1]);
		
		Extension.log("Consuming purchase with receipt: " + receipt);
		
		String signedData = null;

		try {

            JSONObject receiptJson = new JSONObject(receipt);
			signedData = receiptJson.getString("signedData");
		}
		catch (JSONException e) {

			e.printStackTrace();
            Extension.context.dispatchStatusEventAsync("CONSUME_ERROR", "JSONException");
		}
		
		if (signedData == null) {

			Extension.log("Can't consume purchase with null signedData");
            Extension.context.dispatchStatusEventAsync("CONSUME_ERROR", "null signedData");
			return null;
		}
		
		Extension.log("Consuming purchase with signedData: " + signedData);
		
		try {

			Purchase purchase = new Purchase(IabHelper.ITEM_TYPE_INAPP, signedData, null);
			Extension.context.getIabHelper().consumeAsync(purchase, this);
		}
		catch (JSONException e) {

			e.printStackTrace();
            Extension.context.dispatchStatusEventAsync("CONSUME_ERROR", "JSONException");
		}
		
		return null;
	}

    @Override
    public void onConsumeFinished(Purchase purchase, IabResult result) {

        if (result.isSuccess()) {

            Extension.log("Successfully consumed: " + purchase);
            String resultString = BillingActivity.purchaseToResultString(purchase);
            Extension.context.dispatchStatusEventAsync("CONSUME_SUCCESSFUL", resultString);
        }
        else {

            Extension.log("Failed to consume: " + purchase + ". Error: " + result.getMessage());
            Extension.context.dispatchStatusEventAsync("CONSUME_ERROR", result.getMessage());
        }
    }
}