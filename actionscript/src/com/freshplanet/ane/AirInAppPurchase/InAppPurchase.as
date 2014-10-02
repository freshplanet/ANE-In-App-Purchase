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

package com.freshplanet.ane.AirInAppPurchase
{
	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;

	public class InAppPurchase extends EventDispatcher
	{
		private static var _instance:InAppPurchase;
		
		private var extCtx:*;
		
		public function InAppPurchase()
		{
			if (!_instance)
			{
				if (this.isInAppPurchaseSupported)
				{
					extCtx = ExtensionContext.createExtensionContext("com.freshplanet.AirInAppPurchase", null);
					if (extCtx != null)
					{
						extCtx.addEventListener(StatusEvent.STATUS, onStatus);
					} else
					{
						trace('[InAppPurchase] extCtx is null.');
					}
				}
			_instance = this;
			}
			else
			{
				throw Error( 'This is a singleton, use getInstance, do not call the constructor directly');
			}
		}
		
		
		public static function getInstance():InAppPurchase
		{
			return _instance != null ? _instance : new InAppPurchase();
		}
		
		
		public function init(googlePlayKey:String, debug:Boolean = false):void
		{
			if (this.isInAppPurchaseSupported)
			{
				trace("[InAppPurchase] init library");
				extCtx.call("initLib", googlePlayKey, debug);
			}
		}
		
		public function makePurchase(productId:String ):void
		{
			if (this.isInAppPurchaseSupported)
			{
				trace("[InAppPurchase] purchasing", productId);
				extCtx.call("makePurchase", productId);
			} else
			{
				this.dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ERROR, "InAppPurchase not supported"));
			}
		}
		
		// receipt is for android device.
		public function removePurchaseFromQueue(productId:String, receipt:String):void
		{
			if (this.isInAppPurchaseSupported)
			{
				trace("[InAppPurchase] removing product from queue", productId, receipt);
				extCtx.call("removePurchaseFromQueue", productId, receipt);
				
				if (Capabilities.manufacturer.indexOf("iOS") > -1)
				{
					_iosPendingPurchases = _iosPendingPurchases.filter(function(jsonPurchase:String, index:int, purchases:Vector.<Object>):Boolean {
						try
						{
							var purchase:Object = JSON.parse(jsonPurchase);
							return JSON.stringify(purchase.receipt) != receipt;
						} 
						catch(error:Error)
						{
							trace("[InAppPurchase] Couldn't parse purchase: " + jsonPurchase);
						}
						return false;
					});
				}
			}
		}
		
		
		
		public function getProductsInfo(productsId:Array, subscriptionIds:Array):void
		{
			if (this.isInAppPurchaseSupported)
			{
				trace("[InAppPurchase] get Products Info");
				extCtx.call("getProductsInfo", productsId, subscriptionIds);
			} else
			{
				this.dispatchEvent( new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_ERROR) );
			}

		}
		
		
		public function userCanMakeAPurchase():void 
		{
			if (this.isInAppPurchaseSupported)
			{
				trace("[InAppPurchase] check user can make a purchase");
				extCtx.call("userCanMakeAPurchase");
			} else
			{
				this.dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_DISABLED));
			}
		}
			
		public function userCanMakeASubscription():void
		{
			if (Capabilities.manufacturer.indexOf('Android') > -1)
			{
				trace("[InAppPurchase] check user can make a purchase");
				extCtx.call("userCanMakeASubscription");
			} else
			{
				this.dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_DISABLED));
			}
		}
		
		public function makeSubscription(productId:String):void
		{
			if (Capabilities.manufacturer.indexOf('Android') > -1)
			{
				trace("[InAppPurchase] check user can make a subscription");
				extCtx.call("makeSubscription", productId);
			} else
			{
				this.dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ERROR, "InAppPurchase not supported"));
			}
		}
		
		
		public function restoreTransactions():void
		{
			if (Capabilities.manufacturer.indexOf('Android') > -1)
			{
				extCtx.call("restoreTransaction");
			}
			else if (Capabilities.manufacturer.indexOf("iOS") > -1)
			{
				var jsonPurchases:String = "[" + _iosPendingPurchases.join(",") + "]";
				var jsonData:String = "{ \"purchases\": " + jsonPurchases + "}";
				dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.RESTORE_INFO_RECEIVED, jsonData));
			}
		}


		public function stop():void
		{
			if (Capabilities.manufacturer.indexOf('Android') > -1)
			{
				trace("[InAppPurchase] stop library");
				extCtx.call("stopLib");
			}
		}

		
		public function get isInAppPurchaseSupported():Boolean
		{
			var value:Boolean = Capabilities.manufacturer.indexOf('iOS') > -1 || Capabilities.manufacturer.indexOf('Android') > -1;
			trace(value ? '[InAppPurchase]  in app purchase is supported ' : '[InAppPurchase]  in app purchase is not supported ');
			return value;
		}
		
		private var _iosPendingPurchases:Vector.<Object> = new Vector.<Object>();
		
		private function onStatus(event:StatusEvent):void
		{
			trace(event);
			var e:InAppPurchaseEvent;
			switch(event.code)
			{
				case "PRODUCT_INFO_SUCCESS":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_RECEIVED, event.level);
					break;
				case "PURCHASE_SUCCESSFUL":
					if (Capabilities.manufacturer.indexOf("iOS") > -1)
					{
						_iosPendingPurchases.push(event.level);
					}
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_SUCCESSFULL, event.level);
					break;
				case "PURCHASE_ERROR":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ERROR, event.level);
					break;
				case "PURCHASE_ENABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ENABLED, event.level);
					break;
				case "PURCHASE_DISABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_DISABLED, event.level);
					break;
				case "PRODUCT_INFO_ERROR":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_ERROR);
					break;
				case "SUBSCRIPTION_ENABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.SUBSCRIPTION_ENABLED);
					break;
				case "SUBSCRIPTION_DISABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.SUBSCRIPTION_DISABLED);
					break;
				case "RESTORE_INFO_RECEIVED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.RESTORE_INFO_RECEIVED, event.level);
					break;
				default:
				
			}
			if (e)
			{
				this.dispatchEvent(e);
			}
			
		}
		
		
		
	}
}