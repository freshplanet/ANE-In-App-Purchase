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
package com.freshplanet.ane.AirInAppPurchase {

	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;

    /**
     *
     */
	public class InAppPurchase extends EventDispatcher {

        // --------------------------------------------------------------------------------------//
        //																						 //
        // 									   PUBLIC API										 //
        // 																						 //
        // --------------------------------------------------------------------------------------//

        /**
         * If <code>true</code>, logs will be displayed at the ActionScript level.
         */
        public static var logEnabled:Boolean = false;

        /**
         *
         */
        public static function get isSupported():Boolean {
            return _isIOS() || _isAndroid();
        }

        /**
         *
         */
        public static function get instance():InAppPurchase {
            return _instance ? _instance : new InAppPurchase();
        }

        /**
         * INIT_SUCCESSFUL
         * INIT_ERROR
         * @param googlePlayKey
         * @param debug
         */
        public function init(googlePlayKey:String, debug:Boolean = false):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.INIT_ERROR, "InAppPurchase not supported");
            else
                _context.call("initLib", googlePlayKey, debug);
        }

        /**
         * PURCHASE_SUCCESSFUL
         * PURCHASE_ERROR
         * @param productId
         */
        public function makePurchase(productId:String):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.PURCHASE_ERROR, "InAppPurchase not supported");
            else
                _context.call("makePurchase", productId);
        }

        /**
         * PURCHASE_SUCCESSFUL
         * PURCHASE_ERROR
         * @param productId
         */
        public function makeSubscription(productId:String):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.PURCHASE_ERROR, "InAppPurchase not supported");
            else
                _context.call("makeSubscription", productId);
        }

        /**
         * CONSUME_SUCCESSFUL
         * CONSUME_ERROR
         * @param productId
         * @param receipt
         * @param developerPayload used on Android
         */
        public function removePurchaseFromQueue(productId:String, receipt:String, developerPayload:String = null):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.CONSUME_ERROR, "InAppPurchase not supported");
            else {

                _context.call("removePurchaseFromQueue", productId, receipt, developerPayload ? developerPayload : "");

                if (_isIOS()) {

                    var filterPurchase:Function = function(jsonPurchase:String, index:int, purchases:Vector.<Object>):Boolean {

                        try {

                            var purchase:Object = JSON.parse(jsonPurchase);
                            return JSON.stringify(purchase.receipt) != receipt;
                        }
                        catch (error:Error) {
                            _log("ERROR", "couldn't parse purchase: " + jsonPurchase);
                        }

                        return false;
                    };

                    _iosPendingPurchases = _iosPendingPurchases.filter(filterPurchase);
                }
            }
        }

        /**
         * PRODUCT_INFO_RECEIVED
         * PRODUCT_INFO_ERROR
         * @param productsIds
         * @param subscriptionIds
         */
        public function getProductsInfo(productsIds:Array, subscriptionIds:Array):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.PRODUCT_INFO_ERROR, "InAppPurchase not supported");
            else {

                productsIds ||= [];
                subscriptionIds ||= [];
                _context.call("getProductsInfo", productsIds, subscriptionIds);
            }
        }

        /**
         * RESTORE_INFO_RECEIVED
         * RESTORE_INFO_ERROR
         */
        public function restoreTransactions(restoreIOSHistory:Boolean=false):void {

            if (!isSupported)
                _dispatchEvent(InAppPurchaseEvent.RESTORE_INFO_ERROR, "InAppPurchase not supported");
            else if (_isAndroid() || restoreIOSHistory)
                _context.call("restoreTransaction");
            else if (_isIOS()) {

                var jsonPurchases:String = "[" + _iosPendingPurchases.join(",") + "]";
                var jsonData:String = "{ \"purchases\": " + jsonPurchases + "}";

                _dispatchEvent(InAppPurchaseEvent.RESTORE_INFO_RECEIVED, jsonData);
            }
        }


        public function clearTransactions():void 
        {
            _iosPendingPurchases = new Vector.<Object>();
            if (!isSupported || _isAndroid()) {
                _dispatchEvent("CLEAR_TRANSACTIONS_ERROR", "clear transactions not supported");
            } else if (_isIOS()) {
                _context.call("clearTransactions");
            }
        }

        // --------------------------------------------------------------------------------------//
        //																						 //
        // 									 	PRIVATE API										 //
        // 																						 //
        // --------------------------------------------------------------------------------------//

        private static const EXTENSION_ID:String = "com.freshplanet.ane.AirInAppPurchase";
        private static var _instance:InAppPurchase = null;
		private var _context:ExtensionContext = null;

        private var _iosPendingPurchases:Vector.<Object> = new Vector.<Object>();

        /**
         * "private" singleton constructor
         */
		public function InAppPurchase() {

            super();

            if (_instance)
                throw Error("This is a singleton, use getInstance(), do not call the constructor directly.");

            _instance = this;

            _context = ExtensionContext.createExtensionContext(EXTENSION_ID, null);

            if (!_context)
                _log("ERROR", "Extension context is null. Please check if extension.xml is setup correctly.");
            else
                _context.addEventListener(StatusEvent.STATUS, _onStatus);
		}

        /**
         *
         * @param type
         * @param eventData
         */
        private function _dispatchEvent(type:String, eventData:String):void {
            this.dispatchEvent(new InAppPurchaseEvent(type, eventData))
        }

        /**
         *
         * @param event
         */
		private function _onStatus(event:StatusEvent):void {

            if (event.code == InAppPurchaseEvent.PURCHASE_SUCCESSFUL && _isIOS())
                _iosPendingPurchases.push(event.level);
            else if(event.code == "DEBUG")
                _log("DEBUG", event.level);

            _dispatchEvent(event.code, event.level);
		}

        /**
         *
         * @param strings
         */
        private function _log(...strings):void {

            if (logEnabled) {

                strings.unshift(EXTENSION_ID);
                trace.apply(null, strings);
            }
        }

        /**
         *
         * @return
         */
        private static function _isIOS():Boolean {
			return Capabilities.manufacturer.indexOf("iOS") > -1 && Capabilities.os.indexOf("x86_64") < 0 && Capabilities.os.indexOf("i386") < 0;
        }

        /**
         *
         * @return
         */
        private static function _isAndroid():Boolean {
            return Capabilities.manufacturer.indexOf("Android") > -1;
        }
	}
}
