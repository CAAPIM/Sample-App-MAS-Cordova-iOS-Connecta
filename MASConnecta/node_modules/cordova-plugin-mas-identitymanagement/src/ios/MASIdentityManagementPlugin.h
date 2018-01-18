/**
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */

//
//  MASIdentityManagementPlugin.h
//

#import <Cordova/CDV.h>



@interface MASIdentityManagementPlugin : CDVPlugin



///--------------------------------------
/// @name MASUser+IdentityManagement
///--------------------------------------

# pragma mark - MASUser+IdentityManagement

- (void)getUserById:(CDVInvokedUrlCommand *)command;



- (void)getUsersByFilter:(CDVInvokedUrlCommand*)command;



- (void)getThumbnailImage:(CDVInvokedUrlCommand*)command;



///--------------------------------------
/// @name MASGroup+IdentityManagement
///--------------------------------------

# pragma mark - MASGroup+IdentityManagement

/**
 *  Create a group
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)createGroup:(CDVInvokedUrlCommand*)command;



/**
 *  Update a group
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)updateGroup:(CDVInvokedUrlCommand*)command;



/**
 *  Delete a group
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)deleteGroup:(CDVInvokedUrlCommand*)command;



/**
 *  Add members to a group. Supports adding multiple members
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)addMemberToGroup:(CDVInvokedUrlCommand*)command;



/**
 *  Remove members from a group. Supports removing multiple members
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)removeMemberFromGroup:(CDVInvokedUrlCommand*)command;



/**
 *  Find a group by ID
 *
 *  @param command CDInvokedUrlCommand object
 */	
- (void)getGroupById:(CDVInvokedUrlCommand*)command;



/**
 *  Find a group by filter
 *
 *  @param command CDInvokedUrlCommand object
 */	
-(void)getGroupsByFilter:(CDVInvokedUrlCommand*)command;



@end