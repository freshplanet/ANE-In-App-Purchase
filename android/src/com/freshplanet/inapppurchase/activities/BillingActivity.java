package com.freshplanet.inapppurchase.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.freshplanet.inapppurchase.Extension;
import com.freshplanet.inapppurchase.utils.IabHelper;
import com.freshplanet.inapppurchase.utils.IabResult;
import com.freshplanet.inapppurchase.utils.Purchase;

public class BillingActivity extends Activity implements IabHelper.OnIabPurchaseFinishedListener
{
	public static String MAKE_PURCHASE = "MakePurchase";
	public static String MAKE_SUBSCRIPTION = "MakeSubscription";

	static final int RC_REQUEST = 10001;

	private IabHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Extension.log("BillingActivity.onCreate");

		if (Extension.context == null)
		{
			Extension.log("Extension context is null");
			finish();
			return;
		}

		Bundle extras = getIntent().getExtras();
		final String type = extras.getString("type");
		final String purchaseId = extras.getString("purchaseId");

		mHelper = new IabHelper(this, Extension.context.getPublicKey());
		mHelper.enableDebugLogging(Extension.context.getDebugEnabled());
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
		{
			public void onIabSetupFinished(IabResult result)
			{
				Extension.log("BillingActivity helper.onIabSetupFinished");

				if (!result.isSuccess())
				{
					// Oh noes, there was a problem.
					Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
					finish();
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null) return;

				if (type.equals(MAKE_PURCHASE))
				{
					try
					{
						mHelper.launchPurchaseFlow(BillingActivity.this, purchaseId, RC_REQUEST, BillingActivity.this, null);
					} catch (IllegalStateException e)
					{
						Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
						finish();
					}
				} else if (type.equals(MAKE_SUBSCRIPTION))
				{
					try
					{
						mHelper.launchSubscriptionPurchaseFlow(BillingActivity.this, purchaseId, RC_REQUEST, BillingActivity.this);
					} catch (IllegalStateException e)
					{
						Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
						finish();
					}
				} else
				{
					Extension.log("Unsupported billing type: " + type);
					Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
					finish();
				}
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Extension.log("BillingActivity.onStart");
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		Extension.log("BillingActivity.onRestart");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Extension.log("BillingActivity.onResume");
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Extension.log("BillingActivity.onPause");
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Extension.log("BillingActivity.onStop");
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Extension.log("BillingActivity.onDestroy");

		if (mHelper != null)
		{
			Extension.log("BillingActivity.onDestroy - canceling pending purchase");
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "RESULT_USER_CANCELED");
			mHelper.dispose();
			mHelper = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Extension.log("BillingActivity.onActivityResult - requestCode = " + requestCode + " - resultCode = " + resultCode);

		if (Extension.context == null)
		{
			Extension.log("Extension context is null");
			finish();
			return;
		}

		mHelper.handleActivityResult(requestCode, resultCode, data);
	}

	public void onIabPurchaseFinished(IabResult result, Purchase purchase)
	{
		if (Extension.context == null)
		{
			Extension.log("BillingActivity.onIabPurchaseFinished - Extension context is null");
			finish();
			return;
		}

		if (mHelper == null)
		{
			Extension.log("BillingActivity.onIabPurchaseFinished - helper has been disposed");
			finish();
			return;
		}

		if (result.isSuccess())
		{
			Extension.log("Purchase successful");
			try
			{
				JSONObject resultObject = new JSONObject();
				JSONObject receiptObject = new JSONObject();
				receiptObject.put("signedData", purchase.getOriginalJson());
				receiptObject.put("signature", purchase.getSignature());
				resultObject.put("productId", purchase.getSku());
				resultObject.put("receipt", receiptObject);
				resultObject.put("receiptType", "GooglePlay");
				Extension.context.dispatchStatusEventAsync("PURCHASE_SUCCESSFUL", resultObject.toString());
			} catch (JSONException e)
			{
				e.printStackTrace();
				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
			}
		} else
		{
			Extension.log("Purchase error: " + result.getMessage());
			Boolean isCancel = (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED);
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", isCancel ? "RESULT_USER_CANCELED" : result.getMessage());
		}

		mHelper.dispose();
		mHelper = null;

		finish();
	}
}
