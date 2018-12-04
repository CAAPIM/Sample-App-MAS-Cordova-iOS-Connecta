cordova.define("cordova-plugin-mas-connecta.MASPluginMQTTConstants", function(require, exports, module) {
/*
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */

/**
* @class MASPluginMQTTConstants
* @classdesc The Class definition of a generic MQTT message structure.
* @param {string} host Hostname of the MQTT broker.
* @param {number} port Port on which the MQTT broker is listening.
* @param {boolean} enableTLS Enable TLS support for the connection.
* @param {string} sslCACert Base64 encoded certificate, if the MQTT broker requires client certificate.
*/
var MASPluginMQTTConstants = function(host, port, enableTLS, sslCACert) {
 		this.host = host ? host : "";
 		this.port = port ? port : "";
 		this.enableTLS = enableTLS ? enableTLS : false;
 		this.sslCACert = sslCACert ? sslCACert : "";
 		this.connectURL = (this.host && this.port) ? ("tcp://" + this.host + ":" + this.port) : "";
 		this.userName = "";
 		this.password = "";
 		this.connectionTimeOut = 30;
 		this.keepAlive = 60;
	 	this.cleanSession = true;
 	 	this.will = {
			message: "",
			topic: "",
			QoS: 1,
			retain: false
 	    };

		/**
		* Sets the clean session property of the client.
		* @memberOf MASPluginMQTTConstants
		* @function setCleanSession
		* @instance
		* @param {boolean} mCleanSession specifies whether to clean the previous session
		*/
		this.setCleanSession = function(mCleanSession){
			this.cleanSession = mCleanSession;
 	    };

		/**
		* Sets the connection timeout for the MQTT client.
		* @memberOf MASPluginMQTTConstants
		* @function setConnectionTimeOut
		* @instance
		* @param {number} timeOutSeconds Timeout in seconds.
		*/
 	    this.setConnectionTimeOut = function(timeOutSeconds) {
			this.connectionTimeOut = timeOutSeconds;
 	    };

		/**
		* Sets the keep alive property of the client session.
		* @memberOf MASPluginMQTTConstants
		* @function setKeepAlive
		* @instance
		* @param {number} keepAliveSeconds keepAlive in seconds.
		*/
 	    this.setKeepAlive = function(keepAliveSeconds) {
			this.keepAlive = keepAliveSeconds;
 	    };

		/**
		* Sets the will of the MQTT message.
		* @memberOf MASPluginMQTTConstants
		* @function setWillToTopic
		* @instance
		* @param {string} payload The final message that broker sends to all the connected clients before it dies due to abrupt reasons.
		* @param {string} toTopic Name of the Last Will topic.
		* @param {number} willQoS Quality of service for the Last Will.
		* @param {boolean} retain Whether to retain the message if sending fails or not.
		*/
 	    this.setWillToTopic = function(payload, toTopic, willQoS, retain) {
			this.will.message = payload;
			this.will.topic = toTopic;
			this.will.QoS = willQoS;
			this.will.retain = retain;
 	    };

		/**
		* Sets the credentials if the broker connection requires authentication.
		* @memberOf MASPluginMQTTConstants
		* @function setUserCredentials
		* @instance
		* @param {string} userName Broker Account username
		* @param {string} password Broker Account password
		*/
 	    this.setUserCredentials = function(userName, password) {
			this.userName = userName;
			this.password = password;
 	    };
};
 module.exports = MASPluginMQTTConstants;
});
