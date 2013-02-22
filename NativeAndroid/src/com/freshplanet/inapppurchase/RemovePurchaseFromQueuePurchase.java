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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class RemovePurchaseFromQueuePurchase implements FREFunction {

	private static String TAG = "RemovePurchase";
	
	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Log.d(TAG, "starting confirming purchases");
		
		//String productId;
		//String notifyId;
		//String startId;
		String receipt = null;
		try {
			receipt = arg1[1].getAsString();
			Log.d(TAG, "receipt "+receipt);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		

		
		if (receipt == null)
		{
			Log.d(TAG, "receipt is null");

			return null;
		}
			
		JSONObject object = null;
		try {
			object = new JSONObject(receipt);
			Log.d(TAG, "getting json object "+object.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (object == null)
		{
			Log.d(TAG, "json object is null ");
			return null;
		}
				
		String signedData = null;
		try {
			signedData = object.getString("signedData");
			Log.d(TAG, "getting signed data "+signedData);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (signedData == null)
		{
			Log.d(TAG, "signed data is null ");
			return null;
		}
		
		JSONObject signedObject = null;
		
		try {

			signedObject = new JSONObject(signedData);
			Log.d(TAG, "getting signed object "+signedObject.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (signedObject == null)
		{
			Log.d(TAG, "signed object is null ");
			return null;
		}
		
		Long nonce = 0L;
		try {
			nonce = signedObject.getLong("nonce");
			Log.d(TAG, "getting nonce "+nonce.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONArray orders = null;
		try {
			orders = signedObject.getJSONArray("orders");
			Log.d(TAG, "getting orders "+orders.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d(TAG, "number of orders "+Integer.toString(orders.length()));
		
		String[] notifyIds = new String[orders.length()];

		Log.d(TAG, "notifyIds");
		for (int i =0; i < orders.length(); i++)
		{
			JSONObject order = null;
			try {
				order = orders.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (order == null)
			{
				Log.d(TAG, "order is null");

				continue;
			}
			
			try {
				notifyIds[i] = order.getString("notificationId");
				Log.d(TAG, "notifyId "+order.getString("notificationId"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		Log.d(TAG, "starting intent");
		
		
        Intent intent = new Intent(Consts.ACTION_CONFIRM_NOTIFICATION);
        intent.setClass(arg0.getActivity(), BillingService.class);
        intent.putExtra(Consts.NOTIFICATION_ID, notifyIds);
        arg0.getActivity().startService(intent);
		
		Security.removeNonce(nonce);

		return null;
		
	}

}
