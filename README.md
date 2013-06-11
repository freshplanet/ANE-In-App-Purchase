AirInAppPurchase
================

AirInAppPurchase is an ADOBE AIR Native Extension (ANE) to purchase virtual items.
It works both for iOS and Android devices.
It uses Apple In App Purchase for iOS devices and Google Play for Android ones.

Installation
------------
The ANE is already compiled. You can find it under the folder Binaries. 
If you want to recompile it, just run ant in the same directory, after changing the parameters (called properties) in build.xml.

On Android:

 * you will need to add the following in your application descriptor:

    ```xml

    <android>
        <manifestAdditions><![CDATA[
            <manifest android:installLocation="auto">
                ...
                <uses-permission android:name="android.permission.INTERNET"/>
                <uses-permission android:name="com.android.vending.BILLING" />
                ...
                <application>
                    ...
                    <service android:name="com.freshplanet.inapppurchase.BillingService" />
                    <receiver android:name="com.freshplanet.inapppurchase.BillingReceiver">
                        <intent-filter>
                            <action android:name="com.android.vending.billing.IN_APP_NOTIFY" />
                            <action android:name="com.android.vending.billing.RESPONSE_CODE" />
                            <action android:name="com.android.vending.billing.PURCHASE_STATE_CHANGED" />
                        </intent-filter>
                    </receiver>
                    ...
                </application>
            </manifest>
            
        ]]></manifestAdditions>
    </android>
    ``` 


IOS Compilation
---------------

The iOS library has been compiled using the sdk iOS 4.3 and deployment target set to iOS 4.0, using GCC 4.2.
This ANE has NOT been tested with an sdk version superior to iOS 4.3.

It is distributed under Apache 2.0 license.