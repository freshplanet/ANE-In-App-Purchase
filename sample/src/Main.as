/**
 * Copyright 2017 FreshPlanet
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
package {

    import com.freshplanet.ane.AirInAppPurchase.InAppPurchase;
    import com.freshplanet.ui.ScrollableContainer;

    import flash.display.Sprite;
    import flash.display.StageAlign;
    import flash.events.Event;
    import flash.net.URLRequestMethod;
    import flash.system.Capabilities;

    /**
     * Make sure you replace instances of {YOUR_FACEBOOK_ID} in this file and Main.xml
     */
    [SWF(backgroundColor="#057fbc", frameRate='60')]
    public class Main extends Sprite {

        public static var stageWidth:Number = 0;
        public static var indent:Number = 0;

        private var _scrollableContainer:ScrollableContainer = null;

        public function Main() {
            this.addEventListener(Event.ADDED_TO_STAGE, _onAddedToStage);
        }

        private function _onAddedToStage(event:Event):void {

            this.removeEventListener(Event.ADDED_TO_STAGE, _onAddedToStage);
            this.stage.align = StageAlign.TOP_LEFT;

            stageWidth = this.stage.stageWidth;
            indent = stage.stageWidth * 0.025;

            _scrollableContainer = new ScrollableContainer(false, true);
            this.addChild(_scrollableContainer);

            if (!InAppPurchase.isSupported) {

                trace("InAppPurchase ANE is NOT supported on this platform!");
                return;
            }

            /**
             * set these up before any use to make sure you catch everything
             */

            InAppPurchase.logEnabled = true;
            var debug:Boolean = true;

            /**
             * init the ANE!
             */

            var ane:InAppPurchase = InAppPurchase.instance;
            ane.init("{YOUR_GOOGLE_PLAY_KEY}", debug);

            var blocks:Array = [];

            /**
             * methods for performing specific actions (visible to users)
             * mess with the values to mimic your own app!
             */

            blocks.push(new TestBlock("makePurchase", function():void {

                var productId:String = _isIOS() ? "{YOUR_IOS_PRODUCT_ID}" : "{YOUR_ANDROID_PRODUCT_ID}";
                ane.makePurchase(productId);
            }));

            blocks.push(new TestBlock("makeSubscription", function():void {

                var productId:String = _isIOS() ? "{YOUR_IOS_PRODUCT_ID}" : "{YOUR_ANDROID_PRODUCT_ID}";
                ane.makeSubscription(productId);
            }));

            blocks.push(new TestBlock("removePurchaseFromQueue", function():void {

                var productId:String = _isIOS() ? "{YOUR_IOS_PRODUCT_ID}" : "{YOUR_ANDROID_PRODUCT_ID}";
                var receipt:String = "{YOUR_RECEIPT_DATA}";
                ane.removePurchaseFromQueue(productId, receipt);
            }));

            blocks.push(new TestBlock("getProductsInfo", function():void {

                var productIds:Array = null;
                var subscriptionIds:Array = null;

                if (_isIOS()) {

                    productIds = [
                        "{YOUR_IOS_PRODUCT_ID_1}",
                        "{YOUR_IOS_PRODUCT_ID_2}",
                        "{YOUR_IOS_PRODUCT_ID_3}",
                        "{YOUR_IOS_PRODUCT_ID_4}"
                    ];

                    subscriptionIds = [
                        "{YOUR_IOS_PRODUCT_ID_1}",
                        "{YOUR_IOS_PRODUCT_ID_2}",
                        "{YOUR_IOS_PRODUCT_ID_3}",
                        "{YOUR_IOS_PRODUCT_ID_4}"
                    ];
                }
                else { // android


                    productIds = [
                        "{YOUR_ANDROID_PRODUCT_ID_1}",
                        "{YOUR_ANDROID_PRODUCT_ID_2}",
                        "{YOUR_ANDROID_PRODUCT_ID_3}",
                        "{YOUR_ANDROID_PRODUCT_ID_4}"
                    ];

                    subscriptionIds = [
                        "{YOUR_ANDROID_PRODUCT_ID_1}",
                        "{YOUR_ANDROID_PRODUCT_ID_2}",
                        "{YOUR_ANDROID_PRODUCT_ID_3}",
                        "{YOUR_ANDROID_PRODUCT_ID_4}"
                    ];
                }

                ane.getProductsInfo(productIds, subscriptionIds);
            }));

            blocks.push(new TestBlock("restoreTransactions", ane.restoreTransactions));


            /**
             * add ui to screen
             */

            var nextY:Number = indent;

            for each (var block:TestBlock in blocks) {

                _scrollableContainer.addChild(block);
                block.y = nextY;
                nextY +=  block.height + indent;
            }
        }

        /**
         *
         * @return
         */
        private static function _isIOS():Boolean {
            return Capabilities.manufacturer.indexOf("iOS") > -1;
        }

        /**
         *
         * @return
         */
        private static function _isAndroid():Boolean {
            return Capabilities.manufacturer.indexOf("Android") > -1;
        }
    }
}
