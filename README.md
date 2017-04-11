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

