Air Native Extension for In App Purchases (iOS + Android)
======================================

This is an [Air native extension](http://www.adobe.com/devnet/air/native-extensions-for-air.html) for In-App Purchases on iOS and Android. It has been developed by [FreshPlanet](http://freshplanet.com) and is used in the game [SongPop](http://songpop.fm).


Notes
---------

* iOS implementation does NOT contain on-device receipt validation.
* Android implementation uses [In-app Billing Version 3](http://developer.android.com/google/play/billing/api.html).


Installation
---------

The ANE binary (InAppPurchase.ane) is located in the *bin* folder. You should add it to your application project's Build Path and make sure to package it with your app (more information [here](http://help.adobe.com/en_US/air/build/WS597e5dadb9cc1e0253f7d2fc1311b491071-8000.html)).

On Android:

 * you will need to add the following in your application descriptor:

```xml

<android>
    <manifestAdditions><![CDATA[
        <manifest android:installLocation="auto">
            
				<application>
					<activity android:name="com.freshplanet.inapppurchase.activities.BillingActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen"></activity>
				</application>

        </manifest>
    ]]></manifestAdditions>
</android>
```



Build script
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

This ANE has been written by [Thibaut Crenn](https://github.com/titi-us). It belongs to [FreshPlanet Inc.](http://freshplanet.com) and is distributed under the [Apache Licence, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).



Join the FreshPlanet team - GAME DEVELOPMENT in NYC
------

**We are expanding our mobile engineering teams.**

**FreshPlanet is a NYC based mobile game development firm and we are looking for senior engineers to lead the development initiatives for one or more of our games/apps.** We work in small teams (6-9) who have total product control.  These teams consist of mobile engineers, UI/UX designers and product experts.

Please contact Tom Cassidy (tcassidy@freshplanet.com) or apply at http://freshplanet.com/jobs/
