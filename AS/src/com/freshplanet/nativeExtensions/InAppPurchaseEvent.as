package com.freshplanet.nativeExtensions
{
	import flash.events.Event;
	
	public class InAppPurchaseEvent extends Event
	{
		
		// init -> check if previously purchases not being processed by the app
		public static const PURCHASE_SUCCESSFULL:String = "purchaseSuccesfull";
		public static const PURCHASE_ERROR:String   	= "purchaseError";
		
		// user can make a purchase
		public static const PURCHASE_ENABLED:String = "purchaseEnabled";
		// user cannot make a purchase
		public static const PURCHASE_DISABLED:String = "purchaseDisabled";
		
		public static const PRODUCT_INFO_RECEIVED:String = "productInfoReceived";
		public static const PRODUCT_INFO_ERROR:String = "productInfoError";

		
		
		// json encoded string (if any)
		public var data:String;
		
		public function InAppPurchaseEvent(type:String, data:String = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			this.data = data;
			super(type, bubbles, cancelable);
		}
	}
}