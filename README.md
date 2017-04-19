Air Native Extension for In App Purchases (iOS + Android)
======================================

This is an [Air native extension](http://www.adobe.com/devnet/air/native-extensions-for-air.html) for In-App Purchases on iOS and Android. It has been developed by [FreshPlanet](http://freshplanet.com) and is used in the game [SongPop 2](https://www.songpop2.com/).


Notes
---------

* iOS implementation does NOT contain on-device receipt validation.
* Android implementation uses [In-app Billing Version 3](http://developer.android.com/google/play/billing/api.html).


Installation
---------

The ANE binary (InAppPurchase.ane) is located in the *bin* folder. You should add it to your application project's Build Path and make sure to package it with your app (more information [here](http://help.adobe.com/en_US/air/build/WS597e5dadb9cc1e0253f7d2fc1311b491071-8000.html)). See it within our sample project's app descriptor [here](https://github.com/freshplanet/ANE-In-App-Purchase/blob/master/sample/src/Main.xml).

```xml
<extensions>
    ...
    <extensionID>com.freshplanet.ane.AirInAppPurchase</extensionID>
</extensions>
```

**iOS**

Check out the sample project [here](https://github.com/freshplanet/ANE-In-App-Purchase/blob/master/sample/src/Main.xml) for app descriptor inclusions.

**Android**

You will need to add the following activities and permission in your application descriptor:

```xml

<android>
    <manifestAdditions><![CDATA[
        <manifest android:installLocation="auto">
            ...
			<uses-permission android:name="com.android.vending.BILLING"/>

        </manifest>
    ]]></manifestAdditions>
</android>
```

You can check out our example of this in our sample project [here](https://github.com/freshplanet/ANE-In-App-Purchase/blob/master/sample/src/Main.xml).

Using the ANE
---------

todo

Build from source
---------

Should you need to edit the extension source code and/or recompile it, you will find an ant build script (build.xml) in the *build* folder:
    
```bash
cd /path/to/the/ane

# Setup build configuration
cd build
mv example.build.config build.config
# Edit build.config file to provide your machine-specific paths

# Build the ANE
ant
```

Authors
------

This ANE has been written by [Thibaut Crenn](https://github.com/titi-us) and [Adam Schlesinger](https://github.com/AdamFP).

Hi guys,

When I try to package ane to my project in flash builder I see many errors. Can you help me? I can not see your e-mail. Can you send me your e-mail for contacting? My e-mail artem@crimea-sudak.net

Can you help me please. Can you show me instructions, how I can use this ANE. I download last version this ANE (using AIR25 for last version) but when I try to include to package I see many errors (I can't attache screenshot) somethink like:

C:\Users\artem\AppData\Local\Temp\b9e17647-6fd0-4c7e-b50d-ba3e11d88129\app_entry_res\values\strings.xml:48: error: Resource at IDA_CURL_INTERFACE_ALLSESS appears in overlay but not in the base package; use to add.

C:\Users\artem\AppData\Local\Temp\b9e17647-6fd0-4c7e-b50d-ba3e11d88129\app_entry_res\values\strings.xml:49: error: Resource at IDA_CURL_INTERFACE_SERVER appears in overlay but not in the base package; use to add.

C:\Users\artem\AppData\Local\Temp\b9e17647-6fd0-4c7e-b50d-ba3e11d88129\app_entry_res\values\strings.xml:50: error: Resource at IDA_CURL_INTERFACE_TRUSTSER appears in overlay but not in the base package; use to add.

C:\Users\artem\AppData\Local\Temp\b9e17647-6fd0-4c7e-b50d-ba3e11d88129\app_entry_res\values\strings.xml:51: error: Resource at IDA_CURL_INTERFACE_CNAME_MSG appears in overlay but not in the base package; use to add.

C:\Users\artem\AppData\Local\Temp\b9e17647-6fd0-4c7e-b50d-ba3e11d88129\app_entry_res\values\strings.xml:52: error: Resource at IDA_CURL_INTERFACE_VIEW_CERT appears in overlay but not in the base package; use to add.

.....

Can you help with this please?

Thanks,
Artem

