package com.freshplanet.inapppurchase;

import android.os.Handler;
import android.util.Log;

import com.freshplanet.inapppurchase.BillingService.RequestPurchase;
import com.freshplanet.inapppurchase.BillingService.RestoreTransactions;
import com.freshplanet.inapppurchase.Consts.PurchaseState;
import com.freshplanet.inapppurchase.Consts.ResponseCode;

public class CashPurchaseObserver extends PurchaseObserver {

	
    public CashPurchaseObserver(Handler handler) {
        super(Extension.context.getActivity(), handler);
    }

	
	private static String TAG = "CashPurchaseObserver";
	
	@Override
	public void onBillingSupported(boolean supported) {
		Log.d(TAG, "onBillingSupported");
		if (supported)
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_ENABLED", "Yes");
		} else
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_DISABLED", "Yes");
		}
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState,
			String itemId, int quantity, long purchaseTime,
			String developerPayload) {
		Log.d(TAG, "onPurchaseStateChange");
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request,
			ResponseCode responseCode) {
		Log.d(TAG, "onRequestPurchaseResponse");
		
		if (responseCode != Consts.ResponseCode.RESULT_OK && responseCode != Consts.ResponseCode.RESULT_USER_CANCELED)
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", responseCode.toString());
		}
		
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode) {
		Log.d(TAG, "onRestoreTransactionsResponse");
	}


}
