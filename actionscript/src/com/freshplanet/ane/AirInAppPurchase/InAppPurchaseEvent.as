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

package com.freshplanet.ane.AirInAppPurchase {

	import flash.events.Event;
	
	public class InAppPurchaseEvent extends Event {

        public static const INIT_SUCCESSFUL:String = "INIT_SUCCESSFUL";
        public static const INIT_ERROR:String = "INIT_ERROR";

        public static const PURCHASE_SUCCESSFUL:String = "PURCHASE_SUCCESSFUL";
        public static const PURCHASE_ERROR:String = "PURCHASE_ERROR";

        public static const CONSUME_SUCCESSFUL:String = "CONSUME_SUCCESSFUL";
        public static const CONSUME_ERROR:String = "CONSUME_ERROR";

		public static const PURCHASE_ENABLED:String = "PURCHASE_ENABLED";    // user can make a purchase
		public static const PURCHASE_DISABLED:String = "PURCHASE_DISABLED";  // user cannot make a purchase
		
		public static const SUBSCRIPTION_ENABLED:String = "SUBSCRIPTION_ENABLED";    // user can make a subscription
		public static const SUBSCRIPTION_DISABLED:String = "SUBSCRIPTION_DISABLED";  // user cannot make a subscription

		public static const PRODUCT_INFO_RECEIVED:String = "PRODUCT_INFO_RECEIVED";
		public static const PRODUCT_INFO_ERROR:String = "PRODUCT_INFO_ERROR";

        public static const RESTORE_INFO_RECEIVED:String = "RESTORE_INFO_RECEIVED";
        public static const RESTORE_INFO_ERROR:String = "RESTORE_INFO_ERROR";
		
		private var _data:String = null;

        /**
         *
         * @param type
         * @param eventData
         * @param bubbles
         * @param cancelable
         */
		public function InAppPurchaseEvent(type:String, eventData:String = null, bubbles:Boolean = false, cancelable:Boolean = false) {

			_data = eventData;
			super(type, bubbles, cancelable);
		}

        /**
         * json encoded string (if any)
         */
        public function get data():String {
            return _data;
        }
	}
}