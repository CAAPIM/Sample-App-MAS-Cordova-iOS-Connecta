/*
 * Copyright (c) 2016 CA, Inc.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */

package com.ca.mas.cordova.identitymanagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.cordova.core.MASCordovaException;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASFoundationStrings;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.GroupAttributes;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.List;

import static com.ca.mas.cordova.identitymanagement.IdentityManagementUtil.getError;
import static com.ca.mas.cordova.identitymanagement.IdentityManagementUtil.getMASFilteredRequest;
import static com.ca.mas.foundation.MASUser.getCurrentUser;


public class MASIdentityManagementPlugin extends CordovaPlugin {
    private static final String TAG = MASIdentityManagementPlugin.class.getCanonicalName();
    private static MASIdentityManagementPlugin _plugin = null;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        _plugin = this;
    }

    public static Context getCordovaContext() {
        return _plugin.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equalsIgnoreCase("getGroupById")) {
                getGroupById(args, callbackContext);
            } else if (action.equalsIgnoreCase("getGroupsByFilter")) {
                getGroupsByFilter(args, callbackContext);
            } else if (action.equalsIgnoreCase("createGroup")) {
                createGroup(args, callbackContext);
            } else if (action.equalsIgnoreCase("updateGroup")) {
                updateGroup(args, callbackContext);
            } else if (action.equalsIgnoreCase("deleteGroup")) {
                deleteGroup(args, callbackContext);
            } else if (action.equalsIgnoreCase("addMemberToGroup")) {
                addMemberToGroup(args, callbackContext);
            } else if (action.equalsIgnoreCase("removeMemberFromGroup")) {
                removeMemberFromGroup(args, callbackContext);
            } else if (action.equalsIgnoreCase("getUserById")) {
                getUserById(args, callbackContext);
            } else if (action.equalsIgnoreCase("getUsersByFilter")) {
                getUsersByFilter(args, callbackContext);
            } else if (action.equalsIgnoreCase("getThumbnailImage")) {
                getThumbnailImage(args, callbackContext);
            } else {
                callbackContext.error("Invalid action");
                return false;
            }
        }catch (Throwable throwable){
            Log.e(TAG, throwable.getMessage());
            callbackContext.error(getError(throwable));
        }
        return true;
    }

    /**
     * Retrieves a group based on its unique ID
     */
    private void getGroupById(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final String id;
        try {
            id = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(getError(new MASCordovaException("Group Id is missing")));
            return;
        }
        MASGroup.newInstance().getGroupById(id, new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup masGroup) {
                try {
                    success(callbackContext, IdentityManagementUtil.getGroupAsJSON(masGroup));
                } catch (JSONException e) {
                    callbackContext.error(getError(e));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void getGroupsByFilter(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final JSONObject filterJson = args.optJSONObject(0);
        if (filterJson == null) {
            callbackContext.error(getError(new MASCordovaException("Filter request missing")));
            return;
        }

        MASGroup.newInstance().getGroupMetaData(new MASCallback<GroupAttributes>() {
            @Override
            public void onSuccess(GroupAttributes groupAttributes) {
                List<String> attrs = groupAttributes.getAttributes();
                MASFilteredRequest frb = null;
                try {
                    frb = getMASFilteredRequest(attrs, filterJson, IdentityConsts.KEY_GROUP_ATTRIBUTES);
                } catch (JSONException jce) {
                    Log.e(TAG, jce.getMessage());
                    callbackContext.error(getError(new MASCordovaException(jce.getMessage())));
                    return;
                }

                MASGroup.newInstance().getGroupsByFilter(frb, new MASCallback<List<MASGroup>>() {
                    @Override
                    public void onSuccess(List<MASGroup> masGroups) {
                        success(callbackContext, IdentityManagementUtil.getGroupsAsJSON(masGroups));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage());
                        callbackContext.error(getError(throwable));
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Group metadata fetch error:" + throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void createGroup(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final String groupName;
        try {
            groupName = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(getError(new MASCordovaException("Group name is missing")));
            return;
        }
        MASGroup group = MASGroup.newInstance();
        group.setGroupName(groupName);
        group.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup masGroup) {
                success(callbackContext, "Group created successfully.");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void updateGroup(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final JSONObject groupJson;
        MASGroup group = MASGroup.newInstance();
        try {
            groupJson = args.getJSONObject(0);
            if (groupJson == null) {
                throw new JSONException("Group data not found.");
            }
            group.populate(groupJson);
        } catch (JSONException e) {
            callbackContext.error(getError(new MASCordovaException("Invalid Group details")));
            return;
        }

        group.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup masGroup) {
                success(callbackContext, "Group updated successfully.");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void deleteGroup(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final String groupId;
        MASGroup group = MASGroup.newInstance();
        try {
            groupId = args.getString(0);
            if (groupId == null) {
                throw new JSONException("Invalid Group id");
            }
            JSONObject obj = new JSONObject();
            obj.put(IdentityConsts.KEY_ID, groupId);
            group.populate(obj);
        } catch (JSONException e) {
            callbackContext.error(getError(new MASCordovaException("Invalid Group details")));
            return;
        }

        group.delete(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                success(callbackContext, "Group deleted successfully");
            }

            @Override
            public void onError(Throwable e) {
                if (((TargetApiException) e.getCause()).getResponse().getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    callbackContext.error(getError(new MASCordovaException("Operation not allowed, only owner of the group can delete the group")));
                } else if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("Not Found")) {
                    MASCordovaException ex = new MASCordovaException("Group Not Found");
                    callbackContext.error(getError(ex));
                } else {
                    Log.e(TAG, e.getMessage());
                    callbackContext.error(getError(e));
                }
            }
        });
    }

    private void addMemberToGroup(final JSONArray args, final CallbackContext callbackContext) {
        JSONObject groupJson = null;
        JSONArray userList = null;
        MASGroup group = MASGroup.newInstance();
        try {
            groupJson = args.getJSONObject(0);
            if (groupJson == null) {
                throw new JSONException("Group data not found.");
            }
            group.populate(groupJson);
            userList = args.getJSONArray(1);
            if (userList == null || userList.length() == 0) {
                throw new JSONException("No User data found.");
            }
            String type = "User";
            String value = null;
            String display = null;
            for (int i = 0; i < userList.length(); i++) {
                JSONObject obj = userList.getJSONObject(i);
                display = obj.optString(IdentityConsts.KEY_DISPLAY_NAME);
                value = obj.optString(IdentityConsts.KEY_USERNAME);
                MASMember member = new MASMember(type, value, value, display);
                group.addMember(member);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            callbackContext.error(getError(new MASCordovaException("Invalid User or Group data")));
            return;
        }

        group.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup masGroup) {
                success(callbackContext, "User added successfully.");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void removeMemberFromGroup(final JSONArray args, final CallbackContext callbackContext) {
        JSONObject groupJson = null;
        JSONArray userList = null;
        MASGroup group = MASGroup.newInstance();
        try {
            groupJson = args.getJSONObject(0);
            if (groupJson == null) {
                throw new JSONException("Group data not found.");
            }
            group.populate(groupJson);
            userList = args.getJSONArray(1);
            if (userList == null || userList.length() == 0) {
                throw new JSONException("No User data found.");
            }
            String type = "User";
            String value = null;
            String display = null;
            for (int i = 0; i < userList.length(); i++) {
                JSONObject obj = userList.getJSONObject(i);
                display = obj.getString(IdentityConsts.KEY_DISPLAY);
                value = obj.getString(IdentityConsts.KEY_VALUE);
                MASMember member = new MASMember(type, value, value, display);
                group.removeMember(member);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            callbackContext.error(getError(new MASCordovaException("Invalid User or Group data")));
            return;
        }

        group.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup masGroup) {
                success(callbackContext, "User removed successfully.");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void getUserById(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final String id;
        try {
            id = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(getError(new MASCordovaException("User Id is missing")));
            return;
        }
        getCurrentUser().getUserById(id, new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser masUser) {
                try {
                    success(callbackContext, IdentityManagementUtil.getUserAsJSON(masUser));
                } catch (JSONException e) {
                    callbackContext.error(getError(e));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void getUsersByFilter(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        final JSONObject filterJson = args.optJSONObject(0);
        if (filterJson == null) {
            callbackContext.error(getError(new MASCordovaException("Filter request missing")));
            return;
        }

        MASUser.getCurrentUser().getUserMetaData(new MASCallback<UserAttributes>() {
            @Override
            public void onSuccess(UserAttributes userAttributes) {
                List<String> attrs = userAttributes.getAttributes();
                MASFilteredRequest frb = null;
                try {
                    frb = getMASFilteredRequest(attrs, filterJson, IdentityConsts.KEY_USER_ATTRIBUTES);
                } catch (JSONException jce) {
                    Log.e(TAG, jce.getMessage());
                    callbackContext.error(getError(new MASCordovaException(jce.getMessage())));
                    return;
                }
                MASUser.getCurrentUser().getUsersByFilter(frb, new MASCallback<List<MASUser>>() {
                    @Override
                    public void onSuccess(List<MASUser> masUsers) {
                        success(callbackContext, IdentityManagementUtil.getUsersAsJSON(masUsers));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage());
                        callbackContext.error(getError(throwable));
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                callbackContext.error(getError(throwable));
            }
        });
    }

    private void getThumbnailImage(final JSONArray args, final CallbackContext callbackContext) {
        MASUser masUser = getCurrentUser();
        if (masUser == null) {
            MASCordovaException e = new MASCordovaException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED);
            callbackContext.error(getError(e));
            return;
        }
        try {
            Bitmap bitmap = getCurrentUser().getThumbnailImage();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64 = "data:image/jpg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
            success(callbackContext, base64);
        } catch (Exception ex) {
            callbackContext.error("Unable to fetch thumbnail:" + ex.getLocalizedMessage());
        }
    }


    private void success(CallbackContext callbackContext, boolean value) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, value);
        callbackContext.sendPluginResult(result);
    }

    private void success(CallbackContext callbackContext, JSONObject resultData) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultData);
        callbackContext.sendPluginResult(result);
    }

    private void success(CallbackContext callbackContext, Object resultData) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultData.toString());
        callbackContext.sendPluginResult(result);
    }

    private void success(CallbackContext callbackContext, JSONArray resultData) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultData);
        callbackContext.sendPluginResult(result);
    }
}