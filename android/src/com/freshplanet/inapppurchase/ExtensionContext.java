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
import java.util.Map;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.freshplanet.inapppurchase.functions.GetProductsInfoFunction;
import com.freshplanet.inapppurchase.functions.InitFunction;
import com.freshplanet.inapppurchase.functions.MakePurchaseFunction;
import com.freshplanet.inapppurchase.functions.MakeSubscriptionFunction;
import com.freshplanet.inapppurchase.functions.RemovePurchaseFromQueuePurchase;
import com.freshplanet.inapppurchase.functions.RestoreTransactionFunction;
import com.freshplanet.inapppurchase.utils.IabHelper;
import com.freshplanet.inapppurchase.utils.IabResult;
import com.freshplanet.inapppurchase.utils.Inventory;
import com.freshplanet.inapppurchase.utils.Purchase;

public class ExtensionContext extends FREContext implements IabHelper.OnIabSetupFinishedListener,
						   			  						IabHelper.OnConsumeFinishedListener,
						   			  						IabHelper.QueryInventoryFinishedListener
{
	public ExtensionContext() {}
	
	@Override
	public void dispose()
	{
		Extension.context = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions()
	{
		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		
		functionMap.put("initLib", new InitFunction());
		functionMap.put("getProductsInfo", new GetProductsInfoFunction());
		functionMap.put("makePurchase", new MakePurchaseFunction());
		functionMap.put("restoreTransaction", new RestoreTransactionFunction());
		functionMap.put("removePurchaseFromQueue", new RemovePurchaseFromQueuePurchase());
		functionMap.put("makeSubscription", new MakeSubscriptionFunction());
		
		return functionMap;	
	}
	
	private IabHelper _iabHelper;

	private String _publicKey;
	private Boolean _debugEnabled;

	public IabHelper getIabHelper()
	{
		return _iabHelper;
	}

	public String getPublicKey()
	{
		return _publicKey;
	}

	public Boolean getDebugEnabled()
	{
		return _debugEnabled;
	}

	public void setupIab(String key, Boolean debug)
	{
		Extension.log("Initializing IAB Helper with Key: " + key);

		_publicKey = key;
		_debugEnabled = debug;

		if (_iabHelper != null)
		{
			_iabHelper.dispose();
		}

		_iabHelper = new IabHelper(getActivity(), _publicKey);
		_iabHelper.enableDebugLogging(_debugEnabled);
		_iabHelper.startSetup(this);
	}

	public void onIabSetupFinished(IabResult result)
	{
		if (result.isSuccess())
		{
			Extension.log("Initialized IAB Helper successfully");
			dispatchStatusEventAsync("INIT_SUCCESSFULL", "");
		} else
		{
			Extension.log("Failed to initialize IAB Helper: " + result.getMessage());
			dispatchStatusEventAsync("INIT_ERROR", result.getMessage());
		}
	}
	
	public void onConsumeFinished(Purchase purchase, IabResult result)
	{
		if (result.isSuccess())
		{
			Extension.log("Successfully consumed: " + purchase);
		}
		else
		{
			Extension.log("Failed to consume: " + purchase + ". Error: " + result.getMessage());
		}
    }
	
	public void onQueryInventoryFinished(IabResult result, Inventory inventory)
	{
		if (result.isSuccess())
		{
			Extension.log("Query inventory successful: " + inventory);
			String data = inventory != null ? inventory.toString() : "";
			dispatchStatusEventAsync("RESTORE_INFO_RECEIVED", data);
		} else
		{
			Extension.log("Failed to query inventory: " + result.getMessage());
			dispatchStatusEventAsync("PRODUCT_INFO_ERROR", "ERROR");
		}
	}
}
