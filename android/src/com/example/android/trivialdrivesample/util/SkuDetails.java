/* Copyright (c) 2012 Google Inc.
 *
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

package com.example.android.trivialdrivesample.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app product's listing details.
 */
public class SkuDetails {
    private final String mItemType;
    private final String mSku;
    private final String mType;
    private final String mPrice;
    private final long mPriceAmountMicros;
    private final String mPriceCurrencyCode;
    private final String mTitle;
    private final String mDescription;
    private final String mJson;
    private final JSONObject mJsonObject;

    public SkuDetails(String jsonSkuDetails) throws JSONException {
        this(IabHelper.ITEM_TYPE_INAPP, jsonSkuDetails);
    }

    public SkuDetails(String itemType, String jsonSkuDetails) throws JSONException {
        mItemType = itemType;
        mJson = jsonSkuDetails;
        mJsonObject = new JSONObject(mJson);
        mSku = mJsonObject.optString("productId");
        mType = mJsonObject.optString("type");
        mPrice = mJsonObject.optString("price");
        mPriceAmountMicros = mJsonObject.optLong("price_amount_micros");
        mPriceCurrencyCode = mJsonObject.optString("price_currency_code");
        mTitle = mJsonObject.optString("title");
        mDescription = mJsonObject.optString("description");
    }

    public String getSku() { return mSku; }
    public String getType() { return mType; }
    public String getPrice() { return mPrice; }
    public long getPriceAmountMicros() { return mPriceAmountMicros; }
    public String getPriceCurrencyCode() { return mPriceCurrencyCode; }
    public String getTitle() { return mTitle; }
    public String getDescription() { return mDescription; }
    public JSONObject getJson() { return mJsonObject; }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }
}
