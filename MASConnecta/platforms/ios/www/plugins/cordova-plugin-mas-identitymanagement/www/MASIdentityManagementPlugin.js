cordova.define("cordova-plugin-mas-identitymanagement.MASIdentityManagementPlugin", function(require, exports, module) {
/*
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */
//
//  MASIdentityManagementPlugin.js
//

var MASPluginFilteredRequest = require("./MASPluginFilteredRequest");
var MASPluginFilterConstant = require("./MASPluginFilterConstant");

var MASPluginUser = require("cordova-plugin-mas-core.MASPluginUser");
var MASPluginGroup = require("cordova-plugin-mas-core.MASPluginGroup");

/**	
 @class MASIdentityManagement 
*/

MASPluginUser.prototype.instantiate = function(masPluginUser) {
     this.userName = masPluginUser.userName;
     this.familyName = masPluginUser.familyName;
     this.givenName = masPluginUser.givenName;
     this.formattedName = masPluginUser.formattedName;
     this.emailAddresses = masPluginUser.emailAddresses;
     this.phoneNumbers = masPluginUser.phoneNumbers;
     this.addresses = masPluginUser.addresses;
     this.photos = masPluginUser.photos;
     this.groups = masPluginUser.groups;
     this.active = masPluginUser.active;
}

/**
 * Retrieves the 'MASPluginUser' that matches the objectId.
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} objectId object id used to locate the 'MASPluginUser'. 
 */
MASPluginUser.getUserByObjectId = function(successHandler, errorHandler, objectId) {
 	return Cordova.exec(function(masPluginUser) {
 		masUser = Object.create(MASPluginUser.prototype);
 		masUser.instantiate(masPluginUser);
 		successHandler(masUser);
 	}, errorHandler, "MASIdentityManagementPlugin", "getUserById", [objectId]);
};

/**
 * Retrieves the 'MASPluginUser' objects that matches the 'MASPluginFilteredRequest'.
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {MASFilteredRequest} filter 'MASPluginFilteredRequest' to filter the results
 */
MASPluginUser.getUsersByFilter = function(successHandler, errorHandler, masFilteredRequest) {
 	return Cordova.exec(function(masPluginUsers) {
 		var masUsers = new Array();
 		masPluginUsers.forEach(function(masPluginUser) {
 			masUser = Object.create(MASPluginUser.prototype);
 			masUser.instantiate(masPluginUser);
 			masUsers.push(masUser);
 		});
 		successHandler(masUsers);
 	}, errorHandler, "MASIdentityManagementPlugin", "getUsersByFilter", [masFilteredRequest]);
};

/**
 * Retrieves the base64 string of thumbnail image of the 'MASPluginUser'.
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 */
MASPluginUser.prototype.getThumbnailImage = function(successHandler, errorHandler) {
	return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "getThumbnailImage", []);
};

/**
 * Retrieves the 'MASPluginGroup' matching the objectId.
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} objectId object identifier used to locate the group
 */
MASPluginGroup.prototype.getGroupByObjectId = function(successHandler, errorHandler, objectId) {
	return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "getGroupById", [objectId]);
};

/**
 * Retrieves the 'MASPluginGroup' objects that matches the 'MASPluginFilteredRequest'
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {MASFilteredRequest} filter 'MASPluginFilteredRequest' to the filter results.
 */
MASPluginGroup.prototype.getGroupsByFilter =function(successHandler, errorHandler, masFilteredRequest) {
	return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "getGroupsByFilter", [masFilteredRequest]);
};

/**
 * Creates a group with a given name
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} groupName name of the group to be created 
 */
MASPluginGroup.prototype.createGroup = function(successHandler, errorHandler, groupName) {
	if (/\s/.test(groupName)) {
         return errorHandler({
                 errorMessage: "Group name cannot have spaces"
         });
     }
	return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "createGroup", [groupName]);
};

/**
 * Updates a group
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} groupJSON JSON of the group to be updated
 */
MASPluginGroup.prototype.updateGroup = function(successHandler, errorHandler, groupJSON) {
    return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "updateGroup", [groupJSON]);
};

/**
 * Deletes a group
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} groupId groupId of the group to be deleted
 */
MASPluginGroup.prototype.deleteGroup = function(successHandler, errorHandler, groupId) {
    return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "deleteGroup", [groupId]);
};

/**
 * This method supports adding multiple members to a group, hence the developer can pass a JSONArray of user JSON objects
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} groupJSON JSON of the group to be updated
 * @param {array} arrayOfUsers array of the user objects
 */
MASPluginGroup.prototype.addMemberToGroup = function(successHandler, errorHandler, groupJSON, arrayOfUsers) {
    return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "addMemberToGroup", [groupJSON, arrayOfUsers]);
};

/**
 * This method supports removing multiple members from a group, hence the developer can pass a JSONArray of user JSON objects
 * @memberOf MASIdentityManagement
 * @param {function} successHandler user defined success callback
 * @param {function} errorHandler user defined error callback
 * @param {string} groupJSON JSON of the group to be updated
 * @param {array} arrayOfUsers array of the user objects
 */
MASPluginGroup.prototype.removeMemberFromGroup = function(successHandler, errorHandler, groupJSON, arrayOfUsers) {
    return Cordova.exec(successHandler, errorHandler, "MASIdentityManagementPlugin", "removeMemberFromGroup", [groupJSON, arrayOfUsers]);
};


var MASIdentityManagementPlugin = {

		/**
		 MASUser which has the interfaces mapped to the native MASUserIdentityManager class.
		 */
		 MASUser: MASPluginUser,

		/**
		 MASGroup which has the interfaces mapped to the native MASGroupIdentityManager class.
		 */
		 MASGroup: MASPluginGroup,

		/**
		 MASFilteredRequest object which can be used to create a JSON of filters required for searching.
		 */
		 MASFilteredRequest: MASPluginFilteredRequest,

		/**
		 MASFilterConstant which includes the attributes related to searching a user or group.
		 */
		 MASFilterConstant: MASPluginFilterConstant        
};
module.exports = MASIdentityManagementPlugin;
});
