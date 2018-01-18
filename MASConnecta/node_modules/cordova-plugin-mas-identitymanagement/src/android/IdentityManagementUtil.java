/*
 * Copyright (c) 2016 CA, Inc.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */

package com.ca.mas.cordova.identitymanagement;

import com.ca.mas.cordova.core.MASCordovaException;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.user.MASAddress;
import com.ca.mas.identity.user.MASEmail;
import com.ca.mas.identity.user.MASPhone;
import com.ca.mas.identity.user.MASPhoto;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static com.ca.mas.identity.common.MASFilteredRequestBuilder.Operator;

/**
 * A {@link IdentityManagementUtil} Utility class for creating and Filtered Request and resolving error for JS
 */
public class IdentityManagementUtil {

    public static MASFilteredRequest getMASFilteredRequest(List<String> attrs, JSONObject filterJson, String filterType) throws JSONException {
        MASFilteredRequest frb = new MASFilteredRequest(attrs, filterType);
        JSONArray filterArr = filterJson.getJSONArray("filter");
        String sortOrder = filterJson.getString("sortOrder");
        String sortAttribute = filterJson.getString("sortAttribute");
        if (sortOrder != null && sortAttribute != null) {
            frb.setSortOrder(MASFilteredRequestBuilder.SortOrder.valueOf(sortOrder), sortAttribute);
        }
        JSONArray pagination = filterJson.getJSONArray("pagination");
        if (pagination != null && pagination.length() == 2) {
            int startIndex = pagination.getInt(0);
            int count = pagination.getInt(1);
            frb.setPagination(startIndex, count);
        }
        if (filterArr != null && filterArr.length() > 0) {
            for (int i = 0; i < filterArr.length(); i++) {
                String obj = filterArr.getString(0);
                String[] split = obj.split("::");
                if (split == null || split.length < 2) {
                    throw new JSONException("Invalid Search Filter");
                }
                if (split.length == 2 && split[1] != null && split[1].equalsIgnoreCase(Operator.pr.toString())) {
                    frb = resolveMethod(frb, split[0], split[1], null);
                    return frb;
                }
                if (split.length == 3) {
                    frb = resolveMethod(frb, split[0], split[1], split[2]);
                }
            }
        }
        return frb;
    }

    private static MASFilteredRequest resolveMethod(MASFilteredRequest frb, String key, String operator, String value) {
        try {
            Operator op = Operator.valueOf(operator);
            switch (op) {
                case eq:
                    frb.isEqualTo(key, value);
                    break;
                case ne:
                    frb.isNotEqualTo(key, value);
                    break;
                case co:
                    frb.contains(key, value);
                    break;
                case sw:
                    frb.startsWith(key, value);
                    break;
                case ew:
                    frb.endsWith(key, value);
                    break;
                case pr:
                    frb.isPresent(key);
                    break;
                case gt:
                    frb.isGreaterThan(key, value);
                    break;
                case ge:
                    frb.isGreaterThanOrEqual(key, value);
                    break;
                case lt:
                    frb.isLessThan(key, value);
                    break;
                case le:
                    frb.isLessThanOrEqual(key, value);
                    break;
            }
            return frb;

        } catch (Exception ex) {
            return frb;
        }
    }

