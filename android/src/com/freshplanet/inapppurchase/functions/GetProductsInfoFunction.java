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

import java.util.List;

import com.adobe.fre.FREArray;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.freshplanet.inapppurchase.Extension;
import com.freshplanet.inapppurchase.utils.IabHelper;
import com.freshplanet.inapppurchase.utils.IabResult;
import com.freshplanet.inapppurchase.utils.Inventory;

public class GetProductsInfoFunction extends BaseFunction
{
    IabHelper.QueryInventoryFinishedListener listener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
        	if (Extension.context == null)
    		{
    			Extension.log("Extension context is null");
    			return;
    		}
        	
        	if (result.isSuccess())
    		{
    			Extension.log("Query inventory successful");
    			
    			String data = inventory != null ? inventory.toString() : "";
    	        Extension.context.dispatchStatusEventAsync("PRODUCT_INFO_RECEIVED", data) ;
    		}
    		else
    		{
    			Extension.log("Failed to query inventory: " + result.getMessage());
    			Extension.context.dispatchStatusEventAsync("PRODUCT_INFO_ERROR", "ERROR");
    		}
        }
    };

	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		super.call(context, args);
		
		List<String> skusName = getListOfStringFromFREArray((FREArray)args[0]);

		Extension.context.getIabHelper().queryInventoryAsync(true , skusName, listener);
		
		return null;
	}
}
