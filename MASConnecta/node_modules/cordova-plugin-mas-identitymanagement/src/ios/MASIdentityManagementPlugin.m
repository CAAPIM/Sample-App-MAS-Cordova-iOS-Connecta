/**
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */

//
//  MASIdentityManagementPlugin.m
//

#import "MASIdentityManagementPlugin.h"

#import <MASFoundation/MASFoundation.h>
#import <MASIdentityManagement/MASIdentityManagement.h>


@interface MASIdentityManagementPlugin (Private)


- (NSDictionary *)photosWithImgSrc:(NSDictionary*)uiimagePhotos;


@end


@implementation MASIdentityManagementPlugin
///--------------------------------------
/// @name MASUser+IdentityManagement
///--------------------------------------

# pragma mark - MASUser+IdentityManagement

- (void)getUserById:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSString *objectId;
    if (command.arguments.count > 0)
        objectId = [command.arguments objectAtIndex:0];
    
    [MASUser getUserByObjectId:objectId
                    completion:
     ^(MASUser *user, NSError *error) {
        
        if(!error)
        {
            NSDictionary *masUser =
                @{ @"userName": user.userName ? user.userName : @"",
                   @"familyName": user.familyName ? user.familyName : @"",
                   @"givenName": user.givenName ? user.givenName : @"",
                   @"formattedName": user.formattedName ? user.formattedName : @"",
                   @"emailAddresses": user.emailAddresses ? user.emailAddresses : [NSDictionary dictionary],
                   @"phoneNumbers": user.phoneNumbers ? user.phoneNumbers : [NSDictionary dictionary],
                   @"addresses": user.addresses ? user.addresses : [NSDictionary dictionary],
                   @"photos": user.photos ? [self photosWithImgSrc:user.photos] : [NSDictionary dictionary],
                   @"groups": user.groups ? user.groups : [NSArray array],
                   @"active": [NSNumber numberWithBool:[user active]] };
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:masUser];
        }
        else
        {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    }];
}


- (void)getUsersByFilter:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult *result;
    
    NSDictionary *filterJSON = nil;
    if (command.arguments.count > 0)
        filterJSON = [command.arguments objectAtIndex:0];
    
    if (filterJSON) {
        
        [self filterRequestFromJSON:filterJSON
                         completion:
         ^(MASFilteredRequest *filteredRequest, NSError *error){
            
             if (error) {
                 
                 NSDictionary *errorInfo = @{@"errorMessage":[error localizedDescription],
                                             @"errorInfo":[error userInfo]};
                 
                 result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
                 
                 return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
             }
             else
             {              
                 [MASUser getUsersByFilteredRequest:filteredRequest completion:
                  ^(NSArray *userList, NSError *error, NSUInteger totalResults) {
                      
                      if(!error) {
                          
                          NSMutableArray *masUsers = [NSMutableArray array];
                          for (MASUser *user in userList) {
                              
                              NSDictionary *masUser =
                              @{ @"userName": user.userName ? user.userName : @"",
                                 @"familyName": user.familyName ? user.familyName : @"",
                                 @"givenName": user.givenName ? user.givenName : @"",
                                 @"formattedName": user.formattedName ? user.formattedName : @"",
                                 @"emailAddresses": user.emailAddresses ? user.emailAddresses : [NSDictionary dictionary],
                                 @"phoneNumbers": user.phoneNumbers ? user.phoneNumbers : [NSDictionary dictionary],
                                 @"addresses": user.addresses ? user.addresses : [NSDictionary dictionary],
                                 @"photos": user.photos ? [self photosWithImgSrc:user.photos] : [NSDictionary dictionary],
                                 @"groups": user.groups ? user.groups : [NSArray array],
                                 @"active": [NSNumber numberWithBool:[user active]] };
                              
                              [masUsers addObject:masUser];
                          }
                          
                          result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:masUsers];
                      }
                      else
                      {
                          NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                                      @"errorMessage":[error localizedDescription],
                                                      @"errorInfo":[error userInfo]};
                          
                          result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
                      }
                      
                      return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                  }];
             }
         }];
    }
    else
    {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                   messageAsString:@"Filter request missing"];
        
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}


