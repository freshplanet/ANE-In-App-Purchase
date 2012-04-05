package com.freshplanet.nativeExtensions
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
						trace('extCtx is null.');
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
		
		
		
		public function makePurchase(productId:String ):void
		{
			if (this.isInAppPurchaseSupported)
			{
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
				extCtx.call("removePurchaseFromQueue", productId, receipt);
			}
		}
		
		
		
		public function getProductsInfo(productsId:Array):void
		{
			if (this.isInAppPurchaseSupported)
			{
				extCtx.call("getProductsInfo", productsId);
			} else
			{
				this.dispatchEvent( new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_ERROR) );
			}

		}
		
		
		public function userCanMakeAPurchase():void 
		{
			if (this.isInAppPurchaseSupported)
			{
				extCtx.call("userCanMakeAPurchase");
			} else
			{
				this.dispatchEvent(new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_DISABLED));
			}
			
		}
			
		
		
		public function get isInAppPurchaseSupported():Boolean
		{
			var value:Boolean = Capabilities.manufacturer.indexOf('iOS') > -1 || Capabilities.manufacturer.indexOf('Android') > -1;
			trace(' is app purchase supported ? '+value);
			return value;
		}
		
		
		
		private function onStatus(event:StatusEvent):void
		{
			trace(event);
			var e:InAppPurchaseEvent;
			switch(event.code)
			{
				case "PURCHASE_SUCCESSFUL":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_SUCCESSFULL, event.level);
					break;
				case "PURCHASE_ERROR":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ERROR, event.level);
					break;
				case "PURCHASE_ENABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_ENABLED);
					break;
				case "PURCHASE_DISABLED":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PURCHASE_DISABLED);
					break;
				case "PRODUCT_INFO_SUCCESS":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_RECEIVED, event.level);
					break;
				case "PRODUCT_INFO_ERROR":
					e = new InAppPurchaseEvent(InAppPurchaseEvent.PRODUCT_INFO_ERROR);
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