package com.freshplanet.inapppurchase;

import android.os.Handler;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class RestoreTransactionFunction implements FREFunction {

	private static String TAG = "RestoreTransaction";
	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Log.d(TAG, "restoring transactions");
		
		BillingService service = new BillingService();
		service.setContext(arg0.getActivity());
		
		// register a cash purchase observer for ui.
		ResponseHandler.register( new CashPurchaseObserver(new Handler()));

		
		service.restoreTransactions();
		return null;
	}

}
