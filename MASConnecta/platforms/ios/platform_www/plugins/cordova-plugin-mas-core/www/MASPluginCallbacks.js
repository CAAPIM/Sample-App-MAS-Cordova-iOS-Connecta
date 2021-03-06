cordova.define("cordova-plugin-mas-core.MASPluginCallbacks", function(require, exports, module) {
/*
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */
 
var MASPluginConstants = require("./MASPluginConstants"),
    MASPluginUtils = require("./MASPluginUtils");
	
var MASPluginCallbacks = {
    customAuthHandler:null,

    setCustomAuthHandler: function(authHandler){
        this.customAuthHandler = authHandler;
    },
    removeCustomAuthHandler: function(){
        this.customAuthHandler = null;
    },


	/** 
	* Callback where it will prompt for login Credentials.
    * @param {function} callbackResp user defined callback object containing the relevant details of callback.
    */
    MASAuthenticationCallback: function(callbackResp) {
        const error = callbackResp.error;
        const result = callbackResp.result;
        const requestType = result.requestType;

        if(requestType === 'Login'){
            if(document.getElementById('popUp') !== null || document.getElementById('popup') !== null){
                return;
            }
            if(result.requestId !== 'undefined' && !MASPluginUtils.isEmpty(result.requestId)){
                MASPluginConstants.MASLoginAuthRequestId = result.requestId;
            }
            if(!MASPluginUtils.isEmpty(MASPluginCallbacks.customAuthHandler)){
                MASPluginCallbacks.customAuthHandler(result);
                return;
            }
            let oncloseFunc = function(){
                MASPluginUtils.closePopup();
            };

            let onLoadFunc = function(){
                window.localStorage.removeItem("masCallbackResult")
            };
            MASPluginUtils.setPopUpStyle(MASPluginConstants.MASPopupStyle.MASPopupLoginStyle);
            MASPluginUtils.MASPopupUI(MASPluginConstants.MASLoginPage,result,oncloseFunc,onLoadFunc);
        }else if(requestType == "removeQRCode"){
            let event = null;
            if(!MASPluginUtils.isEmpty(error)){
                const callbackJSON = {"requestType":requestType,"error":error};
                if(!MASPluginUtils.isEmpty(MASPluginCallbacks.customAuthHandler)){
                    MASPluginCallbacks.customAuthHandler(callbackJSON);
                    return;
                }
                event = new CustomEvent("errorEvent",{"detail":callbackJSON});
            }else{
                if(!MASPluginUtils.isEmpty(MASPluginCallbacks.customAuthHandler)){
                    MASPluginCallbacks.customAuthHandler(result);
                    return;
                }
                event = new CustomEvent(requestType);
            }
            document.body.dispatchEvent(event);
        }else if(requestType == "qrCodeAuthorizationComplete"){
            if(!MASPluginUtils.isEmpty(error)){
                const callbackJSON = {"requestType":requestType,"error":error};
                if(!MASPluginUtils.isEmpty(MASPluginCallbacks.customAuthHandler)){
                    MASPluginCallbacks.customAuthHandler(callbackJSON);
                    return;
                }
                let errorEvent = new CustomEvent("errorEvent",{"detail":callbackJSON});
                document.body.dispatchEvent(errorEvent);
            }else{
                if(!MASPluginUtils.isEmpty(MASPluginCallbacks.customAuthHandler)){
                    MASPluginCallbacks.customAuthHandler(result);
                    return;
                }
                MASPluginUtils.closePopup();
            }
        }
    },


    /**
     * Callback which is used to prompt for the OTP provided channels
     * @param {function} callbackResp user defined callback object containing the OTP Channels as defined by MAG Server.
     */
    MASOTPChannelSelectCallback: function(callbackResp) {
        const error = callbackResp.error;
        // TODO: How to handle error here, since the popup is still not open
        if(!MASPluginUtils.isEmpty(error)){
            console.log("Error in OTPChannelCallback:"+JSON.stringify(error));
            return;
        }
        const result = callbackResp.result;
        let oncloseFunc = function(){
            MASPluginUtils.closePopup();
        };
        let onLoadFunc = function() {
            window.localStorage.removeItem("masCallbackResult")
        }
        MASPluginUtils.setPopUpStyle(MASPluginConstants.MASPopupStyle.MASPopupOTPStyle);
        MASPluginUtils.MASPopupUI(MASPluginConstants.MASOTPChannelsPage,result,oncloseFunc,onLoadFunc);
    },


    /**
     * Callback for the OTP Listener
     * @param {function} callbackResp user defined callback object containing the OTP failure error message. Called in case of invalid/expired OTP.
     */
    MASOTPAuthenticationCallback: function(callbackResp){
        const error = callbackResp.error;
        // TODO: How to handle error here, since the popup is still not open
        if(!MASPluginUtils.isEmpty(error)){
            console.log("Error in OTPChannelCallback:"+JSON.stringify(error));
            return;
        }

        const result = callbackResp.result;

        let oncloseFunc = function(){
            MASPluginUtils.closePopup();
        };

        let onLoadFunc = function() {
            window.localStorage.removeItem("masCallbackResult")
        };
        MASPluginUtils.setPopUpStyle(MASPluginConstants.MASPopupStyle.MASPopupOTPStyle);
        MASPluginUtils.MASPopupUI(MASPluginConstants.MASOTPPage,result,oncloseFunc,onLoadFunc);
    }
};
module.exports = MASPluginCallbacks;
});
