package com.freshplanet.inapppurchase.functions;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.freshplanet.inapppurchase.Extension;

public class RestoreTransactionFunction extends BaseFunction implements IabHelper.QueryInventoryFinishedListener {

	@Override
	public FREObject call(FREContext context, FREObject[] args) {

		super.call(context, args);
		
		Extension.log("Restoring transactions");
		
		try {
			Extension.context.getIabHelper().queryInventoryAsync(false, this);
		}
		catch (Exception e) {

			e.printStackTrace();
            Extension.context.dispatchStatusEventAsync("RESTORE_INFO_ERROR", "Exception");
		}
		
		return null;
	}

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inv) {

        if (Extension.context == null) {

            Extension.log("Extension context is null");
            return;
        }

        if (result.isSuccess()) {

            Extension.log("Successful query for restorable purchases");

            String data = inv != null ? inv.toString() : "";
            Extension.context.dispatchStatusEventAsync("RESTORE_INFO_RECEIVED", data);
        }
        else {

            Extension.log("Failed query for restorable purchases: " + result.getMessage());
            Extension.context.dispatchStatusEventAsync("RESTORE_INFO_ERROR", result.getMessage());
        }
    }
}