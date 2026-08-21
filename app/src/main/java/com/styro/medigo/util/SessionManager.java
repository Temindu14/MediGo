package com.styro.medigo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserSession(String userId, String name, String email,
                                String phone, String role) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putString(Constants.PREF_USER_ID,    userId);
        editor.putString(Constants.PREF_USER_NAME,  name);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_PHONE, phone);
        editor.putString(Constants.PREF_USER_ROLE,  role);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn()   { return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false); }
    public String getUserId()     { return prefs.getString(Constants.PREF_USER_ID,    ""); }
    public String getUserName()   { return prefs.getString(Constants.PREF_USER_NAME,  ""); }
    public String getUserEmail()  { return prefs.getString(Constants.PREF_USER_EMAIL, ""); }
    public String getUserPhone()  { return prefs.getString(Constants.PREF_USER_PHONE, ""); }
    public String getUserRole()   { return prefs.getString(Constants.PREF_USER_ROLE,  Constants.ROLE_CUSTOMER); }

    public String getProfileImageUrl() {
        return prefs.getString("profileImageUrl", "");
    }

    public void updateProfileImageUrl(String url) {
        editor.putString("profileImageUrl", url);
        editor.apply();
    }

    public String getSelectedOutletId() { return prefs.getString(Constants.PREF_SELECTED_OUTLET_ID, ""); }
    public void setSelectedOutletId(String outletId) {
        editor.putString(Constants.PREF_SELECTED_OUTLET_ID, outletId);
        editor.apply();
    }

    public String getFcmToken() { return prefs.getString(Constants.PREF_FCM_TOKEN, ""); }
    public void setFcmToken(String token) {
        editor.putString(Constants.PREF_FCM_TOKEN, token);
        editor.apply();
    }

    public boolean isCustomer()      { return Constants.ROLE_CUSTOMER.equals(getUserRole()); }
    public boolean isOutletStaff()   { return Constants.ROLE_OUTLET_STAFF.equals(getUserRole()); }
    public boolean isOutletManager() { return Constants.ROLE_OUTLET_MANAGER.equals(getUserRole()); }
    public boolean isMainAdmin()     { return Constants.ROLE_MAIN_ADMIN.equals(getUserRole()); }
}