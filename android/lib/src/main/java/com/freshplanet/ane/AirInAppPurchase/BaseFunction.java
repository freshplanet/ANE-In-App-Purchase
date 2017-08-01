/**
 * Copyright 2017 FreshPlanet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freshplanet.ane.AirInAppPurchase;

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
