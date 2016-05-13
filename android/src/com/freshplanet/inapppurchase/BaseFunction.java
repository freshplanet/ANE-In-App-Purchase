package com.freshplanet.inapppurchase;

import java.util.ArrayList;
import java.util.List;

import com.adobe.fre.FREArray;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class BaseFunction implements FREFunction {

	@Override
	public FREObject call(FREContext context, FREObject[] args) {
		return null;
	}
	
	protected String getStringFromFREObject(FREObject object) {

		try {
			return object.getAsString();
		}
		catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}
	
	protected Boolean getBooleanFromFREObject(FREObject object) {

		try {
			return object.getAsBool();
		}
		catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}
	
	protected List<String> getListOfStringFromFREArray(FREArray array) {

		List<String> result = new ArrayList<String>();
		
		try {

            for (int i = 0; i < array.getLength(); i++) {

				try {
					result.add(getStringFromFREObject(array.getObjectAt((long)i)));
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			return null;
		}
		
		return result;
	}
}
