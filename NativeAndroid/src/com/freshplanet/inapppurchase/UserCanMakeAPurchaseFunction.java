package com.freshplanet.inapppurchase;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

public class UserCanMakeAPurchaseFunction implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		BillingService service = new BillingService();
		
		
		// TODO Auto-generated method stub
		
		FREObject returnedValue;
		try {
			returnedValue = FREObject.newObject( service.checkBillingSupported() );
		} catch (FREWrongThreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			returnedValue = null;
		}
		return returnedValue;
	}

}
