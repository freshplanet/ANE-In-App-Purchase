/*
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
package com.freshplanet.ane.AirInAppPurchase {
public class InAppPurchaseProrationMode {
	/***************************
	 *
	 * PUBLIC
	 *
	 ***************************/

	static public const UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY : InAppPurchaseProrationMode = new InAppPurchaseProrationMode(Private, 0);
	static public const IMMEDIATE_WITH_TIME_PRORATION                 : InAppPurchaseProrationMode = new InAppPurchaseProrationMode(Private, 1);
	static public const IMMEDIATE_AND_CHARGE_PRORATED_PRICE           : InAppPurchaseProrationMode = new InAppPurchaseProrationMode(Private, 2);
	static public const IMMEDIATE_WITHOUT_PRORATION                   : InAppPurchaseProrationMode = new InAppPurchaseProrationMode(Private, 3);
	static public const DEFERRED                    				  : InAppPurchaseProrationMode = new InAppPurchaseProrationMode(Private, 4);


	public static function fromValue(value:String):InAppPurchaseProrationMode {

		switch (value)
		{
			case UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY.value:
				return UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY;
				break;
			case IMMEDIATE_WITH_TIME_PRORATION.value:
				return IMMEDIATE_WITH_TIME_PRORATION;
				break;
			case IMMEDIATE_AND_CHARGE_PRORATED_PRICE.value:
				return IMMEDIATE_AND_CHARGE_PRORATED_PRICE;
				break;
			case IMMEDIATE_WITHOUT_PRORATION.value:
				return IMMEDIATE_WITHOUT_PRORATION;
				break;
			case DEFERRED.value:
				return DEFERRED;
				break;

			default:
				return null;
				break;
		}
	}

	public function get value():int {
		return _value;
	}

	/***************************
	 *
	 * PRIVATE
	 *
	 ***************************/

	private var _value:int;

	public function InAppPurchaseProrationMode(access:Class, value:int) {

		if (access != Private)
			throw new Error("Private constructor call!");

		_value = value;
	}
}
}
final class Private {}