- (void)getThumbnailImage:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult *result;
    
    if([MASUser currentUser])
    {
        [[MASUser currentUser] getThumbnailImageWithCompletion:
         ^(UIImage * _Nullable image, NSError * _Nullable error){
            
             if (image && !error) {
                 
                 result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                              messageAsString:[self base64StringFromImage:image]];
             }
             else if (error) {
                 
                 NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                             @"errorMessage":[error localizedDescription],
                                             @"errorInfo":[error userInfo]};
                 
                 result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                        messageAsDictionary:errorInfo];
             }
             
             [result setKeepCallbackAsBool:YES];
             
             [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }
    else
    {
        NSDictionary *errorInfo = @{@"errorMessage":@"No authenticated user available"};
        
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}


///--------------------------------------
/// @name MASGroup+IdentityManagement
///--------------------------------------

# pragma mark - MASGroup+IdentityManagement

- (void)createGroup:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult *result;
    
    MASGroup *group = [MASGroup group];
    
    NSString *groupName = nil;
    if (command.arguments.count>0) {
        groupName = [command.arguments objectAtIndex:0];
    }
    if(!groupName) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group Name"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    group.groupName = groupName;
    group.owner = [[MASUser currentUser] userName];
    [group saveInBackgroundWithCompletion:^(MASGroup *group, NSError *error) {
        
        if (!error) {
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Group created"];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)updateGroup:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSDictionary *groupJSON = nil;
    if (command.arguments.count>0) {
        groupJSON = [command.arguments objectAtIndex:0];
    }
    if(!groupJSON) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    
    
//    NSError *jsonError;
//    NSData *objectData = [groupJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&jsonError];
    MASGroup *group = [[MASGroup alloc] initWithInfo:groupJSON];
    
    [group saveInBackgroundWithCompletion:^(MASGroup *group, NSError *error){
        if(!error) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Group updated"];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}


- (void)deleteGroup:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSString *groupId = nil;
    if (command.arguments.count>0) {
        groupId = [command.arguments objectAtIndex:0];
    }
    if(!groupId || [groupId isKindOfClass:[NSNull class]]) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    

//    NSError *jsonError;
//    NSData *objectData = [groupJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&jsonError];


    NSMutableDictionary *groupJSON = [NSMutableDictionary dictionary];
    [groupJSON setValue:groupId forKey:@"id"];
    MASGroup *group = [[MASGroup alloc] initWithInfo:groupJSON];
    
    [group deleteInBackgroundWithCompletion:^(BOOL success, NSError *error){
        if(!error) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Group deleted"];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}


- (void)addMemberToGroup:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSArray *usersJSONArray = nil;
    if (command.arguments.count>1) {
        usersJSONArray = [command.arguments objectAtIndex:1];
    }
    if(!usersJSONArray || [[usersJSONArray objectAtIndex:0] isKindOfClass:[NSNull class]]) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid User Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    
    NSMutableArray *usersArray = [NSMutableArray array];
    for(id object in usersJSONArray) {
        NSMutableDictionary *user = [NSMutableDictionary dictionary];
        NSString *userName = [object valueForKey:@"userName"];
        NSString *display = [object valueForKey:@"givenName"];
        [user setValue:userName forKey:@"value"];
        [user setValue:display forKey:@"display"];
        [usersArray addObject:user];
    }
//    NSError *usersJSONError;
//    NSData *usersObjectData = [usersJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSArray *usersJSONArray = [NSJSONSerialization JSONObjectWithData:usersObjectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&usersJSONError];
    
    NSMutableDictionary *groupJSONDictionary = nil;
    if (command.arguments.count>0) {
        groupJSONDictionary = [command.arguments objectAtIndex:0];
    }
    if(!groupJSONDictionary || [groupJSONDictionary isKindOfClass:[NSNull class]]) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    
//    NSError *groupJSONError;
//    NSData *groupObjectData = [groupJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSMutableDictionary *groupJSONDictionary = [NSJSONSerialization JSONObjectWithData:groupObjectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&groupJSONError];
    
    NSMutableArray *existingUsersArray = [groupJSONDictionary objectForKey:@"members"];
    [existingUsersArray addObjectsFromArray:usersArray];
    [groupJSONDictionary  setObject:existingUsersArray forKey:@"members"];
    MASGroup *group = [[MASGroup alloc] initWithInfo:groupJSONDictionary];
    
    [group saveInBackgroundWithCompletion:^(MASGroup *group, NSError *error){
        if(!error) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Group members added"];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}


- (void)removeMemberFromGroup:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSArray *usersJSONArray = nil;
    if (command.arguments.count>1) {
        usersJSONArray = [command.arguments objectAtIndex:1];
    }
    if(!usersJSONArray || [[usersJSONArray objectAtIndex:0] isKindOfClass:[NSNull class]]) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid User Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
//    NSError *usersJSONError;
//    NSData *usersObjectData = [usersJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSArray *usersJSONArray = [NSJSONSerialization JSONObjectWithData:usersObjectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&usersJSONError];
    
    NSMutableDictionary *groupJSONDictionary = nil;
    if (command.arguments.count>0) {
        groupJSONDictionary = [command.arguments objectAtIndex:0];
    }
    if(!groupJSONDictionary || [groupJSONDictionary isKindOfClass:[NSNull class]]) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group Details"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }

    
//    NSError *groupJSONError;
//    NSData *groupObjectData = [groupJSON dataUsingEncoding:NSUTF8StringEncoding];
//    NSMutableDictionary *groupJSONDictionary = [NSJSONSerialization JSONObjectWithData:groupObjectData
//                                      options:NSJSONReadingMutableContainers 
//                                        error:&groupJSONError];
    int flag = 0;
    NSMutableArray *existingUsersArray = [groupJSONDictionary objectForKey:@"members"];
    NSMutableArray *newUsersArray = [NSMutableArray array];
    for(NSDictionary *existingUser in existingUsersArray) {
        flag = 0;
        for(NSDictionary *removeUser in usersJSONArray) {
            NSString *existingUserName = [existingUser objectForKey:@"value"];
            NSString *removeUserName = [removeUser objectForKey:@"value"];
            if([existingUserName isEqualToString:removeUserName]) {
                flag = 1;
                break;
            }
        }
        if(flag == 0) {
            [newUsersArray addObject:existingUser];
        }
    }
    [groupJSONDictionary  setObject:newUsersArray forKey:@"members"];
    MASGroup *group = [[MASGroup alloc] initWithInfo:groupJSONDictionary];

    
    [group saveInBackgroundWithCompletion:^(MASGroup *group, NSError *error){
        if(!error) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Group members removed"];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getGroupById:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSString *groupId = nil;
    if (command.arguments.count>0) {
        groupId = [command.arguments objectAtIndex:0];
    }
    if(!groupId) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Invalid Group ID"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }

    [MASGroup getGroupByObjectId:groupId completion:^(MASGroup *group, NSError *error){
        if(!error) {
            NSMutableDictionary *groupDictionary = [NSMutableDictionary dictionary];
            [groupDictionary setObject:(group.groupName ? group.groupName : @"") forKey:@"displayName"];
            [groupDictionary setObject:(group.owner ? group.owner : @"") forKey:@"owner"];
            [groupDictionary setObject:(group.members ? group.members : [NSArray array]) forKey:@"members"];
            [groupDictionary setObject:group.objectId forKey:@"id"];
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:groupDictionary];
        }
        else {
            NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                        @"errorMessage":[error localizedDescription],
                                        @"errorInfo":[error userInfo]};
            
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        }
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getGroupsByFilter:(CDVInvokedUrlCommand *)command
{
    __block CDVPluginResult *result;
    
    NSDictionary *filterJSON = nil;
    if (command.arguments.count>0) {
        filterJSON = [command.arguments objectAtIndex:0];
    }
    if(!filterJSON) {
        NSDictionary *errorInfo = @{@"errorMessage":@"Filter Request Missing"};
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
        return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
    
    [self filterRequestFromJSON:filterJSON
                     completion:
     ^(MASFilteredRequest *filteredRequest, NSError *error){
       
         if (error) {
             
             NSDictionary *errorInfo = @{@"errorMessage":[error localizedDescription],
                                         @"errorInfo":[error userInfo]};
             
             result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
             
             return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
         }
         else
         {
             [MASGroup getGroupsByFilteredRequest:filteredRequest completion:^(NSArray *groupList, NSError *error, NSUInteger totalResults) {
                 if(!error) {
                     NSMutableArray *resources = [NSMutableArray array];
                     
                     for (MASGroup *group in groupList) {
                         NSMutableDictionary *groupDictionary = [NSMutableDictionary dictionary];
                         NSMutableDictionary *ownerDictionary = [NSMutableDictionary dictionary];
                		 [ownerDictionary setObject:group.owner forKey:@"value"];
                		 [groupDictionary setObject:ownerDictionary forKey:@"owner"];
                         [groupDictionary setObject:(group.groupName ? group.groupName : @"") forKey:@"displayName"];
                         [groupDictionary setObject:(group.members ? group.members : [NSArray array]) forKey:@"members"];
                         [groupDictionary setObject:group.objectId forKey:@"id"];
                         [resources addObject:groupDictionary];
                     }
                     
                     result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resources];
                 }
                 else {
                     NSDictionary *errorInfo = @{@"errorCode":[NSNumber numberWithInteger:[error code]],
                                                 @"errorMessage":[error localizedDescription],
                                                 @"errorInfo":[error userInfo]};
                     
                     result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorInfo];
                 }
                 return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
             }];
         }
    }];
}


///--------------------------------------
/// @name Utility
///--------------------------------------

# pragma mark - Utility

- (void)filterRequestFromJSON:(NSDictionary *)jsonObject
                   completion:(nullable void (^)(MASFilteredRequest *filteredRequest, NSError * error))completion
{
    // NSError *error = nil;
    // NSDictionary *jsonObject =
    //     [NSJSONSerialization JSONObjectWithData:[filterJSON dataUsingEncoding:NSUTF8StringEncoding]
    //                                     options:NSJSONReadingMutableContainers error:&error];
    
    // if (error) {
        
    //     if (completion) {
    //         completion(nil, error);
    //     }
        
    //     return;
    // }
    
    MASFilteredRequest *filteredRequest = [MASFilteredRequest filteredRequest];

    if([[jsonObject objectForKey:@"sortOrder"] isEqualToString:@"descending"])
        [filteredRequest setSortOrder:MASFilteredRequestSortOrderDescending];
    else
        [filteredRequest setSortOrder:MASFilteredRequestSortOrderAscending];
        
    [filteredRequest setSortByAttribute:[jsonObject objectForKey:@"sortAttribute"]];
    
    NSRange pageRange =
        NSMakeRange([[[jsonObject objectForKey:@"pagination"] objectAtIndex:0] intValue],
                    [[[jsonObject objectForKey:@"pagination"] objectAtIndex:1] intValue]);
    [filteredRequest setPaginationRange: pageRange];
    
    NSString *filterJSONExpression = [[jsonObject objectForKey:@"filter"] componentsJoinedByString:@""];
    NSArray *filterArray = [filterJSONExpression componentsSeparatedByString:@"::"];
    
    NSMutableString *expression = [NSMutableString stringWithString:MASIdMgmtFilterPrefix];
    [expression appendString:[filterArray objectAtIndex:0]];
    [expression appendString:MASIdMgmtEmptySpace];
    [expression appendString:[filterArray objectAtIndex:1]];
    [expression appendString:MASIdMgmtEmptySpace];
    [expression appendFormat:@"\"%@\"", [filterArray objectAtIndex:2]];
    
    MASFilter *filter = [MASFilter fromStringFilterExpression:expression];
    [filteredRequest setFilter:filter];
    
    //
    // Notify the block
    //
    if (completion) completion(filteredRequest, nil);
}


- (NSString *)base64StringFromImage:(UIImage *)image {
 
    NSString *base64String = @"data:image/png;base64,";
    base64String = [base64String stringByAppendingString:
                    [UIImagePNGRepresentation(image) base64EncodedStringWithOptions:0]];
    
    return base64String;
}

- (NSDictionary *)photosWithImgSrc:(NSDictionary*)uiimagePhotos {
    
    NSMutableDictionary *photosImgSrc = [NSMutableDictionary dictionary];
    
    if (uiimagePhotos && [[uiimagePhotos allKeys] count]) {
        
        for (NSString *key in [uiimagePhotos allKeys])
        {
            NSString *base64ImgSrc =
                [self base64StringFromImage:[uiimagePhotos objectForKey:key]];
            [photosImgSrc setObject:base64ImgSrc forKey:key];
        }
    }
    
    return photosImgSrc;
}


@end

