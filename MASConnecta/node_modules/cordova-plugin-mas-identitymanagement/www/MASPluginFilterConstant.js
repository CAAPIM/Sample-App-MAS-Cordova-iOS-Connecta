/*
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */
//
//  MASPluginFilterConstant.js
//
 
var MASPluginFilterConstant = function(){
	this.Operator = {
		eq:"eq", // equals to
        ne:"ne", // not equal to
        co:"co", // contains
        sw:"sw", // starts with
        ew:"ew", // ends with
        pr:"pr", // unary: present
        gt:"gt", // greater than
        ge:"ge", // greater than or equal to
        lt:"lt", // less than
        le:"le"  // less than or equal to
	},

	this.Logical = {
		and:"and", // both conditions must be true
        or:"or",  // either condition must be true
        not:"not" // a condition is not true
	},

	this.SortOrder = {
        ascending:"ascending",
        descending:"descending"
    },

	this.UserAttributes = {
		userName:"userName",						// Unique identifier for the User typically used by the user to directly authenticate to the service provider
		familyName:"name.familyName",				// The family name of the User, or Last Name in most Western languages (e.g. Jensen given the full name Ms. Barbara J Jensen, III.)
		givenName:"name.givenName",					// The given name of the User, or First Name in most Western languages (e.g. Barbara given the full name Ms. Barbara J Jensen, III.)
		displayName:"displayName",					// The name of the User, suitable for display to end-users. The name SHOULD be the full name of the User being described if known
		password:"password",						// The User's clear text password.  This attribute is intended to be used as a means to specify an initial password when creating a new User or to reset an existing User's password.
		emailAddress:"emails.value",				//E-mail addresses for the user. Canonical Type values of work, home, and other.
		emailType:"emails.type",					// A label indicating the attribute's function; e.g., 'work' or 'home'.
		emailDisplayName:"emails.display",			//A human readable name, primarily used for display purposes. READ-ONLY.
		isPrimaryEmail:"emails.primary",			// A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g., the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.
		phoneNumber:"phoneNumbers.value",			// phone number of user
		phoneNumberType:"phoneNumbers.type",		// A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.
		phoneNumberDisplay:"phoneNumbers.display",  // A human readable name, primarily used for display purposes. READ-ONLY.
		isPrimaryPhoneNumber:"phoneNumbers.primary",//A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g., the preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no more than once.
		photoBinary:"photos.value",				    // URL of a photo of the User.
		photoDisplayName:"photos.display",			// A human readable name, primarily used for display purposes. READ-ONLY.
		photoType:"photos.type",					// A label indicating the attribute's function; e.g., 'photo' or 'thumbnail'
		groupId:"groups.value",						// The identifier of the User's group
		groupDisplayName:"groups.display",			// A human readable name, primarily used for display purposes. READ-ONLY ex. Engineering,Sales etc.
		groupType:"groups.type",					// A label indicating the attribute's function; e.g., 'direct' or 'indirect'.
		groupRef:"groups.$ref"						// The URI of the corresponding Group resource to which the user belongs ex. https://abc.ca.com:8443/SCIM/MAS/v2/Groups/a1abdb2b-76c6-nnc5-bfba-ee3fa2143647"
	},

	this.GroupAttributes  = {
		displayName:"displayName",			// Human readable name for the Group. REQUIRED.
		ownerName:"owner.value",			// Identifier of the owner of this Group.
		ownerDisplayName:"owner.display",	// The displayName of the Group's owner
		ownerRef:"owner.$ref",				// The URI corresponding to the owner resource of this Group.
		memberName:"members.value",			// Identifier of the member of this Group i.e. userName.
		memberType:"members.type",			// A label indicating the type of resource; e.g., 'User' or 'Group'.
		memberRef:"members.$ref",			// The URI of the corresponding to the member resource of this Group
		memberDisplayName:"members.display" // The displayName of the User.
	}
}
module.exports = MASPluginFilterConstant;