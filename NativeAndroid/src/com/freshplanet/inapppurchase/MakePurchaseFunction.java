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

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Log.d("makePurchase", "v2.6");
		
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
		Log.d("makePurchase", "purchase id : "+purchaseId);
		
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
