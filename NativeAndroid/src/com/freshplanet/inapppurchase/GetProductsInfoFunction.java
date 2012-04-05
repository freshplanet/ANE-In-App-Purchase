package com.freshplanet.inapppurchase;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class GetProductsInfoFunction implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		// TODO Auto-generated method stub
		
		arg0.dispatchStatusEventAsync("PRODUCT_INFO_ERROR", "not supported by android");
		
		return null;
	}

}
