
package com.freshplanet.inapppurchase;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

public class ExtensionContext extends FREContext {

	private static String TAG = "InAppExtensionContext";

	public ExtensionContext() {
		Log.d(TAG, "ExtensionContext.C2DMExtensionContext");
	}
	
	@Override
	public void dispose() {
		Log.d(TAG, "ExtensionContext.dispose");
		Extension.context = null;
	}

	/**
	 * Registers AS function name to Java Function Class
	 */
	@Override
	public Map<String, FREFunction> getFunctions() {
		Log.d(TAG, "ExtensionContext.getFunctions");
		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		functionMap.put("getProductsInfo", new GetProductsInfoFunction());
		functionMap.put("makePurchase", new MakePurchaseFunction());
		functionMap.put("userCanMakeAPurchase", new UserCanMakeAPurchaseFunction());
		functionMap.put("removePurchaseFromQueue", new RemovePurchaseFromQueuePurchase());
		return functionMap;	
	}

}
