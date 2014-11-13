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

import android.content.Intent;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.freshplanet.inapppurchase.Extension;
import com.freshplanet.inapppurchase.activities.BillingActivity;

public class MakePurchaseFunction extends BaseFunction
{
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{	
		super.call(context, args);
		
		String purchaseId = getStringFromFREObject(args[0]);
		if (purchaseId == null)
		{
			Extension.log("Can't make purchase with null purchaseId");
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
			return null;
		}
		
		Extension.log("Making purchase with ID: " + purchaseId);
		
		Intent i = new Intent(context.getActivity().getApplicationContext(), BillingActivity.class);
		i.putExtra("type", BillingActivity.MAKE_PURCHASE);
		i.putExtra("purchaseId", purchaseId);
		context.getActivity().startActivity(i);

		return null;
	}
}