    public static JSONObject getError(Throwable throwable) {
        int errorCode = MAGErrorCode.UNKNOWN;
        String errorMessage = throwable.getMessage();
        String errorMessageDetail = "";
        //Try to capture the root cause of the error
        if (throwable instanceof MAGException) {
            MAGException ex = (MAGException) throwable;
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable instanceof MAGRuntimeException) {
            MAGRuntimeException ex = (MAGRuntimeException) throwable;
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGException) {
            MAGException ex = (MAGException) throwable.getCause();
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGRuntimeException) {
            MAGRuntimeException ex = (MAGRuntimeException) throwable.getCause();
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGServerException) {
            MAGServerException serverException = ((MAGServerException) throwable.getCause());
            errorCode = serverException.getErrorCode();
            errorMessage = serverException.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof TargetApiException) {
            TargetApiException e = ((TargetApiException) throwable.getCause());
            try {
                errorCode = ServerClient.findErrorCode(e.getResponse());
            } catch (Exception ignore) {
            }

        } else if (errorMessage != null && errorMessage.equalsIgnoreCase("The session is currently locked.")) {
            errorCode = MAGErrorCode.UNKNOWN;

        } else if (throwable != null && throwable instanceof MASCordovaException) {
            errorMessage = throwable.getMessage();

        } else if ((throwable instanceof NullPointerException || throwable instanceof IllegalStateException) && (MAS.getContext() == null || MAS.getState(MASIdentityManagementPlugin.getCordovaContext()) != MASConstants.MAS_STATE_STARTED)) {
            errorMessageDetail = "Mobile SSO has not been initialized.";
        } else {
            errorMessageDetail = throwable.getMessage();
        }

        JSONObject error = new JSONObject();
        try {
            error.put("errorCode", errorCode);
            error.put("errorMessage", errorMessage);
            StringWriter errors = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errors));
            error.put("errorInfo", errors.toString());
            if (!"".equals(errorMessageDetail)) {
                error.put("errorMessageDetail", errorMessageDetail);
                error.put("errorMessage", "Internal Server Error");
            }
        } catch (JSONException ignore) {
        }

        return error;
    }

    public static JSONArray getGroupsAsJSON(List<MASGroup> groups) {
        JSONArray list = new JSONArray();
        for (MASGroup grp : groups) {
            try {
                list.put(getGroupAsJSON(grp));
            } catch (JSONException jce) {

            }
        }
        return list;
    }

    public static JSONObject getGroupAsJSON(MASGroup group) throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(IdentityConsts.KEY_ID, group.getId());
        jobj.put(IdentityConsts.KEY_DISPLAY_NAME, group.getGroupName());
        JSONObject owner = new JSONObject();
        owner.put(IdentityConsts.KEY_VALUE, group.getOwner().getValue());
        owner.put(IdentityConsts.KEY_DISPLAY, group.getOwner().getDisplay());
        owner.put(IdentityConsts.KEY_REFERENCE, group.getOwner().getRef());
        jobj.put(IdentityConsts.KEY_OWNER, owner);
        JSONArray members = new JSONArray();
        for (MASMember m : group.getMembers()) {
            JSONObject member = new JSONObject();
            member.put(IdentityConsts.KEY_VALUE, m.getValue());
            member.put(IdentityConsts.KEY_DISPLAY, m.getDisplay());
            member.put(IdentityConsts.KEY_TYPE, m.getType());
            member.put(IdentityConsts.KEY_REFERENCE, m.getRef());
            members.put(member);
        }
        jobj.put(IdentityConsts.KEY_MEMBERS, members);
        return jobj;
    }

    public static JSONArray getUsersAsJSON(List<MASUser> users) {
        JSONArray list = new JSONArray();
        for (MASUser user : users) {
            try {
                list.put(getUserAsJSON(user));
            } catch (JSONException jce) {

            }
        }
        return list;
    }

    public static JSONObject getUserAsJSON(MASUser masUser) throws JSONException {
        JSONObject map = new JSONObject();
        map.put(IdentityConsts.KEY_USERNAME, masUser.getUserName());
        map.put(IdentityConsts.KEY_DISPLAY_NAME, masUser.getDisplayName());
        map.put(IdentityConsts.KEY_GIVEN_NAME, masUser.getName().getGivenName());
        map.put(IdentityConsts.KEY_FAMILY_NAME, masUser.getName().getFamilyName());
        map.put("formattedName", masUser.getName().getGivenName() + " " + masUser.getName().getFamilyName());
        if (masUser.getMeta() != null && masUser.getMeta().getLocation() != null) {
            map.put(IdentityConsts.KEY_REFERENCE, masUser.getMeta().getLocation());
        }


        map.put("active", masUser.isActive());

        // Loading the email addresses
        JSONArray emailArray = new JSONArray();
        if (masUser.getEmailList() != null && !masUser.getEmailList().isEmpty()) {
            for (MASEmail email : masUser.getEmailList()) {
                JSONObject obj = new JSONObject();
                obj.put(IdentityConsts.KEY_TYPE, email.getType());
                obj.put(IdentityConsts.KEY_VALUE, email.getValue());
                emailArray.put(obj);
            }
        }
        map.put("emailAddresses", emailArray);

        // Loading the personal addresses
        JSONObject addressMap = new JSONObject();
        if (masUser.getAddressList() != null && !masUser.getAddressList().isEmpty()) {
            for (MASAddress address : masUser.getAddressList()) {
                try {
                    addressMap.put(address.getType(), address.getAsJSONObject());
                } catch (JSONException jce) {
                }
            }
        }
        map.put(IdentityConsts.KEY_ADDRS, addressMap);

        // Loading the phone numbers
        JSONArray phoneArray = new JSONArray();
        if (masUser.getPhoneList() != null && !masUser.getPhoneList().isEmpty()) {
            for (MASPhone phone : masUser.getPhoneList()) {
                JSONObject obj = new JSONObject();
                obj.put(IdentityConsts.KEY_TYPE, phone.getType());
                obj.put(IdentityConsts.KEY_VALUE, phone.getValue());
                phoneArray.put(obj);
            }
        }
        map.put(IdentityConsts.KEY_PHONE_NUMBERS, phoneArray);

        // Loading the photos
        JSONObject photoMap = new JSONObject();
        if (masUser.getPhotoList() != null && !masUser.getPhotoList().isEmpty()) {
            for (MASPhoto photo : masUser.getPhotoList()) {
                photoMap.put(photo.getType(), photo.getValue());
            }
        }
        map.put(IdentityConsts.KEY_PHOTOS, photoMap);

        JSONArray groupArray = new JSONArray();
        if (masUser.getGroupList() != null && !masUser.getGroupList().isEmpty()) {
            for (MASGroup group : masUser.getGroupList()) {
                JSONObject obj = new JSONObject();
                obj.put(IdentityConsts.KEY_VALUE, group.getValue());
                obj.put(IdentityConsts.KEY_DISPLAY, group.getGroupName());// TODO:Never getting populated in SDK end
                obj.put(IdentityConsts.KEY_REFERENCE, group.getReference());
                groupArray.put(obj);
            }
        }
        map.put(IdentityConsts.KEY_GROUPS, groupArray);
        return map;
    }
}