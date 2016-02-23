package com.freshplanet.inapppurchase.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.freshplanet.inapppurchase.Extension;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Purchase;

public class BillingActivity extends Activity implements IabHelper.OnIabPurchaseFinishedListener {

	public static String MAKE_PURCHASE = "MakePurchase";
	public static String MAKE_SUBSCRIPTION = "MakeSubscription";
	
    static final int RC_REQUEST = 10001;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Extension.log("BillingActivity.onCreate");
		
		if (Extension.context == null) {

			Extension.log("Extension context is null");
            Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "null context");

			finish();
			return;
		}

		Bundle extras = getIntent().getExtras();
		String type = extras.getString("type");
		String purchaseId = extras.getString("purchaseId");

		if (type.equals(MAKE_PURCHASE)) {

			try {
				Extension.context.getIabHelper().launchPurchaseFlow(this, purchaseId, RC_REQUEST, this, null);
			}
			catch (IllegalStateException e) {

				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "IllegalStateException");
		    	finish();
			}
		}
		else if (type.equals(MAKE_SUBSCRIPTION)) {

			try {
				Extension.context.getIabHelper().launchSubscriptionPurchaseFlow(this, purchaseId, RC_REQUEST, this);
			}
			catch (IllegalStateException e) {

				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "IllegalStateException");
				finish();
			}
		}
		else {

			Extension.log("Unsupported billing type: " + type);
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "unsupported billing type");
	    	finish();
		}
	}

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

        if (Extension.context == null) {

            Extension.log("Extension context is null");
            finish();
            return;
        }

        if (result.isSuccess()) {

            Extension.log("Purchase successful");

            String resultString = purchaseToResultString(purchase);

            if (resultString != null)
                Extension.context.dispatchStatusEventAsync("PURCHASE_SUCCESSFUL", resultString);
            else
                Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "null resultString");

        }
        else if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
            Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "RESULT_USER_CANCELED");
        }
        else {

            Extension.log("Purchase error: " + result.getMessage());
            Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", result.getMessage());
        }

        finish();
    }

    public static String purchaseToResultString(Purchase purchase) {

        String resultString = null;

        try {

            JSONObject receiptObject = new JSONObject();
            receiptObject.put("signedData", purchase.getOriginalJson());
            receiptObject.put("signature", purchase.getSignature());

            JSONObject resultObject = new JSONObject();
            resultObject.put("productId", purchase.getSku());
            resultObject.put("receiptType", "GooglePlay");
            resultObject.put("receipt", receiptObject);

            resultString = resultObject.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return resultString;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Extension.log("BillingActivity.onActivityResult - requestCode = " + requestCode + " - resultCode = " + resultCode);

        if (Extension.context == null) {

            Extension.log("Extension context is null");
            finish();
            return;
        }

        Extension.context.getIabHelper().handleActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onStart() {

		super.onStart();
		Extension.log("BillingActivity.onStart");
	}
    
	@Override
    protected void onRestart() {

		super.onRestart();
		Extension.log("BillingActivity.onRestart");
		finish();
	}

	@Override
    protected void onResume() {

		super.onResume();
		Extension.log("BillingActivity.onResume");
	}

	@Override
    protected void onPause() {

		super.onPause();
		Extension.log("BillingActivity.onPause");
	}

	@Override
    protected void onStop() {

		super.onStop();
		Extension.log("BillingActivity.onStop");
	}

	@Override
    protected void onDestroy() {

		super.onDestroy();
		Extension.log("BillingActivity.onDestroy");
	}
}