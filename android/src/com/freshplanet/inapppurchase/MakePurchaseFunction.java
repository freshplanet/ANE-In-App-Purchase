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

import android.os.Handler;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class MakePurchaseFunction implements FREFunction {

    private static final String TAG = "MakePurchase";
	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Log.d(TAG, "v2.6");
		
		String purchaseId = null;
		try {
			purchaseId = arg1[0].getAsString();
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
		Log.d(TAG, "purchase id : "+purchaseId);
		
		// start a service.
		BillingService service = new BillingService();
		service.setContext(arg0.getActivity());
		
		// register a cash purchase observer for ui.
		ResponseHandler.register( new CashPurchaseObserver(new Handler()));
		
		if (purchaseId != null)
		{
			service.requestPurchase(purchaseId, null);
		}
		
		return null;
	}

}
