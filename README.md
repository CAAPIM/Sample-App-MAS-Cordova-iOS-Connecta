:bangbang: | Please note that Cordova and Xamarin support for the SDK has been deprecated with release 2.1. The 2.1 release will be the last release with Cordova and Xamarin support. Please do not submit any new changes as they are no longer being accepted. Please contact Broadcom support [**https://support.broadcom.com/**](https://support.broadcom.com/) to report any defects or make a request for an update.
:---: | :---
# Sample-App-MAS-Cordova-iOS-Connecta

## Get Started
1. Open a terminal window to the top level folder of this Sample App (ie: ~/Sample-App-MAS-Cordova-iOS-Connecta/MASConnecta/platforms/ios)
2. In the terminal type: `pod update`   
   If this fails try: `pod install`
3. Open the .xcworkspace (ie: connectaapp.xcworkspace).
4. In the CA OAuth Manager, create an app, and export the msso_config file (https://you_server_name:8443/oauth/manager). For help with this file, see [*MAS Cordova Developer Guide*](https://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/mobile-sdk-for-ca-mobile-api-gateway/2-0/Cordova/Cordova_2-1.html).
5. Copy the contents of the exported msso_config into the msso_config file in the Xcode workspace.
6. Build and Deploy the app to a device or Simulator.

In case, if you need to work with native libraries directly i.e. not using the Cocoapods denpendecy injection, then follow the steps mentioned in [*MAS Cordova Developer Guide*](https://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/mobile-sdk-for-ca-mobile-api-gateway/2-0/Cordova/Cordova_2-1.html) under the section Developer Mode.
